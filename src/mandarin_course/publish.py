"""Build deterministic, versioned app packages from generated local audio."""

from __future__ import annotations

import hashlib
import json
import re
import shutil
import subprocess
import tempfile
import unicodedata
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field, model_validator
from pypinyin import Style, lazy_pinyin

from .audio_builder import AudioBuildError, probe_duration, timeline_path_for
from .config import Settings
from .lesson_schema import Lesson, Segment, load_lesson
from .tts import TTSError, cache_key, voice_ids_for


class PublishError(RuntimeError):
    pass


class VocabWord(BaseModel):
    model_config = ConfigDict(extra="forbid")
    hanzi: str = Field(min_length=1)
    pinyin: str = Field(min_length=1)
    meaning_vi: str = Field(min_length=1)


class VocabSidecar(BaseModel):
    model_config = ConfigDict(extra="forbid")
    schema_version: Literal[1]
    lesson_id: str = Field(pattern=r"^lesson_[0-9]{3}$")
    status: Literal["draft", "reviewed"]
    words: list[VocabWord]

    @model_validator(mode="after")
    def unique_words(self) -> "VocabSidecar":
        values = [word.hanzi for word in self.words]
        if len(values) != len(set(values)):
            raise ValueError("vocabulary words must have unique hanzi")
        return self


def load_vocab(path: Path, lesson_id: str) -> VocabSidecar:
    try:
        vocab = VocabSidecar.model_validate_json(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exc:
        raise PublishError(f"Vocabulary sidecar is missing: {path}") from exc
    if vocab.lesson_id != lesson_id:
        raise PublishError(
            f"Vocabulary sidecar lesson_id {vocab.lesson_id!r} does not match {lesson_id!r}"
        )
    return vocab


_PINYIN_WORD = re.compile(r"[A-Za-z\u00c0-\u024f\u1e00-\u1eff\u0300-\u036f\u00fc\u00dc:]+[1-5]?")
_HANZI = re.compile(r"[\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff]")
_TONE_MARKS = {
    **{char: 1 for char in "āēīōūǖĀĒĪŌŪǕ"},
    **{char: 2 for char in "áéíóúǘÁÉÍÓÚǗ"},
    **{char: 3 for char in "ǎěǐǒǔǚǍĚǏǑǓǙ"},
    **{char: 4 for char in "àèìòùǜÀÈÌÒÙǛ"},
}


def _plain_pinyin(value: str) -> str:
    value = value.replace("u:", "ü").replace("U:", "Ü")
    decomposed = unicodedata.normalize("NFD", value)
    return "".join(char for char in decomposed if not unicodedata.combining(char)).lower().replace("ü", "v")


def _hanzi_syllables(text: str) -> list[str]:
    chars = [char for char in text if _HANZI.fullmatch(char)]
    return lazy_pinyin(chars, style=Style.NORMAL, strict=False)


def _pinyin_words(pinyin: str) -> list[str]:
    return _PINYIN_WORD.findall(pinyin)


def tokenize_mandarin(text: str, pinyin: str, item_ids: dict[str, str] | None = None) -> list[dict[str, str]]:
    """Align author-separated pinyin words to Hanzi spans; never guess boundaries."""
    item_ids = item_ids or {}
    words = _pinyin_words(pinyin)
    chars = list(text)
    hanzi_positions = [index for index, char in enumerate(chars) if _HANZI.fullmatch(char)]
    syllables = _hanzi_syllables(text)
    if len(syllables) != len(hanzi_positions):
        raise ValueError("pypinyin could not produce one syllable per Hanzi character")

    spans: list[tuple[int, int, str]] = []
    syllable_at = 0
    for word in words:
        target = _plain_pinyin(word.rstrip("12345"))
        joined = ""
        start = syllable_at
        while syllable_at < len(syllables) and len(joined) < len(target):
            joined += _plain_pinyin(syllables[syllable_at])
            syllable_at += 1
        if joined != target:
            raise ValueError(f"{word!r} does not align at Hanzi syllable {start + 1}")
        spans.append((start, syllable_at, word))
    if syllable_at != len(syllables):
        raise ValueError(f"pinyin covers {syllable_at} of {len(syllables)} Hanzi syllables")

    tokens: list[dict[str, str]] = []
    char_at = 0
    for start, end, word in spans:
        first = hanzi_positions[start]
        last = hanzi_positions[end - 1]
        while char_at < first:
            if not chars[char_at].isspace():
                tokens.append({"hanzi": chars[char_at]})
            char_at += 1
        hanzi = "".join(char for char in chars[first : last + 1] if _HANZI.fullmatch(char))
        token = {"hanzi": hanzi, "pinyin": word}
        if hanzi in item_ids:
            token["item_id"] = item_ids[hanzi]
        tokens.append(token)
        char_at = last + 1
    while char_at < len(chars):
        if not chars[char_at].isspace():
            tokens.append({"hanzi": chars[char_at]})
        char_at += 1
    return tokens


def tones_from_pinyin(hanzi: str, pinyin: str) -> list[int]:
    tokens = tokenize_mandarin(hanzi, pinyin)
    tones: list[int] = []
    for token in tokens:
        authored = token.get("pinyin")
        if not authored:
            continue
        source = authored.rstrip("12345")
        explicit = int(authored[-1]) if authored[-1:].isdigit() else None
        generated = _hanzi_syllables(token["hanzi"])
        offset = 0
        plain = _plain_pinyin(source)
        for index, syllable in enumerate(generated):
            width = len(_plain_pinyin(syllable))
            piece = source[offset : offset + width]
            offset += width
            marked = next((_TONE_MARKS[c] for c in piece if c in _TONE_MARKS), None)
            tones.append(marked or (explicit if index == len(generated) - 1 else None) or 5)
        if offset != len(plain):
            raise ValueError(f"cannot extract tones from {pinyin!r}")
    return tones


def numbered_pinyin(hanzi: str, pinyin: str) -> str:
    """Canonical tone-number pinyin used in cross-lesson stable item IDs."""
    tokens = tokenize_mandarin(hanzi, pinyin)
    numbered: list[str] = []
    for token in tokens:
        authored = token.get("pinyin")
        if not authored:
            continue
        syllables = _hanzi_syllables(token["hanzi"])
        tones = tones_from_pinyin(token["hanzi"], authored)
        numbered.extend(
            f"{_plain_pinyin(syllable)}{tone}"
            for syllable, tone in zip(syllables, tones, strict=True)
        )
    return "".join(numbered)


def word_item_id(hanzi: str, pinyin: str) -> str:
    return f"w_{hanzi}_{numbered_pinyin(hanzi, pinyin)}"


def sentence_item_id(text: str) -> str:
    digest = hashlib.sha256(text.encode("utf-8")).hexdigest()[:12]
    return f"s_{digest}"


def _strip_punctuation(text: str) -> str:
    return "".join(char for char in text if _HANZI.fullmatch(char))


def _json_bytes(value: Any) -> bytes:
    return (json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + "\n").encode("utf-8")


def _write_json(path: Path, value: Any) -> None:
    path.write_bytes(_json_bytes(value))


def _digest(path: Path) -> dict[str, Any]:
    data = path.read_bytes()
    return {"bytes": len(data), "sha256": hashlib.sha256(data).hexdigest()}


def _artifact(path: Path, relative: str, *, duration: bool = False) -> dict[str, Any]:
    result = {"file": relative, **_digest(path)}
    if duration:
        result["duration_ms"] = round(probe_duration(path) * 1000)
    return result


def _cache_path(segment: Segment, lesson: Lesson, settings: Settings) -> Path | None:
    try:
        voice_ids = voice_ids_for(segment, settings, lesson)
    except TTSError:
        return None
    key = cache_key(
        segment,
        voice_ids if segment.turns else voice_ids[0],
        settings.model_id,
        settings.output_format,
    )
    path = settings.cache_dir / f"{key}.mp3"
    return path if path.is_file() and path.stat().st_size else None


def _make_clip(source: Path, clips_dir: Path) -> dict[str, Any]:
    with tempfile.NamedTemporaryFile(suffix=".mp3", dir=clips_dir, delete=False) as handle:
        temporary = Path(handle.name)
    command = [
        "ffmpeg", "-y", "-v", "error", "-i", str(source),
        "-af", "loudnorm=I=-16:TP=-1.5:LRA=11", "-ac", "1", "-ar", "44100",
        "-c:a", "libmp3lame", "-b:a", "64k", "-map_metadata", "-1",
        "-fflags", "+bitexact", "-flags:a", "+bitexact", str(temporary),
    ]
    result = subprocess.run(command, capture_output=True, text=True)
    if result.returncode:
        temporary.unlink(missing_ok=True)
        detail = result.stderr.strip().splitlines()[-1] if result.stderr.strip() else "unknown error"
        raise AudioBuildError(f"FFmpeg failed while creating clip: {detail}")
    digest = _digest(temporary)
    destination = clips_dir / f"{digest['sha256']}.mp3"
    if destination.exists():
        temporary.unlink()
    else:
        temporary.replace(destination)
    return {"file": f"clips/{destination.name}", **digest}


def _reference_lines(lesson: Lesson, item_ids: dict[str, str], failures: list[str]) -> list[dict[str, Any]]:
    lines: list[dict[str, Any]] = []
    for segment in lesson.segments:
        if segment.turns:
            sources = [
                (turn.speaker, turn.text, turn.pinyin, turn.translation)
                for turn in segment.turns
            ]
        else:
            sources = [(segment.speaker, segment.text, segment.pinyin, segment.translation)]
        for turn_index, (speaker, text, pinyin, translation) in enumerate(sources):
            line: dict[str, Any] = {
                "id": segment.id if not segment.turns else f"{segment.id}_turn_{turn_index + 1}",
                "type": segment.type,
                "language": segment.language,
                "speaker": speaker,
                "text": text,
                "pinyin": pinyin,
                "translation": translation,
            }
            if segment.language == "zh-CN":
                try:
                    line["tokens"] = tokenize_mandarin(text, pinyin or "", item_ids)
                except ValueError:
                    failures.append(segment.id)
                    continue
            lines.append(line)
    return lines


def _load_and_validate_timeline(
    source: Path,
    lesson: Lesson,
    track: str,
    reference_lines: list[dict[str, Any]],
) -> dict[str, Any]:
    if not source.is_file():
        raise PublishError(f"Published {track} track has no timeline: {source}")
    try:
        timeline = json.loads(source.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise PublishError(f"Invalid {track} timeline: {source}: {exc}") from exc
    required = {"schema_version", "lesson_id", "lesson_version", "track", "duration_ms", "cues"}
    missing = sorted(required - timeline.keys())
    if missing:
        raise PublishError(f"Invalid {track} timeline; missing: {', '.join(missing)}")
    if (
        timeline["schema_version"] != 1
        or timeline["lesson_id"] != lesson.lesson_id
        or timeline["lesson_version"] != lesson.version
        or timeline["track"] != track
    ):
        raise PublishError(f"{track.capitalize()} timeline metadata does not match the lesson")
    reference_ids = {line["id"] for line in reference_lines}
    cue_ids: set[Any] = set()
    for cue in timeline["cues"]:
        cue_ids.add(cue.get("line_id"))
        grouped = cue.get("line_ids", [])
        if not isinstance(grouped, list):
            raise PublishError(f"Invalid {track} timeline; line_ids must be a list")
        cue_ids.update(grouped)
    unknown = sorted(cue_ids - reference_ids, key=str)
    if unknown:
        raise PublishError(
            f"{track.capitalize()} timeline has unknown reference line id(s): "
            + ", ".join(map(str, unknown))
        )
    return timeline


def _segment_line_ids(segment: Segment) -> list[str]:
    if segment.turns:
        return [f"{segment.id}_turn_{index + 1}" for index in range(len(segment.turns))]
    return [segment.id]


def _enrich_timeline(timeline: dict[str, Any], lesson: Lesson, track: str) -> dict[str, Any]:
    """Join cues to their segment's language/type/activity and derive top-level sections.

    Fails loudly rather than guessing when the timeline (built from cached audio) no
    longer matches the current lesson JSON, since that means the audio is stale.
    """
    segment_by_line_id: dict[str, Segment] = {}
    expected_sequence: list[str] = []
    for segment in lesson.segments:
        line_ids = _segment_line_ids(segment)
        for line_id in line_ids:
            segment_by_line_id[line_id] = segment
        expected_sequence.extend(line_ids[0] for _ in range(segment.repetitions))

    actual_sequence = [cue.get("line_id") for cue in timeline["cues"]]
    if actual_sequence != expected_sequence:
        raise PublishError(
            f"{track.capitalize()} timeline no longer matches {lesson.lesson_id} v{lesson.version}: "
            "cue line ids or their order differ from the current lesson segments. "
            "Regenerate the audio instead of publishing a stale cache."
        )

    cues: list[dict[str, Any]] = []
    sections: list[dict[str, Any]] = []
    current_activity: str | None = None
    for cue in timeline["cues"]:
        line_id = cue.get("line_id")
        segment = segment_by_line_id.get(line_id)
        if segment is None:
            raise PublishError(f"{track.capitalize()} timeline cue {line_id!r} has no matching segment")
        enriched = {**cue, "language": segment.language, "type": segment.type}
        if segment.activity != current_activity:
            sections.append({
                "id": f"sec_{len(sections) + 1:02d}",
                "title": segment.activity,
                "start_ms": cue["start_ms"],
            })
            current_activity = segment.activity
        enriched["section_id"] = sections[-1]["id"]
        cues.append(enriched)

    for index in range(len(sections) - 1):
        sections[index]["end_ms"] = sections[index + 1]["start_ms"]
    if sections:
        sections[-1]["end_ms"] = timeline["duration_ms"]

    return {**timeline, "cues": cues, "sections": sections}


@dataclass
class PublishReport:
    package_dir: Path
    files: list[tuple[str, int]]
    total_bytes: int
    words_with_clips: list[str]
    words_without_clips: list[str]
    sentences_without_clips: list[str]
    qa_without_clips: list[str]
    practice_present: bool
    warnings: list[str] = field(default_factory=list)


def publish_lesson(lesson_path: Path, settings: Settings, out: Path, force: bool = False) -> PublishReport:
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

    destination = out / "lessons" / lesson.lesson_id / f"v{lesson.version}"
    if destination.exists() and not force:
        raise PublishError(f"Package already exists: {destination} (use --force for testing)")
    destination.parent.mkdir(parents=True, exist_ok=True)
    warnings: list[str] = []
    failures: list[str] = []
    with tempfile.TemporaryDirectory(prefix=f".{lesson.lesson_id}-", dir=destination.parent) as temp_name:
        package = Path(temp_name) / "package"
        clips_dir = package / "clips"
        clips_dir.mkdir(parents=True)
        shutil.copyfile(guided_source, package / "guided.mp3")
        if practice_present:
            shutil.copyfile(practice_source, package / "practice.mp3")
        else:
            warnings.append("Practice track omitted: practice MP3 is missing")

        metadata = {
            "id": lesson.lesson_id,
            "title": lesson.title,
            "level": lesson.level,
            "version": lesson.version,
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
        _write_json(package / "timeline_guided.json", _enrich_timeline(guided_timeline, lesson, "guided"))
        if practice_present:
            practice_timeline_source = timeline_path_for(practice_source)
            practice_timeline = _load_and_validate_timeline(practice_timeline_source, practice, "practice", practice_lines)
            _write_json(package / "timeline_practice.json", _enrich_timeline(practice_timeline, practice, "practice"))

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
            "schema_version": 1, "lesson_id": lesson.lesson_id, "lesson_version": lesson.version,
            "words": words, "sentences": sentence_items, "qa": qa_items,
        })

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
    if practice_present:
        tracks["practice"] = _artifact(destination / "practice.mp3", "practice.mp3", duration=True)
        tracks["practice"]["timeline"] = _artifact(
            destination / "timeline_practice.json", "timeline_practice.json"
        )
    catalog_entry = {
        "id": lesson.lesson_id, "version": lesson.version,
        "order": int(lesson.lesson_id.rsplit("_", 1)[1]), "title": lesson.title, "level": lesson.level,
        "base_url": f"lessons/{lesson.lesson_id}/v{lesson.version}/", "tracks": tracks,
        "files": {
            "manifest": _artifact(destination / "lesson.json", "lesson.json"),
            "reference": _artifact(destination / "reference.json", "reference.json"),
            "items": _artifact(destination / "items.json", "items.json"),
        },
    }
    catalog_path = out / "catalog.json"
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
    return PublishReport(destination, files, sum(size for _, size in files), words_with, words_without, sentences_without, qa_without, practice_present, warnings)


def render_publish_report(report: PublishReport) -> str:
    lines = [f"Published {report.package_dir}", "Files:"]
    lines.extend(f"  {name}: {size} bytes" for name, size in report.files)
    lines.extend([
        f"Total package size: {report.total_bytes} bytes",
        f"Words with clips ({len(report.words_with_clips)}): {', '.join(report.words_with_clips) or 'none'}",
        f"Words without clips ({len(report.words_without_clips)}): {', '.join(report.words_without_clips) or 'none'}",
        f"Sentences without clips ({len(report.sentences_without_clips)}): {', '.join(report.sentences_without_clips) or 'none'}",
        f"QA without clips ({len(report.qa_without_clips)}): {', '.join(report.qa_without_clips) or 'none'}",
        f"Practice track: {'present' if report.practice_present else 'not present'}",
    ])
    lines.extend(f"Warning: {warning}" for warning in report.warnings)
    return "\n".join(lines) + "\n"
