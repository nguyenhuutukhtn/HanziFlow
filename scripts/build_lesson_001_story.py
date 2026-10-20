"""Build the Lesson 1 mini-story draft.

The story is the office takeout: Anna thinks it is Li Ming's, Li Ming thinks
it is Anna's, it is a colleague's. Present time only. Grammar that survives
the trim is 是/不是, 还是, and 的 for whose.

Does not replace lessons/lesson_001.json. That file stays the pronunciation
reference. This draft is lessons/lesson_001_story.json.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GUIDED = ROOT / "lessons" / "lesson_001_story.json"
PRACTICE = ROOT / "lessons" / "lesson_001_story_practice.json"

# key, speaker, text, pinyin, gloss, pause_after_ms
# Three events: the takeout is here; each guess is wrong; the name on the bag is the colleague's.
STORY = [
    ("food", "coach_zh", "这是外卖。", "zhè shì wàimài.", "Đây là đồ ăn mang về.", 1000),
    ("who", "coach_zh", "这是谁的？", "zhè shì shuí de?", "Đây là của ai?", 900),
    ("desk_an", "coach_zh", "这不是安娜的桌子。", "zhè bú shì Ānnà de zhuōzi.", "Đây không phải bàn của Anna.", 1000),
    ("desk_lm", "coach_zh", "这不是李明的桌子。", "zhè bú shì Lǐ Míng de zhuōzi.", "Đây không phải bàn của Lý Minh.", 1000),
    ("look", "coach_zh", "安娜看袋子，李明也看袋子。", "Ānnà kàn dàizi, Lǐ Míng yě kàn dàizi.", "Anna nhìn túi. Lý Minh cũng nhìn túi.", 1400),
    ("ask_lm", "anna", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?", 900),
    ("lm_no", "li_ming", "不是。这是安娜的吗？", "bú shì. zhè shì Ānnà de ma?", "Không phải. Đây là của Anna phải không?", 900),
    ("anna_no", "anna", "不是。", "bú shì.", "Không phải.", 1400),
    ("neither", "coach_zh", "不是安娜的。不是李明的。", "bú shì Ānnà de. bú shì Lǐ Míng de.", "Không phải của Anna. Không phải của Lý Minh.", 1100),
    ("name", "coach_zh", "袋子上有同事的名字。", "dàizi shàng yǒu tóngshì de míngzi.", "Trên túi có tên đồng nghiệp.", 1200),
    ("result", "coach_zh", "这是同事的外卖。", "zhè shì tóngshì de wàimài.", "Đây là đồ ăn mang về của đồng nghiệp.", 900),
    ("colleague", "coach_zh", "这是同事的。", "zhè shì tóngshì de.", "Đây là của đồng nghiệp.", 1400),
]

POV = [
    ("pov_food", "coach_zh", "这是外卖。", "zhè shì wàimài.", "Đây là đồ ăn mang về.", 1000),
    ("pov_ask", "anna", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?", 900),
    ("pov_yes", "li_ming", "是。", "shì.", "Phải.", 1200),
    ("pov_name", "coach_zh", "袋子上有李明的名字。", "dàizi shàng yǒu Lǐ Míng de míngzi.", "Trên túi có tên Lý Minh.", 1100),
    ("pov_his", "coach_zh", "这是李明的。", "zhè shì Lǐ Míng de.", "Đây là của Lý Minh.", 1000),
    ("pov_not", "coach_zh", "不是同事的。", "bú shì tóngshì de.", "Không phải của đồng nghiệp.", 1400),
]

WORDS = [
    ("waimai", "外卖。", "wàimài.", "Đồ ăn mang về."),
    ("bushi", "不是。", "bú shì.", "Không phải."),
    ("tongshi", "同事。", "tóngshì.", "Đồng nghiệp."),
    ("shei", "谁的？", "shuí de?", "Của ai?"),
    ("mingzi", "名字。", "míngzi.", "Tên."),
]

MAPPED = [
    ("map_look", "coach_zh", "安娜看袋子，李明也看袋子。", "Ānnà kàn dàizi, Lǐ Míng yě kàn dàizi.", "Anna nhìn túi. Lý Minh cũng nhìn túi."),
    ("map_food", "coach_zh", "这是外卖。", "zhè shì wàimài.", "Đây là đồ ăn mang về."),
    ("map_desk_an", "coach_zh", "这不是安娜的桌子。", "zhè bú shì Ānnà de zhuōzi.", "Đây không phải bàn của Anna."),
    ("map_desk_lm", "coach_zh", "这不是李明的桌子。", "zhè bú shì Lǐ Míng de zhuōzi.", "Đây không phải bàn của Lý Minh."),
    ("map_who", "coach_zh", "这是谁的？", "zhè shì shuí de?", "Đây là của ai?"),
    ("map_ask", "anna", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?"),
    ("map_no", "li_ming", "不是。", "bú shì.", "Không phải."),
    ("map_neither", "coach_zh", "不是安娜的。不是李明的。", "bú shì Ānnà de. bú shì Lǐ Míng de.", "Không phải của Anna. Không phải của Lý Minh."),
    ("map_name", "coach_zh", "袋子上有同事的名字。", "dàizi shàng yǒu tóngshì de míngzi.", "Trên túi có tên đồng nghiệp."),
    ("map_whose", "coach_zh", "这是同事的。", "zhè shì tóngshì de.", "Đây là của đồng nghiệp."),
]

# question, pinyin, vietnamese cue, answer, answer pinyin, answer gloss, pause, praise
CIRCLE = [
    ("q_food", "这是外卖吗？", "zhè shì wàimài ma?", "Đây là đồ ăn mang về phải không?", "是。", "shì.", "Phải.", 4500, None),
    ("q_who", "这是谁的？", "zhè shì shuí de?", "Của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000, None),
    ("q_anna", "这是安娜的吗？", "zhè shì Ānnà de ma?", "Đây là của Anna phải không?", "不是。", "bú shì.", "Không phải.", 4500, None),
    ("q_lm", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?", "不是。", "bú shì.", "Không phải.", 4500, "Đúng rồi."),
    ("q_ts", "这是同事的吗？", "zhè shì tóngshì de ma?", "Đây là của đồng nghiệp phải không?", "是。", "shì.", "Phải.", 4500, None),
    ("q_has_name", "袋子上有名字吗？", "dàizi shàng yǒu míngzi ma?", "Trên túi có tên phải không?", "有。", "yǒu.", "Có.", 4500, None),
    ("q_name", "袋子上有同事的名字吗？", "dàizi shàng yǒu tóngshì de míngzi ma?", "Trên túi có tên đồng nghiệp phải không?", "有。", "yǒu.", "Có.", 4500, None),
    ("q_name_anna", "袋子上有安娜的名字吗？", "dàizi shàng yǒu Ānnà de míngzi ma?", "Trên túi có tên Anna phải không?", "没有。", "méi yǒu.", "Không có.", 4500, None),
    ("q_name_lm", "袋子上有李明的名字吗？", "dàizi shàng yǒu Lǐ Míng de míngzi ma?", "Trên túi có tên Lý Minh phải không?", "没有。", "méi yǒu.", "Không có.", 4500, None),
    ("q_or_lm", "这是李明的还是同事的？", "zhè shì Lǐ Míng de háishi tóngshì de?", "Của Lý Minh hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500, "Tốt."),
    ("q_or_anna", "这是安娜的还是同事的？", "zhè shì Ānnà de háishi tóngshì de?", "Của Anna hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500, None),
    ("q_or_name", "袋子上有李明的名字还是同事的名字？", "dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi?", "Trên túi có tên Lý Minh hay tên đồng nghiệp?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500, None),
    ("q_name_who", "袋子上有谁的名字？", "dàizi shàng yǒu shuí de míngzi?", "Trên túi có tên ai?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5000, None),
]

POV_CIRCLE = [
    ("pq_lm", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?", "是。", "shì.", "Phải.", 4500),
    ("pq_ts", "这是同事的吗？", "zhè shì tóngshì de ma?", "Đây là của đồng nghiệp phải không?", "不是。", "bú shì.", "Không phải.", 4500),
    ("pq_name", "袋子上有李明的名字吗？", "dàizi shàng yǒu Lǐ Míng de míngzi ma?", "Trên túi có tên Lý Minh phải không?", "有。", "yǒu.", "Có.", 4500),
    ("pq_name_no", "袋子上有同事的名字吗？", "dàizi shàng yǒu tóngshì de míngzi ma?", "Trên túi có tên đồng nghiệp phải không?", "没有。", "méi yǒu.", "Không có.", 4500),
    ("pq_who", "这是谁的？", "zhè shì shuí de?", "Của ai?", "李明的。", "Lǐ Míng de.", "Của Lý Minh.", 5000),
    ("pq_or", "这是李明的还是同事的？", "zhè shì Lǐ Míng de háishi tóngshì de?", "Của Lý Minh hay của đồng nghiệp?", "李明的。", "Lǐ Míng de.", "Của Lý Minh.", 5500),
    ("pq_or_name", "袋子上有李明的名字还是同事的名字？", "dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi?", "Trên túi có tên Lý Minh hay tên đồng nghiệp?", "李明的名字。", "Lǐ Míng de míngzi.", "Tên Lý Minh.", 5500),
]

BUILDUP = [
    (
        "anna",
        "b_ask",
        "这是李明的吗？",
        "zhè shì Lǐ Míng de ma?",
        "Đây là của Lý Minh phải không?",
        [
            ("c1", "李明的", "Lǐ Míng de", "Của Lý Minh."),
            ("c2", "是李明的", "shì Lǐ Míng de", "Là của Lý Minh."),
        ],
    ),
    (
        "coach_zh",
        "b_ts",
        "这是同事的。",
        "zhè shì tóngshì de.",
        "Đây là của đồng nghiệp.",
        [
            ("c1", "同事的", "tóngshì de", "Của đồng nghiệp."),
            ("c2", "是同事的", "shì tóngshì de", "Là của đồng nghiệp."),
        ],
    ),
    (
        "coach_zh",
        "b_or",
        "这是安娜的还是同事的？",
        "zhè shì Ānnà de háishi tóngshì de?",
        "Của Anna hay của đồng nghiệp?",
        [
            ("c1", "同事的", "tóngshì de", "Của đồng nghiệp."),
            ("c2", "还是同事的", "háishi tóngshì de", "Hay của đồng nghiệp."),
        ],
    ),
    (
        "coach_zh",
        "b_food",
        "这是同事的外卖吗？",
        "zhè shì tóngshì de wàimài ma?",
        "Đây là đồ ăn mang về của đồng nghiệp phải không?",
        [
            ("c1", "同事的", "tóngshì de", "Của đồng nghiệp."),
            ("c2", "同事的外卖", "tóngshì de wàimài", "Đồ ăn mang về của đồng nghiệp."),
        ],
    ),
    (
        "coach_zh",
        "b_who",
        "这是谁的外卖？",
        "zhè shì shuí de wàimài?",
        "Đây là đồ ăn mang về của ai?",
        [
            ("c1", "谁的", "shuí de", "Của ai."),
            ("c2", "是谁的", "shì shuí de", "Là của ai."),
        ],
    ),
    (
        "coach_zh",
        "b_bag",
        "袋子上有李明的名字还是同事的名字？",
        "dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi?",
        "Trên túi có tên Lý Minh hay tên đồng nghiệp?",
        [
            ("c1", "同事的名字", "tóngshì de míngzi", "Tên đồng nghiệp."),
            ("c2", "李明的名字还是同事的名字", "Lǐ Míng de míngzi háishi tóngshì de míngzi", "Tên Lý Minh hay tên đồng nghiệp."),
        ],
    ),
]


def seg(
    sid: str,
    activity: str,
    kind: str,
    language: str,
    text: str,
    *,
    speaker: str,
    pinyin: str | None = None,
    translation: str | None = None,
    pause_after: int = 0,
    speed: float = 1.0,
) -> dict:
    item = {
        "id": sid,
        "activity": activity,
        "type": kind,
        "language": language,
        "speaker": speaker,
        "text": text,
        "pause_after_ms": pause_after,
        "playback_speed": speed,
    }
    if language == "zh-CN":
        item["pinyin"] = pinyin
        item["translation"] = translation
    return item


def pair(activity: str, sid: str, zh: str, pinyin: str, vi: str, pause: int = 700) -> list[dict]:
    return [
        seg(sid, activity, "narration", "zh-CN", zh, speaker="coach_zh", pinyin=pinyin, translation=vi, pause_after=400),
        seg(f"{sid}_vi", activity, "narration", "vi-VN", vi, speaker="coach", pause_after=pause),
    ]


def story_lines(activity: str, prefix: str, rows: list[tuple], pause: int, extra_pause: int = 0) -> list[dict]:
    out = []
    for row in rows:
        key, speaker, text, pinyin, gloss = row[:5]
        line_pause = (row[5] if len(row) > 5 else pause) + extra_pause
        kind = "narration" if speaker == "coach_zh" else "dialogue"
        out.append(
            seg(
                f"{prefix}_{key}",
                activity,
                kind,
                "zh-CN",
                text,
                speaker=speaker,
                pinyin=pinyin,
                translation=gloss,
                pause_after=line_pause,
            )
        )
    return out


def gloss_line(activity: str, prefix: str, speaker: str, text: str, pinyin: str, gloss: str) -> list[dict]:
    return [
        seg(f"{prefix}", activity, "dialogue" if speaker != "coach_zh" else "narration", "zh-CN", text, speaker=speaker, pinyin=pinyin, translation=gloss, pause_after=500),
        seg(f"{prefix}_vi", activity, "narration", "vi-VN", gloss, speaker="coach", pause_after=400),
        seg(f"{prefix}_again", activity, "dialogue" if speaker != "coach_zh" else "narration", "zh-CN", text, speaker=speaker, pinyin=pinyin, translation=gloss, pause_after=800),
    ]


def teach(
    activity: str,
    prefix: str,
    zh: str,
    pinyin: str,
    vi: str,
    example: str,
    example_py: str,
    example_vi: str,
    answer: str,
    answer_py: str,
    answer_vi: str,
) -> list[dict]:
    out = gloss_line(activity, prefix, "coach_zh", zh, pinyin, vi)
    out += gloss_line(activity, f"{prefix}_ex", "coach_zh", example, example_py, example_vi)
    out += pair(activity, f"{prefix}_ans", answer, answer_py, answer_vi, 500)
    return out


def circle(activity: str, prefix: str, rows: list[tuple], with_vietnamese: bool, allow_praise: bool, speak_answer: bool = False) -> list[dict]:
    out = []
    for key, text, pinyin, cue, answer, answer_py, answer_gloss, pause, *rest in rows:
        praise = rest[0] if rest else None
        out.append(
            seg(
                f"{prefix}_{key}",
                activity,
                "prompt",
                "zh-CN",
                text,
                speaker="coach_zh",
                pinyin=pinyin,
                translation=cue,
                pause_after=0 if with_vietnamese else pause,
            )
        )
        if with_vietnamese:
            out.append(seg(f"{prefix}_{key}_vi", activity, "narration", "vi-VN", cue, speaker="coach", pause_after=pause))
        out.append(
            seg(
                f"{prefix}_{key}_a",
                activity,
                "model_answer",
                "zh-CN",
                answer,
                speaker="coach_zh",
                pinyin=answer_py,
                translation=answer_gloss,
                pause_after=400 if (with_vietnamese and speak_answer) else 700,
            )
        )
        if with_vietnamese and speak_answer:
            out.append(
                seg(
                    f"{prefix}_{key}_av",
                    activity,
                    "narration",
                    "vi-VN",
                    answer_gloss,
                    speaker="coach",
                    pause_after=500,
                )
            )
        if allow_praise and with_vietnamese and praise:
            zh_praise, zh_py = ("对。", "duì.") if praise.startswith("Đúng") else ("好。", "hǎo.")
            out += pair(activity, f"{prefix}_{key}_ok", zh_praise, zh_py, praise, 400)
    return out


def buildup_block() -> list[dict]:
    out = pair(
        "Nói theo",
        "say_lead",
        "你听一遍，再跟我说。",
        "nǐ tīng yí biàn, zài gēn wǒ shuō.",
        "Bạn nghe một lần, rồi nói theo mình.",
        600,
    )
    for speaker, key, text, pinyin, gloss, steps in BUILDUP:
        kind = "dialogue" if speaker != "coach_zh" else "narration"
        previous = ""
        for step, chunk, chunk_py, chunk_gloss in steps:
            if previous and previous not in chunk:
                raise SystemExit(f"buildup step does not contain the previous chunk: {previous} -> {chunk}")
            previous = chunk
            out.append(
                seg(
                    f"{key}_{step}",
                    "Nói theo",
                    kind,
                    "zh-CN",
                    chunk,
                    speaker=speaker,
                    pinyin=chunk_py,
                    translation=chunk_gloss,
                    pause_after=900,
                )
            )
        if previous not in text:
            raise SystemExit(f"full line does not contain the previous chunk: {previous} -> {text}")
        out.append(seg(f"{key}_slow", "Nói theo", kind, "zh-CN", text, speaker=speaker, pinyin=pinyin, translation=gloss, pause_after=700, speed=0.85))
        out.append(seg(f"{key}_nat", "Nói theo", kind, "zh-CN", text, speaker=speaker, pinyin=pinyin, translation=gloss, pause_after=1000))
    return out


def _rows(keys: list[str]) -> list[tuple]:
    by_key = {row[0]: row for row in CIRCLE}
    return [by_key[key] for key in keys]


FIRST_YES = _rows(["q_food", "q_who", "q_anna", "q_lm", "q_ts", "q_has_name", "q_name", "q_name_lm"])
FIRST_OR = _rows(["q_or_lm", "q_or_anna"])
C2_WHO = _rows(["q_food", "q_who", "q_anna", "q_lm", "q_ts"])
C2_NAME = _rows(["q_has_name", "q_name", "q_name_lm", "q_name_anna", "q_name_who"])
C2_OR = _rows(["q_or_lm", "q_or_anna", "q_or_name"])
LATER = _rows(
    [
        "q_food",
        "q_who",
        "q_anna",
        "q_lm",
        "q_ts",
        "q_has_name",
        "q_name",
        "q_name_lm",
        "q_name_anna",
        "q_name_who",
        "q_or_lm",
        "q_or_anna",
        "q_or_name",
    ]
)


def row(key: str, text: str, pinyin: str, cue: str, answer: str, answer_py: str, answer_gloss: str, pause: int = 5000, praise: str | None = None) -> tuple:
    return (key, text, pinyin, cue, answer, answer_py, answer_gloss, pause, praise)


def yes_no(key: str, text: str, pinyin: str, cue: str, yes: bool, pause: int = 5000, praise: str | None = None) -> tuple:
    if yes:
        return row(key, text, pinyin, cue, "是。", "shì.", "Phải.", pause, praise)
    return row(key, text, pinyin, cue, "不是。", "bú shì.", "Không phải.", pause, praise)


def has_name(key: str, text: str, pinyin: str, cue: str, yes: bool, pause: int = 5000) -> tuple:
    if yes:
        return row(key, text, pinyin, cue, "有。", "yǒu.", "Có.", pause)
    return row(key, text, pinyin, cue, "没有。", "méi yǒu.", "Không có.", pause)


OWNERS = (
    ("ts", "同事", "tóngshì", "đồng nghiệp", True),
    ("an", "安娜", "Ānnà", "Anna", False),
    ("lm", "李明", "Lǐ Míng", "Lý Minh", False),
)


def owner_frame(frame: str, template: str, py_template: str, vi_template: str, kind: str = "yes") -> list[tuple]:
    rows = []
    for key, name, py, vi, yes in OWNERS:
        text = template.format(name=name)
        pinyin = py_template.format(py=py)
        cue = vi_template.format(vi=vi)
        if kind == "has":
            rows.append(has_name(f"{frame}_{key}", text, pinyin, cue, yes))
        else:
            rows.append(yes_no(f"{frame}_{key}", text, pinyin, cue, yes))
    return rows


FOOD_IS = owner_frame("food", "这是{name}的外卖吗？", "zhè shì {py} de wàimài ma?", "Đây là đồ ăn mang về của {vi} phải không?")
WHOSE_FOOD = owner_frame("whose_food", "外卖是{name}的吗？", "wàimài shì {py} de ma?", "Đồ ăn mang về là của {vi} phải không?")
BAG_IS = owner_frame("bag_is", "袋子上是{name}的名字吗？", "dàizi shàng shì {py} de míngzi ma?", "Trên túi là tên của {vi} phải không?")
LOOK = [
    yes_no("look_an", "安娜看袋子吗？", "Ānnà kàn dàizi ma?", "Anna có nhìn túi không?", True),
    yes_no("look_lm", "李明看袋子吗？", "Lǐ Míng kàn dàizi ma?", "Lý Minh có nhìn túi không?", True),
    yes_no("look_both", "他们看袋子上的名字吗？", "tāmen kàn dàizi shàng de míngzi ma?", "Họ có nhìn tên trên túi không?", True),
]
GUESSES = [
    yes_no("guess_an", "安娜猜是李明的吗？", "Ānnà cāi shì Lǐ Míng de ma?", "Anna đoán là của Lý Minh phải không?", True),
    yes_no("guess_lm", "李明猜是安娜的吗？", "Lǐ Míng cāi shì Ānnà de ma?", "Lý Minh đoán là của Anna phải không?", True),
    yes_no("guess_no", "安娜猜是同事的吗？", "Ānnà cāi shì tóngshì de ma?", "Anna đoán là của đồng nghiệp phải không?", False),
    yes_no("know", "他们知道是谁的吗？", "tāmen zhīdào shì shuí de ma?", "Họ biết của ai phải không?", False),
    has_name("bag_there", "桌上有一袋外卖吗？", "zhuō shàng yǒu yí dài wàimài ma?", "Trên bàn có một túi đồ ăn mang về phải không?", True),
]
WHO_FORMS = [
    row("who_food", "这是谁的外卖？", "zhè shì shuí de wàimài?", "Đây là đồ ăn mang về của ai?", "同事的外卖。", "tóngshì de wàimài.", "Đồ ăn mang về của đồng nghiệp.", 5200),
    row("who_is", "外卖是谁的？", "wàimài shì shuí de?", "Đồ ăn mang về là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
    row("who_bag", "袋子上是谁的名字？", "dàizi shàng shì shuí de míngzi?", "Trên túi là tên của ai?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5200),
]
OR_FORMS = [
    row("or_food_an", "这是同事的外卖还是安娜的外卖？", "zhè shì tóngshì de wàimài háishi Ānnà de wàimài?", "Đồ ăn mang về của đồng nghiệp hay của Anna?", "同事的外卖。", "tóngshì de wàimài.", "Đồ ăn mang về của đồng nghiệp.", 5500),
    row("or_food_lm", "这是同事的外卖还是李明的外卖？", "zhè shì tóngshì de wàimài háishi Lǐ Míng de wàimài?", "Đồ ăn mang về của đồng nghiệp hay của Lý Minh?", "同事的外卖。", "tóngshì de wàimài.", "Đồ ăn mang về của đồng nghiệp.", 5500),
    row("or_whose", "外卖是安娜的还是同事的？", "wàimài shì Ānnà de háishi tóngshì de?", "Đồ ăn mang về là của Anna hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("or_guess", "安娜猜是李明的还是同事的？", "Ānnà cāi shì Lǐ Míng de háishi tóngshì de?", "Anna đoán là của Lý Minh hay của đồng nghiệp?", "李明的。", "Lǐ Míng de.", "Của Lý Minh.", 5500),
    row("or_guess_lm", "李明猜是安娜的还是同事的？", "Lǐ Míng cāi shì Ānnà de háishi tóngshì de?", "Lý Minh đoán là của Anna hay của đồng nghiệp?", "安娜的。", "Ānnà de.", "Của Anna.", 5500),
    row("or_bag_an", "袋子上有安娜的名字还是同事的名字？", "dàizi shàng yǒu Ānnà de míngzi háishi tóngshì de míngzi?", "Trên túi có tên Anna hay tên đồng nghiệp?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500),
]
TABLE = owner_frame("table", "桌上的外卖是{name}的吗？", "zhuō shàng de wàimài shì {py} de ma?", "Đồ ăn mang về trên bàn là của {vi} phải không?")
NAME_IS = owner_frame("name_is", "袋子上的名字是{name}的吗？", "dàizi shàng de míngzi shì {py} de ma?", "Tên trên túi là của {vi} phải không?")
MORE_YES = [
    yes_no("know_an", "安娜知道是谁的吗？", "Ānnà zhīdào shì shuí de ma?", "Anna biết của ai phải không?", False),
    yes_no("know_lm", "李明知道是谁的吗？", "Lǐ Míng zhīdào shì shuí de ma?", "Lý Minh biết của ai phải không?", False),
    yes_no("also_an", "安娜也看袋子吗？", "Ānnà yě kàn dàizi ma?", "Anna cũng nhìn túi phải không?", True),
    yes_no("also_lm", "李明也看袋子吗？", "Lǐ Míng yě kàn dàizi ma?", "Lý Minh cũng nhìn túi phải không?", True),
    yes_no("one_bag", "这是一袋外卖吗？", "zhè shì yí dài wàimài ma?", "Đây là một túi đồ ăn mang về phải không?", True),
    yes_no("guess_food_an", "安娜猜这是李明的外卖吗？", "Ānnà cāi zhè shì Lǐ Míng de wàimài ma?", "Anna đoán đây là đồ ăn mang về của Lý Minh phải không?", True),
    yes_no("guess_food_lm", "李明猜这是安娜的外卖吗？", "Lǐ Míng cāi zhè shì Ānnà de wàimài ma?", "Lý Minh đoán đây là đồ ăn mang về của Anna phải không?", True),
]
MORE_WHO = [
    row("who_table", "桌上的外卖是谁的？", "zhuō shàng de wàimài shì shuí de?", "Đồ ăn mang về trên bàn là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
]
MORE_OR = [
    row("or_table_an", "桌上的外卖是同事的还是安娜的？", "zhuō shàng de wàimài shì tóngshì de háishi Ānnà de?", "Đồ ăn mang về trên bàn là của đồng nghiệp hay của Anna?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("or_table_lm", "桌上的外卖是同事的还是李明的？", "zhuō shàng de wàimài shì tóngshì de háishi Lǐ Míng de?", "Đồ ăn mang về trên bàn là của đồng nghiệp hay của Lý Minh?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
]
NEW_YES = FOOD_IS + WHOSE_FOOD + BAG_IS + LOOK + GUESSES + TABLE + NAME_IS + MORE_YES
NEW_WHO = WHO_FORMS + MORE_WHO
NEW_OR = OR_FORMS + MORE_OR
LATE_YES = NEW_YES
LATE_WHO = WHO_FORMS + [
    row("late_who", "这是谁的？", "zhè shì shuí de?", "Của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
]
LATE_OR = OR_FORMS + _rows(["q_or_lm", "q_or_anna", "q_or_name"])
LATE_NAME = BAG_IS + owner_frame(
    "bag_has",
    "袋子上有{name}的名字吗？",
    "dàizi shàng yǒu {py} de míngzi ma?",
    "Trên túi có tên của {vi} phải không?",
    "has",
) + [
    row("late_name_who", "袋子上有谁的名字？", "dàizi shàng yǒu shuí de míngzi?", "Trên túi có tên ai?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5200),
]

RETOLD = [
    ("bag", "coach_zh", "桌上有一袋外卖。", "zhuō shàng yǒu yí dài wàimài.", "Trên bàn có một túi đồ ăn mang về.", 1000),
    ("who", "coach_zh", "这是谁的外卖？", "zhè shì shuí de wàimài?", "Đây là đồ ăn mang về của ai?", 900),
    ("guess_a", "coach_zh", "安娜猜是李明的。", "Ānnà cāi shì Lǐ Míng de.", "Anna đoán là của Lý Minh.", 900),
    ("guess_l", "coach_zh", "李明猜是安娜的。", "Lǐ Míng cāi shì Ānnà de.", "Lý Minh đoán là của Anna.", 900),
    ("desk_an", "coach_zh", "这不是安娜的桌子。", "zhè bú shì Ānnà de zhuōzi.", "Đây không phải bàn của Anna.", 900),
    ("desk_lm", "coach_zh", "这不是李明的桌子。", "zhè bú shì Lǐ Míng de zhuōzi.", "Đây không phải bàn của Lý Minh.", 900),
    ("look", "coach_zh", "他们看袋子上的名字。", "tāmen kàn dàizi shàng de míngzi.", "Họ nhìn tên trên túi.", 1000),
    ("name", "coach_zh", "袋子上有同事的名字。", "dàizi shàng yǒu tóngshì de míngzi.", "Trên túi có tên đồng nghiệp.", 1100),
    ("neither", "coach_zh", "不是安娜的。不是李明的。", "bú shì Ānnà de. bú shì Lǐ Míng de.", "Không phải của Anna. Không phải của Lý Minh.", 1100),
    ("result", "coach_zh", "这是同事的。", "zhè shì tóngshì de.", "Đây là của đồng nghiệp.", 1200),
]

ANNA = [
    ("food", "coach_zh", "这是外卖。", "zhè shì wàimài.", "Đây là đồ ăn mang về.", 900),
    ("ask", "anna", "这是安娜的吗？", "zhè shì Ānnà de ma?", "Đây là của Anna phải không?", 900),
    ("yes", "li_ming", "是。", "shì.", "Phải.", 1000),
    ("name", "coach_zh", "袋子上有安娜的名字。", "dàizi shàng yǒu Ānnà de míngzi.", "Trên túi có tên Anna.", 1100),
    ("hers", "coach_zh", "这是安娜的。", "zhè shì Ānnà de.", "Đây là của Anna.", 900),
    ("not_lm", "coach_zh", "不是李明的。", "bú shì Lǐ Míng de.", "Không phải của Lý Minh.", 800),
    ("not_ts", "coach_zh", "不是同事的。", "bú shì tóngshì de.", "Không phải của đồng nghiệp.", 1200),
]

ANNA_Q = [
    yes_no("a_an", "这是安娜的吗？", "zhè shì Ānnà de ma?", "Đây là của Anna phải không?", True),
    yes_no("a_ts", "这是同事的吗？", "zhè shì tóngshì de ma?", "Đây là của đồng nghiệp phải không?", False),
    yes_no("a_lm", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?", False),
    has_name("a_name", "袋子上有安娜的名字吗？", "dàizi shàng yǒu Ānnà de míngzi ma?", "Trên túi có tên Anna phải không?", True),
    has_name("a_name_ts", "袋子上有同事的名字吗？", "dàizi shàng yǒu tóngshì de míngzi ma?", "Trên túi có tên đồng nghiệp phải không?", False),
    row("a_who", "这是谁的？", "zhè shì shuí de?", "Của ai?", "安娜的。", "Ānnà de.", "Của Anna.", 5000),
    row("a_or", "这是安娜的还是同事的？", "zhè shì Ānnà de háishi tóngshì de?", "Của Anna hay của đồng nghiệp?", "安娜的。", "Ānnà de.", "Của Anna.", 5500),
    row("a_or_name", "袋子上有安娜的名字还是同事的名字？", "dàizi shàng yǒu Ānnà de míngzi háishi tóngshì de míngzi?", "Trên túi có tên Anna hay tên đồng nghiệp?", "安娜的名字。", "Ānnà de míngzi.", "Tên Anna.", 5500),
]


ROUND_YES = [
    yes_no("food", "这是外卖吗？", "zhè shì wàimài ma?", "Đây là đồ ăn mang về phải không?", True),
    yes_no("anna", "这是安娜的吗？", "zhè shì Ānnà de ma?", "Đây là của Anna phải không?", False, praise="Đúng rồi."),
    has_name("has_name", "袋子上有名字吗？", "dàizi shàng yǒu míngzi ma?", "Trên túi có tên phải không?", True),
    yes_no("ts_food", "这是同事的外卖吗？", "zhè shì tóngshì de wàimài ma?", "Đây là đồ ăn mang về của đồng nghiệp phải không?", True),
    yes_no("food_lm", "外卖是李明的吗？", "wàimài shì Lǐ Míng de ma?", "Đồ ăn mang về là của Lý Minh phải không?", False),
    has_name("name_ts", "袋子上有同事的名字吗？", "dàizi shàng yǒu tóngshì de míngzi ma?", "Trên túi có tên đồng nghiệp phải không?", True),
    yes_no("lm", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?", False),
    has_name("bag", "桌上有一袋外卖吗？", "zhuō shàng yǒu yí dài wàimài ma?", "Trên bàn có một túi đồ ăn mang về phải không?", True),
    yes_no("food_ts", "外卖是同事的吗？", "wàimài shì tóngshì de ma?", "Đồ ăn mang về là của đồng nghiệp phải không?", True, praise="Tốt."),
    has_name("name_an", "袋子上有安娜的名字吗？", "dàizi shàng yǒu Ānnà de míngzi ma?", "Trên túi có tên Anna phải không?", False),
    yes_no("one", "这是一袋外卖吗？", "zhè shì yí dài wàimài ma?", "Đây là một túi đồ ăn mang về phải không?", True),
    yes_no("table_an", "桌上的外卖是安娜的吗？", "zhuō shàng de wàimài shì Ānnà de ma?", "Đồ ăn mang về trên bàn là của Anna phải không?", False),
    yes_no("also_an", "安娜也看袋子吗？", "Ānnà yě kàn dàizi ma?", "Anna cũng nhìn túi phải không?", True),
    has_name("name_lm", "袋子上有李明的名字吗？", "dàizi shàng yǒu Lǐ Míng de míngzi ma?", "Trên túi có tên Lý Minh phải không?", False),
    yes_no("table_ts", "桌上的外卖是同事的吗？", "zhuō shàng de wàimài shì tóngshì de ma?", "Đồ ăn mang về trên bàn là của đồng nghiệp phải không?", True),
    yes_no("also_lm", "李明也看袋子吗？", "Lǐ Míng yě kàn dàizi ma?", "Lý Minh cũng nhìn túi phải không?", True),
]
ROUND_YES_2A = [
    yes_no("look_name", "他们看袋子上的名字吗？", "tāmen kàn dàizi shàng de míngzi ma?", "Họ có nhìn tên trên túi không?", True),
    yes_no("guess_ts", "安娜猜这是同事的吗？", "Ānnà cāi zhè shì tóngshì de ma?", "Anna đoán đây là của đồng nghiệp phải không?", False),
    yes_no("guess_lm_an", "李明猜这是安娜的吗？", "Lǐ Míng cāi zhè shì Ānnà de ma?", "Lý Minh đoán đây là của Anna phải không?", True),
    yes_no("know", "他们知道外卖是谁的吗？", "tāmen zhīdào wàimài shì shuí de ma?", "Họ biết đồ ăn mang về là của ai phải không?", False),
    yes_no("look_an", "安娜看袋子吗？", "Ānnà kàn dàizi ma?", "Anna có nhìn túi không?", True),
    yes_no("look_lm", "李明看袋子吗？", "Lǐ Míng kàn dàizi ma?", "Lý Minh có nhìn túi không?", True),
    yes_no("guess_an", "安娜猜这是李明的吗？", "Ānnà cāi zhè shì Lǐ Míng de ma?", "Anna đoán đây là của Lý Minh phải không?", True),
    yes_no("guess_lm_ts", "李明猜这是同事的吗？", "Lǐ Míng cāi zhè shì tóngshì de ma?", "Lý Minh đoán đây là của đồng nghiệp phải không?", False),
]
ROUND_YES_2B = [
    yes_no("name_lm", "袋子上的名字是李明的吗？", "dàizi shàng de míngzi shì Lǐ Míng de ma?", "Tên trên túi là của Lý Minh phải không?", False),
    yes_no("name_ts", "袋子上的名字是同事的吗？", "dàizi shàng de míngzi shì tóngshì de ma?", "Tên trên túi là của đồng nghiệp phải không?", True),
    yes_no("name_an", "袋子上的名字是安娜的吗？", "dàizi shàng de míngzi shì Ānnà de ma?", "Tên trên túi là của Anna phải không?", False),
    yes_no("know_an", "安娜知道外卖是谁的吗？", "Ānnà zhīdào wàimài shì shuí de ma?", "Anna biết đồ ăn mang về là của ai phải không?", False),
    yes_no("know_lm", "李明知道外卖是谁的吗？", "Lǐ Míng zhīdào wàimài shì shuí de ma?", "Lý Minh biết đồ ăn mang về là của ai phải không?", False),
    yes_no("an_food", "这是安娜的外卖吗？", "zhè shì Ānnà de wàimài ma?", "Đây là đồ ăn mang về của Anna phải không?", False),
    yes_no("food_an", "外卖是安娜的吗？", "wàimài shì Ānnà de ma?", "Đồ ăn mang về là của Anna phải không?", False),
    yes_no("table_lm", "桌上的外卖是李明的吗？", "zhuō shàng de wàimài shì Lǐ Míng de ma?", "Đồ ăn mang về trên bàn là của Lý Minh phải không?", False),
]
ROUND_YES_3 = [
    yes_no("food_lm", "外卖是李明的吗？", "wàimài shì Lǐ Míng de ma?", "Đồ ăn mang về là của Lý Minh phải không?", False),
    yes_no("lm_food", "这是李明的外卖吗？", "zhè shì Lǐ Míng de wàimài ma?", "Đây là đồ ăn mang về của Lý Minh phải không?", False),
    yes_no("guess_food_lm", "李明猜这是安娜的外卖吗？", "Lǐ Míng cāi zhè shì Ānnà de wàimài ma?", "Lý Minh đoán đây là đồ ăn mang về của Anna phải không?", True),
    yes_no("anna", "这是安娜的吗？", "zhè shì Ānnà de ma?", "Đây là của Anna phải không?", False),
    yes_no("guess_food_an", "安娜猜这是李明的外卖吗？", "Ānnà cāi zhè shì Lǐ Míng de wàimài ma?", "Anna đoán đây là đồ ăn mang về của Lý Minh phải không?", True),
    yes_no("ts", "这是同事的吗？", "zhè shì tóngshì de ma?", "Đây là của đồng nghiệp phải không?", True),
    yes_no("ts_food", "这是同事的外卖吗？", "zhè shì tóngshì de wàimài ma?", "Đây là đồ ăn mang về của đồng nghiệp phải không?", True),
    has_name("table", "桌上有外卖吗？", "zhuō shàng yǒu wàimài ma?", "Trên bàn có đồ ăn mang về phải không?", True),
    yes_no("food", "这是外卖吗？", "zhè shì wàimài ma?", "Đây là đồ ăn mang về phải không?", True),
    has_name("has_name", "袋子上有名字吗？", "dàizi shàng yǒu míngzi ma?", "Trên túi có tên phải không?", True),
    yes_no("look", "他们看袋子吗？", "tāmen kàn dàizi ma?", "Họ có nhìn túi không?", True),
    yes_no("guess_ts_food", "安娜猜这是同事的外卖吗？", "Ānnà cāi zhè shì tóngshì de wàimài ma?", "Anna đoán đây là đồ ăn mang về của đồng nghiệp phải không?", False),
]
ROUND_YES_EXTRA = [
    yes_no("both_look", "安娜和李明看袋子吗？", "Ānnà hé Lǐ Míng kàn dàizi ma?", "Anna và Lý Minh có nhìn túi không?", True),
    yes_no("both_know", "安娜和李明知道外卖是谁的吗？", "Ānnà hé Lǐ Míng zhīdào wàimài shì shuí de ma?", "Anna và Lý Minh biết đồ ăn mang về là của ai phải không?", False),
    yes_no("an_see", "安娜看袋子上的名字吗？", "Ānnà kàn dàizi shàng de míngzi ma?", "Anna có nhìn tên trên túi không?", True),
    yes_no("lm_see", "李明看袋子上的名字吗？", "Lǐ Míng kàn dàizi shàng de míngzi ma?", "Lý Minh có nhìn tên trên túi không?", True),
    yes_no("guess_self_an", "安娜猜这是安娜的吗？", "Ānnà cāi zhè shì Ānnà de ma?", "Anna đoán đây là của Anna phải không?", False),
    yes_no("guess_self_lm", "李明猜这是李明的吗？", "Lǐ Míng cāi zhè shì Lǐ Míng de ma?", "Lý Minh đoán đây là của Lý Minh phải không?", False),
    yes_no("guess_food_an2", "安娜猜这是李明的外卖吗？", "Ānnà cāi zhè shì Lǐ Míng de wàimài ma?", "Anna đoán đây là đồ ăn mang về của Lý Minh phải không?", True),
    yes_no("guess_food_lm2", "李明猜这是安娜的外卖吗？", "Lǐ Míng cāi zhè shì Ānnà de wàimài ma?", "Lý Minh đoán đây là đồ ăn mang về của Anna phải không?", True),
    yes_no("name_ts_short", "这是同事的名字吗？", "zhè shì tóngshì de míngzi ma?", "Đây là tên đồng nghiệp phải không?", True),
    yes_no("name_an_short", "这是安娜的名字吗？", "zhè shì Ānnà de míngzi ma?", "Đây là tên Anna phải không?", False),
    yes_no("name_lm_short", "这是李明的名字吗？", "zhè shì Lǐ Míng de míngzi ma?", "Đây là tên Lý Minh phải không?", False),
]


ROUND_MORE = [
    has_name("desk_ts", "桌上有同事的外卖吗？", "zhuō shàng yǒu tóngshì de wàimài ma?", "Trên bàn có đồ ăn mang về của đồng nghiệp phải không?", True),
    has_name("desk_an", "桌上有安娜的外卖吗？", "zhuō shàng yǒu Ānnà de wàimài ma?", "Trên bàn có đồ ăn mang về của Anna phải không?", False),
    yes_no("both_name", "安娜和李明看袋子上的名字吗？", "Ānnà hé Lǐ Míng kàn dàizi shàng de míngzi ma?", "Anna và Lý Minh có nhìn tên trên túi không?", True),
    has_name("desk_lm", "桌上有李明的外卖吗？", "zhuō shàng yǒu Lǐ Míng de wàimài ma?", "Trên bàn có đồ ăn mang về của Lý Minh phải không?", False),
    yes_no("is_ts_food", "外卖是同事的外卖吗？", "wàimài shì tóngshì de wàimài ma?", "Đồ ăn mang về là đồ của đồng nghiệp phải không?", True),
    yes_no("is_an_food", "外卖是安娜的外卖吗？", "wàimài shì Ānnà de wàimài ma?", "Đồ ăn mang về là đồ của Anna phải không?", False),
    yes_no("see_an", "安娜看的是袋子吗？", "Ānnà kàn de shì dàizi ma?", "Anna nhìn cái túi phải không?", True),
    yes_no("is_lm_food", "外卖是李明的外卖吗？", "wàimài shì Lǐ Míng de wàimài ma?", "Đồ ăn mang về là đồ của Lý Minh phải không?", False),
    yes_no("see_lm", "李明看的是袋子吗？", "Lǐ Míng kàn de shì dàizi ma?", "Lý Minh nhìn cái túi phải không?", True),
    yes_no("guess_w_an", "安娜猜外卖是李明的吗？", "Ānnà cāi wàimài shì Lǐ Míng de ma?", "Anna đoán đồ ăn mang về là của Lý Minh phải không?", True),
    yes_no("guess_w_lm", "李明猜外卖是安娜的吗？", "Lǐ Míng cāi wàimài shì Ānnà de ma?", "Lý Minh đoán đồ ăn mang về là của Anna phải không?", True),
    yes_no("guess_w_no", "安娜猜外卖是同事的吗？", "Ānnà cāi wàimài shì tóngshì de ma?", "Anna đoán đồ ăn mang về là của đồng nghiệp phải không?", False),
    yes_no("guess_w_lm_no", "李明猜外卖是同事的吗？", "Lǐ Míng cāi wàimài shì tóngshì de ma?", "Lý Minh đoán đồ ăn mang về là của đồng nghiệp phải không?", False),
    yes_no("guess_them", "他们猜这是同事的吗？", "tāmen cāi zhè shì tóngshì de ma?", "Họ đoán đây là của đồng nghiệp phải không?", False),
    yes_no("see_bag", "他们看的是袋子吗？", "tāmen kàn de shì dàizi ma?", "Họ nhìn cái túi phải không?", True),
    yes_no("see_name", "他们看的是名字吗？", "tāmen kàn de shì míngzi ma?", "Họ nhìn cái tên phải không?", True),
    yes_no("this_ts", "这袋外卖是同事的吗？", "zhè dài wàimài shì tóngshì de ma?", "Túi đồ ăn mang về này là của đồng nghiệp phải không?", True),
    yes_no("this_an", "这袋外卖是安娜的吗？", "zhè dài wàimài shì Ānnà de ma?", "Túi đồ ăn mang về này là của Anna phải không?", False),
    yes_no("this_lm", "这袋外卖是李明的吗？", "zhè dài wàimài shì Lǐ Míng de ma?", "Túi đồ ăn mang về này là của Lý Minh phải không?", False),
    yes_no("both_see", "安娜和李明看的是袋子吗？", "Ānnà hé Lǐ Míng kàn de shì dàizi ma?", "Anna và Lý Minh nhìn cái túi phải không?", True),
    yes_no("guess_them_lm", "他们猜这是李明的吗？", "tāmen cāi zhè shì Lǐ Míng de ma?", "Họ đoán đây là của Lý Minh phải không?", False),
    yes_no("guess_them_an", "他们猜这是安娜的吗？", "tāmen cāi zhè shì Ānnà de ma?", "Họ đoán đây là của Anna phải không?", False),
]
ROUND_KNOW = [
    row("know_they", "他们知道，还是不知道？", "tāmen zhīdào, háishi bù zhīdào?", "Họ biết, hay không biết?", "不知道。", "bù zhīdào.", "Không biết.", 5500),
    row("know_an", "安娜知道，还是不知道？", "Ānnà zhīdào, háishi bù zhīdào?", "Anna biết, hay không biết?", "不知道。", "bù zhīdào.", "Không biết.", 5500),
    row("know_lm", "李明知道，还是不知道？", "Lǐ Míng zhīdào, háishi bù zhīdào?", "Lý Minh biết, hay không biết?", "不知道。", "bù zhīdào.", "Không biết.", 5500),
    row("has_or", "袋子上有名字还是没有名字？", "dàizi shàng yǒu míngzi háishi méi yǒu míngzi?", "Trên túi có tên hay không có tên?", "有名字。", "yǒu míngzi.", "Có tên.", 5500),
]


def _rotate(rows: list[tuple]) -> list[tuple]:
    mid = len(rows) // 2
    return rows[mid:] + rows[:mid]


ROUND_WHO = [
    row("table", "桌上的外卖是谁的？", "zhuō shàng de wàimài shì shuí de?", "Đồ ăn mang về trên bàn là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("food", "这是谁的外卖？", "zhè shì shuí de wàimài?", "Đây là đồ ăn mang về của ai?", "同事的外卖。", "tóngshì de wàimài.", "Đồ ăn mang về của đồng nghiệp.", 5200),
    row("name", "袋子上的名字是谁的？", "dàizi shàng de míngzi shì shuí de?", "Tên trên túi là của ai?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5200),
    row("whose", "外卖是谁的？", "wàimài shì shuí de?", "Đồ ăn mang về là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
    row("bag", "袋子上有谁的名字？", "dàizi shàng yǒu shuí de míngzi?", "Trên túi có tên ai?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5200),
    row("who", "这是谁的？", "zhè shì shuí de?", "Của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
    row("this_who", "这袋外卖是谁的？", "zhè dài wàimài shì shuí de?", "Túi đồ ăn mang về này là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("food_who", "那是谁的外卖？", "nà shì shuí de wàimài?", "Kia là đồ ăn mang về của ai?", "同事的外卖。", "tóngshì de wàimài.", "Đồ ăn mang về của đồng nghiệp.", 5200),
    row("name_who", "名字是谁的？", "míngzi shì shuí de?", "Tên là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
]
ROUND_OR = [
    row("lm_ts", "这是李明的还是同事的？", "zhè shì Lǐ Míng de háishi tóngshì de?", "Của Lý Minh hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("ts_an", "这是同事的还是安娜的？", "zhè shì tóngshì de háishi Ānnà de?", "Của đồng nghiệp hay của Anna?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("food_an", "外卖是安娜的还是同事的？", "wàimài shì Ānnà de háishi tóngshì de?", "Đồ ăn mang về là của Anna hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("food_lm", "这是同事的外卖还是李明的外卖？", "zhè shì tóngshì de wàimài háishi Lǐ Míng de wàimài?", "Đồ ăn mang về của đồng nghiệp hay của Lý Minh?", "同事的外卖。", "tóngshì de wàimài.", "Đồ ăn mang về của đồng nghiệp.", 5500),
    row("name_lm", "袋子上有李明的名字还是同事的名字？", "dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi?", "Trên túi có tên Lý Minh hay tên đồng nghiệp?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500),
    row("name_ts", "袋子上有同事的名字还是安娜的名字？", "dàizi shàng yǒu tóngshì de míngzi háishi Ānnà de míngzi?", "Trên túi có tên đồng nghiệp hay tên Anna?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500),
    row("guess_an", "安娜猜这是李明的还是同事的？", "Ānnà cāi zhè shì Lǐ Míng de háishi tóngshì de?", "Anna đoán đây là của Lý Minh hay của đồng nghiệp?", "李明的。", "Lǐ Míng de.", "Của Lý Minh.", 5500),
    row("guess_lm", "李明猜这是同事的还是安娜的？", "Lǐ Míng cāi zhè shì tóngshì de háishi Ānnà de?", "Lý Minh đoán đây là của đồng nghiệp hay của Anna?", "安娜的。", "Ānnà de.", "Của Anna.", 5500),
    row("table", "桌上的外卖是李明的还是同事的？", "zhuō shàng de wàimài shì Lǐ Míng de háishi tóngshì de?", "Đồ ăn mang về trên bàn là của Lý Minh hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
]


ROUND_SHORT = [
    yes_no("food", "这是外卖吗？", "zhè shì wàimài ma?", "Đây là đồ ăn mang về phải không?", True),
    yes_no("lm", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?", False, praise="Đúng rồi."),
    yes_no("an", "这是安娜的吗？", "zhè shì Ānnà de ma?", "Đây là của Anna phải không?", False),
    yes_no("ts", "这是同事的吗？", "zhè shì tóngshì de ma?", "Đây là của đồng nghiệp phải không?", True),
    yes_no("food_lm", "外卖是李明的吗？", "wàimài shì Lǐ Míng de ma?", "Đồ ăn mang về là của Lý Minh phải không?", False),
    yes_no("food_ts", "外卖是同事的吗？", "wàimài shì tóngshì de ma?", "Đồ ăn mang về là của đồng nghiệp phải không?", True, praise="Tốt."),
    yes_no("food_an", "外卖是安娜的吗？", "wàimài shì Ānnà de ma?", "Đồ ăn mang về là của Anna phải không?", False),
    row("who", "这是谁的？", "zhè shì shuí de?", "Của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
    row("food_who", "外卖是谁的？", "wàimài shì shuí de?", "Đồ ăn mang về là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
]
ROUND_FAMILIAR = [
    yes_no("food", "这是外卖吗？", "zhè shì wàimài ma?", "Đây là đồ ăn mang về phải không?", True),
    yes_no("food_ts", "外卖是同事的吗？", "wàimài shì tóngshì de ma?", "Đồ ăn mang về là của đồng nghiệp phải không?", True),
    yes_no("food_lm", "外卖是李明的吗？", "wàimài shì Lǐ Míng de ma?", "Đồ ăn mang về là của Lý Minh phải không?", False),
    yes_no("food_an", "外卖是安娜的吗？", "wàimài shì Ānnà de ma?", "Đồ ăn mang về là của Anna phải không?", False),
    row("who", "这是谁的？", "zhè shì shuí de?", "Của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
    yes_no("lm", "这是李明的吗？", "zhè shì Lǐ Míng de ma?", "Đây là của Lý Minh phải không?", False),
    yes_no("ts", "这是同事的吗？", "zhè shì tóngshì de ma?", "Đây là của đồng nghiệp phải không?", True),
]
ROUND_BAG = [
    has_name("has", "袋子上有名字吗？", "dàizi shàng yǒu míngzi ma?", "Trên túi có tên phải không?", True),
    has_name("name_ts", "袋子上有同事的名字吗？", "dàizi shàng yǒu tóngshì de míngzi ma?", "Trên túi có tên đồng nghiệp phải không?", True),
    has_name("name_lm", "袋子上有李明的名字吗？", "dàizi shàng yǒu Lǐ Míng de míngzi ma?", "Trên túi có tên Lý Minh phải không?", False),
    has_name("name_an", "袋子上有安娜的名字吗？", "dàizi shàng yǒu Ānnà de míngzi ma?", "Trên túi có tên Anna phải không?", False),
]
ROUND_NAME_IS = [
    yes_no("ts", "袋子上的名字是同事的吗？", "dàizi shàng de míngzi shì tóngshì de ma?", "Tên trên túi là của đồng nghiệp phải không?", True),
    yes_no("lm", "袋子上的名字是李明的吗？", "dàizi shàng de míngzi shì Lǐ Míng de ma?", "Tên trên túi là của Lý Minh phải không?", False),
    yes_no("an", "袋子上的名字是安娜的吗？", "dàizi shàng de míngzi shì Ānnà de ma?", "Tên trên túi là của Anna phải không?", False),
]
ROUND_OR_SHORT = [
    row("lm", "这是李明的还是同事的？", "zhè shì Lǐ Míng de háishi tóngshì de?", "Của Lý Minh hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("ts", "这是同事的还是安娜的？", "zhè shì tóngshì de háishi Ānnà de?", "Của đồng nghiệp hay của Anna?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("food", "外卖是安娜的还是同事的？", "wàimài shì Ānnà de háishi tóngshì de?", "Đồ ăn mang về là của Anna hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
]
ROUND_BAG_OR = [
    row("name", "袋子上有李明的名字还是同事的名字？", "dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi?", "Trên túi có tên Lý Minh hay tên đồng nghiệp?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500),
]
ROUND_NEW = [
    yes_no("ts_food", "这是同事的外卖吗？", "zhè shì tóngshì de wàimài ma?", "Đây là đồ ăn mang về của đồng nghiệp phải không?", True),
    yes_no("lm_food", "这是李明的外卖吗？", "zhè shì Lǐ Míng de wàimài ma?", "Đây là đồ ăn mang về của Lý Minh phải không?", False),
    yes_no("look_an", "安娜看袋子吗？", "Ānnà kàn dàizi ma?", "Anna có nhìn túi không?", True),
    yes_no("look_lm", "李明看袋子吗？", "Lǐ Míng kàn dàizi ma?", "Lý Minh có nhìn túi không?", True),
    yes_no("guess_an", "安娜猜这是李明的吗？", "Ānnà cāi zhè shì Lǐ Míng de ma?", "Anna đoán đây là của Lý Minh phải không?", True),
    yes_no("guess_lm", "李明猜这是安娜的吗？", "Lǐ Míng cāi zhè shì Ānnà de ma?", "Lý Minh đoán đây là của Anna phải không?", True),
    yes_no("guess_ts", "安娜猜这是同事的吗？", "Ānnà cāi zhè shì tóngshì de ma?", "Anna đoán đây là của đồng nghiệp phải không?", False),
]
ROUND_WHO_LATE = [
    row("food", "这是谁的外卖？", "zhè shì shuí de wàimài?", "Đây là đồ ăn mang về của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("name", "袋子上的名字是谁的？", "dàizi shàng de míngzi shì shuí de?", "Tên trên túi là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("bag", "袋子上有谁的名字？", "dàizi shàng yǒu shuí de míngzi?", "Trên túi có tên ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("table", "桌上的外卖是谁的？", "zhuō shàng de wàimài shì shuí de?", "Đồ ăn mang về trên bàn là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
]
ROUND_OR_LATE = [
    row("lm", "这是李明的还是同事的？", "zhè shì Lǐ Míng de háishi tóngshì de?", "Của Lý Minh hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("swap", "外卖是同事的还是李明的？", "wàimài shì tóngshì de háishi Lǐ Míng de?", "Đồ ăn mang về là của đồng nghiệp hay của Lý Minh?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("an", "这是安娜的还是同事的？", "zhè shì Ānnà de háishi tóngshì de?", "Của Anna hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("name_an", "袋子上有同事的名字还是安娜的名字？", "dàizi shàng yǒu tóngshì de míngzi háishi Ānnà de míngzi?", "Trên túi có tên đồng nghiệp hay tên Anna?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500),
    row("name", "袋子上有李明的名字还是同事的名字？", "dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi?", "Trên túi có tên Lý Minh hay tên đồng nghiệp?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500),
]
ROUND_EXPAND = [
    has_name("desk_bag", "桌上有一袋外卖吗？", "zhuō shàng yǒu yí dài wàimài ma?", "Trên bàn có một túi đồ ăn mang về phải không?", True),
    yes_no("desk_an", "桌上的外卖是安娜的吗？", "zhuō shàng de wàimài shì Ānnà de ma?", "Đồ ăn mang về trên bàn là của Anna phải không?", False),
    yes_no("desk_ts", "桌上的外卖是同事的吗？", "zhuō shàng de wàimài shì tóngshì de ma?", "Đồ ăn mang về trên bàn là của đồng nghiệp phải không?", True),
    yes_no("an_food", "这是安娜的外卖吗？", "zhè shì Ānnà de wàimài ma?", "Đây là đồ ăn mang về của Anna phải không?", False),
    yes_no("one", "这是一袋外卖吗？", "zhè shì yí dài wàimài ma?", "Đây là một túi đồ ăn mang về phải không?", True),
    yes_no("desk_lm", "桌上的外卖是李明的吗？", "zhuō shàng de wàimài shì Lǐ Míng de ma?", "Đồ ăn mang về trên bàn là của Lý Minh phải không?", False),
    yes_no("also_an", "安娜也看袋子吗？", "Ānnà yě kàn dàizi ma?", "Anna cũng nhìn túi phải không?", True),
    yes_no("name_lm", "这是李明的名字吗？", "zhè shì Lǐ Míng de míngzi ma?", "Đây là tên Lý Minh phải không?", False),
    yes_no("name_ts", "这是同事的名字吗？", "zhè shì tóngshì de míngzi ma?", "Đây là tên đồng nghiệp phải không?", True),
    yes_no("name_an", "这是安娜的名字吗？", "zhè shì Ānnà de míngzi ma?", "Đây là tên Anna phải không?", False),
    yes_no("look", "他们看袋子吗？", "tāmen kàn dàizi ma?", "Họ có nhìn túi không?", True),
    yes_no("guess_lm_ts", "李明猜这是同事的吗？", "Lǐ Míng cāi zhè shì tóngshì de ma?", "Lý Minh đoán đây là của đồng nghiệp phải không?", False),
    yes_no("see_an", "安娜看袋子上的名字吗？", "Ānnà kàn dàizi shàng de míngzi ma?", "Anna có nhìn tên trên túi không?", True),
    yes_no("guess_self_an", "安娜猜这是安娜的吗？", "Ānnà cāi zhè shì Ānnà de ma?", "Anna đoán đây là của Anna phải không?", False),
    yes_no("see_lm", "李明看袋子上的名字吗？", "Lǐ Míng kàn dàizi shàng de míngzi ma?", "Lý Minh có nhìn tên trên túi không?", True),
    yes_no("guess_self_lm", "李明猜这是李明的吗？", "Lǐ Míng cāi zhè shì Lǐ Míng de ma?", "Lý Minh đoán đây là của Lý Minh phải không?", False),
    yes_no("see_both", "他们看袋子上的名字吗？", "tāmen kàn dàizi shàng de míngzi ma?", "Họ có nhìn tên trên túi không?", True),
    yes_no("bag_an", "这袋外卖是安娜的吗？", "zhè dài wàimài shì Ānnà de ma?", "Túi đồ ăn mang về này là của Anna phải không?", False),
    yes_no("bag_ts", "这袋外卖是同事的吗？", "zhè dài wàimài shì tóngshì de ma?", "Túi đồ ăn mang về này là của đồng nghiệp phải không?", True),
    yes_no("bag_lm", "这袋外卖是李明的吗？", "zhè dài wàimài shì Lǐ Míng de ma?", "Túi đồ ăn mang về này là của Lý Minh phải không?", False),
    row("bag_who", "这袋外卖是谁的？", "zhè dài wàimài shì shuí de?", "Túi đồ ăn mang về này là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    has_name("desk_food", "桌上有外卖吗？", "zhuō shàng yǒu wàimài ma?", "Trên bàn có đồ ăn mang về phải không?", True),
    has_name("desk_an_food", "桌上有安娜的外卖吗？", "zhuō shàng yǒu Ānnà de wàimài ma?", "Trên bàn có đồ ăn mang về của Anna phải không?", False),
    has_name("desk_ts_food", "桌上有同事的外卖吗？", "zhuō shàng yǒu tóngshì de wàimài ma?", "Trên bàn có đồ ăn mang về của đồng nghiệp phải không?", True),
    has_name("desk_lm_food", "桌上有李明的外卖吗？", "zhuō shàng yǒu Lǐ Míng de wàimài ma?", "Trên bàn có đồ ăn mang về của Lý Minh phải không?", False),
    row("or_ts_lm", "这是同事的还是李明的？", "zhè shì tóngshì de háishi Lǐ Míng de?", "Của đồng nghiệp hay của Lý Minh?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    yes_no("whose_an", "名字是安娜的吗？", "míngzi shì Ānnà de ma?", "Tên là của Anna phải không?", False),
    yes_no("whose_ts", "名字是同事的吗？", "míngzi shì tóngshì de ma?", "Tên là của đồng nghiệp phải không?", True),
    yes_no("whose_lm", "名字是李明的吗？", "míngzi shì Lǐ Míng de ma?", "Tên là của Lý Minh phải không?", False),
    row("or_food_lm", "外卖是李明的还是同事的？", "wàimài shì Lǐ Míng de háishi tóngshì de?", "Đồ ăn mang về là của Lý Minh hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    yes_no("both_look", "安娜和李明看袋子吗？", "Ānnà hé Lǐ Míng kàn dàizi ma?", "Anna và Lý Minh có nhìn túi không?", True),
    yes_no("also_lm", "李明也看袋子吗？", "Lǐ Míng yě kàn dàizi ma?", "Lý Minh cũng nhìn túi phải không?", True),
    row("or_desk", "桌上的外卖是安娜的还是同事的？", "zhuō shàng de wàimài shì Ānnà de háishi tóngshì de?", "Đồ ăn mang về trên bàn là của Anna hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("or_name_an", "袋子上有安娜的名字还是同事的名字？", "dàizi shàng yǒu Ānnà de míngzi háishi tóngshì de míngzi?", "Trên túi có tên Anna hay tên đồng nghiệp?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500),
    row("or_desk_lm", "桌上的外卖是李明的还是同事的？", "zhuō shàng de wàimài shì Lǐ Míng de háishi tóngshì de?", "Đồ ăn mang về trên bàn là của Lý Minh hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    yes_no("both_name", "安娜和李明看袋子上的名字吗？", "Ānnà hé Lǐ Míng kàn dàizi shàng de míngzi ma?", "Anna và Lý Minh có nhìn tên trên túi không?", True),
    yes_no("ts_food", "这是同事的外卖吗？", "zhè shì tóngshì de wàimài ma?", "Đây là đồ ăn mang về của đồng nghiệp phải không?", True),
    yes_no("lm_food", "这是李明的外卖吗？", "zhè shì Lǐ Míng de wàimài ma?", "Đây là đồ ăn mang về của Lý Minh phải không?", False),
    yes_no("look_an", "安娜看袋子吗？", "Ānnà kàn dàizi ma?", "Anna có nhìn túi không?", True),
    yes_no("look_lm", "李明看袋子吗？", "Lǐ Míng kàn dàizi ma?", "Lý Minh có nhìn túi không?", True),
    yes_no("guess_an", "安娜猜这是李明的吗？", "Ānnà cāi zhè shì Lǐ Míng de ma?", "Anna đoán đây là của Lý Minh phải không?", True),
    yes_no("guess_lm", "李明猜这是安娜的吗？", "Lǐ Míng cāi zhè shì Ānnà de ma?", "Lý Minh đoán đây là của Anna phải không?", True),
    yes_no("guess_ts", "安娜猜这是同事的吗？", "Ānnà cāi zhè shì tóngshì de ma?", "Anna đoán đây là của đồng nghiệp phải không?", False),
    row("who_food", "这是谁的外卖？", "zhè shì shuí de wàimài?", "Đây là đồ ăn mang về của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("who_name", "袋子上的名字是谁的？", "dàizi shàng de míngzi shì shuí de?", "Tên trên túi là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("who_bag", "袋子上有谁的名字？", "dàizi shàng yǒu shuí de míngzi?", "Trên túi có tên ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("who_table", "桌上的外卖是谁的？", "zhuō shàng de wàimài shì shuí de?", "Đồ ăn mang về trên bàn là của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5200),
    row("or_swap", "外卖是同事的还是李明的？", "wàimài shì tóngshì de háishi Lǐ Míng de?", "Đồ ăn mang về là của đồng nghiệp hay của Lý Minh?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("or_an", "这是安娜的还是同事的？", "zhè shì Ānnà de háishi tóngshì de?", "Của Anna hay của đồng nghiệp?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
    row("or_name2", "袋子上有同事的名字还是安娜的名字？", "dàizi shàng yǒu tóngshì de míngzi háishi Ānnà de míngzi?", "Trên túi có tên đồng nghiệp hay tên Anna?", "同事的名字。", "tóngshì de míngzi.", "Tên đồng nghiệp.", 5500),
]
ROUND_RETELL = [
    yes_no("food", "这是外卖吗？", "zhè shì wàimài ma?", "Đây là đồ ăn mang về phải không?", True),
    row("who", "这是谁的？", "zhè shì shuí de?", "Của ai?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5000),
    yes_no("food_ts", "外卖是同事的吗？", "wàimài shì tóngshì de ma?", "Đồ ăn mang về là của đồng nghiệp phải không?", True),
    row("or_ts", "这是同事的还是安娜的？", "zhè shì tóngshì de háishi Ānnà de?", "Của đồng nghiệp hay của Anna?", "同事的。", "tóngshì de.", "Của đồng nghiệp.", 5500),
]


def guided_segments() -> list[dict]:
    segments: list[dict] = []
    segments += pair(
        "Nghe chuyện",
        "roles",
        "我们一起听小故事吧。",
        "wǒmen yìqǐ tīng xiǎo gùshi ba.",
        "Mình cùng nghe một câu chuyện ngắn nhé.",
        500,
    )
    segments += pair(
        "Nghe chuyện",
        "voices",
        "我先用中文讲。再用越南语说意思。",
        "wǒ xiān yòng Zhōngwén jiǎng. zài yòng Yuènányǔ shuō yìsi.",
        "Mình kể bằng tiếng Trung trước. Rồi nói nghĩa bằng tiếng Việt.",
        700,
    )
    segments += pair(
        "Nghe chuyện",
        "scene_bag",
        "桌上有一袋外卖。安娜和李明看袋子。",
        "zhuō shàng yǒu yí dài wàimài. Ānnà hé Lǐ Míng kàn dàizi.",
        "Trên bàn có một túi đồ ăn mang về. Anna và Lý Minh nhìn túi.",
        700,
    )
    segments += pair(
        "Nghe chuyện",
        "listen_first",
        "我们先听。不用回答。",
        "wǒmen xiān tīng. bú yòng huídá.",
        "Mình nghe trước. Chưa cần trả lời.",
        500,
    )
    segments += story_lines("Nghe chuyện", "s1", STORY, 1000)
    segments += pair(
        "Giải nghĩa",
        "word_cue",
        "我们一起听这些词。",
        "wǒmen yìqǐ tīng zhèxiē cí.",
        "Mình cùng nghe những từ này.",
        400,
    )
    segments += pair(
        "Giải nghĩa",
        "word_lead",
        "听不懂也没关系。",
        "tīng bù dǒng yě méi guānxi.",
        "Nghe chưa hiểu cũng không sao.",
        700,
    )
    for key, text, pinyin, gloss in WORDS:
        segments += gloss_line("Giải nghĩa", f"w_{key}", "coach_zh", text, pinyin, gloss)
    segments += pair(
        "Giải nghĩa",
        "frame_words",
        "有。没有。袋子。看。不知道。",
        "yǒu. méi yǒu. dàizi. kàn. bù zhīdào.",
        "Có. Không có. Túi. Nhìn. Không biết.",
        500,
    )
    segments += pair(
        "Giải nghĩa",
        "frame_lead",
        "还有“吗”、“谁的”、“还是”。我们先听。",
        "hái yǒu “ma”, “shuí de”, “háishi”. wǒmen xiān tīng.",
        "Còn câu hỏi có hoặc không, của ai, và chọn một trong hai. Mình nghe trước.",
        700,
    )
    segments += pair(
        "Giải nghĩa",
        "map_lead",
        "我们听一句，再听意思。",
        "wǒmen tīng yí jù, zài tīng yìsi.",
        "Mình nghe một câu, rồi nghe nghĩa.",
        400,
    )
    segments += gloss_line("Giải nghĩa", "map_bag", "coach_zh", "桌上有一袋外卖。", "zhuō shàng yǒu yí dài wàimài.", "Trên bàn có một túi đồ ăn mang về.")
    segments += gloss_line("Giải nghĩa", "map_unknown", "coach_zh", "安娜和李明不知道是谁的。", "Ānnà hé Lǐ Míng bù zhīdào shì shuí de.", "Anna và Lý Minh không biết của ai.")
    for key, speaker, text, pinyin, gloss in MAPPED:
        segments += gloss_line("Giải nghĩa", key, speaker, text, pinyin, gloss)
    segments += pair(
        "Giải nghĩa",
        "s2_lead",
        "我们再听一次故事吧。",
        "wǒmen zài tīng yí cì gùshi ba.",
        "Mình nghe lại chuyện một lần nữa nhé.",
        400,
    )
    segments += story_lines("Giải nghĩa", "s2", STORY, 900)
    segments += teach(
        "Hỏi đáp",
        "sig_ma",
        "句子最后的“吗”，表示这是“是”或者“不是”的问题。",
        "jùzi zuìhòu de “ma”, biǎoshì zhè shì “shì” huòzhě “bú shì” de wèntí.",
        "Từ này ở cuối câu cho biết đây là câu hỏi có hoặc không.",
        "这是外卖吗？",
        "zhè shì wàimài ma?",
        "Đây là đồ ăn mang về phải không?",
        "是。",
        "shì.",
        "Phải.",
    )
    segments += teach(
        "Hỏi đáp",
        "sig_who",
        "“谁的”问：这是谁的？",
        "“shuí de” wèn: zhè shì shuí de?",
        "Cụm này hỏi: đây là của ai?",
        "这是谁的？",
        "zhè shì shuí de?",
        "Của ai?",
        "同事的。",
        "tóngshì de.",
        "Của đồng nghiệp.",
    )
    segments += teach(
        "Hỏi đáp",
        "sig_or",
        "“还是”，你选这一样，或者那一样。",
        "“háishi”, nǐ xuǎn zhè yí yàng, huòzhě nà yí yàng.",
        "Có từ này, bạn chọn một trong hai.",
        "这是李明的还是同事的？",
        "zhè shì Lǐ Míng de háishi tóngshì de?",
        "Của Lý Minh hay của đồng nghiệp?",
        "同事的。",
        "tóngshì de.",
        "Của đồng nghiệp.",
    )
    segments += pair(
        "Hỏi đáp",
        "ask_lead",
        "我们先听短句。",
        "wǒmen xiān tīng duǎn jù.",
        "Mình nghe câu ngắn trước nhé.",
        400,
    )
    segments += pair(
        "Hỏi đáp",
        "ask_how",
        "你答“是”，或者“不是”。",
        "nǐ dá “shì”, huòzhě “bú shì”.",
        "Bạn chỉ cần trả lời có hoặc không.",
        500,
    )
    segments += circle("Hỏi đáp", "r1", ROUND_SHORT, True, True, True)
    segments += pair(
        "Hỏi đáp",
        "r2_go",
        "我们再听短句。",
        "wǒmen zài tīng duǎn jù.",
        "Mình nghe lại câu ngắn nhé.",
        400,
    )
    segments += pair("Hỏi đáp", "r2_food", "外卖。", "wàimài.", "Đồ ăn mang về.", 300)
    segments += circle("Hỏi đáp", "r2a", ROUND_FAMILIAR[:4], True, False, True)
    segments += pair("Hỏi đáp", "r2_who", "谁的。", "shuí de.", "Của ai.", 300)
    segments += circle("Hỏi đáp", "r2b", ROUND_FAMILIAR[4:], True, False, True)
    segments += circle("Hỏi đáp", "r2b2", [ROUND_SHORT[2], ROUND_SHORT[8]], True, False, True)
    segments += pair(
        "Hỏi đáp",
        "r2_long",
        "这一轮还有越南语。",
        "zhè yì lún hái yǒu Yuènányǔ.",
        "Vòng này mình vẫn có tiếng Việt nhé.",
        400,
    )
    segments += pair("Hỏi đáp", "r2_name", "名字。", "míngzi.", "Tên.", 300)
    segments += circle("Hỏi đáp", "r2c", ROUND_BAG[:2], True, False, True)
    segments += pair(
        "Hỏi đáp",
        "r2_chunk",
        "同事的名字。李明的名字还是同事的名字。",
        "tóngshì de míngzi. Lǐ Míng de míngzi háishi tóngshì de míngzi.",
        "Tên đồng nghiệp. Tên Lý Minh hay tên đồng nghiệp.",
        400,
    )
    segments += circle("Hỏi đáp", "r2e", ROUND_BAG_OR, True, False, True)
    segments += circle("Hỏi đáp", "r2c3", ROUND_BAG[2:], True, False, True)
    segments += pair(
        "Hỏi đáp",
        "r2_or",
        "听“还是”。你选“同事的”，或者“李明的”。",
        "tīng “háishi”. nǐ xuǎn “tóngshì de”, huòzhě “Lǐ Míng de”.",
        "Nghe từ này. Bạn chọn “của đồng nghiệp”, hoặc “của Lý Minh”.",
        400,
    )
    segments += circle("Hỏi đáp", "r2f", ROUND_OR_SHORT, True, False, True)
    segments += pair(
        "Hỏi đáp",
        "s_mid_lead",
        "我们再听一次故事吧。",
        "wǒmen zài tīng yí cì gùshi ba.",
        "Mình nghe lại chuyện một lần nữa nhé.",
        400,
    )
    segments += story_lines("Hỏi đáp", "s_mid", STORY, 800)
    segments += pair(
        "Hỏi đáp",
        "r3_lead",
        "短句听中文。这一轮长句还有越南语。",
        "duǎn jù tīng Zhōngwén. zhè yì lún cháng jù hái yǒu Yuènányǔ.",
        "Câu ngắn nghe tiếng Trung. Vòng này câu dài vẫn có tiếng Việt nhé.",
        400,
    )
    segments += circle("Hỏi đáp", "r3a", ROUND_FAMILIAR + [ROUND_SHORT[2], ROUND_SHORT[8]], False, False)
    segments += pair("Hỏi đáp", "r3_bag", "袋子。名字。", "dàizi. míngzi.", "Túi. Tên.", 300)
    segments += circle("Hỏi đáp", "r3b", [ROUND_BAG[1], *ROUND_NAME_IS], True, False, True)
    segments += circle("Hỏi đáp", "r3c", ROUND_NEW, True, False, True)
    segments += pair("Hỏi đáp", "r4_who", "谁的。", "shuí de.", "Của ai.", 300)
    segments += circle("Hỏi đáp", "r4w", ROUND_WHO_LATE, True, False, True)
    segments += pair(
        "Hỏi đáp",
        "r4_or",
        "再听“还是”。你选“同事的”，或者“李明的”。",
        "zài tīng “háishi”. nǐ xuǎn “tóngshì de”, huòzhě “Lǐ Míng de”.",
        "Nghe lại từ này. Bạn chọn “của đồng nghiệp”, hoặc “của Lý Minh”.",
        400,
    )
    segments += circle("Hỏi đáp", "r4o", ROUND_OR_LATE, True, False, True)
    segments += buildup_block()
    segments += pair(
        "Đổi một chi tiết",
        "pov_cue",
        "我们再听一次吧。",
        "wǒmen zài tīng yí cì ba.",
        "Mình nghe lại một lần nữa nhé.",
        400,
    )
    segments += pair(
        "Đổi một chi tiết",
        "pov_lead",
        "这次袋子上有李明的名字。这是李明的。",
        "zhè cì dàizi shàng yǒu Lǐ Míng de míngzi. zhè shì Lǐ Míng de.",
        "Lần này trên túi có tên Lý Minh. Đây là của Lý Minh.",
        700,
    )
    segments += story_lines("Đổi một chi tiết", "pov", POV, 1000)
    segments += circle("Đổi một chi tiết", "p1", POV_CIRCLE, True, False, True)
    segments.append(
        seg(
            "pov_again",
            "Đổi một chi tiết",
            "narration",
            "zh-CN",
            "我们再听一次吧。",
            speaker="coach_zh",
            pinyin="wǒmen zài tīng yí cì ba.",
            translation="Mình nghe lại một lần nữa nhé.",
            pause_after=400,
        )
    )
    segments += story_lines("Đổi một chi tiết", "pov2", POV, 900)
    segments += circle("Đổi một chi tiết", "p2", list(reversed(POV_CIRCLE)), False, False)
    segments += pair(
        "Kể lại",
        "rt_lead",
        "我们用别的话说一次。",
        "wǒmen yòng bié de huà shuō yí cì.",
        "Mình kể lại bằng lời khác.",
        500,
    )
    for key, speaker, text, pinyin, gloss, *_rest in RETOLD:
        segments += gloss_line("Kể lại", f"rt_{key}", speaker, text, pinyin, gloss)
    segments += story_lines("Kể lại", "rt2", RETOLD, 900)
    segments += pair(
        "Kể lại",
        "k_ask",
        "这一轮还有越南语。",
        "zhè yì lún hái yǒu Yuènányǔ.",
        "Vòng này mình vẫn có tiếng Việt nhé.",
        400,
    )
    segments += circle("Kể lại", "k1", ROUND_RETELL, True, False, True)
    segments += pair(
        "Nghe lại",
        "close_cue",
        "我们再听一次故事吧。意思你知道。",
        "wǒmen zài tīng yí cì gùshi ba. yìsi nǐ zhīdào.",
        "Mình nghe lại chuyện một lần nữa. Nghĩa thì bạn đã biết.",
        400,
    )
    segments.append(
        seg(
            "close_lead",
            "Nghe lại",
            "narration",
            "zh-CN",
            "袋子上有同事的名字。这是同事的。",
            speaker="coach_zh",
            pinyin="dàizi shàng yǒu tóngshì de míngzi. zhè shì tóngshì de.",
            translation="Trên túi có tên đồng nghiệp. Đây là của đồng nghiệp.",
            pause_after=600,
        )
    )
    segments += story_lines("Nghe lại", "s3", STORY, 1000)
    segments.append(
        seg(
            "close_2",
            "Nghe lại",
            "narration",
            "zh-CN",
            "用别的话说一次。",
            speaker="coach_zh",
            pinyin="yòng bié de huà shuō yí cì.",
            translation="Kể lại bằng lời khác.",
            pause_after=400,
        )
    )
    segments += story_lines("Nghe lại", "s4", RETOLD, 1000)
    segments.append(
        seg(
            "close_3",
            "Nghe lại",
            "narration",
            "zh-CN",
            "最后再听一次。",
            speaker="coach_zh",
            pinyin="zuìhòu zài tīng yí cì.",
            translation="Lần cuối, nghe lại một lần nữa.",
            pause_after=400,
        )
    )
    segments += story_lines("Nghe lại", "s5", STORY, 1000)
    segments += pair(
        "Nghe lại",
        "end_today",
        "今天先听到这里。",
        "jīntiān xiān tīng dào zhèlǐ.",
        "Hôm nay mình nghe đến đây thôi.",
        400,
    )
    segments += pair(
        "Nghe lại",
        "end",
        "明天我们再听故事。",
        "míngtiān wǒmen zài tīng gùshi.",
        "Ngày mai mình cùng nghe lại chuyện.",
        400,
    )
    return segments


def practice_segments() -> list[dict]:
    segments = pair(
        "Nghe chuyện",
        "c1_open",
        "听故事。桌上有一袋外卖。安娜和李明不知道是谁的。",
        "tīng gùshi. zhuō shàng yǒu yí dài wàimài. Ānnà hé Lǐ Míng bù zhīdào shì shuí de.",
        "Nghe chuyện. Trên bàn có một túi đồ ăn mang về. Anna và Lý Minh không biết của ai.",
        700,
    )
    segments += story_lines("Nghe chuyện", "p1", STORY, 700)
    segments.append(seg("p2_lead", "Nghe chuyện", "narration", "zh-CN", "再听一次。", speaker="coach_zh", pinyin="zài tīng yí cì.", translation="Nghe lại lần nữa.", pause_after=400))
    segments += story_lines("Nghe chuyện", "p2", STORY, 600)
    segments.append(seg("p3_lead", "Nghe chuyện", "narration", "zh-CN", "再听一次。", speaker="coach_zh", pinyin="zài tīng yí cì.", translation="Nghe lại lần nữa.", pause_after=400))
    segments += story_lines("Nghe chuyện", "p3", STORY, 600)
    return segments


def lesson(variant: str, segments: list[dict], minutes: int, coach_phrases: list[str] | None = None) -> dict:
    return {
        "lesson_id": "lesson_001",
        "title": "Đồ ăn mang về của đồng nghiệp",
        "level": "absolute_beginner",
        "target_duration_minutes": minutes,
        "communicative_goals": [
            "Hear whose takeout it is",
            "Answer 是 and 不是",
            "Choose with 还是",
            "Answer 这是谁的",
        ],
        "prerequisites": [],
        "new_patterns": [
            "这是外卖。",
            "这是……的吗？",
            "不是。",
            "这是同事的。",
            "这是谁的？",
            "……还是……？",
            "袋子上有……的名字。",
        ],
        "review_pattern_ids": [],
        "coach_phrases_introduced": coach_phrases
        if coach_phrases is not None
        else [
            "听故事。",
            "你来回答。",
            "再听一次。",
        ],
        "coach_phrases_reduced": [],
        "version": 1,
        "variant": variant,
        "segments": segments,
    }


def estimate_seconds(segments: list[dict]) -> tuple[float, float]:
    def seconds(segment: dict, hanzi_rate: float, vi_rate: float) -> float:
        if segment["language"] == "zh-CN":
            hanzi = sum(1 for char in segment["text"] if "\u4e00" <= char <= "\u9fff")
            spoken = hanzi * hanzi_rate + 0.35
        else:
            spoken = len(segment["text"]) * vi_rate + 0.4
        return spoken / segment.get("playback_speed", 1.0) + segment.get("pause_after_ms", 0) / 1000

    brisk = sum(seconds(segment, 0.28, 0.065) for segment in segments)
    careful = sum(seconds(segment, 0.40, 0.085) for segment in segments)
    return brisk, careful


def main() -> None:
    guided_rows = guided_segments()
    brisk, careful = estimate_seconds(guided_rows)
    listening = round(brisk * 0.84 / 60)
    guided = lesson(
        "guided",
        guided_rows,
        listening,
        [
            "我们一起听小故事吧。",
            "你试着回答吧。",
            "我们再听一次故事吧。",
        ],
    )
    print(f"estimate brisk {brisk / 60:.1f} min; careful {careful / 60:.1f}; likely listening {brisk * 0.84 / 60:.1f}")
    practice = lesson("practice", practice_segments(), 5)
    for path, payload in ((GUIDED, guided), (PRACTICE, practice)):
        path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"{path.name}: {len(payload['segments'])} segments")


if __name__ == "__main__":
    main()
