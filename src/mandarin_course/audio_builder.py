"""FFmpeg-based audio assembly with per-segment pauses."""

from __future__ import annotations

import json
import shutil
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path
from typing import Callable

from .lesson_schema import Lesson, Segment
from .tts import CachedTTS


class AudioBuildError(RuntimeError):
    pass


@dataclass(frozen=True)
class TimelineItem:
    kind: str
    segment_id: str
    duration_ms: int = 0
    repetition: int | None = None


def build_timeline(lesson: Lesson) -> list[TimelineItem]:
    timeline: list[TimelineItem] = []
    for segment in lesson.segments:
        if segment.pause_before_ms:
            timeline.append(TimelineItem("silence", segment.id, segment.pause_before_ms))
        for repetition in range(segment.repetitions):
            timeline.append(TimelineItem("speech", segment.id, repetition=repetition))
            if repetition < segment.repetitions - 1 and segment.pause_after_ms:
                timeline.append(TimelineItem("silence", segment.id, segment.pause_after_ms))
        if segment.pause_after_ms:
            timeline.append(TimelineItem("silence", segment.id, segment.pause_after_ms))
    return timeline


def _run(command: list[str]) -> None:
    result = subprocess.run(command, capture_output=True, text=True)
    if result.returncode:
        detail = result.stderr.strip().splitlines()[-1] if result.stderr.strip() else "unknown error"
        raise AudioBuildError(f"FFmpeg failed: {detail}")


def probe_duration(path: Path) -> float:
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", str(path)],
        capture_output=True,
        text=True,
    )
    if result.returncode:
        raise AudioBuildError(f"ffprobe could not inspect {path}")
    return float(result.stdout.strip())


MAX_TIMELINE_DRIFT_MS = 150
CHINESE_ONLY_GAP_MS = 400
RESPONSE_GAP_MIN_MS = 1500


def chinese_only(lesson: Lesson, duration_ms_for: Callable[[Segment], int] | None = None) -> Lesson:
    """Remove dropped segments while preserving response and recall gaps."""
    kept = []
    dropped = []
    for segment in lesson.segments:
        if segment.language == "vi-VN" or segment.guided_only:
            dropped.append(segment)
            continue
        if dropped and kept:
            pause_before_ms = CHINESE_ONLY_GAP_MS
            if len(dropped) == 1 and dropped[0].language == "vi-VN" and dropped[0].text.strip() and kept[-1].text.strip() == segment.text.strip():
                if duration_ms_for is None:
                    raise AudioBuildError(f"Missing cached duration for recall gap at segment {dropped[0].id}")
                pause_before_ms = duration_ms_for(dropped[0]) + dropped[0].pause_after_ms
            else:
                longest_pause = max(item.pause_after_ms for item in dropped)
                if longest_pause >= RESPONSE_GAP_MIN_MS:
                    pause_before_ms = longest_pause
            segment = segment.model_copy(update={"pause_before_ms": pause_before_ms})
        kept.append(segment)
        dropped = []
    return lesson.model_copy(update={"segments": kept})


def validate_timeline_drift(
    final_duration_ms: float,
    timeline_end_ms: float,
    maximum_ms: int = MAX_TIMELINE_DRIFT_MS,
) -> float:
    drift_ms = abs(final_duration_ms - timeline_end_ms)
    if drift_ms > maximum_ms:
        raise AudioBuildError(
            f"Final MP3/timeline drift is {drift_ms:.1f} ms; maximum is {maximum_ms} ms"
        )
    return drift_ms


def timeline_path_for(audio_path: Path) -> Path:
    return audio_path.with_suffix(".timeline.json")


def build_audio(lesson: Lesson, tts: CachedTTS, destination: Path, *, track: str | None = None) -> float:
    if not shutil.which("ffmpeg") or not shutil.which("ffprobe"):
        raise AudioBuildError("FFmpeg and ffprobe must be installed and available on PATH")
    by_id = {segment.id: segment for segment in lesson.segments}
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="mandarin-course-") as temp_name:
        temp = Path(temp_name)
        normalized: dict[str, Path] = {}
        normalized_duration_ms: dict[str, float] = {}
        parts: list[Path] = []
        cues: list[dict[str, object]] = []
        elapsed_ms = 0.0
        for index, item in enumerate(build_timeline(lesson)):
            part = temp / f"{index:05d}.wav"
            if item.kind == "speech":
                segment = by_id[item.segment_id]
                source = tts.get(segment, lesson)
                if item.segment_id not in normalized:
                    norm = temp / f"speech_{item.segment_id}.wav"
                    filters = [f"atempo={segment.playback_speed:.4f}", "loudnorm=I=-16:TP=-1.5:LRA=11"]
                    _run(["ffmpeg", "-y", "-v", "error", "-i", str(source), "-af", ",".join(filters), "-ar", "44100", "-ac", "1", "-c:a", "pcm_s16le", str(norm)])
                    normalized[item.segment_id] = norm
                    normalized_duration_ms[item.segment_id] = probe_duration(norm) * 1000
                shutil.copyfile(normalized[item.segment_id], part)
                end_ms = elapsed_ms + normalized_duration_ms[item.segment_id]
                cue: dict[str, object] = {
                    "line_id": item.segment_id,
                    "start_ms": round(elapsed_ms),
                    "end_ms": round(end_ms),
                }
                if segment.turns:
                    line_ids = [
                        f"{segment.id}_turn_{turn_index + 1}"
                        for turn_index in range(len(segment.turns))
                    ]
                    cue.update(
                        line_id=line_ids[0],
                        line_ids=line_ids,
                        granularity="segment",
                    )
                cues.append(cue)
                elapsed_ms = end_ms
            else:
                seconds = item.duration_ms / 1000
                _run(["ffmpeg", "-y", "-v", "error", "-f", "lavfi", "-i", "anullsrc=r=44100:cl=mono", "-t", f"{seconds:.3f}", "-c:a", "pcm_s16le", str(part)])
                elapsed_ms += item.duration_ms
            parts.append(part)
        concat_file = temp / "concat.txt"
        concat_file.write_text("".join(f"file '{path.as_posix()}'\n" for path in parts), encoding="utf-8")
        temporary_output = destination.with_suffix(".part.mp3")
        try:
            _run([
                "ffmpeg", "-y", "-v", "error", "-f", "concat", "-safe", "0", "-i", str(concat_file),
                "-ac", "1", "-c:a", "libmp3lame", "-b:a", "64k", "-map_metadata", "-1", str(temporary_output),
            ])
            if not temporary_output.is_file() or temporary_output.stat().st_size == 0:
                raise AudioBuildError("FFmpeg produced no output")
            final_duration = probe_duration(temporary_output)
            validate_timeline_drift(final_duration * 1000, elapsed_ms)
            timeline = {
                "schema_version": 1,
                "lesson_id": lesson.lesson_id,
                "lesson_version": lesson.version,
                "track": track or lesson.variant or "guided",
                "duration_ms": round(elapsed_ms),
                "cues": cues,
            }
            temporary_timeline = timeline_path_for(destination).with_suffix(".part.json")
            temporary_timeline.write_text(
                json.dumps(timeline, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
                encoding="utf-8",
            )
            temporary_output.replace(destination)
            temporary_timeline.replace(timeline_path_for(destination))
        except Exception:
            temporary_output.unlink(missing_ok=True)
            timeline_path_for(destination).with_suffix(".part.json").unlink(missing_ok=True)
            raise
    return probe_duration(destination)
