from __future__ import annotations

import json
import shutil
import tempfile
from pathlib import Path

from .audio_builder import timeline_path_for
from .characters import CharacterLoaders, resolve_lesson_characters
from .config import Settings
from .lesson_schema import load_lesson
from .publish_support import (
    PublishReport, _artifact, _cache_path, _catalog_entry, _make_clip,
    _matches_catalog_entry, _write_json,
)
from .publish_timeline import PublishError, _enrich_timeline, _load_and_validate_timeline, _reference_lines
from .publish_tokens import _strip_punctuation, load_vocab, sentence_item_id, tokenize_mandarin, tones_from_pinyin, word_item_id
from .story.ledger import chars_taught_before
def publish_lesson(lesson_path: Path, settings: Settings, out: Path, force: bool = False) -> PublishReport:
    """Publish a lesson package; the version number is assigned here, not read from the
    lesson JSON ``version`` field (kept in the schema but ignored for packaging; see
    docs/app_architecture.md "Versioning rule"). A package identical to the current
    catalog entry publishes nothing and is reported as unchanged.
    """
    if not shutil.which("ffmpeg") or not shutil.which("ffprobe"):
        raise PublishError("FFmpeg and ffprobe must be installed and available on PATH")
    lesson = load_lesson(lesson_path)
    if lesson.variant != "guided":
        raise PublishError("The publish input must have variant 'guided'")
    guided_source = settings.output_dir / lesson.lesson_id / f"{lesson.lesson_id}_guided.mp3"
    if not guided_source.is_file():
        raise PublishError(f"Required guided audio is missing: {guided_source}")
    practice_path = lesson_path.parent / f"{lesson.lesson_id}_practice.json"
    practice = load_lesson(practice_path) if practice_path.is_file() else None
    if practice and (
        practice.lesson_id != lesson.lesson_id
        or practice.version != lesson.version
        or practice.variant != "practice"
    ):
        raise PublishError(
            "Practice lesson must match the guided lesson_id/version and have variant 'practice'"
        )
    practice_source = settings.output_dir / lesson.lesson_id / f"{lesson.lesson_id}_practice.mp3"
    practice_present = bool(practice and practice_source.is_file())
    vocab = load_vocab(lesson_path.parent / f"{lesson.lesson_id}_vocab.json", lesson.lesson_id)
    item_ids = {word.hanzi: word_item_id(word.hanzi, word.pinyin) for word in vocab.words}
    character_bundle = CharacterLoaders()
    missing_character_data = character_bundle.missing_sources()
    if missing_character_data:
        paths = ", ".join(missing_character_data)
        raise PublishError(f"Character data missing: {paths}; run: just fetch-character-data")
    character_loaders = character_bundle.as_dict()
    characters = resolve_lesson_characters(lesson.model_dump(mode="json"), chars_taught_before(lesson_path.parent, lesson.lesson_id), character_loaders, vocab.words)
    missing_authored = [entry["char"] for entry in characters["characters"] if not entry.get("han_viet") or not entry.get("vi_meaning")]
    if missing_authored:
        raise PublishError(f"{len(missing_authored)} characters lack Vietnamese data: run just export-characters {lesson.lesson_id.rsplit('_', 1)[1]}")

    catalog_path = out / "catalog.json"
    previous_entry = _catalog_entry(catalog_path, lesson.lesson_id)
    current_version = int(previous_entry["version"]) if previous_entry else 0
    # Embed the current version while building so an unchanged rebuild reproduces the
    # existing package byte-for-byte; bumped to current + 1 below only if content differs.
    trial_version = current_version or 1

    lessons_dir = out / "lessons" / lesson.lesson_id
    lessons_dir.mkdir(parents=True, exist_ok=True)
    warnings: list[str] = []
    failures: list[str] = []
    with tempfile.TemporaryDirectory(prefix=f".{lesson.lesson_id}-", dir=lessons_dir) as temp_name:
        package = Path(temp_name) / "package"
        clips_dir = package / "clips"
        clips_dir.mkdir(parents=True)
        shutil.copyfile(guided_source, package / "guided.mp3")
        guided_zh_source = settings.output_dir / lesson.lesson_id / f"{lesson.lesson_id}_guided_zh.mp3"
        guided_zh_present = guided_zh_source.is_file()
        if guided_zh_present:
            shutil.copyfile(guided_zh_source, package / "guided_zh.mp3")
        if practice_present:
            shutil.copyfile(practice_source, package / "practice.mp3")
        else:
            warnings.append("Practice track omitted: practice MP3 is missing")

        metadata = {
            "id": lesson.lesson_id,
            "title": lesson.title,
            "level": lesson.level,
            "version": trial_version,
            "communicative_goals": lesson.communicative_goals,
            "new_patterns": lesson.new_patterns,
        }
        _write_json(package / "lesson.json", metadata)
        guided_lines = _reference_lines(lesson, item_ids, failures)
        practice_lines = _reference_lines(practice, item_ids, failures) if practice else []
        if failures:
            ids = ", ".join(sorted(set(failures)))
            raise PublishError(f"Pinyin alignment failed for segment(s): {ids}")
        _write_json(package / "reference.json", {"tracks": {"guided": guided_lines, "practice": practice_lines}})
        guided_timeline_source = timeline_path_for(guided_source)
        guided_timeline = _load_and_validate_timeline(guided_timeline_source, lesson, "guided", guided_lines)
        guided_enriched = _enrich_timeline(guided_timeline, lesson, "guided", trial_version)
        _write_json(package / "timeline_guided.json", guided_enriched)
        guided_zh_enriched = None
        if guided_zh_present:
            guided_zh_timeline = _load_and_validate_timeline(
                timeline_path_for(guided_zh_source), lesson, "guided_zh", guided_lines
            )
            guided_zh_enriched = _enrich_timeline(guided_zh_timeline, lesson, "guided_zh", trial_version)
            _write_json(package / "timeline_guided_zh.json", guided_zh_enriched)
        practice_enriched: dict[str, Any] | None = None
        if practice_present:
            practice_timeline_source = timeline_path_for(practice_source)
            practice_timeline = _load_and_validate_timeline(practice_timeline_source, practice, "practice", practice_lines)
            practice_enriched = _enrich_timeline(practice_timeline, practice, "practice", trial_version)
            _write_json(package / "timeline_practice.json", practice_enriched)

        cache_by_id = {
            segment.id: _cache_path(segment, lesson, settings)
            for segment in lesson.segments
            if segment.language == "zh-CN" and not segment.turns
        }
        for segment_id, source in cache_by_id.items():
            if source is None:
                warnings.append(f"Cached clip missing: {segment_id}")
        clip_by_source: dict[Path, dict[str, Any]] = {}

        def clip_for(segment: Segment) -> dict[str, Any] | None:
            source = cache_by_id.get(segment.id)
            if source is None:
                return None
            if source not in clip_by_source:
                clip_by_source[source] = _make_clip(source, clips_dir)
            return clip_by_source[source]

        words: list[dict[str, Any]] = []
        words_with: list[str] = []
        words_without: list[str] = []
        for vocab_word in vocab.words:
            word = {
                "id": item_ids[vocab_word.hanzi],
                "hanzi": vocab_word.hanzi,
                "pinyin": vocab_word.pinyin,
                "tones": tones_from_pinyin(vocab_word.hanzi, vocab_word.pinyin),
                "meaning_vi": vocab_word.meaning_vi,
            }
            source_segment = next(
                (segment for segment in lesson.segments if not segment.turns and segment.language == "zh-CN" and _strip_punctuation(segment.text) == vocab_word.hanzi),
                None,
            )
            clip = clip_for(source_segment) if source_segment else None
            if clip:
                word["clip"] = clip
                words_with.append(vocab_word.hanzi)
            else:
                words_without.append(vocab_word.hanzi)
            words.append(word)

        sentence_items: list[dict[str, Any]] = []
        sentences_without: list[str] = []
        seen_text: set[str] = set()
        for segment in lesson.segments:
            if segment.turns or segment.language != "zh-CN" or segment.speaker not in {"coach_zh", "anna", "li_ming"} or segment.text in seen_text:
                continue
            seen_text.add(segment.text)
            entry: dict[str, Any] = {
                "id": sentence_item_id(segment.text), "hanzi": segment.text,
                "pinyin": segment.pinyin, "meaning_vi": segment.translation,
                "activity": segment.activity, "speaker": segment.speaker,
                "tokens": tokenize_mandarin(segment.text, segment.pinyin or "", item_ids),
            }
            clip = clip_for(segment)
            if clip:
                entry["clip"] = clip
            else:
                sentences_without.append(segment.id)
            sentence_items.append(entry)

        qa_items: list[dict[str, Any]] = []
        qa_without: list[str] = []
        for question, answer in zip(lesson.segments, lesson.segments[1:]):
            if question.type != "prompt" or answer.type != "model_answer" or question.language != "zh-CN" or answer.language != "zh-CN":
                continue
            pair: dict[str, Any] = {
                "id": question.id,
                "activity": question.activity,
                "speaker": question.speaker,
            }
            missing_pair = False
            for label, segment in (("question", question), ("answer", answer)):
                value: dict[str, Any] = {"hanzi": segment.text, "pinyin": segment.pinyin, "meaning_vi": segment.translation}
                clip = clip_for(segment)
                if clip:
                    value["clip"] = clip
                else:
                    missing_pair = True
                pair[label] = value
            if missing_pair:
                qa_without.append(question.id)
            qa_items.append(pair)

        _write_json(package / "items.json", {
            "schema_version": 1, "lesson_id": lesson.lesson_id, "lesson_version": trial_version,
            "words": words, "sentences": sentence_items, "qa": qa_items,
        })
        _write_json(
            package / "characters.json",
            characters,
        )

        if not force and _matches_catalog_entry(package, previous_entry):
            existing = lessons_dir / f"v{current_version}"
            files = [
                (path.relative_to(existing).as_posix(), path.stat().st_size)
                for path in sorted(existing.rglob("*")) if path.is_file()
            ]
            return PublishReport(
                existing, files, sum(size for _, size in files), words_with, words_without,
                sentences_without, qa_without, practice_present, current_version, False, warnings,
            )

        final_version = current_version + 1 if current_version else 1
        if final_version != trial_version:
            # Only these files embed the package version; patch them in place rather
            # than re-deriving the whole package.
            metadata["version"] = final_version
            _write_json(package / "lesson.json", metadata)
            _write_json(package / "items.json", {
                "schema_version": 1, "lesson_id": lesson.lesson_id, "lesson_version": final_version,
                "words": words, "sentences": sentence_items, "qa": qa_items,
            })
            guided_enriched["lesson_version"] = final_version
            _write_json(package / "timeline_guided.json", guided_enriched)
            if guided_zh_enriched is not None:
                guided_zh_enriched["lesson_version"] = final_version
                _write_json(package / "timeline_guided_zh.json", guided_zh_enriched)
            if practice_enriched is not None:
                practice_enriched["lesson_version"] = final_version
                _write_json(package / "timeline_practice.json", practice_enriched)

        destination = lessons_dir / f"v{final_version}"
        if destination.exists() and not force:
            raise PublishError(f"Package already exists: {destination} (use --force for testing)")
        backup = destination.with_name(destination.name + ".replaced")
        if destination.exists():
            if backup.exists():
                shutil.rmtree(backup)
            destination.rename(backup)
        try:
            package.rename(destination)
        except Exception:
            if backup.exists():
                backup.rename(destination)
            raise
        if backup.exists():
            shutil.rmtree(backup)

    tracks = {"guided": _artifact(destination / "guided.mp3", "guided.mp3", duration=True)}
    tracks["guided"]["timeline"] = _artifact(
        destination / "timeline_guided.json", "timeline_guided.json"
    )
    if guided_zh_present:
        tracks["guided_zh"] = _artifact(destination / "guided_zh.mp3", "guided_zh.mp3", duration=True)
        tracks["guided_zh"]["timeline"] = _artifact(
            destination / "timeline_guided_zh.json", "timeline_guided_zh.json"
        )
    if practice_present:
        tracks["practice"] = _artifact(destination / "practice.mp3", "practice.mp3", duration=True)
        tracks["practice"]["timeline"] = _artifact(
            destination / "timeline_practice.json", "timeline_practice.json"
        )
    catalog_entry = {
        "id": lesson.lesson_id, "version": final_version,
        "order": int(lesson.lesson_id.rsplit("_", 1)[1]), "title": lesson.title, "level": lesson.level,
        "base_url": f"lessons/{lesson.lesson_id}/v{final_version}/", "tracks": tracks,
        "files": {
            "manifest": _artifact(destination / "lesson.json", "lesson.json"),
            "reference": _artifact(destination / "reference.json", "reference.json"),
            "items": _artifact(destination / "items.json", "items.json"),
            "characters": _artifact(destination / "characters.json", "characters.json"),
        },
    }
    catalog = json.loads(catalog_path.read_text(encoding="utf-8")) if catalog_path.is_file() else {"schema_version": 1, "catalog_version": 1, "lessons": []}
    previous_lessons = catalog.get("lessons", [])
    next_lessons = sorted(
        [entry for entry in catalog.get("lessons", []) if entry.get("id") != lesson.lesson_id] + [catalog_entry],
        key=lambda entry: (entry["order"], entry["id"]),
    )
    if previous_lessons and previous_lessons != next_lessons:
        catalog["catalog_version"] = int(catalog.get("catalog_version", 1)) + 1
    catalog["lessons"] = next_lessons
    out.mkdir(parents=True, exist_ok=True)
    temporary_catalog = out / ".catalog.json.tmp"
    _write_json(temporary_catalog, catalog)
    temporary_catalog.replace(catalog_path)

    files = [(path.relative_to(destination).as_posix(), path.stat().st_size) for path in sorted(destination.rglob("*")) if path.is_file()]
    return PublishReport(
        destination, files, sum(size for _, size in files), words_with, words_without,
        sentences_without, qa_without, practice_present, final_version, True, warnings,
    )
