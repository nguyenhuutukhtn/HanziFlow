from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from .lesson_schema import Lesson, Segment


class PublishError(RuntimeError):
    pass


from .publish_tokens import tokenize_mandarin


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


def _enrich_timeline(timeline: dict[str, Any], lesson: Lesson, track: str, package_version: int) -> dict[str, Any]:
    """Join cues to their segment's language/type/activity and derive top-level sections.

    Fails loudly rather than guessing when the timeline (built from cached audio) no
    longer matches the current lesson JSON, since that means the audio is stale.
    ``lesson_version`` in the returned timeline is overwritten with ``package_version``
    (the assigned package version), not the input timeline's own value — every file in
    a package must agree on the version. The staleness check above still compares the
    *input* timeline against the lesson JSON's own ``version`` field, unrelated to this.
    """
    segment_by_line_id: dict[str, Segment] = {}
    expected_sequence: list[str] = []
    for segment in lesson.segments:
        # Same drop rule as audio_builder.chinese_only.
        if track == "guided_zh" and (segment.language == "vi-VN" or segment.guided_only):
            continue
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

    return {**timeline, "lesson_version": package_version, "cues": cues, "sections": sections}
