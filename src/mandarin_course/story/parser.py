"""Parse a lesson story source (`lessons/src/lesson_XXX.story.md`) into blocks.

Format (see lessons/src/lesson_001.story.md for a full example):

    key: value                  lesson metadata, before the first `#` section
    # guided | # practice       variant section; `key: value` lines right after it override metadata
    # data                      named row sets used by the variant blocks
    ## <block type> [name] [key=value ...] [flag ...]
    row | row | row             pipe rows, one per line, until the next heading
    > note                      ignored

List values in metadata are separated by `|`. The parser only checks structure;
row shapes are checked by the block types in `blocks.py`.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path

VARIANTS = ("guided", "practice")
SECTIONS = (*VARIANTS, "data")
VARIANT_BLOCKS = {"activity", "say", "zh", "story", "gloss", "teach", "ask", "buildup", "shadow"}
DATA_BLOCKS = {"lines", "questions", "tags"}
LIST_KEYS = {"goals", "prerequisites", "patterns", "review_patterns", "coach_phrases", "coach_phrases_reduced"}


class StoryError(Exception):
    def __init__(self, line: int, message: str):
        super().__init__(f"line {line}: {message}")
        self.line = line
        self.message = message


@dataclass
class Row:
    line: int
    cells: list[str]


@dataclass
class Block:
    line: int
    kind: str
    name: str = ""
    args: dict[str, str] = field(default_factory=dict)
    flags: set[str] = field(default_factory=set)
    rows: list[Row] = field(default_factory=list)


@dataclass
class Variant:
    line: int
    meta: dict[str, tuple[int, str | list[str]]] = field(default_factory=dict)
    blocks: list[Block] = field(default_factory=list)


@dataclass
class StorySource:
    meta: dict[str, tuple[int, str | list[str]]] = field(default_factory=dict)
    variants: dict[str, Variant] = field(default_factory=dict)
    data: list[Block] = field(default_factory=list)


def _meta_value(key: str, value: str) -> str | list[str]:
    if key in LIST_KEYS:
        return [item.strip() for item in value.split("|") if item.strip()]
    return value.strip()


def _parse_header(line_no: int, text: str, section: str) -> Block:
    kind, _, rest = text.partition(" ")
    allowed = DATA_BLOCKS if section == "data" else VARIANT_BLOCKS
    if kind not in VARIANT_BLOCKS | DATA_BLOCKS:
        raise StoryError(line_no, f"unknown block type '{kind}'")
    if kind not in allowed:
        raise StoryError(line_no, f"block type '{kind}' is not allowed in '# {section}'")
    block = Block(line_no, kind)
    if kind == "activity":
        block.name = rest.strip()
        if not block.name:
            raise StoryError(line_no, "activity needs a name")
        return block
    for token in rest.split():
        if "=" in token:
            key, _, value = token.partition("=")
            block.args[key] = value
        elif kind in {"lines", "questions"} and not block.name:
            block.name = token
        else:
            block.flags.add(token)
    if kind in {"lines", "questions"} and not block.name:
        raise StoryError(line_no, f"'{kind}' needs a set name")
    return block


def parse_story(text: str) -> tuple[StorySource, list[StoryError]]:
    source = StorySource()
    errors: list[StoryError] = []
    section: str | None = None
    block: Block | None = None
    for line_no, raw in enumerate(text.splitlines(), start=1):
        line = raw.strip()
        if not line or line.startswith(">") or line.startswith("<!--"):
            continue
        try:
            if line.startswith("# "):
                name = line[2:].strip()
                block = None
                if name not in SECTIONS:
                    section = "?"  # skip its content; one error is enough
                    raise StoryError(line_no, f"unknown section '# {name}' (expected one of {', '.join(SECTIONS)})")
                if name in source.variants or (name == "data" and section == "data"):
                    raise StoryError(line_no, f"section '# {name}' appears twice")
                section = name
                if section in VARIANTS:
                    source.variants[section] = Variant(line_no)
            elif section == "?":
                continue
            elif line.startswith("## "):
                if section is None:
                    raise StoryError(line_no, "block before any '# guided', '# practice' or '# data' section")
                block = Block(line_no, "?")  # swallows the rows if the header is bad
                block = _parse_header(line_no, line[3:].strip(), section)
                if section == "data":
                    source.data.append(block)
                else:
                    source.variants[section].blocks.append(block)
            elif block is not None:
                block.rows.append(Row(line_no, [cell.strip() for cell in line.split("|")]))
            elif ":" in line and (section is None or section in VARIANTS):
                key, _, value = line.partition(":")
                target = source.meta if section is None else source.variants[section].meta
                target[key.strip()] = (line_no, _meta_value(key.strip(), value))
            else:
                raise StoryError(line_no, "expected 'key: value', a '#' section or a '##' block")
        except StoryError as exc:
            errors.append(exc)
    return source, errors


def read_story(path: str | Path) -> tuple[StorySource, list[StoryError]]:
    return parse_story(Path(path).read_text(encoding="utf-8"))
