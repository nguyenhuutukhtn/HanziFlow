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


def test_modes_v1_lesson_accepts_mode_variant_new_speakers_and_gender_override(lesson_data):
    valid = deepcopy(lesson_data)
    valid.update(format="modes_v1", variant="hieu", voice_genders={"coach_zh": "female"})
    valid["segments"][0]["speaker"] = "wang_fang"
    valid["segments"].extend(
        [
            {**deepcopy(valid["segments"][0]), "id": "chen_dong", "speaker": "chen_dong"},
            {**deepcopy(valid["segments"][0]), "id": "coach_zh", "speaker": "coach_zh"},
            {
                **deepcopy(valid["segments"][0]),
                "id": "coach_vi",
                "speaker": "coach",
                "language": "vi-VN",
                "pinyin": None,
                "translation": None,
            },
        ]
    )
    assert Lesson.model_validate(valid).format == "modes_v1"


@pytest.mark.parametrize(
    ("format", "variant"),
    [("modes_v1", "guided"), ("linear_v1", "noi")],
)
def test_lesson_format_rejects_wrong_variant(lesson_data, format, variant):
    invalid = deepcopy(lesson_data)
    invalid.update(format=format, variant=variant)
    with pytest.raises(ValidationError, match="variant"):
        Lesson.model_validate(invalid)


def test_wang_fang_must_speak_mandarin(lesson_data):
    invalid = deepcopy(lesson_data)
    invalid["segments"][0].update(speaker="wang_fang", language="vi-VN", pinyin=None, translation=None)
    with pytest.raises(ValidationError, match="Character speakers must use Mandarin"):
        Lesson.model_validate(invalid)


def test_voice_gender_must_name_a_lesson_speaker(lesson_data):
    invalid = deepcopy(lesson_data)
    invalid["voice_genders"] = {"bob": "female"}
    with pytest.raises(ValidationError, match="voice_genders"):
        Lesson.model_validate(invalid)
