from __future__ import annotations

import json
from dataclasses import asdict
from pathlib import Path

import pytest

from mandarin_course.lesson_schema import Lesson


@pytest.fixture
def lesson_data() -> dict:
    return {
        "lesson_id": "lesson_999",
        "title": "Test lesson",
        "level": "absolute_beginner",
        "target_duration_minutes": 5,
        "communicative_goals": ["Test"],
        "prerequisites": [],
        "new_patterns": ["你好"],
        "review_pattern_ids": [],
        "version": 1,
        "segments": [
            {
                "id": "hello",
                "activity": "Test",
                "type": "pattern",
                "language": "zh-CN",
                "text": "你好。",
                "pinyin": "nǐ hǎo",
                "translation": "Hello.",
                "pause_before_ms": 100,
                "pause_after_ms": 200,
            }
        ],
    }


@pytest.fixture
def lesson(lesson_data: dict) -> Lesson:
    return Lesson.model_validate(lesson_data)


@pytest.fixture
def voices_registry(tmp_path: Path) -> Path:
    from mandarin_course.voice_registry import VoiceProfile

    path = tmp_path / "voices.json"
    profiles = [
        VoiceProfile(voice_id="anna-voice", name="Anna", language="zh", accent="beijing mandarin", gender="female"),
        VoiceProfile(voice_id="li-ming-voice", name="Li Ming", language="zh", accent="taiwan mandarin", gender="male"),
        VoiceProfile(voice_id="vietnamese-coach", name="Coach", language="vi", accent="southern", gender="female"),
    ]
    path.write_text(json.dumps([asdict(profile) for profile in profiles], ensure_ascii=False), encoding="utf-8")
    return path

