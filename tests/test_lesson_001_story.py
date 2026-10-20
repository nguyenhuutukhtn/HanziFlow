from mandarin_course.lesson_schema import load_lesson


def _chinese(lesson):
    return [segment for segment in lesson.segments if segment.language == "zh-CN"]


def test_story_draft_stays_inside_the_week_one_budget():
    lesson = load_lesson("lessons/lesson_001.json")
    assert lesson.lesson_id == "lesson_001"
    assert lesson.variant == "guided"
    assert lesson.level == "absolute_beginner"
    spoken = "\n".join(segment.text for segment in _chinese(lesson))
    for banned in ("了", "在", "个", "只", "以为"):
        assert banned not in spoken
    vietnamese = "\n".join(segment.text for segment in lesson.segments if segment.language == "vi-VN")
    assert "Li Ming" not in vietnamese
    assert "Anna nói" not in vietnamese
    assert "ta nói" not in vietnamese
    for label in ("Nghe chuyện.", "Nghe từ.", "Tiếp tục.", "Nghe, rồi nói.", "Đến lượt bạn."):
        assert label not in vietnamese
    assert not any("\u4e00" <= char <= "\u9fff" for char in vietnamese)
    for segment in _chinese(lesson):
        if "不是" in segment.text:
            assert "bú shì" in (segment.pinyin or "")
            assert "bù shì" not in (segment.pinyin or "")
    praise = [segment.text for segment in lesson.segments if segment.text in {"Đúng rồi.", "Tốt."}]
    assert praise == ["Đúng rồi.", "Tốt."]
    assert "这是同事的。" in spoken
    assert "袋子上有同事的名字。" in spoken
    assert "名字是同事的。" not in spoken
    assert "我的" not in spoken
    assert "安娜看外卖" not in spoken
    story_pauses = [segment.pause_after_ms for segment in lesson.segments if segment.id.startswith("s1_")]
    assert story_pauses and max(story_pauses) <= 1600
    answer_waits = [segment.pause_after_ms for segment in lesson.segments if segment.type == "prompt" and segment.pause_after_ms]
    assert answer_waits and min(answer_waits) >= 4500
    by_index = list(lesson.segments)
    for index, segment in enumerate(by_index):
        if segment.language != "vi-VN":
            continue
        previous = by_index[index - 1]
        assert previous.language == "zh-CN"
        assert previous.translation == segment.text
    assert "桌上有一袋外卖。" in spoken
    assert "他们看袋子上的名字。" in spoken
    assert "这是李明的还是同事的？" in spoken
    ids = {segment.id: segment for segment in lesson.segments}
    assert "c1_q_or_name" not in ids
    for slow_id in ("b_ask_slow", "b_ts_slow", "b_or_slow"):
        slow = ids[slow_id]
        assert slow.playback_speed == 0.85
        assert slow.pause_after_ms == 3000
        assert ids[slow_id.removesuffix("_slow") + "_nat"].text == slow.text
        assert ids[slow_id.removesuffix("_slow") + "_nat"].pause_after_ms == 4200
    buildup_chunks = [
        segment for segment in lesson.segments
        if segment.id.startswith(("b_ask_c", "b_ts_c", "b_or_c"))
    ]
    assert buildup_chunks and all(segment.pause_after_ms == 1800 for segment in buildup_chunks)
    assert "李明的" in ids["b_ask_c2"].text
    assert "是李明的" in ids["b_ask_slow"].text
    assert "同事的" in ids["b_ts_c2"].text
    assert "是同事的" in ids["b_ts_slow"].text
    assert ids["sig_ma"].text == "句子最后的“吗”，表示这是“是”或者“不是”的问题。"
    assert ids["sig_ma_vi"].text == "Từ này ở cuối câu cho biết đây là câu hỏi có hoặc không."
    assert ids["sig_ma_again"].text == ids["sig_ma"].text
    assert ids["sig_ma_ex"].text == "这是外卖吗？"
    assert ids["sig_who"].text == "听到“谁的”，你说这是谁的。"
    assert ids["sig_who_vi"].text == "Nghe thấy cụm này, bạn nói đây là của ai."
    assert ids["sig_who_again"].text == ids["sig_who"].text
    assert ids["sig_who_ex"].text == "这是谁的？"
    assert ids["sig_or"].text == "听到“还是”，你选这一样，或者那一样。"
    assert ids["sig_or_vi"].text == "Nghe thấy từ này, bạn chọn một trong hai."
    assert ids["sig_or_again"].text == ids["sig_or"].text
    assert ids["sig_or_ex"].text == "这是李明的还是同事的？"
    assert 40 <= lesson.target_duration_minutes <= 60
    assert ids["r2_food_vi"].text == "Đồ ăn mang về."
    assert ids["r2_name_vi"].text == "Tên."
    assert ids["r2_who_vi"].text == "Của ai."
    assert ids["r2_or_vi"].text == "Nghe thấy từ này, bạn chọn một trong hai."
    assert ids["sig_ma_ans_vi"].text == "Phải."
    assert "李明猜是安娜的外卖吗？" not in spoken
    assert "外卖是谁的外卖？" not in spoken
    assert ids["r1_long_name"].text.startswith("袋子上有李明的名字还是")
    assert ids["r1_food"].text == "这是外卖吗？"
    assert "r1_name_ts_vi" in ids
    assert "r2c_name_ts_vi" not in ids
    assert "r3_food_vi" not in ids
    assert "r3_name_vi" not in ids
    food_at = next(index for index, segment in enumerate(lesson.segments) if segment.id == "r1_food")
    long_at = next(index for index, segment in enumerate(lesson.segments) if segment.id == "r1_long_name")
    assert food_at < long_at
    prompts: dict[str, dict[str, int]] = {}
    for segment in lesson.segments:
        if segment.type != "prompt":
            continue
        prompts.setdefault(segment.activity, {})
        prompts[segment.activity][segment.text] = prompts[segment.activity].get(segment.text, 0) + 1
    assert all(count <= 3 for counts in prompts.values() for count in counts.values())
    say_vi = [segment.id for segment in lesson.segments if segment.activity == "Nói theo" and segment.language == "vi-VN"]
    assert say_vi[0] == "say_lead_vi"
    assert all(segment_id == "say_lead_vi" or segment_id.endswith("_gloss_vi") for segment_id in say_vi)
    assert [segment.id for segment in lesson.segments if segment.activity == "Nghe lại" and segment.language == "vi-VN"] == ["close_cue_vi", "end_today_vi", "end_vi"]


def test_story_practice_is_the_same_scene_without_drills():
    lesson = load_lesson("lessons/lesson_001_practice.json")
    assert lesson.variant == "practice"
    assert not any(segment.type in {"model_answer", "prompt", "pattern"} for segment in lesson.segments)
    guides = [segment for segment in lesson.segments if segment.language == "vi-VN"]
    assert len(guides) == 1
    spoken = [segment.text for segment in lesson.segments if segment.text == "这是同事的。"]
    assert len(spoken) == 3
