from copy import deepcopy

import pytest
from pydantic import ValidationError

from mandarin_course.lesson_schema import Lesson


def test_missing_required_field_is_invalid(lesson_data):
    invalid = deepcopy(lesson_data)
    del invalid["title"]
    with pytest.raises(ValidationError):
        Lesson.model_validate(invalid)


def test_duplicate_segment_id_is_invalid(lesson_data):
    invalid = deepcopy(lesson_data)
    invalid["segments"].append(deepcopy(invalid["segments"][0]))
    with pytest.raises(ValidationError, match="unique"):
        Lesson.model_validate(invalid)


def test_negative_pause_is_invalid(lesson_data):
    invalid = deepcopy(lesson_data)
    invalid["segments"][0]["pause_after_ms"] = -1
    with pytest.raises(ValidationError):
        Lesson.model_validate(invalid)


def test_mandarin_requires_pinyin(lesson_data):
    invalid = deepcopy(lesson_data)
    del invalid["segments"][0]["pinyin"]
    with pytest.raises(ValidationError, match="pinyin"):
        Lesson.model_validate(invalid)


def test_dialogue_turns_reject_coach_in_mandarin(lesson_data):
    invalid = deepcopy(lesson_data)
    invalid["segments"][0]["type"] = "dialogue"
    invalid["segments"][0]["turns"] = [{"speaker": "coach", "text": "你好"}]
    with pytest.raises(ValidationError, match="coach"):
        Lesson.model_validate(invalid)


def test_dialogue_turns_reject_coach_zh_in_mandarin(lesson_data):
    invalid = deepcopy(lesson_data)
    invalid["segments"][0]["type"] = "dialogue"
    invalid["segments"][0]["turns"] = [{"speaker": "coach_zh", "text": "你好"}]
    with pytest.raises(ValidationError, match="coach"):
        Lesson.model_validate(invalid)


def test_coach_zh_accepts_mandarin_narration(lesson_data):
    valid = deepcopy(lesson_data)
    valid["segments"][0]["speaker"] = "coach_zh"
    lesson = Lesson.model_validate(valid)
    assert lesson.segments[0].speaker == "coach_zh"


def test_coach_zh_rejects_non_mandarin(lesson_data):
    invalid = deepcopy(lesson_data)
    invalid["segments"][0]["speaker"] = "coach_zh"
    invalid["segments"][0]["language"] = "vi-VN"
    invalid["segments"][0]["pinyin"] = None
    invalid["segments"][0]["translation"] = None
    with pytest.raises(ValidationError, match="coach_zh"):
        Lesson.model_validate(invalid)
