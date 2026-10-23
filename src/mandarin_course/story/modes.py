"""Import the four lesson-2 listening/speaking modes."""

from __future__ import annotations

import json
import re
from dataclasses import dataclass
from pathlib import Path

from .blocks import LINE_PAUSE
from .metrics import VI_SECONDS_PER_CHAR, ZH_SECONDS_PER_CHAR, _hanzi

MODES = {"Hiểu": "hieu", "Nghe": "nghe", "Phản xạ": "phan_xa", "Nói": "noi"}
SPEAKERS = {"narrator", "wang_fang", "chen_dong", "coach_vi", "gap"}
WH_WORDS = "谁哪什么几还是怎么"


class ModesError(ValueError):
    pass


@dataclass
class Row:
    line: int
    ref: str
    speaker: str
    text: str
    pinyin: str
    translation: str


def _error(line: int, reason: str) -> ModesError:
    return ModesError(f"09_modes.md:{line}: {reason}")


def parse_modes(path: Path) -> dict[str, list[Row]]:
    sections: dict[str, list[Row]] = {mode: [] for mode in MODES.values()}
    current: str | None = None
    in_block = False
    for number, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        heading = re.fullmatch(r"##\s+(.+)", raw.strip())
        if heading:
            current = MODES.get(heading.group(1).strip())
            in_block = False
            continue
        if raw.strip().startswith("```"):
            in_block = not in_block
            continue
        if not (current and in_block) or not raw.strip():
            continue
        cells = [cell.strip() for cell in raw.split("|")]
        if len(cells) != 5:
            raise _error(number, f"expected 5 cells, got {len(cells)}")
        ref, speaker, text, pinyin, translation = cells
        if speaker not in SPEAKERS:
            raise _error(number, f"unknown speaker '{speaker}'")
        if speaker == "gap" and not sections[current]:
            raise _error(number, "gap cannot be the first row")
        if speaker == "coach_vi" and current != "hieu":
            raise _error(number, "coach_vi is allowed only in Hiểu")
        sections[current].append(Row(number, ref, speaker, text, pinyin, translation))
    return sections


def _segment(mode: str, row: Row) -> dict:
    segment = {
        "id": f"{mode}_{row.ref.replace('.', '_')}",
        "activity": {v: k for k, v in MODES.items()}[mode],
        "type": "narration" if row.speaker in {"narrator", "coach_vi"} else "dialogue",
        "language": "vi-VN" if row.speaker == "coach_vi" else "zh-CN",
        "speaker": "coach" if row.speaker == "coach_vi" else ("coach_zh" if row.speaker == "narrator" else row.speaker),
        "text": row.translation if row.speaker == "coach_vi" else row.text,
        "pause_after_ms": LINE_PAUSE,
    }
    if row.speaker != "coach_vi":
        segment.update(pinyin=row.pinyin, translation=row.translation)
    return segment


def _answer_pause(question: str, answer: str) -> int:
    value = round(3000 + 1.5 * ZH_SECONDS_PER_CHAR * 1000 * len(_hanzi(answer)))
    if any(char in question for char in WH_WORDS):
        value += 1000
    return max(3000, min(7000, value))


def _repeat_pause(text: str) -> int:
    value = round(1000 + 1.5 * ZH_SECONDS_PER_CHAR * 1000 * len(_hanzi(text)))
    return max(2000, min(8000, value))


def build_mode(mode: str, rows: list[Row]) -> tuple[list[dict], int]:
    segments: list[dict] = []
    gaps = 0
    for index, row in enumerate(rows):
        if row.speaker == "gap":
            if not segments:
                raise _error(row.line, "gap cannot be the first row")
            gaps += 1
            if mode == "phan_xa":
                next_row = rows[index + 1] if index + 1 < len(rows) else None
                if next_row is None or next_row.speaker == "gap":
                    raise _error(row.line, "answer gap must be followed by an answer")
                segments[-1]["pause_after_ms"] = _answer_pause(segments[-1]["text"], next_row.text)
            elif mode == "noi":
                segments[-1]["pause_after_ms"] = _repeat_pause(segments[-1]["text"])
            continue
        segments.append(_segment(mode, row))
    return segments, gaps


def _duration_minutes(segments: list[dict]) -> int:
    seconds = sum(
        (len(_hanzi(segment["text"]) if segment["language"] == "zh-CN" else segment["text"]) *
         (ZH_SECONDS_PER_CHAR if segment["language"] == "zh-CN" else VI_SECONDS_PER_CHAR))
        + segment.get("pause_after_ms", 0) / 1000
        for segment in segments
    )
    return max(5, min(60, round(seconds / 60)))


def import_modes(lesson_id: str, content_dir: Path = Path("content"), lessons_dir: Path = Path("lessons")) -> list[tuple[str, int, int, int]]:
    source = content_dir / f"lesson_{lesson_id}" / "09_modes.md"
    meta = json.loads((content_dir / f"lesson_{lesson_id}" / "meta.json").read_text(encoding="utf-8"))
    parsed = parse_modes(source)
    results = []
    for mode in ("hieu", "nghe", "phan_xa", "noi"):
        segments, gaps = build_mode(mode, parsed[mode])
        lesson = {**meta, "format": "modes_v1", "variant": mode, "version": 1,
                  "target_duration_minutes": _duration_minutes(segments), "segments": segments}
        output = lessons_dir / f"lesson_{lesson_id}_{mode}.json"
        output.write_text(json.dumps(lesson, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        results.append((mode, len(segments), gaps, lesson["target_duration_minutes"]))
    return results
