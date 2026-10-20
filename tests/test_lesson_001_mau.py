from mandarin_course.lesson_schema import load_lesson

PRAISE = {"Đúng rồi.", "Đúng.", "Hay."}
HARD = {
    "你叫什么？",
    "我叫安娜。你呢？",
    "很高兴认识你。",
    "早，安娜！最近怎么样？",
    "那我先走了。再见！",
}
CHUNKS = {
    "什么",
    "叫什么",
    "你呢",
    "认识你",
    "怎么样",
    "最近怎么样",
    "我先走了",
    "那我先走了",
}
SCENE_1 = "你好！你好！你叫什么？我叫安娜。你呢？我叫李明。很高兴认识你。我也是。再见！再见！"
SCENE_2 = "早，李明！早，安娜！最近怎么样？挺好的。你呢？也不错。那我先走了。再见！再见！"


def test_lesson_001_mau_is_the_long_template():
    lesson = load_lesson("lessons/lesson_001_mau.json")
    assert lesson.lesson_id == "lesson_001"
    assert lesson.variant == "guided"
    assert lesson.review_pattern_ids == []
    assert 25 <= lesson.target_duration_minutes <= 30
    assert all(segment.language != "en-US" for segment in lesson.segments)
    assert all(segment.repetitions == 1 for segment in lesson.segments)
    assert all("Li Ming" not in segment.text for segment in lesson.segments)
    assert all(segment.text not in {"你", "好", "不", "我叫", "走了", "我叫安娜", "很高兴"} for segment in lesson.segments)

    activities = []
    for segment in lesson.segments:
        if not activities or activities[-1] != segment.activity:
            activities.append(segment.activity)
    assert activities == [
        "Nghe cả cuộc",
        "Từng câu",
        "Từng câu lần nữa",
        "Từng câu lần ba",
        "Ôn và ráp câu",
        "Ôn và ráp câu lần nữa",
        "Ôn và ráp câu lần ba",
        "Hỏi đáp",
        "Hỏi đáp lần nữa",
        "Hỏi đáp lần ba",
        "Nghe lại",
    ]

    scenes = [segment.text for segment in lesson.segments if segment.turns]
    assert scenes[0] == SCENE_1
    assert SCENE_2 in scenes
    assert scenes[-2] == SCENE_1
    assert scenes[-1] == SCENE_2

    ending = [segment for segment in lesson.segments if segment.activity == "Nghe lại"]
    assert ending[0].language == "vi-VN"
    assert ending[-2].text == "Hết bài. Ngày mai nghe lại chính bài này nhé."
    assert ending[-1].text == "再见！"
    assert ending[-1].language == "zh-CN"
    assert all(segment.language == "zh-CN" for segment in ending[1:-2])
    assert sum(1 for segment in ending if segment.turns) == 6

    for segment in lesson.segments:
        if segment.playback_speed != 1.0:
            assert segment.playback_speed == 0.85
            assert segment.text in HARD
        if segment.text in CHUNKS:
            assert segment.playback_speed == 1.0
            assert segment.type == "dialogue"

    first_review = [segment.text for segment in lesson.segments if segment.activity == "Ôn và ráp câu"]
    assert _window(first_review, "什么") == [
        "什么",
        "Gì?",
        "什么",
        "叫什么",
        "Tên gì?",
        "叫什么",
        "你叫什么？",
        "Bạn tên gì?",
        "你叫什么？",
    ]
    assert _window(first_review, "你呢")[:6] == [
        "你呢",
        "Còn bạn?",
        "你呢",
        "我叫安娜。你呢？",
        "Mình tên Anna. Còn bạn?",
        "我叫安娜。你呢？",
    ]
    assert _window(first_review, "认识你")[:6] == [
        "认识你",
        "Gặp bạn.",
        "认识你",
        "很高兴认识你。",
        "Rất vui được gặp bạn.",
        "很高兴认识你。",
    ]
    assert _window(first_review, "怎么样")[:6] == [
        "怎么样",
        "Thế nào?",
        "怎么样",
        "最近怎么样",
        "Dạo này thế nào?",
        "最近怎么样",
    ]
    assert _window(first_review, "我先走了")[:6] == [
        "我先走了",
        "Mình đi trước.",
        "我先走了",
        "那我先走了",
        "Vậy mình đi trước.",
        "那我先走了",
    ]

    segments = lesson.segments
    for index, segment in enumerate(segments):
        if segment.type != "model_answer":
            continue
        assert segments[index - 1].type == "prompt"
        assert segments[index - 1].metadata["expects"] == segment.id
        praised = segments[index + 1].text in PRAISE
        following = segments[index + 2] if praised else segments[index + 1]
        if praised:
            assert segment.activity == "Hỏi đáp"
            assert segment.text in {"我叫安娜。你呢？", "早，安娜！最近怎么样？"}
        if segment.activity != "Hỏi đáp" or segments[index - 1].text in {"你好！", "再见！"}:
            assert following.language == "zh-CN"
        else:
            assert following.language == "vi-VN"
    praises = [segment for segment in segments if segment.text in PRAISE]
    assert len(praises) == 2
    assert {segment.activity for segment in praises} == {"Hỏi đáp"}
    assert lesson.coach_phrases_introduced == [
        "你叫什么？",
        "我叫…",
        "你呢？",
        "很高兴认识你。",
        "再见",
        "最近怎么样？",
        "挺好的。",
        "那我先走了。",
    ]
    assert lesson.coach_phrases_reduced == ["你叫什么？", "最近怎么样？", "再见"]
    assert all("Cũng vậy" not in segment.text for segment in segments)
    assert all(
        not any("\u4e00" <= char <= "\u9fff" for char in segment.text)
        for segment in segments
        if segment.language == "vi-VN"
    )
    opened = [segment.text for segment in segments if segment.activity == "Nghe cả cuộc"]
    assert opened[opened.index("Muốn hỏi tên một người bạn, bạn nói:") + 1] == "你叫什么？"
    assert "ta nói" not in " ".join(segment.text for segment in segments if segment.language == "vi-VN")
    assert "Anna nói" not in " ".join(segment.text for segment in segments)
    assert "Anna trả lời" not in " ".join(segment.text for segment in segments)
    assert "Sang buổi sáng vài ngày sau. Cũng vậy." not in opened
    switches = [segment for segment in segments if segment.metadata.get("code_switch") is True]
    assert switches
    assert all(segment.language == "zh-CN" and segment.speaker in {"anna", "li_ming"} for segment in switches)


def _window(texts: list[str], start: str) -> list[str]:
    index = texts.index(start)
    return texts[index : index + 9]
