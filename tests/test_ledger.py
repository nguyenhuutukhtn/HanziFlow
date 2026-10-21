from __future__ import annotations

import json
from pathlib import Path

from mandarin_course.characters.resolve import resolve_lesson_characters
from mandarin_course.story.ledger import build_ledger, chars_taught_before


def _lesson(lesson_id: str, title: str, texts: list[str]) -> dict:
    return {
        "lesson_id": lesson_id,
        "title": title,
        "communicative_goals": [f"goal {lesson_id}"],
        "new_patterns": [f"pattern {lesson_id}"],
        "segments": [{"language": "zh-CN", "text": text} for text in texts]
        + [{"language": "vi-VN", "text": "Xin chào."}],
    }


def _write(lessons_dir: Path, lesson: dict, vocab: list[tuple[str, str, str]], premise: str) -> None:
    lesson_id = lesson["lesson_id"]
    (lessons_dir / f"{lesson_id}.json").write_text(json.dumps(lesson, ensure_ascii=False), encoding="utf-8")
    (lessons_dir / f"{lesson_id}_practice.json").write_text("{}", encoding="utf-8")
    words = [{"hanzi": h, "pinyin": p, "meaning_vi": v} for h, p, v in vocab]
    (lessons_dir / f"{lesson_id}_vocab.json").write_text(
        json.dumps({"lesson_id": lesson_id, "words": words}, ensure_ascii=False), encoding="utf-8"
    )
    header = f"> {lesson_id} story source.\n> Story: {premise}\n> Present time only. Grammar: 是.\n\nlesson_id: {lesson_id}\n"
    (lessons_dir / "src" / f"{lesson_id}.story.md").write_text(header, encoding="utf-8")


def _two_lessons(tmp_path: Path) -> Path:
    lessons_dir = tmp_path / "lessons"
    (lessons_dir / "src").mkdir(parents=True)
    _write(lessons_dir, _lesson("lesson_001", "Bài một", ["女人。", "我们是女人。"]),
           [("女人", "nǚrén", "phụ nữ"), ("我们", "wǒmen", "chúng ta")], "a woman at the market")
    _write(lessons_dir, _lesson("lesson_002", "Bài hai", ["人妈好。"]),
           [("妈", "mā", "mẹ"), ("好", "hǎo", "tốt")], "mother says hello")
    (lessons_dir / "src" / "bible.md").write_text("#### Cast\n- 安娜 (Anna)\n", encoding="utf-8")
    return lessons_dir


def test_ledger_lists_earlier_lessons_words_chars_premise_and_bible(tmp_path):
    lessons_dir = _two_lessons(tmp_path)
    ledger = build_ledger(lessons_dir, "lesson_002")
    assert "lesson_001 — Bài một" in ledger
    assert "Premise: a woman at the market" in ledger
    assert "Grammar note: Present time only. Grammar: 是." in ledger
    assert "| 我们 | wǒmen | chúng ta |" in ledger
    assert "New characters (5): 女人我们是" in ledger
    assert "Known words (2): 女人、我们" in ledger
    assert "### Story bible" in ledger and "- 安娜 (Anna)" in ledger
    assert "Bài hai" not in ledger  # only lessons before 002
    assert ledger == build_ledger(lessons_dir, "002")  # deterministic, short id accepted


def test_first_lesson_ledger_has_no_earlier_lessons(tmp_path):
    lessons_dir = _two_lessons(tmp_path)
    ledger = build_ledger(lessons_dir, "lesson_001")
    assert ledger.startswith("(none — this is the first lesson)")
    assert chars_taught_before(lessons_dir, "lesson_001") == set()


def test_ledger_stays_under_200_lines_for_ten_lessons(tmp_path):
    lessons_dir = tmp_path / "lessons"
    (lessons_dir / "src").mkdir(parents=True)
    for n in range(1, 11):
        vocab = [(chr(0x4E00 + n * 100 + i), "x", "y") for i in range(75)]
        _write(lessons_dir, _lesson(f"lesson_{n:03d}", f"L{n}", ["".join(h for h, _, _ in vocab)]), vocab, f"premise {n}")
    ledger = build_ledger(lessons_dir, "lesson_011")
    assert len(ledger.splitlines()) <= 200
    assert "premise 1" in ledger and "Known words (750)" in ledger


def test_lesson_2_characters_exclude_lesson_1_chars_and_prereqs(tmp_path):
    lessons_dir = _two_lessons(tmp_path)
    loaders = {
        "mmah": {"妈": {"decomp_ids": "⿰女马", "semantic": "女", "phonetic": "马"}},
        "radicals": {"女": {"name_en": "woman", "name_vi": "nữ"}},
    }
    lesson_2 = json.loads((lessons_dir / "lesson_002.json").read_text(encoding="utf-8"))
    seen = chars_taught_before(lessons_dir, "lesson_002")
    assert seen == {"女", "人", "我", "们", "是"}
    result = resolve_lesson_characters(lesson_2, seen, loaders)
    assert [entry["char"] for entry in result["characters"]] == ["妈", "好"]
    prereqs = [entry["char"] for entry in result["components_prereq"]]
    assert "女" not in prereqs and "马" in prereqs
