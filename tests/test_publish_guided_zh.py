from __future__ import annotations

from mandarin_course.lesson_schema import Lesson
from mandarin_course.publish_timeline import _enrich_timeline


def test_enrich_guided_zh_skips_vietnamese_and_guided_only_segments(lesson_data):
    lesson_data["segments"] = [
        {"id": "a", "activity": "one", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "你好。", "pinyin": "nǐ hǎo", "translation": "Xin chào."},
        {"id": "b", "activity": "one", "type": "narration", "language": "zh-CN", "guided_only": True,
         "speaker": "coach_zh", "text": "再用越南语说。", "pinyin": "zài yòng Yuènányǔ shuō", "translation": "Rồi nói tiếng Việt."},
        {"id": "c", "activity": "one", "type": "context", "language": "vi-VN",
         "speaker": "coach", "text": "Xin chào.", "translation": "Xin chào."},
        {"id": "d", "activity": "one", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "再见。", "pinyin": "zài jiàn", "translation": "Tạm biệt."},
    ]
    timeline = {
        "schema_version": 1, "lesson_id": "lesson_999", "lesson_version": 1, "track": "guided_zh",
        "duration_ms": 20, "cues": [
            {"line_id": "a", "start_ms": 0, "end_ms": 10},
            {"line_id": "d", "start_ms": 10, "end_ms": 20},
        ],
    }

    enriched = _enrich_timeline(timeline, Lesson.model_validate(lesson_data), "guided_zh", 1)

    assert [cue["line_id"] for cue in enriched["cues"]] == ["a", "d"]
