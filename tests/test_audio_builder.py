from __future__ import annotations

import math
import shutil
import struct
import wave
from pathlib import Path

import pytest

from mandarin_course.audio_builder import build_audio, build_timeline
from mandarin_course.config import Settings
from mandarin_course.tts import CachedTTS


class WaveTTS:
    def __init__(self):
        self.calls = 0

    def synthesize(self, segment, destination: Path, lesson):
        self.calls += 1
        destination.parent.mkdir(parents=True, exist_ok=True)
        with wave.open(str(destination), "wb") as audio:
            audio.setparams((1, 2, 8_000, 0, "NONE", "not compressed"))
            samples = [int(4000 * math.sin(2 * math.pi * 440 * i / 8_000)) for i in range(800)]
            audio.writeframes(b"".join(struct.pack("<h", sample) for sample in samples))


def settings(tmp_path, voices_path: Path):
    return Settings(
        "test", "eleven_multilingual_v2", tmp_path / "cache", tmp_path / "output", tmp_path / "db.sqlite",
        voices_path=voices_path,
    )


def test_timeline_preserves_order_and_silence(lesson):
    timeline = build_timeline(lesson)
    assert [(item.kind, item.segment_id, item.duration_ms) for item in timeline] == [
        ("silence", "hello", 100),
        ("speech", "hello", 0),
        ("silence", "hello", 200),
    ]


@pytest.mark.skipif(not shutil.which("ffmpeg"), reason="FFmpeg not installed")
def test_builder_inserts_silence_and_creates_nonempty_mp3(tmp_path, lesson, voices_registry):
    provider = WaveTTS()
    destination = tmp_path / "lesson.mp3"
    duration = build_audio(lesson, CachedTTS(provider, settings(tmp_path, voices_registry)), destination)
    assert destination.stat().st_size > 0
    assert 0.35 <= duration <= 0.55
    assert provider.calls == 1


@pytest.mark.skipif(not shutil.which("ffmpeg"), reason="FFmpeg not installed")
def test_playback_speed_slows_audio_locally_without_new_tts(tmp_path, lesson, voices_registry):
    provider = WaveTTS()
    cached = CachedTTS(provider, settings(tmp_path, voices_registry))
    no_pauses = lesson.segments[0].model_copy(update={
        "pause_before_ms": 0,
        "pause_after_ms": 0,
    })
    normal = lesson.model_copy(update={"segments": [no_pauses]})
    slower = lesson.model_copy(update={
        "segments": [no_pauses.model_copy(update={"playback_speed": 0.75})]
    })
    normal_duration = build_audio(normal, cached, tmp_path / "normal.mp3")
    slower_duration = build_audio(slower, cached, tmp_path / "slower.mp3")
    assert slower_duration > normal_duration * 1.2
    assert provider.calls == 1
