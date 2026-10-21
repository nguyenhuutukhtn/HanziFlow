"""Export/import the small authored Vietnamese character-data loop."""

from __future__ import annotations

import json
import re
from pathlib import Path

from ..lesson_schema import load_lesson
from ..logs import write_log
from ..story.ledger import chars_taught_before
from .data import CharacterLoaders
from .resolve import resolve_lesson_characters

BUNDLES_DIR = Path("build/bundles")
AUTHORED_PATH = Path("data/characters_vi.json")
_JSON_BLOCK = re.compile(r"```json\s*\n(.*?)```", re.DOTALL | re.IGNORECASE)
_HAN_VIET = re.compile(r"^[a-zà-ỹđ]+(?:[ /][a-zà-ỹđ]+)*$")


def _lesson(short_id: str, lessons_dir: Path):
    from ..publish_tokens import load_vocab

    lesson_id = f"lesson_{short_id}"
    lesson_path = lessons_dir / f"{lesson_id}.json"
    lesson = load_lesson(lesson_path)
    vocab = load_vocab(lessons_dir / f"{lesson_id}_vocab.json", lesson_id)
    return lesson, vocab


def _missing_rows(short_id: str, lessons_dir: Path) -> list[dict]:
    lesson, vocab = _lesson(short_id, lessons_dir)
    data = json.loads(AUTHORED_PATH.read_text(encoding="utf-8")) if AUTHORED_PATH.is_file() else {}
    resolved = resolve_lesson_characters(lesson.model_dump(mode="json"), chars_taught_before(lessons_dir, lesson.lesson_id), CharacterLoaders().as_dict(), vocab.words)
    unihan = CharacterLoaders().unihan.load()
    rows = []
    for character in resolved["characters"]:
        if character["char"] in data:
            continue
        rows.append({
            "char": character["char"],
            "pinyin": character["pinyin"],
            "en_meaning": character["en_meaning"],
            "kVietnamese_hint": unihan.get(character["char"], {}).get("han_viet"),
            "lesson_words": character["lesson_words"],
        })
    return rows


def export_characters(short_id: str, lessons_dir: Path = Path("lessons")) -> Path:
    rows = _missing_rows(short_id, lessons_dir)
    BUNDLES_DIR.mkdir(parents=True, exist_ok=True)
    path = BUNDLES_DIR / f"characters_{short_id}.md"
    header = (
        f"# Character data: lesson_{short_id}\n\n"
        "Fill one JSON object per character in the reply. `han_viet` is the Sino-Vietnamese reading, "
        "not a Nôm reading; use one or two common readings separated by `/`. "
        "`vi_meaning` is the Vietnamese sense used by this lesson's words, at most 6 words.\n\n"
        "Reply with exactly one fenced `json` block containing an array of objects with `char`, "
        "`han_viet`, and `vi_meaning`.\n\n"
        "## Characters to author\n\n"
        "| char | pinyin | English | kVietnamese hint | lesson_words |\n"
        "|---|---|---|---|---|\n"
    )
    table = "".join(
        f"| {row['char']} | {row['pinyin']} | {row['en_meaning']} | {row['kVietnamese_hint'] or ''} | {', '.join(row['lesson_words'])} |\n"
        for row in rows
    )
    path.write_text(header + table, encoding="utf-8")
    return path


def _reply_rows(path: Path) -> list[dict]:
    match = _JSON_BLOCK.search(path.read_text(encoding="utf-8"))
    if not match:
        raise ValueError("reply must contain exactly one fenced json block")
    value = json.loads(match.group(1))
    if not isinstance(value, list) or any(not isinstance(row, dict) for row in value):
        raise ValueError("fenced json block must contain an array of objects")
    return value


def import_characters(short_id: str, reply_path: Path, lessons_dir: Path = Path("lessons")) -> str:
    lesson, _ = _lesson(short_id, lessons_dir)
    requested = {character["char"] for character in _missing_rows(short_id, lessons_dir)}
    rows = _reply_rows(reply_path)
    errors = []
    supplied = {row.get("char") for row in rows}
    missing = sorted(requested - supplied)
    if missing:
        errors.append(f"missing characters: {', '.join(missing)}")
    for row in rows:
        character = row.get("char", "?")
        if character not in requested:
            errors.append(f"unexpected character: {character}")
        if not isinstance(row.get("han_viet"), str) or not _HAN_VIET.fullmatch(row.get("han_viet", "")):
            errors.append(f"{character}: han_viet must be lowercase Vietnamese letters, spaces, or /")
        if not isinstance(row.get("vi_meaning"), str) or not row.get("vi_meaning", "").strip():
            errors.append(f"{character}: vi_meaning is required")
        elif len(row["vi_meaning"].split()) > 6:
            errors.append(f"{character}: vi_meaning must be at most 6 words")
    if errors:
        raise ValueError("\n".join(errors[:20]))
    existing = json.loads(AUTHORED_PATH.read_text(encoding="utf-8")) if AUTHORED_PATH.is_file() else {}
    existing.update({row["char"]: {"han_viet": row["han_viet"], "vi_meaning": row["vi_meaning"]} for row in rows})
    AUTHORED_PATH.parent.mkdir(parents=True, exist_ok=True)
    AUTHORED_PATH.write_text(json.dumps(dict(sorted(existing.items())), ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    resolved = resolve_lesson_characters(lesson.model_dump(mode="json"), chars_taught_before(lessons_dir, lesson.lesson_id), CharacterLoaders().as_dict(), _lesson(short_id, lessons_dir)[1].words)
    review = [
        "# Character review: " + lesson.lesson_id,
        "",
        "| char | pinyin | hán-việt | nghĩa | lesson_words |",
        "|---|---|---|---|---|",
    ]
    authored = json.loads(AUTHORED_PATH.read_text(encoding="utf-8"))
    for character in resolved["characters"]:
        item = authored[character["char"]]
        review.append(f"| {character['char']} | {character['pinyin']} | {item['han_viet']} | {item['vi_meaning']} | {', '.join(character['lesson_words'])} |")
    (BUNDLES_DIR / f"characters_{short_id}.review.md").write_text("\n".join(review) + "\n", encoding="utf-8")
    return f"Imported {len(rows)} character(s) for {lesson.lesson_id}; total authored: {len(existing)}"


def command_export_characters(short_id: str, lessons_dir: Path) -> int:
    path = export_characters(short_id, lessons_dir)
    print(f"Wrote {path} ({len(path.read_text(encoding='utf-8').splitlines())} lines)")
    return 0


def command_import_characters(short_id: str, reply_path: Path, lessons_dir: Path) -> int:
    try:
        print(import_characters(short_id, reply_path, lessons_dir))
    except (ValueError, OSError, json.JSONDecodeError) as exc:
        log_path = write_log(f"import-characters-{short_id}", str(exc))
        print(f"Character import failed: {exc}\nFull list: {log_path}")
        return 1
    return 0
