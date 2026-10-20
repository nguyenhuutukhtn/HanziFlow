"""FFmpeg-based audio assembly with per-segment pauses."""

from __future__ import annotations

import shutil
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path

from .lesson_schema import Lesson
from .tts import CachedTTS


class AudioBuildError(RuntimeError):
    pass


@dataclass(frozen=True)
class TimelineItem:
    kind: str
    segment_id: str
    duration_ms: int = 0


def build_timeline(lesson: Lesson) -> list[TimelineItem]:
    timeline: list[TimelineItem] = []
    for segment in lesson.segments:
        if segment.pause_before_ms:
            timeline.append(TimelineItem("silence", segment.id, segment.pause_before_ms))
        for repetition in range(segment.repetitions):
            timeline.append(TimelineItem("speech", segment.id))
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


def build_audio(lesson: Lesson, tts: CachedTTS, destination: Path) -> float:
    if not shutil.which("ffmpeg") or not shutil.which("ffprobe"):
        raise AudioBuildError("FFmpeg and ffprobe must be installed and available on PATH")
    by_id = {segment.id: segment for segment in lesson.segments}
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="mandarin-course-") as temp_name:
        temp = Path(temp_name)
        normalized: dict[str, Path] = {}
        parts: list[Path] = []
        for index, item in enumerate(build_timeline(lesson)):
            part = temp / f"{index:05d}.wav"
            if item.kind == "speech":
                source = tts.get(by_id[item.segment_id], lesson)
                if item.segment_id not in normalized:
                    segment = by_id[item.segment_id]
                    norm = temp / f"speech_{item.segment_id}.wav"
                    filters = [f"atempo={segment.playback_speed:.4f}", "loudnorm=I=-16:TP=-1.5:LRA=11"]
                    _run(["ffmpeg", "-y", "-v", "error", "-i", str(source), "-af", ",".join(filters), "-ar", "44100", "-ac", "1", "-c:a", "pcm_s16le", str(norm)])
                    normalized[item.segment_id] = norm
                shutil.copyfile(normalized[item.segment_id], part)
            else:
                seconds = item.duration_ms / 1000
                _run(["ffmpeg", "-y", "-v", "error", "-f", "lavfi", "-i", "anullsrc=r=44100:cl=mono", "-t", f"{seconds:.3f}", "-c:a", "pcm_s16le", str(part)])
            parts.append(part)
        concat_file = temp / "concat.txt"
        concat_file.write_text("".join(f"file '{path.as_posix()}'\n" for path in parts), encoding="utf-8")
        temporary_output = destination.with_suffix(".part.mp3")
        try:
            _run(["ffmpeg", "-y", "-v", "error", "-f", "concat", "-safe", "0", "-i", str(concat_file), "-c:a", "libmp3lame", "-b:a", "128k", str(temporary_output)])
            if not temporary_output.is_file() or temporary_output.stat().st_size == 0:
                raise AudioBuildError("FFmpeg produced no output")
            temporary_output.replace(destination)
        except Exception:
            temporary_output.unlink(missing_ok=True)
            raise
    return probe_duration(destination)
