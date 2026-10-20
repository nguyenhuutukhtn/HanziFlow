from copy import deepcopy

import pytest
from pydantic import ValidationError

from mandarin_course.lesson_schema import Lesson, load_lesson


def _assert_guided_audio(lesson) -> None:
    assert lesson.variant == "guided"
    assert all(segment.language != "en-US" for segment in lesson.segments)
    assert any(segment.language == "vi-VN" for segment in lesson.segments)
    assert all(segment.repetitions == 1 for segment in lesson.segments)
    by_id = {segment.id: segment for segment in lesson.segments}
    shadows = [segment for segment in lesson.segments if segment.activity == "Nói theo" and segment.language == "zh-CN"]
    assert shadows
    for segment in shadows:
        if "_c" in segment.id:
            assert segment.playback_speed == 1.0
            assert segment.text not in {"你", "我", "不", "我叫"}
        elif segment.id.endswith("_nat"):
            assert segment.playback_speed == 1.0
        else:
            assert segment.playback_speed == 0.85
    hard = [segment for segment in shadows if segment.id.endswith("_nat")]
    assert 3 <= len(hard) <= 5
    for segment in hard:
        slow = by_id[segment.id.removesuffix("_nat")]
        assert slow.text == segment.text
        assert slow.playback_speed == 0.85
        assert any(chunk.id.startswith(slow.id + "_c") for chunk in shadows)
    assert all(segment.activity != "Again, faster" for segment in lesson.segments)
    glosses = [segment for segment in lesson.segments if segment.activity == "Từng câu" and segment.id.endswith("_vi")]
    assert glosses
    for gloss in glosses:
        source_id = gloss.id.removesuffix("_vi")
        before = by_id[source_id]
        after = by_id[f"{source_id}_again"]
        assert before.language == "zh-CN" and after.language == "zh-CN"
        assert before.text == after.text
        assert after.playback_speed == 1.0
        assert gloss.text == before.translation
        assert lesson.segments.index(before) < lesson.segments.index(gloss) < lesson.segments.index(after)
    vietnamese = "\n".join(segment.text for segment in lesson.segments if segment.language == "vi-VN")
    assert "Li Ming" not in vietnamese
    reflex_line = "Lần cuối. Hãy trả lời ngay khi nghe câu hỏi. Đừng dịch trong đầu."
    reflex_intros = [segment for segment in lesson.segments if segment.activity == "Phản xạ" and segment.type == "narration"]
    assert len(reflex_intros) == 2
    assert all(segment.text == reflex_line for segment in reflex_intros)
    reflex_prompts = [segment for segment in lesson.segments if segment.activity == "Phản xạ" and segment.type == "prompt"]
    reply_pause = {
        segment.text: segment.pause_after_ms
        for segment in lesson.segments
        if segment.activity == "Trả lời" and segment.type == "prompt"
    }
    assert reflex_prompts
    assert all(1500 <= segment.pause_after_ms <= 3000 for segment in reflex_prompts)
    assert all(segment.playback_speed == 1.0 for segment in reflex_prompts)
    for prompt in reflex_prompts:
        assert prompt.pause_after_ms < reply_pause[prompt.text]
        answer = by_id[str(prompt.metadata["expects"])]
        assert answer.type == "model_answer"
        assert answer.activity == "Phản xạ"


def _assert_practice_audio(path: str, scene_text: str) -> None:
    lesson = load_lesson(path)
    assert lesson.variant == "practice"
    assert lesson.lesson_id == path.removeprefix("lessons/").removesuffix("_practice.json")
    assert not any(segment.type in {"model_answer", "prompt", "pattern"} for segment in lesson.segments)
    assert all(segment.playback_speed == 1.0 for segment in lesson.segments)
    assert all(segment.pause_after_ms <= 1600 for segment in lesson.segments)
    spoken = [segment.text for segment in lesson.segments]
    assert spoken.count(scene_text) == 3
    guides = [segment for segment in lesson.segments if segment.language == "vi-VN"]
    assert len(guides) == 2
    assert all(segment.id.startswith("c1_") for segment in guides)
    assert "Li Ming" not in "\n".join(segment.text for segment in guides)


def test_lesson_002_is_one_real_visit():
    lesson = load_lesson("lessons/lesson_002.json")
    assert lesson.lesson_id == "lesson_002"
    spoken = "\n".join(segment.text for segment in lesson.segments)
    pinyin = "\n".join(segment.pinyin or "" for segment in lesson.segments)
    for leftover in ("对不起", "不要", "好的。对不起", "我不要"):
        assert leftover not in spoken
    assert "bù yào" not in pinyin
    assert "bú yòng" in pinyin
    assert "你还要咖啡吗" in spoken
    assert "茶" not in spoken
    assert "那我先走了。" in lesson.review_pattern_ids
    chair = next(segment for segment in lesson.segments if segment.id == "chair_intro")
    assert "quyển vở" in chair.text
    assert "điện thoại" in chair.text
    assert "Ừ, cảm ơn." in spoken
    assert "Có, cảm ơn." not in spoken
    assert "你要咖啡吗" in spoken
    _assert_guided_audio(lesson)
    _assert_practice_audio(
        "lessons/lesson_002_practice.json",
        "你好！请坐。好的，谢谢。你要咖啡吗？要，谢谢。你还要咖啡吗？不用了，谢谢。",
    )


def test_lesson_003_does_not_thank_for_a_dislike():
    lesson = load_lesson("lessons/lesson_003.json")
    spoken = "\n".join(segment.text for segment in lesson.segments)
    assert "不喜欢，谢谢" not in spoken
    assert "不喜欢。" in spoken
    assert "我想吃面条" in spoken
    assert "我要面条" not in spoken
    assert "辣的" not in spoken
    assert "还要吗" in spoken
    assert "不用了，谢谢" in spoken
    intro = next(segment for segment in lesson.segments if segment.id == "table_intro")
    assert "có thấy ngon không" in intro.text
    assert "thích ăn cay" not in intro.text
    _assert_guided_audio(lesson)
    _assert_practice_audio(
        "lessons/lesson_003_practice.json",
        "你好！你想吃什么？我想吃面条。你想喝什么？我想喝水。还要别的吗？不用了，谢谢。",
    )


def test_real_lesson_is_valid():
    lesson = load_lesson("lessons/lesson_001.json")
    assert lesson.lesson_id == "lesson_001"
    assert len(lesson.segments) >= 50
    spoken = "\n".join(segment.text for segment in lesson.segments)
    for textbook in ("你好吗", "我很好", "你叫什么名字", "大卫", "王芳"):
        assert textbook not in spoken
    assert "Khá ổn" in spoken
    assert "Cũng ổn" not in spoken
    _assert_guided_audio(lesson)
    _assert_practice_audio(
        "lessons/lesson_001_practice.json",
        "你好！你好！你叫什么？我叫安娜。你呢？我叫李明。很高兴认识你。我也是。再见！再见！",
    )


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
