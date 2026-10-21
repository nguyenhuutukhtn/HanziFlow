from __future__ import annotations

import json
import math
import shutil
import struct
import wave
from pathlib import Path

import pytest

from mandarin_course.audio_builder import AudioBuildError, build_audio, build_timeline, chinese_only, validate_timeline_drift
from mandarin_course.config import Settings
from mandarin_course.lesson_schema import Lesson
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


@pytest.mark.skipif(not shutil.which("ffmpeg"), reason="FFmpeg not installed")
def test_timeline_cue_math_uses_measured_speech_and_exact_pauses(tmp_path, lesson, voices_registry):
    provider = WaveTTS()
    segment = lesson.segments[0].model_copy(update={
        "repetitions": 2,
        "pause_before_ms": 100,
        "pause_after_ms": 200,
        "playback_speed": 0.75,
    })
    repeated = lesson.model_copy(update={"variant": "guided", "segments": [segment]})
    destination = tmp_path / "repeated.mp3"
    build_audio(repeated, CachedTTS(provider, settings(tmp_path, voices_registry)), destination)
    timeline = json.loads((tmp_path / "repeated.timeline.json").read_text())
    first, second = timeline["cues"]
    assert first["line_id"] == second["line_id"] == "hello"
    assert first["start_ms"] == 100
    assert second["start_ms"] - first["end_ms"] == 200
    assert timeline["duration_ms"] - second["end_ms"] == 200
    assert first["end_ms"] - first["start_ms"] > 120  # 100 ms source slowed by atempo=0.75


@pytest.mark.skipif(not shutil.which("ffmpeg"), reason="FFmpeg not installed")
def test_turns_segment_builds_grouped_cue_for_each_repetition(tmp_path, lesson_data, voices_registry):
    lesson_data["variant"] = "guided"
    lesson_data["segments"] = [{
        "id": "exchange", "activity": "a", "type": "dialogue", "language": "zh-CN",
        "text": "你好。你好！", "pinyin": "nǐ hǎo. nǐ hǎo!", "translation": "Xin chào.",
        "repetitions": 2, "pause_before_ms": 100, "pause_after_ms": 200,
        "playback_speed": 0.75,
        "turns": [
            {"speaker": "anna", "text": "你好。", "pinyin": "nǐ hǎo.", "translation": "Xin chào."},
            {"speaker": "li_ming", "text": "你好！", "pinyin": "nǐ hǎo!", "translation": "Chào bạn!"},
        ],
    }]
    dialogue = Lesson.model_validate(lesson_data)
    destination = tmp_path / "dialogue.mp3"
    build_audio(dialogue, CachedTTS(WaveTTS(), settings(tmp_path, voices_registry)), destination)
    timeline = json.loads((tmp_path / "dialogue.timeline.json").read_text())
    assert len(timeline["cues"]) == 2
    first, second = timeline["cues"]
    expected = {
        "line_id": "exchange_turn_1",
        "line_ids": ["exchange_turn_1", "exchange_turn_2"],
        "granularity": "segment",
    }
    assert {key: first[key] for key in expected} == expected
    assert {key: second[key] for key in expected} == expected
    assert first["start_ms"] == 100
    assert second["start_ms"] - first["end_ms"] == 200
    assert timeline["duration_ms"] - second["end_ms"] == 200


def test_timeline_drift_over_limit_fails():
    with pytest.raises(AudioBuildError, match="drift is 151.0 ms"):
        validate_timeline_drift(1_151, 1_000)


def test_chinese_only_drops_vietnamese_and_replaces_only_internal_gap(lesson_data):
    lesson_data["segments"] = [
        {"id": "lead", "activity": "a", "type": "context", "language": "vi-VN", "speaker": "coach", "text": "Mở đầu", "translation": "Mở đầu", "pause_after_ms": 900},
        {"id": "one", "activity": "a", "type": "narration", "language": "zh-CN", "speaker": "coach_zh", "text": "你好。", "pinyin": "nǐ hǎo", "translation": "Xin chào.", "pause_before_ms": 120, "pause_after_ms": 250},
        {"id": "middle", "activity": "a", "type": "context", "language": "vi-VN", "speaker": "coach", "text": "Giải thích", "translation": "Giải thích", "pause_before_ms": 800, "pause_after_ms": 900},
        {"id": "two", "activity": "a", "type": "narration", "language": "zh-CN", "speaker": "coach_zh", "text": "再见。", "pinyin": "zài jiàn", "translation": "Tạm biệt.", "pause_before_ms": 70},
        {"id": "tail", "activity": "a", "type": "context", "language": "vi-VN", "speaker": "coach", "text": "Kết thúc", "translation": "Kết thúc", "pause_after_ms": 700},
    ]
    result = chinese_only(Lesson.model_validate(lesson_data))
    assert [segment.id for segment in result.segments] == ["one", "two"]
    assert result.segments[0].pause_before_ms == 120
    assert result.segments[1].pause_before_ms == 400


def test_chinese_only_preserves_a_response_gap_from_a_dropped_hint(lesson_data):
    lesson_data["segments"] = [
        {"id": "prompt", "activity": "q", "type": "prompt", "language": "zh-CN", "speaker": "coach_zh", "text": "你好吗？", "pinyin": "nǐ hǎo ma", "translation": "Bạn khỏe không？", "pause_after_ms": 100},
        {"id": "hint", "activity": "q", "type": "context", "language": "vi-VN", "speaker": "coach", "text": "Gợi ý", "translation": "Gợi ý", "pause_after_ms": 5000},
        {"id": "answer", "activity": "q", "type": "model_answer", "language": "zh-CN", "speaker": "coach_zh", "text": "我很好。", "pinyin": "wǒ hěn hǎo", "translation": "Tôi khỏe.", "pause_before_ms": 80},
    ]
    result = chinese_only(Lesson.model_validate(lesson_data))
    assert [segment.id for segment in result.segments] == ["prompt", "answer"]
    assert result.segments[1].pause_before_ms == 5000


def test_chinese_only_uses_the_longest_pause_in_a_dropped_run(lesson_data):
    lesson_data["segments"] = [
        {"id": "prompt", "activity": "q", "type": "prompt", "language": "zh-CN", "speaker": "coach_zh", "text": "你好吗？", "pinyin": "nǐ hǎo ma", "translation": "Bạn khỏe không？"},
        {"id": "hint_one", "activity": "q", "type": "context", "language": "vi-VN", "speaker": "coach", "text": "Gợi ý 1", "translation": "Gợi ý 1", "pause_after_ms": 1800},
        {"id": "hint_two", "activity": "q", "type": "context", "language": "vi-VN", "speaker": "coach", "text": "Gợi ý 2", "translation": "Gợi ý 2", "pause_after_ms": 3200},
        {"id": "answer", "activity": "q", "type": "model_answer", "language": "zh-CN", "speaker": "coach_zh", "text": "我很好。", "pinyin": "wǒ hěn hǎo", "translation": "Tôi khỏe."},
    ]
    result = chinese_only(Lesson.model_validate(lesson_data))
    assert result.segments[1].pause_before_ms == 3200


def test_chinese_only_uses_cached_duration_for_an_identical_repeat(lesson_data):
    lesson_data["segments"] = [
        {"id": "first", "activity": "q", "type": "prompt", "language": "zh-CN", "speaker": "coach_zh", "text": "同一句。", "pinyin": "tóng yí jù", "translation": "Cùng một câu."},
        {"id": "hint", "activity": "q", "type": "context", "language": "vi-VN", "speaker": "coach", "text": "Gợi ý", "translation": "Gợi ý", "pause_after_ms": 400},
        {"id": "again", "activity": "q", "type": "model_answer", "language": "zh-CN", "speaker": "coach_zh", "text": "同一句。", "pinyin": "tóng yí jù", "translation": "Cùng một câu."},
    ]
    result = chinese_only(Lesson.model_validate(lesson_data), duration_ms_for=lambda segment: 900)
    assert result.segments[1].pause_before_ms == 1300


def test_chinese_only_drops_guided_only_segments(lesson_data):
    lesson_data["segments"] = [
        {"id": "before", "activity": "q", "type": "prompt", "language": "zh-CN", "speaker": "coach_zh", "text": "前面。", "pinyin": "qiánmiàn", "translation": "Trước."},
        {"id": "coach", "activity": "q", "type": "narration", "language": "zh-CN", "speaker": "coach_zh", "text": "只有越南语。", "pinyin": "zhǐyǒu Yuènányǔ", "translation": "Chỉ có tiếng Việt.", "guided_only": True, "pause_after_ms": 600},
        {"id": "after", "activity": "q", "type": "prompt", "language": "zh-CN", "speaker": "coach_zh", "text": "后面。", "pinyin": "hòumiàn", "translation": "Sau."},
    ]
    result = chinese_only(Lesson.model_validate(lesson_data))
    assert [segment.id for segment in result.segments] == ["before", "after"]
    assert result.segments[1].pause_before_ms == 400
