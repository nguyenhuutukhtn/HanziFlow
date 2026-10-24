"""Publishing support for the four-track ``modes_v1`` lesson format."""

from __future__ import annotations

import json
import shutil
import tempfile
from pathlib import Path

from .audio_builder import timeline_path_for
from .characters import CharacterLoaders, resolve_lesson_characters
from .config import Settings
from .lesson_schema import Lesson, load_lesson
from .publish_notes import load_notes, notes_body
from .publish_support import (
    PublishReport, _artifact, _cache_path, _catalog_entry, _make_clip, _matches_catalog_entry, _write_json,
)
from .publish_timeline import PublishError, _enrich_timeline, _load_and_validate_timeline, _reference_lines
from .publish_tokens import _strip_punctuation, load_vocab, tones_from_pinyin, word_item_id
from .story.ledger import chars_taught_before

MODES = ("hieu", "tu_vung", "nghe", "phan_xa", "noi")


def _mode_gaps(timeline: dict, lesson: Lesson) -> list[dict]:
    """Expose authored response/repetition pauses using measured cue boundaries."""
    segments = {segment.id: segment for segment in lesson.segments}
    cues = timeline["cues"]
    gaps = []
    for index, cue in enumerate(cues):
        kind = segments[cue["line_id"]].gap
        if kind:
            gaps.append({
                "start_ms": cue["end_ms"],
                "end_ms": cues[index + 1]["start_ms"] if index + 1 < len(cues) else timeline["duration_ms"],
                "kind": kind,
                "after_line_id": cue["line_id"],
            })
    return gaps


def _mode_lessons(lesson_path: Path) -> dict[str, Lesson]:
    """Load and validate the complete sibling set for one modes lesson."""
    first = load_lesson(lesson_path)
    lessons: dict[str, Lesson] = {}
    for mode in MODES:
        path = lesson_path.parent / f"{first.lesson_id}_{mode}.json"
        if not path.is_file():
            raise PublishError(f"Required modes lesson file is missing: {path}")
        lesson = load_lesson(path)
        if (
            lesson.format != "modes_v1"
            or lesson.variant != mode
            or lesson.lesson_id != first.lesson_id
            or lesson.version != first.version
        ):
            raise PublishError(f"Modes lesson must match lesson_id/version and variant '{mode}': {path}")
        lessons[mode] = lesson
    return lessons


def publish_modes_lesson(lesson_path: Path, settings: Settings, out: Path, force: bool = False) -> PublishReport:
    if not shutil.which("ffmpeg") or not shutil.which("ffprobe"):
        raise PublishError("FFmpeg and ffprobe must be installed and available on PATH")
    lessons = _mode_lessons(lesson_path)
    lesson = lessons["hieu"]
    sources = {
        mode: settings.output_dir / lesson.lesson_id / f"{lesson.lesson_id}_{mode}.mp3"
        for mode in MODES
    }
    for source in sources.values():
        if not source.is_file():
            raise PublishError(f"Required modes audio is missing: {source}")

    character_bundle = CharacterLoaders()
    if missing_sources := character_bundle.missing_sources():
        raise PublishError(f"Character data missing: {', '.join(missing_sources)}; run: just fetch-character-data")
    all_segments = [segment.model_dump(mode="json") for mode in MODES for segment in lessons[mode].segments]
    character_input = lesson.model_dump(mode="json") | {"segments": all_segments}
    characters = resolve_lesson_characters(
        character_input, chars_taught_before(lesson_path.parent, lesson.lesson_id), character_bundle.as_dict()
    )
    omitted = [entry for entry in characters["characters"] if not entry.get("han_viet") or not entry.get("vi_meaning")]
    characters["characters"] = [entry for entry in characters["characters"] if entry not in omitted]

    vocab_path = lesson_path.parent / f"{lesson.lesson_id}_vocab.json"
    vocab = load_vocab(vocab_path, lesson.lesson_id) if vocab_path.is_file() else None
    item_ids = {word.hanzi: word_item_id(word.hanzi, word.pinyin) for word in vocab.words} if vocab else {}
    notes = load_notes(lesson_path.parent / f"{lesson.lesson_id}_notes.json", lesson.lesson_id)

    catalog_path = out / "catalog.json"
    previous_entry = _catalog_entry(catalog_path, lesson.lesson_id)
    current_version = int(previous_entry["version"]) if previous_entry else 0
    trial_version = current_version or 1
    lessons_dir = out / "lessons" / lesson.lesson_id
    lessons_dir.mkdir(parents=True, exist_ok=True)
    warnings = [f"{len(omitted)} characters lack Vietnamese data (omitted)"] if omitted else []

    with tempfile.TemporaryDirectory(prefix=f".{lesson.lesson_id}-", dir=lessons_dir) as temp_name:
        package = Path(temp_name) / "package"
        package.mkdir()
        lines: dict[str, list[dict]] = {}
        timelines: dict[str, dict] = {}
        for mode in MODES:
            source = sources[mode]
            shutil.copyfile(source, package / f"{mode}.mp3")
            failures: list[str] = []
            lines[mode] = _reference_lines(lessons[mode], item_ids, failures)
            if failures:
                raise PublishError(f"Pinyin alignment failed for segment(s): {', '.join(sorted(set(failures)))}")
            raw = _load_and_validate_timeline(timeline_path_for(source), lessons[mode], mode, lines[mode])
            timelines[mode] = _enrich_timeline(raw, lessons[mode], mode, trial_version)
            timelines[mode]["gaps"] = _mode_gaps(timelines[mode], lessons[mode])
            _write_json(package / f"timeline_{mode}.json", timelines[mode])

        words: list[dict] = []
        words_with: list[str] = []
        words_without: list[str] = []
        if vocab is not None:
            clips_dir = package / "clips"
            clips_dir.mkdir()
            clip_by_source: dict[Path, dict] = {}
            clip_by_text: dict[str, dict] = {}
            vocab_hanzi = {word.hanzi for word in vocab.words}
            for mode in MODES:
                for segment in lessons[mode].segments:
                    if segment.turns or segment.language != "zh-CN":
                        continue
                    text = _strip_punctuation(segment.text)
                    if text in clip_by_text or text not in vocab_hanzi:
                        continue
                    source = _cache_path(segment, lessons[mode], settings)
                    if source is None:
                        continue
                    if source not in clip_by_source:
                        clip_by_source[source] = _make_clip(source, clips_dir)
                    clip_by_text[text] = clip_by_source[source]
            for vocab_word in vocab.words:
                word = {
                    "id": word_item_id(vocab_word.hanzi, vocab_word.pinyin),
                    "hanzi": vocab_word.hanzi, "pinyin": vocab_word.pinyin,
                    "tones": tones_from_pinyin(vocab_word.hanzi, vocab_word.pinyin),
                    "meaning_vi": vocab_word.meaning_vi,
                }
                if vocab_word.hanzi in clip_by_text:
                    word["clip"] = clip_by_text[vocab_word.hanzi]
                    words_with.append(vocab_word.hanzi)
                else:
                    words_without.append(vocab_word.hanzi)
                words.append(word)

        def write_derived(version: int) -> None:
            _write_json(package / "items.json", {
                "schema_version": 1, "lesson_id": lesson.lesson_id, "lesson_version": version,
                "words": words, "sentences": [], "qa": [],
            })
            if notes is not None:
                _write_json(package / "notes.json", {"schema_version": 1, "lesson_id": lesson.lesson_id, "lesson_version": version, **notes_body(notes)})

        metadata = {
            "id": lesson.lesson_id, "title": lesson.title, "level": lesson.level,
            "version": trial_version, "format": "modes_v1",
            "communicative_goals": lesson.communicative_goals, "new_patterns": lesson.new_patterns,
        }
        _write_json(package / "lesson.json", metadata)
        _write_json(package / "reference.json", {"tracks": lines}, sort_keys=False)
        write_derived(trial_version)
        _write_json(package / "characters.json", characters)

        if not force and _matches_catalog_entry(package, previous_entry):
            existing = lessons_dir / f"v{current_version}"
            files = [(path.relative_to(existing).as_posix(), path.stat().st_size) for path in sorted(existing.rglob("*")) if path.is_file()]
            return PublishReport(existing, files, sum(size for _, size in files), words_with, words_without, [], [], False, current_version, False, warnings)

        final_version = current_version + 1 if current_version else 1
        if final_version != trial_version:
            metadata["version"] = final_version
            _write_json(package / "lesson.json", metadata)
            write_derived(final_version)
            for mode in MODES:
                timelines[mode]["lesson_version"] = final_version
                _write_json(package / f"timeline_{mode}.json", timelines[mode])

        destination = lessons_dir / f"v{final_version}"
        if destination.exists() and not force:
            raise PublishError(f"Package already exists: {destination} (use --force for testing)")
        if destination.exists():
            shutil.rmtree(destination)
        package.rename(destination)

    tracks = {}
    for mode in MODES:
        tracks[mode] = _artifact(destination / f"{mode}.mp3", f"{mode}.mp3", duration=True)
        tracks[mode]["timeline"] = _artifact(destination / f"timeline_{mode}.json", f"timeline_{mode}.json")
    catalog_entry = {
        "id": lesson.lesson_id, "version": final_version, "order": int(lesson.lesson_id.rsplit("_", 1)[1]),
        "format": "modes_v1", "title": lesson.title, "level": lesson.level,
        "base_url": f"lessons/{lesson.lesson_id}/v{final_version}/", "tracks": tracks,
        "files": {name: _artifact(destination / filename, filename) for name, filename in (
            ("manifest", "lesson.json"), ("reference", "reference.json"), ("items", "items.json"), ("characters", "characters.json"),
        )},
    }
    if (destination / "notes.json").is_file():
        catalog_entry["files"]["notes"] = _artifact(destination / "notes.json", "notes.json")
    catalog = json.loads(catalog_path.read_text(encoding="utf-8")) if catalog_path.is_file() else {"schema_version": 1, "catalog_version": 1, "lessons": []}
    previous_lessons = catalog.get("lessons", [])
    catalog["lessons"] = sorted(
        [entry for entry in previous_lessons if entry.get("id") != lesson.lesson_id] + [catalog_entry],
        key=lambda entry: (entry["order"], entry["id"]),
    )
    if previous_lessons and previous_lessons != catalog["lessons"]:
        catalog["catalog_version"] = int(catalog.get("catalog_version", 1)) + 1
    out.mkdir(parents=True, exist_ok=True)
    _write_json(out / ".catalog.json.tmp", catalog, sort_keys=False)
    (out / ".catalog.json.tmp").replace(catalog_path)
    files = [(path.relative_to(destination).as_posix(), path.stat().st_size) for path in sorted(destination.rglob("*")) if path.is_file()]
    return PublishReport(destination, files, sum(size for _, size in files), words_with, words_without, [], [], False, final_version, True, warnings)
