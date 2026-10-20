from mandarin_course.transcript import render_transcript
from mandarin_course.lesson_schema import DialogueTurn


def test_practice_transcript_has_structure_and_pinyin(lesson):
    text = render_transcript(lesson)
    assert "# Test lesson — Practice transcript" in text
    assert "## Test" in text
    assert "nǐ hǎo" in text
    assert "你好" not in text
    assert "Hello." not in text
    full = render_transcript(lesson, full_reference=True, show_hanzi=True)
    assert "Hello." in full
    assert "你好" in full


def test_practice_hides_model_answers(lesson):
    prompt = lesson.segments[0].model_copy(update={"id": "q", "type": "prompt"})
    answer = lesson.segments[0].model_copy(update={"id": "a", "type": "model_answer", "pinyin": "secret answer"})
    changed = lesson.model_copy(update={"segments": [prompt, answer]})
    practice = render_transcript(changed)
    full = render_transcript(changed, full_reference=True)
    assert "secret answer" not in practice
    assert "secret answer" in full


def test_transcript_labels_dialogue_speakers(lesson):
    dialogue = lesson.segments[0].model_copy(update={
        "type": "dialogue",
        "speaker": None,
        "turns": [
            DialogueTurn(speaker="anna", text="你好"),
            DialogueTurn(speaker="li_ming", text="你好"),
        ],
    })
    changed = lesson.model_copy(update={"segments": [dialogue]})
    assert "Speakers: Anna ↔ Li Ming" in render_transcript(changed)
