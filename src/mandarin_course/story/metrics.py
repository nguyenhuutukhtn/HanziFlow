"""Advisory lesson metrics for the authoring loop."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from ..characters.resolve import _CJK
from .ledger import _chars, _lessons_before, _vocab, normalize_lesson_id

ZH_SECONDS_PER_CHAR = 0.28
VI_SECONDS_PER_CHAR = 0.065


def command_lesson_metrics(short_id: str, against: str, lessons_dir: Path, as_json: bool) -> int:
    result = compare_metrics(lessons_dir, f"lesson_{short_id}", f"lesson_{against}")
    print(json.dumps(result, ensure_ascii=False, indent=2) if as_json else render_metrics(result))
    return 0


def _lesson(lessons_dir: Path, lesson_id: str) -> dict[str, Any]:
    return json.loads((lessons_dir / f"{lesson_id}.json").read_text(encoding="utf-8"))


def _zh_segments(lesson: dict[str, Any]) -> list[dict[str, Any]]:
    return [segment for segment in lesson.get("segments", []) if segment.get("language") == "zh-CN"]


def _vi_segments(lesson: dict[str, Any]) -> list[dict[str, Any]]:
    return [segment for segment in lesson.get("segments", []) if segment.get("language") == "vi-VN"]


def _hanzi(text: str) -> str:
    return "".join(_CJK.findall(text))


def _word_occurrences(text: str, words: list[str]) -> dict[str, int]:
    return {word: text.count(word) for word in words}


def _tokenize(text: str, vocabulary: set[str]) -> list[str]:
    tokens: list[str] = []
    for run in _CJK.findall(text):
        index = 0
        while index < len(run):
            matches = [word for word in vocabulary if run.startswith(word, index)]
            if matches:
                token = max(matches, key=len)
                tokens.append(token)
                index += len(token)
            else:
                tokens.append(run[index])
                index += 1
    return tokens


def _measure(lessons_dir: Path, lesson_id: str) -> dict[str, Any]:
    lesson = _lesson(lessons_dir, lesson_id)
    vocab = [row["hanzi"] for row in _vocab(lessons_dir, lesson_id)]
    previous = _lessons_before(lessons_dir, lesson_id)
    prior_words = {row["hanzi"] for old in previous for row in _vocab(lessons_dir, old["lesson_id"])}
    prior_chars = {char for old in previous for char in _chars(_hanzi("\n".join(s.get("text", "") for s in _zh_segments(old))))}
    zh = _zh_segments(lesson)
    vi = _vi_segments(lesson)
    zh_text = "\n".join(segment.get("text", "") for segment in zh)
    new_words = [word for word in vocab if word not in prior_words]
    occurrences = _word_occurrences(zh_text, new_words)
    repeats = list(occurrences.values())
    all_words = prior_words | set(vocab)
    tokens = _tokenize(zh_text, all_words)
    unintroduced = sorted({token for token in tokens if token not in all_words})
    zh_lengths = [len(_hanzi(segment.get("text", ""))) for segment in zh]
    vi_chars = sum(len(segment.get("text", "")) for segment in vi)
    zh_chars = sum(zh_lengths)
    total_seconds = zh_chars * ZH_SECONDS_PER_CHAR + vi_chars * VI_SECONDS_PER_CHAR
    return {
        "new_words": len(new_words),
        "new_characters": len({char for char in _chars(zh_text) if char not in prior_chars}),
        "reused_words": len([word for word in vocab if word in prior_words]),
        "zh_sentence_avg_chars": round(sum(zh_lengths) / len(zh_lengths), 2) if zh_lengths else 0,
        "zh_sentence_max_chars": max(zh_lengths, default=0),
        "zh_lines": len(zh),
        "vietnamese_share": round(len(vi) / len(lesson.get("segments", [])), 4) if lesson.get("segments") else 0,
        "vietnamese_estimated_seconds": round(vi_chars * VI_SECONDS_PER_CHAR, 2),
        "repeats_min": min(repeats, default=0),
        "repeats_avg": round(sum(repeats) / len(repeats), 2) if repeats else 0,
        "low_repeat_words": [word for word, count in occurrences.items() if count < 3],
        "unintroduced_tokens": unintroduced,
        "estimated_total_seconds": round(total_seconds, 2),
    }


def _status(value: float, model: float, *, force_flag: bool = False) -> str:
    if force_flag:
        return "FLAG"
    if model == 0:
        return "ok" if value == 0 else "FLAG"
    return "ok" if abs(value - model) <= abs(model) * 0.30 else "FLAG"


def compare_metrics(lessons_dir: Path, lesson_id: str, against: str) -> dict[str, Any]:
    current = _measure(lessons_dir, lesson_id)
    model = _measure(lessons_dir, against)
    statuses = {}
    scalar_keys = [
        "new_words", "new_characters", "reused_words", "zh_sentence_avg_chars", "zh_sentence_max_chars",
        "zh_lines", "vietnamese_share", "vietnamese_estimated_seconds", "repeats_min", "repeats_avg",
        "estimated_total_seconds",
    ]
    for key in scalar_keys:
        statuses[key] = _status(current[key], model[key])
    statuses["low_repeat_words"] = _status(
        len(current["low_repeat_words"]), len(model["low_repeat_words"]),
        force_flag=lesson_id != against and bool(current["low_repeat_words"]),
    )
    statuses["unintroduced_tokens"] = _status(
        len(current["unintroduced_tokens"]), len(model["unintroduced_tokens"]),
        force_flag=bool(current["unintroduced_tokens"]),
    )
    return {"lesson_id": lesson_id, "against": against, "metrics": current, "model": model, "status": statuses}


def render_metrics(result: dict[str, Any]) -> str:
    metrics, model, status = result["metrics"], result["model"], result["status"]
    lines = [
        f"new words: {metrics['new_words']} · model {model['new_words']} · {status['new_words']}",
        f"new characters: {metrics['new_characters']} · model {model['new_characters']} · {status['new_characters']}",
        f"reused words: {metrics['reused_words']} · model {model['reused_words']} · {status['reused_words']}",
        f"zh sentence chars avg/max: {metrics['zh_sentence_avg_chars']}/{metrics['zh_sentence_max_chars']} · model {model['zh_sentence_avg_chars']}/{model['zh_sentence_max_chars']} · {status['zh_sentence_avg_chars']}",
        f"zh lines: {metrics['zh_lines']} · model {model['zh_lines']} · {status['zh_lines']}",
        f"Vietnamese share: {metrics['vietnamese_share']:.1%} ({metrics['vietnamese_estimated_seconds']}s) · model {model['vietnamese_share']:.1%} ({model['vietnamese_estimated_seconds']}s) · {status['vietnamese_share']}",
        f"repeats min/avg: {metrics['repeats_min']}/{metrics['repeats_avg']} · model {model['repeats_min']}/{model['repeats_avg']} · {status['repeats_min']}",
        f"words heard < 3: {', '.join(metrics['low_repeat_words']) or 'none'} · model {len(model['low_repeat_words'])} · {status['low_repeat_words']}",
        f"unintroduced zh tokens: {', '.join(metrics['unintroduced_tokens']) or 'none'} · model {len(model['unintroduced_tokens'])} · {status['unintroduced_tokens']}",
        f"estimated total duration: {metrics['estimated_total_seconds']}s · model {model['estimated_total_seconds']}s · {status['estimated_total_seconds']}",
        f"rates: zh {ZH_SECONDS_PER_CHAR}s/char; Vietnamese {VI_SECONDS_PER_CHAR}s/char",
    ]
    return "\n".join(lines) + "\n"
