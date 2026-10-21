"""Lesson ledger: what lessons before N taught (words, characters, patterns, premise, bible).

Built only from committed sources (story-source headers, lesson JSON, `*_vocab.json`,
`lessons/src/bible.md`) and fully deterministic. Used by `export-lesson` ("Already
taught") and `mandarin-course ledger <id>`; `chars_taught_before` also feeds publish.
"""

from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

from ..characters.resolve import _CJK, _lesson_segments

MAX_LINES = 200
FULL_LESSONS = 2
VOCAB_PER_ROW = 3
LEDGER_DIR = Path("build")
_LESSON_FILE = re.compile(r"^lesson_\d+$")


def normalize_lesson_id(value: str) -> str:
    return value if value.startswith("lesson_") else f"lesson_{value}"


def _lessons_before(lessons_dir: Path, lesson_id: str) -> list[dict[str, Any]]:
    lesson_id = normalize_lesson_id(lesson_id)
    paths = sorted(p for p in lessons_dir.glob("lesson_*.json") if _LESSON_FILE.match(p.stem) and p.stem < lesson_id)
    return [json.loads(p.read_text(encoding="utf-8")) for p in paths]


def _zh_text(lesson: dict[str, Any]) -> str:
    return "\n".join(segment.get("text", "") for segment in _lesson_segments(lesson))


def _chars(text: str) -> list[str]:
    result: list[str] = []
    for run in _CJK.findall(text):
        for char in run:
            if char not in result:
                result.append(char)
    return result


def chars_taught_before(lessons_dir: Path, lesson_id: str) -> set[str]:
    """Every character in the zh-CN segments of guided lessons before `lesson_id`."""
    return {char for lesson in _lessons_before(lessons_dir, lesson_id) for char in _chars(_zh_text(lesson))}


def _story_notes(lessons_dir: Path, lesson_id: str) -> tuple[str, str]:
    path = lessons_dir / "src" / f"{lesson_id}.story.md"
    if not path.is_file():
        return "(no story source)", ""
    premise, grammar = "(no `> Story:` note)", ""
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.startswith(">"):
            if line.strip():
                break
            continue
        note = line.lstrip(">").strip()
        if note.startswith("Story:"):
            premise = note.removeprefix("Story:").strip()
        elif "Grammar:" in note:
            grammar = note
    return premise, grammar


def _vocab(lessons_dir: Path, lesson_id: str) -> list[dict[str, str]]:
    path = lessons_dir / f"{lesson_id}_vocab.json"
    if not path.is_file():
        return []
    return [row for row in json.loads(path.read_text(encoding="utf-8")).get("words", []) if row.get("hanzi")]


def _vocab_table(rows: list[dict[str, str]]) -> list[str]:
    if not rows:
        return ["(no vocab file)"]
    header = "| " + " | ".join(["hanzi | pinyin | vi"] * VOCAB_PER_ROW) + " |"
    lines = [header, "|" + "---|" * (3 * VOCAB_PER_ROW)]
    for start in range(0, len(rows), VOCAB_PER_ROW):
        chunk = rows[start:start + VOCAB_PER_ROW]
        cells = [f"{r['hanzi']} | {r.get('pinyin', '')} | {r.get('meaning_vi', '')}" for r in chunk]
        cells += [" | "] * (VOCAB_PER_ROW - len(chunk))
        lines.append("| " + " | ".join(cells) + " |")
    return lines


def _lesson_block(info: dict[str, Any], level: int) -> list[str]:
    """level 0 = full, 1 = compact (premise + hanzi list), 2 = one line."""
    lesson, vocab = info["lesson"], info["vocab"]
    head = f"#### {lesson['lesson_id']} — {lesson.get('title', '')}"
    if level == 2:
        return [f"- {lesson['lesson_id']} — {lesson.get('title', '')}: {info['premise']}"]
    if level == 1:
        return [head, f"Premise: {info['premise']}", "Vocab: " + "、".join(r["hanzi"] for r in vocab), ""]
    lines = [head, f"Premise: {info['premise']}"]
    if info["grammar"]:
        lines.append(f"Grammar note: {info['grammar']}")
    lines.append("Goals: " + " | ".join(lesson.get("communicative_goals", [])))
    lines.append("Patterns: " + " | ".join(lesson.get("new_patterns", [])))
    lines.append(f"New characters ({len(info['new_chars'])}): " + "".join(info["new_chars"]))
    lines += ["Vocab:", ""] + _vocab_table(vocab) + [""]
    return lines


def _cumulative(infos: list[dict[str, Any]]) -> list[str]:
    words: list[str] = []
    for info in infos:
        words += [row["hanzi"] for row in info["vocab"] if row["hanzi"] not in words]
    chars = [char for info in infos for char in info["new_chars"]]
    recycle = [w for w in words if sum(w in info["zh"] for info in infos) == 1]
    return [
        "### Cumulative",
        f"Known words ({len(words)}): " + "、".join(words),
        f"Known characters ({len(chars)}): " + "".join(chars),
        f"Recycle candidates ({len(recycle)}, heard in only one earlier lesson — reuse them): " + "、".join(recycle),
        "",
    ]


def _bible(lessons_dir: Path) -> list[str]:
    path = lessons_dir / "src" / "bible.md"
    if not path.is_file():
        return []
    return ["### Story bible (lessons/src/bible.md)", ""] + path.read_text(encoding="utf-8").rstrip().splitlines()


def build_ledger(lessons_dir: Path, before_lesson_id: str) -> str:
    lessons = _lessons_before(lessons_dir, before_lesson_id)
    if not lessons:
        lines = ["(none — this is the first lesson)", ""] + _bible(lessons_dir)
        return "\n".join(lines).rstrip() + "\n"
    infos, seen = [], set()
    for lesson in lessons:
        premise, grammar = _story_notes(lessons_dir, lesson["lesson_id"])
        zh = _zh_text(lesson)
        new_chars = [c for c in _chars(zh) if c not in seen]
        seen.update(new_chars)
        infos.append({"lesson": lesson, "vocab": _vocab(lessons_dir, lesson["lesson_id"]), "premise": premise,
                      "grammar": grammar, "zh": zh, "new_chars": new_chars})
    tail = _cumulative(infos) + _bible(lessons_dir)
    older, recent = infos[:-FULL_LESSONS], infos[-FULL_LESSONS:]
    for older_level in (1, 2):
        body = ["### Earlier lessons"]
        body += [line for info in older for line in _lesson_block(info, older_level)]
        if older_level == 2 and older:
            body.append("")
        body += [line for info in recent for line in _lesson_block(info, 0)]
        lines = body + tail
        if len(lines) <= MAX_LINES:
            break
    return "\n".join(lines).rstrip() + "\n"


def command_ledger(short_id: str, lessons_dir: Path) -> int:
    text = build_ledger(lessons_dir, normalize_lesson_id(short_id))
    LEDGER_DIR.mkdir(parents=True, exist_ok=True)
    path = LEDGER_DIR / f"ledger_{short_id.removeprefix('lesson_')}.md"
    path.write_text(text, encoding="utf-8")
    print(f"Wrote {path} ({len(text.splitlines())} lines)")
    return 0
