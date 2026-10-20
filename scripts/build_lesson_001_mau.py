"""Build the Lesson 1 template script. Review artifact, not the runtime."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "lessons" / "lesson_001_mau.json"

PRAISE = ("Đúng rồi.", "Đúng.", "Hay.")


def line(key, speaker, text, pinyin, gloss, say_ms, answer_ms):
    return {
        "key": key,
        "speaker": speaker,
        "text": text,
        "pinyin": pinyin,
        "gloss": gloss,
        "say_ms": say_ms,
        "answer_ms": answer_ms,
        "steps": None,
    }


def hard(key, speaker, text, pinyin, gloss, say_ms, answer_ms, steps):
    item = line(key, speaker, text, pinyin, gloss, say_ms, answer_ms)
    item["steps"] = steps
    return item


S1 = [
    line("nihao_lm", "li_ming", "你好！", "nǐ hǎo!", "Xin chào.", 1600, 1600),
    line("nihao_an", "anna", "你好！", "nǐ hǎo!", "Xin chào.", 1600, 1600),
    hard(
        "jiao",
        "li_ming",
        "你叫什么？",
        "nǐ jiào shénme?",
        "Bạn tên gì?",
        2500,
        2800,
        [
            ("shenme", "什么", "shénme", "Gì?"),
            ("jiao_shenme", "叫什么", "jiào shénme", "Tên gì?"),
        ],
    ),
    hard(
        "anna_ne",
        "anna",
        "我叫安娜。你呢？",
        "wǒ jiào Ānnà. nǐ ne?",
        "Mình tên Anna. Còn bạn?",
        3000,
        3000,
        [
            ("ni_ne", "你呢", "nǐ ne", "Còn bạn?"),
        ],
    ),
    line("liming", "li_ming", "我叫李明。", "wǒ jiào Lǐ Míng.", "Mình tên Lý Minh.", 2200, 2200),
    hard(
        "gaoxing",
        "anna",
        "很高兴认识你。",
        "hěn gāoxìng rènshi nǐ.",
        "Rất vui được gặp bạn.",
        2800,
        2600,
        [
            ("renshi", "认识你", "rènshi nǐ", "Gặp bạn."),
        ],
    ),
    line("ye_shi", "li_ming", "我也是。", "wǒ yě shì.", "Mình cũng vậy.", 1800, 1800),
    line("bye_an", "anna", "再见！", "zàijiàn!", "Tạm biệt.", 1600, 1600),
    line("bye_lm", "li_ming", "再见！", "zàijiàn!", "Tạm biệt.", 1600, 1600),
]

S2 = [
    line("zao_lm", "anna", "早，李明！", "zǎo, Lǐ Míng!", "Chào buổi sáng, Lý Minh.", 1800, 1800),
    hard(
        "zuijin",
        "li_ming",
        "早，安娜！最近怎么样？",
        "zǎo, Ānnà! zuìjìn zěnmeyàng?",
        "Chào buổi sáng, Anna. Dạo này thế nào?",
        3200,
        3000,
        [
            ("zenme", "怎么样", "zěnmeyàng", "Thế nào?"),
            ("zuijin_zenme", "最近怎么样", "zuìjìn zěnmeyàng", "Dạo này thế nào?"),
        ],
    ),
    line("tinghao", "anna", "挺好的。你呢？", "tǐng hǎo de. nǐ ne?", "Khá ổn. Còn bạn?", 2400, 2500),
    line("bucuo", "li_ming", "也不错。", "yě bú cuò.", "Cũng không tệ.", 1800, 1800),
    hard(
        "xian_zou",
        "anna",
        "那我先走了。再见！",
        "nà wǒ xiān zǒu le. zàijiàn!",
        "Mình đi trước đây. Tạm biệt.",
        2800,
        2200,
        [
            ("wo_zou", "我先走了", "wǒ xiān zǒu le", "Mình đi trước."),
            ("na_wo_zou", "那我先走了", "nà wǒ xiān zǒu le", "Vậy mình đi trước."),
        ],
    ),
    line("bye_lm_2", "li_ming", "再见！", "zàijiàn!", "Tạm biệt.", 1600, 1600),
]

# Question, answer key, include a short meaning after the model answer.
QA1 = [
    ("nihao_lm", "nihao_an", False),
    ("jiao", "anna_ne", True),
    ("anna_ne", "liming", True),
    ("gaoxing", "ye_shi", True),
    ("bye_an", "bye_lm", False),
]
QA2 = [
    ("zao_lm", "zuijin", True),
    ("zuijin_q", "tinghao", True),
    ("tinghao", "bucuo", True),
    ("xian_zou", "bye_lm_2", True),
]

ZUIJIN_Q = line(
    "zuijin_q",
    "li_ming",
    "最近怎么样？",
    "zuìjìn zěnmeyàng?",
    "Dạo này thế nào?",
    2500,
    2500,
)
WO_JIAO = line("wo_jiao", "anna", "我叫安娜。", "wǒ jiào Ānnà.", "Mình tên Anna.", 2200, 2200)
NI_NE = line("ni_ne_q", "anna", "你呢？", "nǐ ne?", "Còn bạn?", 1600, 1600)
TING_HAO = line("ting_hao", "anna", "挺好的。", "tǐng hǎo de.", "Khá ổn.", 1800, 1800)
NA_ZOU = line("na_zou", "anna", "那我先走了。", "nà wǒ xiān zǒu le.", "Vậy mình đi trước.", 2200, 2200)

BY_KEY = {item["key"]: item for item in S1 + S2 + [ZUIJIN_Q, WO_JIAO, NI_NE, TING_HAO, NA_ZOU]}


class Builder:
    def __init__(self):
        self.segments: list[dict] = []
        self.counts: dict[str, int] = {}

    def add(self, segment: dict) -> None:
        self.segments.append(segment)

    def ident(self, prefix: str) -> str:
        self.counts[prefix] = self.counts.get(prefix, 0) + 1
        return f"{prefix}_{self.counts[prefix]:03d}"

    def coach(self, activity: str, text: str, pause: int = 800) -> None:
        self.add({
            "id": self.ident("vi"),
            "activity": activity,
            "type": "narration",
            "language": "vi-VN",
            "speaker": "coach",
            "text": text,
            "pause_after_ms": pause,
        })

    def chinese(
        self,
        activity: str,
        item: dict,
        pause: int,
        speed: float | None = None,
        kind: str = "dialogue",
        expects: str | None = None,
        text: str | None = None,
        pinyin: str | None = None,
        gloss: str | None = None,
        code_switch: bool = False,
    ) -> str:
        segment_id = self.ident("zh")
        segment = {
            "id": segment_id,
            "activity": activity,
            "type": kind,
            "language": "zh-CN",
            "speaker": item["speaker"],
            "text": item["text"] if text is None else text,
            "pinyin": item["pinyin"] if pinyin is None else pinyin,
            "translation": item["gloss"] if gloss is None else gloss,
            "pause_after_ms": pause,
        }
        if speed is not None:
            segment["playback_speed"] = speed
        metadata = {}
        if expects:
            metadata["expects"] = expects
        if code_switch:
            metadata["code_switch"] = True
        if metadata:
            segment["metadata"] = metadata
        self.add(segment)
        return segment_id

    def first_hearing(self, activity: str, item: dict, lead: str, follow: str) -> None:
        self.coach(activity, lead, 280)
        self.chinese(activity, item, 400, code_switch=True)
        self.coach(activity, follow, 280)
        self.chinese(activity, item, 800, code_switch=True)

    def remind(self, activity: str, lead: str, item: dict) -> None:
        self.coach(activity, lead, 280)
        self.chinese(activity, item, 700, code_switch=True)

    def scene(self, activity: str, items: list[dict], kind: str, pause: int) -> None:
        turns = [{"speaker": item["speaker"], "text": item["text"]} for item in items]
        self.add({
            "id": self.ident("zh"),
            "activity": activity,
            "type": kind,
            "language": "zh-CN",
            "text": "".join(item["text"] for item in items),
            "pinyin": " ".join(item["pinyin"] for item in items),
            "translation": " ".join(item["gloss"] for item in items),
            "pause_after_ms": pause,
            "turns": turns,
        })

    def meaning_line(
        self,
        activity: str,
        item: dict,
        imitate_ms: int,
        imitate_kind: str = "dialogue",
    ) -> None:
        self.chinese(activity, item, 400)
        self.coach(activity, item["gloss"], 400)
        self.chinese(activity, item, imitate_ms + 500, kind=imitate_kind)

    def build(self) -> list[dict]:
        self.whole_scenes()
        self.line_by_line(
            "Từng câu",
            "Từng câu nhé. Nghe tiếng Trung, nghe nghĩa, rồi nhắc lại.",
            gloss=True,
            cue="Buổi sáng vài ngày sau, trước giờ học.",
        )
        self.line_by_line(
            "Từng câu lần nữa",
            "Lại từng câu nhé. Lần này không dịch nữa. Nhắc trong khoảng lặng.",
            gloss=False,
            cue="Buổi sáng vài ngày sau.",
        )
        self.line_by_line(
            "Từng câu lần ba",
            "Lần nữa nhé. Chỉ tiếng Trung. Thử nói xem.",
            gloss=False,
            cue="Buổi sáng vài ngày sau.",
        )
        self.both_scenes("Từng câu lần ba")
        self.review_and_build()
        self.questions("Hỏi đáp", 1.0, "Đến lượt bạn. Nghe câu hỏi, trả lời, rồi nghe mẫu.", gloss=True)
        self.questions("Hỏi đáp lần nữa", 0.85, "Hỏi lại nhé. Trả lời trước khi nghe mẫu.", gloss=False)
        self.questions("Hỏi đáp lần ba", 0.75, "Thử nói xem. Trả lời, rồi nghe mẫu.", gloss=False)
        self.natural_ending()
        return self.segments

    def whole_scenes(self) -> None:
        activity = "Nghe cả cuộc"
        self.coach(activity, "Anna gặp Lý Minh. Hai người cùng lớp, gặp nhau lần đầu. Nghe cả cuộc nhé.")
        self.scene(activity, S1, "dialogue", 1800)
        self.explain_scene_one(activity)
        self.coach(activity, "Nghe lại lần nữa nhé.")
        self.scene(activity, S1, "context", 1800)
        self.coach(activity, "Vài ngày sau, Anna và Lý Minh gặp thoáng qua trước giờ học. Nghe tiếp nhé.")
        self.scene(activity, S2, "dialogue", 1800)
        self.explain_scene_two(activity)
        self.coach(activity, "Nghe lại lần nữa nhé.")
        self.scene(activity, S2, "context", 1800)

    def explain_scene_one(self, activity: str) -> None:
        self.first_hearing(
            activity,
            BY_KEY["jiao"],
            "Muốn hỏi tên một người bạn, bạn nói:",
            "Nghĩa là bạn tên gì. Nghe lại nhé:",
        )
        self.first_hearing(
            activity,
            WO_JIAO,
            "Đến lượt bạn giới thiệu tên mình:",
            "Mình tên Anna. Bạn có thể thay Anna bằng tên của mình. Nghe lại:",
        )
        self.first_hearing(
            activity,
            NI_NE,
            "Muốn hỏi lại còn bạn, chỉ cần nói:",
            "Nghe lại nhé:",
        )
        self.first_hearing(
            activity,
            BY_KEY["gaoxing"],
            "Khi vừa gặp ai đó, bạn có thể nói:",
            "Rất vui được gặp bạn. Nghe lại nhé:",
        )
        self.first_hearing(
            activity,
            BY_KEY["bye_an"],
            "Khi muốn chia tay, bạn nói:",
            "Tạm biệt. Nghe lại nhé:",
        )

    def explain_scene_two(self, activity: str) -> None:
        self.first_hearing(
            activity,
            ZUIJIN_Q,
            "Muốn hỏi dạo này thế nào, bạn nói:",
            "Dạo này thế nào. Nghe lại nhé:",
        )
        self.first_hearing(
            activity,
            TING_HAO,
            "Nếu ai hỏi thăm dạo này, bạn có thể trả lời:",
            "Khá ổn. Nghe lại nhé:",
        )
        self.first_hearing(
            activity,
            NA_ZOU,
            "Khi bạn cần đi trước, nói:",
            "Vậy mình đi trước nhé. Nghe lại:",
        )

    def line_by_line(self, activity: str, lead: str, gloss: bool, cue: str) -> None:
        self.coach(activity, lead)
        for item in S1:
            if gloss:
                self.meaning_line(activity, item, item["say_ms"])
            else:
                self.chinese(activity, item, item["say_ms"])
        self.remind(activity, cue, ZUIJIN_Q)
        for item in S2:
            if gloss:
                self.meaning_line(activity, item, item["say_ms"])
            else:
                self.chinese(activity, item, item["say_ms"])

    def review_and_build(self) -> None:
        activity = "Ôn và ráp câu"
        self.coach(
            activity,
            "Nghe lại nhé. Câu ngắn thì nói cả câu. Câu khó thì nghe từ cuối lên, rồi ghép lại.",
        )
        self.review_scene(activity, S1, "pattern", gloss_steps=True)
        self.remind(activity, "Buổi sáng vài ngày sau. Câu khó vẫn nghe từ cuối lên nhé.", ZUIJIN_Q)
        self.review_scene(activity, S2, "pattern", gloss_steps=True)
        activity = "Ôn và ráp câu lần nữa"
        self.coach(activity, "Ráp lại từ đầu nhé.")
        self.review_scene(activity, S1, "dialogue", gloss_steps=False)
        self.remind(activity, "Buổi sáng vài ngày sau.", ZUIJIN_Q)
        self.review_scene(activity, S2, "dialogue", gloss_steps=False)
        activity = "Ôn và ráp câu lần ba"
        self.coach(activity, "Ráp lại lần nữa nhé.")
        self.review_scene(activity, S1, "dialogue", gloss_steps=False)
        self.remind(activity, "Buổi sáng vài ngày sau.", ZUIJIN_Q)
        self.review_scene(activity, S2, "dialogue", gloss_steps=False)
        self.both_scenes(activity)

    def both_scenes(self, activity: str) -> None:
        self.coach(activity, "Nghe lại cả cuộc nhé, cho quen tai.")
        self.scene(activity, S1, "context", 1400)
        self.scene(activity, S2, "context", 1400)

    def review_scene(self, activity: str, items: list[dict], kind: str, gloss_steps: bool) -> None:
        for item in items:
            if item["steps"]:
                self.build_hard(activity, item, kind, gloss_steps)
            elif gloss_steps:
                self.meaning_line(activity, item, item["say_ms"], imitate_kind=kind)
            else:
                self.chinese(activity, item, item["say_ms"], kind=kind)

    def build_hard(self, activity: str, item: dict, kind: str, gloss_steps: bool) -> None:
        for _step_key, text, pinyin, gloss in item["steps"]:
            self.chinese(activity, item, 300, text=text, pinyin=pinyin, gloss=gloss)
            if gloss_steps:
                self.coach(activity, gloss, 300)
            self.chinese(activity, item, 2000, text=text, pinyin=pinyin, gloss=gloss)
        self.chinese(activity, item, item["say_ms"], speed=0.85, kind=kind)
        if gloss_steps:
            self.coach(activity, item["gloss"], 300)
        self.chinese(activity, item, 1200)

    def questions(self, activity: str, pause_scale: float, intro: str, gloss: bool) -> None:
        self.coach(activity, intro)
        if gloss:
            self.remind(activity, "Khi được hỏi tên, bạn dùng tên của mình nhé.", BY_KEY["jiao"])
            self.ask_scene(activity, QA1, pause_scale, gloss=True)
            self.remind(activity, "Buổi sáng vài ngày sau nhé.", ZUIJIN_Q)
        else:
            self.ask_scene(activity, QA1, pause_scale, gloss=False)
            self.remind(activity, "Buổi sáng vài ngày sau.", ZUIJIN_Q)
        self.ask_scene(activity, QA2, pause_scale, gloss=gloss)

    def ask_scene(
        self,
        activity: str,
        pairs: list[tuple[str, str, bool]],
        pause_scale: float,
        gloss: bool,
    ) -> None:
        for index, (question_key, answer_key, with_meaning) in enumerate(pairs):
            question = BY_KEY[question_key]
            answer = BY_KEY[answer_key]
            answer_id = self.ident("zh")
            pause = max(1500, int(question["answer_ms"] * pause_scale))
            self.chinese(activity, question, pause, kind="prompt", expects=answer_id)
            self.add({
                "id": answer_id,
                "activity": activity,
                "type": "model_answer",
                "language": "zh-CN",
                "speaker": answer["speaker"],
                "text": answer["text"],
                "pinyin": answer["pinyin"],
                "translation": answer["gloss"],
                "pause_after_ms": 500,
            })
            if gloss and answer["steps"]:
                self.coach(activity, PRAISE[index % len(PRAISE)], 400)
            if with_meaning and gloss:
                self.coach(activity, f"{question['gloss']} {answer['gloss']}", 400)
            if answer["steps"]:
                self.chinese(activity, answer, answer["say_ms"], speed=0.85)
            self.chinese(activity, answer, 900)
            self.chinese(activity, question, 500)
            self.chinese(activity, answer, 1000)

    def natural_ending(self) -> None:
        activity = "Nghe lại"
        self.coach(activity, "Nghe lại cả hai cuộc nhé. Lần này chỉ tiếng Trung.")
        for _ in range(3):
            self.scene(activity, S1, "context", 1600)
            self.scene(activity, S2, "context", 1600)
        self.coach(activity, "Hết bài. Ngày mai nghe lại chính bài này nhé.", 400)
        self.chinese(activity, BY_KEY["bye_an"], 800, code_switch=True)


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
    segments = Builder().build()
    lesson = {
        "lesson_id": "lesson_001",
        "title": "Greetings and Introductions",
        "level": "absolute_beginner",
        "target_duration_minutes": 26,
        "communicative_goals": [
            "Greet someone you just met",
            "Ask their name and say yours",
            "Say you are glad to meet them",
            "A few days later, ask how they have been and answer",
            "Say you are heading off, and say goodbye",
        ],
        "prerequisites": [],
        "new_patterns": [
            "你好",
            "你叫什么？",
            "我叫…",
            "你呢？",
            "很高兴认识你。",
            "我也是。",
            "再见",
            "早",
            "最近怎么样？",
            "挺好的。",
            "也不错。",
            "那我先走了。",
        ],
        "review_pattern_ids": [],
        "coach_phrases_introduced": [
            "你叫什么？",
            "我叫…",
            "你呢？",
            "很高兴认识你。",
            "再见",
            "最近怎么样？",
            "挺好的。",
            "那我先走了。",
        ],
        "coach_phrases_reduced": [
            "你叫什么？",
            "最近怎么样？",
            "再见",
        ],
        "version": 1,
        "variant": "guided",
        "segments": segments,
    }
    OUT.write_text(json.dumps(lesson, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    brisk, careful = estimate_seconds(segments)
    print(f"segments {len(segments)}")
    print(f"estimate brisk {brisk / 60:.1f} min, careful {careful / 60:.1f} min")


if __name__ == "__main__":
    main()
