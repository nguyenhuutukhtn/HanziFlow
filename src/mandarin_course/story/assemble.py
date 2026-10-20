"""Assemble guided + practice lesson JSON from a parsed story source."""

from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from pathlib import Path

from pydantic import ValidationError

from ..lesson_schema import Lesson
from .blocks import FLAGS, LINE_BLOCKS, QUESTION_BLOCKS, Builder, Line, Question, line_row, question_row
from .parser import VARIANTS, Block, StoryError, StorySource, read_story

MAX_ERRORS = 20
REQUIRED_META = ("lesson_id", "title", "level", "version")
SELECTION = re.compile(r"^([a-z0-9_]+)(?:\[(-?\d*):(-?\d*)\])?$")
# Share of the brisk estimate a learner actually spends listening (pauses overlap answering).
LISTENING_SHARE = 0.84


@dataclass
class VariantResult:
    name: str
    path: Path
    lesson: dict
    brisk_seconds: float
    careful_seconds: float
    builder: Builder


@dataclass
class BuildResult:
    variants: list[VariantResult] = field(default_factory=list)
    errors: list[StoryError] = field(default_factory=list)


def estimate_seconds(segments: list[dict]) -> tuple[float, float]:
    def seconds(segment: dict, hanzi_rate: float, vi_rate: float) -> float:
        if segment["language"] == "zh-CN":
            hanzi = sum(1 for char in segment["text"] if "一" <= char <= "鿿")
            spoken = hanzi * hanzi_rate + 0.35
        else:
            spoken = len(segment["text"]) * vi_rate + 0.4
        return spoken / segment.get("playback_speed", 1.0) + segment.get("pause_after_ms", 0) / 1000

    brisk = sum(seconds(segment, 0.28, 0.065) for segment in segments)
    careful = sum(seconds(segment, 0.40, 0.085) for segment in segments)
    return brisk, careful


class _Sets:
    def __init__(self, source: StorySource, errors: list[StoryError]):
        self.lines: dict[str, list[Line]] = {}
        self.questions: dict[str, list[Question]] = {}
        self.tags: list[tuple[int, str, list[str]]] = []
        for block in source.data:
            if block.kind == "tags":
                for row in block.rows:
                    if len(row.cells) != 2 or not all(row.cells):
                        errors.append(StoryError(row.line, "expected segment_id | tag[, tag]"))
                    else:
                        self.tags.append((row.line, row.cells[0], [t.strip() for t in row.cells[1].split(",")]))
                continue
            target, parse = (self.lines, line_row) if block.kind == "lines" else (self.questions, question_row)
            if block.name in target:
                errors.append(StoryError(block.line, f"set '{block.name}' is defined twice"))
                continue
            target[block.name] = []
            for row in block.rows:
                try:
                    target[block.name].append(parse(row))
                except StoryError as exc:
                    errors.append(exc)

    def select(self, block: Block, sets: dict, kind: str) -> list:
        out = []
        for part in block.args["from"].split("+"):
            match = SELECTION.match(part)
            if not match or match.group(1) not in sets:
                raise StoryError(block.line, f"unknown {kind} set '{part}' (known: {', '.join(sets) or 'none'})")
            name, start, stop = match.groups()
            out += sets[name][int(start) if start else None:int(stop) if stop else None]
        return out


def _rows(block: Block, sets: _Sets) -> list:
    handler_rows = LINE_BLOCKS.get(block.kind)
    parse = handler_rows[1] if handler_rows else question_row
    if "from" in block.args:
        if block.rows:
            raise StoryError(block.line, "use either from= or inline rows, not both")
        if parse not in (line_row, question_row):
            raise StoryError(block.line, f"'{block.kind}' takes inline rows only")
        rows = sets.select(block, sets.lines if parse is line_row else sets.questions,
                           "lines" if parse is line_row else "questions")
    else:
        rows = [parse(row) for row in block.rows]
    if not rows:
        raise StoryError(block.line, f"'{block.kind}' has no rows")
    return list(reversed(rows)) if "reverse" in block.flags else rows


def _build_variant(blocks: list[Block], sets: _Sets, errors: list[StoryError]) -> Builder:
    b = Builder()
    for block in blocks:
        try:
            if block.kind == "activity":
                b.activity = block.name
                continue
            unknown = block.flags - FLAGS.get(block.kind, set())
            if unknown:
                raise StoryError(block.line, f"unknown flag(s) for '{block.kind}': {', '.join(sorted(unknown))}")
            if not b.activity:
                raise StoryError(block.line, "'## activity <name>' must come before the first block")
            handler = LINE_BLOCKS[block.kind][0] if block.kind in LINE_BLOCKS else QUESTION_BLOCKS[block.kind]
            handler(b, block, _rows(block, sets))
        except StoryError as exc:
            errors.append(exc)
    return b


def _meta(source: StorySource, variant: str, key: str, default=None):
    own = source.variants[variant].meta
    return (own.get(key) or source.meta.get(key) or (0, default))[1]


def _lesson(source: StorySource, variant: str, segments: list[dict], minutes: int) -> dict:
    return {
        "lesson_id": _meta(source, variant, "lesson_id"),
        "title": _meta(source, variant, "title"),
        "level": _meta(source, variant, "level"),
        "target_duration_minutes": minutes,
        "communicative_goals": _meta(source, variant, "goals", []),
        "prerequisites": _meta(source, variant, "prerequisites", []),
        "new_patterns": _meta(source, variant, "patterns", []),
        "review_pattern_ids": _meta(source, variant, "review_patterns", []),
        "coach_phrases_introduced": _meta(source, variant, "coach_phrases", []),
        "coach_phrases_reduced": _meta(source, variant, "coach_phrases_reduced", []),
        "version": int(_meta(source, variant, "version")),
        "variant": variant,
        "segments": segments,
    }


def _validate(lesson: dict, b: Builder, fallback_line: int, errors: list[StoryError]) -> None:
    seen: dict[str, int] = {}
    for segment, line in zip(b.segments, b.lines):
        if segment["id"] in seen:
            errors.append(StoryError(line, f"duplicate segment id '{segment['id']}' (first at line {seen[segment['id']]})"))
        seen.setdefault(segment["id"], line)
    try:
        Lesson.model_validate(lesson)
    except ValidationError as exc:
        for error in exc.errors():
            loc = error["loc"]
            line = b.lines[loc[1]] if len(loc) > 1 and loc[0] == "segments" and isinstance(loc[1], int) else fallback_line
            field_name = ".".join(str(part) for part in loc)
            errors.append(StoryError(line, f"{field_name}: {error['msg']}"))


def build_story(path: str | Path, out_dir: str | Path | None = None) -> BuildResult:
    """Parse, build and validate; nothing is written."""
    path = Path(path)
    source, errors = read_story(path)
    result = BuildResult(errors=errors)
    for key in REQUIRED_META:
        if key not in source.meta:
            errors.append(StoryError(1, f"missing lesson metadata '{key}:'"))
    if "version" in source.meta and not str(source.meta["version"][1]).isdigit():
        errors.append(StoryError(source.meta["version"][0], "version must be a whole number"))
    if "guided" not in source.variants:
        errors.append(StoryError(1, "missing '# guided' section"))
    if errors:
        return result
    sets = _Sets(source, errors)
    out_dir = Path(out_dir) if out_dir else (path.parent.parent if path.parent.name == "src" else path.parent)
    for name in VARIANTS:
        if name not in source.variants:
            continue
        b = _build_variant(source.variants[name].blocks, sets, errors)
        brisk, careful = estimate_seconds(b.segments)
        minutes_meta = source.variants[name].meta.get("minutes")
        minutes = int(minutes_meta[1]) if minutes_meta else round(brisk * LISTENING_SHARE / 60)
        lesson = _lesson(source, name, b.segments, minutes)
        stem = lesson["lesson_id"] if name == "guided" else f"{lesson['lesson_id']}_{name}"
        result.variants.append(VariantResult(name, out_dir / f"{stem}.json", lesson, brisk, careful, b))
    by_id = {}
    for variant in result.variants:
        for segment in variant.lesson["segments"]:
            by_id.setdefault(segment["id"], []).append(segment)
    for line, sid, tags in sets.tags:
        if sid not in by_id:
            errors.append(StoryError(line, f"tags for unknown segment id '{sid}'"))
        for segment in by_id.get(sid, []):
            segment["tts_tags"] = tags
    if errors:  # schema errors would only echo the block errors above
        return result
    for variant in result.variants:
        _validate(variant.lesson, variant.builder, source.variants[variant.name].line, errors)
    return result


def write_story(result: BuildResult) -> None:
    for variant in result.variants:
        variant.path.write_text(json.dumps(variant.lesson, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

