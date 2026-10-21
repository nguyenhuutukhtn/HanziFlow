from __future__ import annotations

import hashlib
import json
import subprocess
import tempfile
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from .audio_builder import AudioBuildError, probe_duration
from .config import Settings
from .lesson_schema import Lesson, Segment
from .publish_timeline import PublishError
from .tts import TTSError, cache_key, voice_ids_for


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
    version: int
    published: bool
    warnings: list[str] = field(default_factory=list)


def _catalog_entry(catalog_path: Path, lesson_id: str) -> dict[str, Any] | None:
    if not catalog_path.is_file():
        return None
    catalog = json.loads(catalog_path.read_text(encoding="utf-8"))
    return next((entry for entry in catalog.get("lessons", []) if entry.get("id") == lesson_id), None)


def _matches_catalog_entry(package: Path, entry: dict[str, Any] | None) -> bool:
    if entry is None:
        return False
    tracks = entry.get("tracks", {})
    files = entry.get("files", {})
    checks: list[tuple[Path, dict[str, Any] | None]] = [
        (package / "lesson.json", files.get("manifest")),
        (package / "reference.json", files.get("reference")),
        (package / "items.json", files.get("items")),
        (package / "guided.mp3", tracks.get("guided")),
        (package / "timeline_guided.json", (tracks.get("guided") or {}).get("timeline")),
    ]
    practice_before = tracks.get("practice")
    if bool(practice_before) != (package / "practice.mp3").is_file():
        return False
    guided_zh_before = tracks.get("guided_zh")
    if bool(guided_zh_before) != (package / "guided_zh.mp3").is_file():
        return False
    if guided_zh_before:
        checks.extend([
            (package / "guided_zh.mp3", guided_zh_before),
            (package / "timeline_guided_zh.json", guided_zh_before.get("timeline")),
        ])
    if practice_before:
        checks.extend([
            (package / "practice.mp3", practice_before),
            (package / "timeline_practice.json", practice_before.get("timeline")),
        ])
    for path, old in checks:
        if old is None or not path.is_file():
            return False
        digest = _digest(path)
        if digest["sha256"] != old.get("sha256") or digest["bytes"] != old.get("bytes"):
            return False
    return True


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
    key = cache_key(segment, voice_ids if segment.turns else voice_ids[0], settings.model_id, settings.output_format)
    path = settings.cache_dir / f"{key}.mp3"
    return path if path.is_file() and path.stat().st_size else None


def _make_clip(source: Path, clips_dir: Path) -> dict[str, Any]:
    with tempfile.NamedTemporaryFile(suffix=".mp3", dir=clips_dir, delete=False) as handle:
        temporary = Path(handle.name)
    command = ["ffmpeg", "-y", "-v", "error", "-i", str(source), "-af", "loudnorm=I=-16:TP=-1.5:LRA=11", "-ac", "1", "-ar", "44100", "-c:a", "libmp3lame", "-b:a", "64k", "-map_metadata", "-1", "-fflags", "+bitexact", "-flags:a", "+bitexact", str(temporary)]
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

