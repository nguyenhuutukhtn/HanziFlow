from __future__ import annotations

import json
from pathlib import Path

import pytest

from mandarin_course.publish import PublishError
from mandarin_course.publish_notes import load_notes
from mandarin_course.publish_tokens import load_vocab, tones_from_pinyin

LESSONS = Path(__file__).resolve().parents[1] / "lessons"


@pytest.mark.parametrize("lesson_id", ["lesson_001", "lesson_002"])
def test_real_notes_files_validate(lesson_id):
    notes = load_notes(LESSONS / f"{lesson_id}_notes.json", lesson_id)
    assert notes and notes["patterns"] and notes["questions"]
    assert all(entry["examples"] for entry in (*notes["patterns"], *notes["questions"]))


def test_real_lesson_2_vocab_has_tones_for_every_word():
    vocab = load_vocab(LESSONS / "lesson_002_vocab.json", "lesson_002")
    assert len(vocab.words) > 50
    assert all(tones_from_pinyin(word.hanzi, word.pinyin) for word in vocab.words)


def test_missing_notes_source_is_none(tmp_path):
    assert load_notes(tmp_path / "lesson_009_notes.json", "lesson_009") is None


def _mutated(tmp_path, change):
    data = json.loads((LESSONS / "lesson_002_notes.json").read_text(encoding="utf-8"))
    change(data)
    path = tmp_path / "lesson_002_notes.json"
    path.write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")
    return path


def test_invalid_notes_name_the_id(tmp_path):
    first = json.loads((LESSONS / "lesson_002_notes.json").read_text(encoding="utf-8"))["patterns"][0]["id"]
    path = _mutated(tmp_path, lambda d: d["patterns"][0].pop("explain_vi"))
    with pytest.raises(PublishError, match=first):
        load_notes(path, "lesson_002")
    path = _mutated(tmp_path, lambda d: d["questions"][0].update(id=d["patterns"][0]["id"]))
    with pytest.raises(PublishError, match=f"duplicate notes id: {first}"):
        load_notes(path, "lesson_002")
    path = _mutated(tmp_path, lambda d: d["questions"][0].update(examples=[]))
    with pytest.raises(PublishError, match="at least one example"):
        load_notes(path, "lesson_002")


def test_tokenizer_ignores_colon_after_pinyin_word_but_keeps_u_colon():
    from mandarin_course.publish_tokens import tokenize_mandarin

    tokens = tokenize_mandarin("看见，意思是：我看到了。", "Kànjiàn, yìsi shì: wǒ kàndào le.")
    assert [t["pinyin"] for t in tokens if "pinyin" in t][:3] == ["Kànjiàn", "yìsi", "shì"]
    from mandarin_course.publish_tokens import _pinyin_words

    assert _pinyin_words("nu:3 lu:e shì: wǒ") == ["nu:3", "lu:e", "shì", "wǒ"]
