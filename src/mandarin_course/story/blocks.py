"""Block types: each turns one `## <type>` section of a story source into segments.

Row shapes (cells separated by `|`, `[...]` optional):
    text row      key | 中文 | pinyin | Vietnamese [| pause_ms]
    line row      key | speaker | 中文 | pinyin | Vietnamese [| pause_ms]
    question row  key | 问题 | pinyin | Vietnamese cue | answer [| pause_ms] [| praise]
                  answer is 是 / 不是 / 有 / 没有, or three cells: 中文 | pinyin | Vietnamese
Defaults (pauses, speeds, speakers) live here, not in the source.
"""

from __future__ import annotations

from dataclasses import dataclass

from .parser import Block, Row, StoryError

SPEAKERS = {"coach_zh", "anna", "li_ming"}
SAY_PAUSE = 400
LINE_PAUSE = 1000
QUESTION_PAUSE = 5000
TEACH_ANSWER_PAUSE = 500
CHUNK_PAUSE = 1800
SLOW_PAUSE = 3000
NATURAL_PAUSE = 4200
SHADOW_SLOW_PAUSE = 600
SLOW_SPEED = 0.85
SHORT_ANSWERS = {
    "是": ("是。", "shì.", "Phải."),
    "不是": ("不是。", "bú shì.", "Không phải."),
    "有": ("有。", "yǒu.", "Có."),
    "没有": ("没有。", "méi yǒu.", "Không có."),
}
PRAISE = {"Đúng rồi.": ("对。", "duì."), "Tốt.": ("好。", "hǎo.")}


@dataclass
class Line:
    line: int
    key: str
    speaker: str
    text: str
    pinyin: str
    gloss: str
    pause: int | None = None


@dataclass
class Question:
    line: int
    key: str
    text: str
    pinyin: str
    cue: str
    answer: tuple[str, str, str]
    pause: int
    praise: str | None


def _pause(row: Row, cell: str) -> int:
    if not cell.isdigit():
        raise StoryError(row.line, f"pause must be milliseconds, got '{cell}'")
    return int(cell)


def _check_cells(row: Row, shape: str, minimum: int, maximum: int) -> None:
    if not minimum <= len(row.cells) <= maximum or not all(row.cells[:minimum]):
        raise StoryError(row.line, f"expected {shape}, got {len(row.cells)} cells")


def text_row(row: Row, speaker: str = "coach_zh") -> Line:
    _check_cells(row, "key | 中文 | pinyin | Vietnamese [| pause_ms]", 4, 5)
    pause = _pause(row, row.cells[4]) if len(row.cells) == 5 else None
    return Line(row.line, row.cells[0], speaker, *row.cells[1:4], pause)


def line_row(row: Row) -> Line:
    _check_cells(row, "key | speaker | 中文 | pinyin | Vietnamese [| pause_ms]", 5, 6)
    if row.cells[1] not in SPEAKERS:
        raise StoryError(row.line, f"unknown speaker '{row.cells[1]}' (expected one of {', '.join(sorted(SPEAKERS))})")
    pause = _pause(row, row.cells[5]) if len(row.cells) == 6 else None
    return Line(row.line, *row.cells[:5], pause)


def question_row(row: Row) -> Question:
    shape = "key | 问题 | pinyin | cue | answer [| pause_ms] [| praise]"
    _check_cells(row, shape, 5, 9)
    key, text, pinyin, cue = row.cells[:4]
    if row.cells[4] in SHORT_ANSWERS:
        answer, rest = SHORT_ANSWERS[row.cells[4]], row.cells[5:]
    else:
        _check_cells(row, shape.replace("answer", "答 | pinyin | Vietnamese"), 7, 9)
        answer, rest = tuple(row.cells[4:7]), row.cells[7:]
    if len(rest) > 2:
        raise StoryError(row.line, f"expected {shape}, got {len(row.cells)} cells")
    pause = _pause(row, rest[0]) if rest and rest[0] else QUESTION_PAUSE
    praise = rest[1] if len(rest) > 1 and rest[1] else None
    if praise and praise not in PRAISE:
        raise StoryError(row.line, f"unknown praise '{praise}' (expected one of {', '.join(PRAISE)})")
    return Question(row.line, key, text, pinyin, cue, answer, pause, praise)


class Builder:
    """Collects segments in order, with the source line each came from."""

    def __init__(self) -> None:
        self.segments: list[dict] = []
        self.lines: list[int] = []
        self.activity = ""

    def seg(self, line: int, sid: str, kind: str, language: str, text: str, *, speaker: str,
            pinyin: str | None = None, translation: str | None = None, pause: int = 0, speed: float = 1.0) -> None:
        item = {
            "id": sid,
            "activity": self.activity,
            "type": kind,
            "language": language,
            "speaker": speaker,
            "text": text,
            "pause_after_ms": pause,
            "playback_speed": speed,
        }
        if language == "zh-CN":
            item["pinyin"] = pinyin
            item["translation"] = translation
        self.segments.append(item)
        self.lines.append(line)

    def zh(self, line: Line, sid: str, pause: int, speed: float = 1.0, kind: str | None = None) -> None:
        kind = kind or ("narration" if line.speaker == "coach_zh" else "dialogue")
        self.seg(line.line, sid, kind, "zh-CN", line.text, speaker=line.speaker, pinyin=line.pinyin,
                 translation=line.gloss, pause=pause, speed=speed)

    def vi(self, line_no: int, sid: str, text: str, pause: int) -> None:
        self.seg(line_no, sid, "narration", "vi-VN", text, speaker="coach", pause=pause)

    def pair(self, line: Line, sid: str, pause: int) -> None:
        """Coach line in Mandarin, then the same line in Vietnamese."""
        self.zh(line, sid, 400)
        self.vi(line.line, f"{sid}_vi", line.gloss, pause)

    def gloss(self, line: Line, sid: str) -> None:
        """Line, its meaning in Vietnamese, the line again."""
        self.zh(line, sid, 500)
        self.vi(line.line, f"{sid}_vi", line.gloss, 400)
        self.zh(line, f"{sid}_again", 800)


def _id(prefix: str, key: str) -> str:
    return f"{prefix}_{key}" if prefix else key


def _require(block: Block, *names: str) -> None:
    for name in names:
        if not block.args.get(name):
            raise StoryError(block.line, f"'{block.kind}' needs {name}=")


def block_say(b: Builder, block: Block, lines: list[Line]) -> None:
    for line in lines:
        b.pair(line, line.key, SAY_PAUSE if line.pause is None else line.pause)


def block_zh(b: Builder, block: Block, lines: list[Line]) -> None:
    for line in lines:
        b.zh(line, line.key, SAY_PAUSE if line.pause is None else line.pause)


def block_story(b: Builder, block: Block, lines: list[Line]) -> None:
    _require(block, "prefix")
    for line in lines:
        b.zh(line, _id(block.args["prefix"], line.key), LINE_PAUSE if line.pause is None else line.pause)


def block_gloss(b: Builder, block: Block, lines: list[Line]) -> None:
    for line in lines:
        b.gloss(line, _id(block.args.get("prefix", ""), line.key))


def block_shadow(b: Builder, block: Block, lines: list[Line]) -> None:
    _require(block, "prefix")
    for line in lines:
        sid = _id(block.args["prefix"], line.key)
        b.zh(line, f"{sid}_slow", SHADOW_SLOW_PAUSE, SLOW_SPEED)
        b.zh(line, f"{sid}_nat", NATURAL_PAUSE)


def block_teach(b: Builder, block: Block, lines: list[Line]) -> None:
    _require(block, "prefix")
    if [line.key for line in lines] != ["rule", "ex", "ans"]:
        raise StoryError(block.line, "'teach' needs exactly three rows: rule, ex, ans")
    rule, example, answer = lines
    prefix = block.args["prefix"]
    b.gloss(rule, prefix)
    b.gloss(example, f"{prefix}_ex")
    b.pair(answer, f"{prefix}_ans", TEACH_ANSWER_PAUSE if answer.pause is None else answer.pause)


def block_buildup(b: Builder, block: Block, lines: list[Line]) -> None:
    """Backward buildup: each chunk contains the previous one; the full line contains the last chunk."""
    _require(block, "prefix")
    speaker = block.args.get("speaker", "coach_zh")
    if speaker not in SPEAKERS:
        raise StoryError(block.line, f"unknown speaker '{speaker}'")
    if not lines or lines[0].key != "full":
        raise StoryError(block.line, "'buildup' starts with a 'full' row, then the chunk rows")
    full, chunks = lines[0], lines[1:]
    for line in lines:
        line.speaker = speaker
    prefix = block.args["prefix"]
    b.gloss(full, f"{prefix}_gloss")
    previous = ""
    for chunk in chunks:
        if previous and previous not in chunk.text:
            raise StoryError(chunk.line, f"buildup step does not contain the previous chunk: {previous} -> {chunk.text}")
        previous = chunk.text
        b.zh(chunk, f"{prefix}_{chunk.key}", CHUNK_PAUSE)
    if previous not in full.text:
        raise StoryError(block.line, f"full line does not contain the last chunk: {previous} -> {full.text}")
    b.zh(full, f"{prefix}_slow", SLOW_PAUSE, SLOW_SPEED)
    b.zh(full, f"{prefix}_nat", NATURAL_PAUSE)


def block_ask(b: Builder, block: Block, questions: list[Question]) -> None:
    """Circling questions. `vi`: Vietnamese cue and answer meaning; `praise`: allow the row's praise (only with `vi`)."""
    _require(block, "prefix")
    with_vi = "vi" in block.flags
    for q in questions:
        sid = _id(block.args["prefix"], q.key)
        b.seg(q.line, sid, "prompt", "zh-CN", q.text, speaker="coach_zh", pinyin=q.pinyin,
              translation=q.cue, pause=0 if with_vi else q.pause)
        if with_vi:
            b.vi(q.line, f"{sid}_vi", q.cue, q.pause)
        answer, answer_py, answer_gloss = q.answer
        b.seg(q.line, f"{sid}_a", "model_answer", "zh-CN", answer, speaker="coach_zh", pinyin=answer_py,
              translation=answer_gloss, pause=400 if with_vi else 700)
        if with_vi:
            b.vi(q.line, f"{sid}_av", answer_gloss, 500)
        if with_vi and "praise" in block.flags and q.praise:
            zh_praise, zh_py = PRAISE[q.praise]
            b.pair(Line(q.line, "", "coach_zh", zh_praise, zh_py, q.praise), f"{sid}_ok", 400)


# block type -> (handler, row parser for inline rows / `from=` sets)
LINE_BLOCKS = {
    "say": (block_say, text_row),
    "zh": (block_zh, text_row),
    "teach": (block_teach, text_row),
    "buildup": (block_buildup, text_row),
    "story": (block_story, line_row),
    "gloss": (block_gloss, line_row),
    "shadow": (block_shadow, line_row),
}
QUESTION_BLOCKS = {"ask": block_ask}
FLAGS = {"ask": {"vi", "praise", "reverse"}, "story": {"reverse"}, "gloss": {"reverse"}, "shadow": {"reverse"}}
