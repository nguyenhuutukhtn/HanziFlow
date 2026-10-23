import json
import math
import struct
import subprocess
import wave
from dataclasses import asdict
from pathlib import Path

import pytest

from mandarin_course.audio_builder import build_audio
from mandarin_course.config import Settings
from mandarin_course.lesson_schema import Lesson
from mandarin_course.tts import CachedTTS
from mandarin_course.voice_registry import VoiceProfile, resolve_roles_with_pinning


def test_lesson_two_cast_uses_gender_overrides_and_pins_once(tmp_path):
    profiles = [
        VoiceProfile("anna-voice", "Anna", "zh", "beijing", "female"),
        VoiceProfile("li-ming-voice", "Li Ming", "zh", "beijing", "male"),
        VoiceProfile("narrator-voice", "Narrator", "zh", "beijing", "female"),
        VoiceProfile("wang-voice", "Wang Fang", "zh", "beijing", "female"),
        VoiceProfile("chen-voice", "Chen Dong", "zh", "beijing", "male"),
    ]
    profiles.append(VoiceProfile("old-narrator-voice", "Old narrator", "zh", "beijing", "male"))
    cast = tmp_path / "lesson_002" / "voice_cast.json"
    lesson_one_cast = tmp_path / "lesson_001" / "voice_cast.json"
    lesson_one_cast.parent.mkdir()
    lesson_one_cast.write_text(json.dumps({
        "zh-CN:anna": "anna-voice", "zh-CN:li_ming": "li-ming-voice", "zh-CN:coach_zh": "old-narrator-voice",
    }))
    first = resolve_roles_with_pinning(
        profiles, language="zh-CN", lesson_id="lesson_002", version=1,
        roles=["coach_zh", "wang_fang", "chen_dong"], prefer_accent="beijing",
        cast_path=cast, role_genders={"coach_zh": "female"},
    )
    second = resolve_roles_with_pinning(
        profiles, language="zh-CN", lesson_id="lesson_002", version=1,
        roles=["chen_dong", "wang_fang", "coach_zh"], prefer_accent="beijing",
        cast_path=cast, role_genders={"coach_zh": "female"},
    )
    # No lesson-1 voice in a new role: narrator ≠ Anna, Chen Dong ≠ lesson 1's (male) narrator.
    lesson_one_ids = {"anna-voice", "li-ming-voice", "old-narrator-voice"}
    assert {profile.voice_id for profile in first.values()}.isdisjoint(lesson_one_ids)
    assert [first[role].gender for role in ("coach_zh", "wang_fang", "chen_dong")] == ["female", "female", "male"]
    assert len({profile.voice_id for profile in first.values()}) == 3
    assert {role: profile.voice_id for role, profile in first.items()} == {
        role: profile.voice_id for role, profile in second.items()
    }


class WaveTTS:
    def synthesize(self, segment, destination: Path, lesson):
        destination.parent.mkdir(parents=True, exist_ok=True)
        with wave.open(str(destination), "wb") as audio:
            audio.setparams((1, 2, 8_000, 0, "NONE", "not compressed"))
            samples = [int(4000 * math.sin(2 * math.pi * 440 * i / 8_000)) for i in range(800)]
            audio.writeframes(b"".join(struct.pack("<h", sample) for sample in samples))


@pytest.mark.skipif(not __import__("shutil").which("ffmpeg"), reason="FFmpeg not installed")
def test_mode_audio_uses_mode_track_and_skips_guided_zh(tmp_path):
    data = {
        "lesson_id": "lesson_002", "title": "你在哪儿？", "level": "absolute_beginner",
        "target_duration_minutes": 5, "communicative_goals": ["test"],
        "new_patterns": ["在 + nơi chốn"], "format": "modes_v1", "variant": "noi", "version": 1,
        "segments": [
            {"id": "noi_s1", "activity": "Nói", "type": "narration", "language": "zh-CN", "speaker": "coach_zh", "text": "你好。", "pinyin": "nǐ hǎo", "translation": "Xin chào."},
            {"id": "noi_s2", "activity": "Nói", "type": "dialogue", "language": "zh-CN", "speaker": "wang_fang", "text": "在这里。", "pinyin": "zài zhèlǐ", "translation": "Ở đây."},
            {"id": "noi_s3", "activity": "Nói", "type": "dialogue", "language": "zh-CN", "speaker": "chen_dong", "text": "好。", "pinyin": "hǎo", "translation": "Được."},
        ],
    }
    lesson = Lesson.model_validate(data)
    voices = tmp_path / "voices.json"
    voices.write_text(json.dumps([asdict(VoiceProfile("v", "Voice", "zh", gender="female"))]))
    settings = Settings("test", "eleven_v4", tmp_path / "cache", tmp_path / "output", tmp_path / "db", voices_path=voices)
    destination = settings.output_dir / "lesson_002_noi.mp3"
    build_audio(lesson, CachedTTS(WaveTTS(), settings), destination, track=lesson.variant)
    timeline = json.loads(destination.with_suffix(".timeline.json").read_text())
    assert destination.is_file()
    assert timeline["track"] == "noi"
    assert not (settings.output_dir / "lesson_002_guided_zh.mp3").exists()


def test_same_role_in_another_lesson_keeps_its_voice(tmp_path):
    profiles = [
        VoiceProfile("coach-a", "Coach A", "vi", None, "female"),
        VoiceProfile("coach-b", "Coach B", "vi", None, "female"),
    ]
    (tmp_path / "lesson_001").mkdir()
    (tmp_path / "lesson_001" / "voice_cast.json").write_text(json.dumps({"vi-VN:coach": "coach-b"}))
    cast = tmp_path / "lesson_002" / "voice_cast.json"
    resolved = resolve_roles_with_pinning(
        profiles, language="vi-VN", lesson_id="lesson_002", version=1,
        roles=["coach"], prefer_accent=None, cast_path=cast,
    )
    assert resolved["coach"].voice_id == "coach-b"
    assert json.loads(cast.read_text()) == {"vi-VN:coach": "coach-b"}
