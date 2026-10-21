"""Resolve lesson characters from loaded offline data."""

from __future__ import annotations

import re
import unicodedata
from collections.abc import Mapping
from typing import Any

from pypinyin import Style, pinyin as to_pinyin

_CJK = re.compile(r"[一-鿿㐀-䶿]+")
_IDS_OPERATORS = set("⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻")
_TONE_MARKS = {
    1: "āēīōūǖ", 2: "áéíóúǘ", 3: "ǎěǐǒǔǚ", 4: "àèìòùǜ",
}


def _get(loaders: Any, name: str) -> Any:
    value = loaders.get(name, {}) if isinstance(loaders, Mapping) else getattr(loaders, name, {})
    return value.load() if hasattr(value, "load") else value


def _record(source: Any, char: str) -> dict[str, Any]:
    if isinstance(source, Mapping):
        value = source.get(char, {})
    else:
        value = {}
    return value if isinstance(value, Mapping) else {}


def _plain(value: str) -> str:
    value = unicodedata.normalize("NFD", re.sub(r"[1-5]", "", value).replace("u:", "ü").lower())
    return "".join(char for char in value if not unicodedata.combining(char))


def _tone_mark(value: str) -> str:
    match = re.fullmatch(r"([a-zü:]+)([1-5])", value.lower())
    if not match:
        return value
    syllable, tone = match.groups()
    if tone == "5":
        return syllable.replace("u:", "ü")
    if "a" in syllable or "e" in syllable:
        index = next(i for i, vowel in enumerate(syllable) if vowel in "ae")
    elif "ou" in syllable:
        index = syllable.index("o")
    else:
        index = max(i for i, vowel in enumerate(syllable) if vowel in "aeiouü")
    marked = _TONE_MARKS[int(tone)]["aeiouü".index(syllable[index])]
    return syllable[:index] + marked + syllable[index + 1:]


def _cedict_candidates(value: Any) -> list[Mapping[str, str]]:
    if not isinstance(value, Mapping):
        return []
    candidates = value.get("candidates")
    if isinstance(candidates, list):
        return [candidate for candidate in candidates if isinstance(candidate, Mapping)]
    return [value]


def _pick_cedict(value: Any, pinyin_hint: str | None) -> dict[str, str]:
    candidates = _cedict_candidates(value)
    if not candidates:
        return {"pinyin": "", "en_meaning": ""}
    lowercase = [candidate for candidate in candidates if not candidate.get("pinyin", "")[:1].isupper()]
    if lowercase:
        candidates = lowercase
    if pinyin_hint:
        matching = [candidate for candidate in candidates if _plain(candidate.get("pinyin", "")) == _plain(pinyin_hint)]
        if matching:
            candidates = matching
    usable = [candidate for candidate in candidates if not re.search(r"(?:^|[ (;])(?:variant of|old variant|archaic)", candidate.get("raw_meaning", "").lower())]
    if usable:
        candidates = usable
    chosen = candidates[0]
    return {"pinyin": _tone_mark(chosen.get("pinyin", "")), "en_meaning": chosen.get("en_meaning", "")}


def _ids_components(ids: str) -> list[str]:
    if not ids or ids[0] not in _IDS_OPERATORS:
        return []
    index = 1
    result = []
    for _ in range(2):
        if index >= len(ids):
            break
        if ids[index] in _IDS_OPERATORS:
            nested = ids[index:]
            nested_components = _ids_components(nested)
            result.append(nested_components[0] if nested_components else "")
            index += 1 + len(nested_components)
        else:
            result.append(ids[index])
            index += 1
    return [component for component in result if component]


def resolve_char(char: str, loaders: Any, pinyin_hint: str | None = None) -> dict[str, Any]:
    mmah = _record(_get(loaders, "mmah"), char)
    cedict = _pick_cedict(_record(_get(loaders, "cedict"), char), pinyin_hint)
    unihan = _record(_get(loaders, "unihan"), char)
    hsk = _get(loaders, "hsk30")
    strokes = _get(loaders, "stroke")
    authored = _record(_get(loaders, "characters_vi"), char)
    pinyin = cedict.get("pinyin", "")
    return {
        "char": char,
        "pinyin": pinyin,
        "han_viet": authored.get("han_viet"),
        "hsk_level": hsk.get(char),
        "decomp_ids": mmah.get("decomp_ids", ""),
        "semantic": mmah.get("semantic"),
        "phonetic": mmah.get("phonetic"),
        "en_meaning": cedict.get("en_meaning", "") if isinstance(cedict, Mapping) else "",
        "vi_meaning": authored.get("vi_meaning"),
        "phonetic_family": sorted(c for c, value in _get(loaders, "mmah").items() if _record(_get(loaders, "mmah"), c).get("phonetic") == mmah.get("phonetic")) if mmah.get("phonetic") else [],
        "stroke_available": char in strokes,
        "lesson_words": [],
    }


def _lesson_segments(lesson_json: Any) -> list[Any]:
    segments = lesson_json.get("segments", []) if isinstance(lesson_json, Mapping) else lesson_json.segments
    result = []
    for segment in segments:
        language = segment.get("language") if isinstance(segment, Mapping) else segment.language
        if language != "zh-CN":
            continue
        turns = segment.get("turns", []) if isinstance(segment, Mapping) else segment.turns
        if turns:
            result.extend(turns)
        else:
            result.append(segment)
    return result


def _vocab_words(vocab: Any) -> list[str]:
    if vocab is None:
        return []
    rows = vocab.get("words", []) if isinstance(vocab, Mapping) else vocab
    result = []
    for row in rows:
        word = row.get("hanzi") if isinstance(row, Mapping) else getattr(row, "hanzi", None)
        if word and word not in result:
            result.append(word)
    return result


def resolve_lesson_characters(
    lesson_json: Any, seen_chars: set[str], loaders: Any, vocab: Any = None
) -> dict[str, Any]:
    vocab_words = _vocab_words(vocab)
    pinyin_hints: dict[str, str] = {}
    for word in vocab_words:
        source = next((row for row in (vocab.get("words", []) if isinstance(vocab, Mapping) else vocab) if (row.get("hanzi") if isinstance(row, Mapping) else getattr(row, "hanzi", "")) == word), None)
        authored_pinyin = source.get("pinyin", "") if isinstance(source, Mapping) else getattr(source, "pinyin", "")
        syllables = to_pinyin(word, style=Style.TONE, strict=False)
        for char, syllable in zip(word, syllables):
            pinyin_hints.setdefault(char, syllable[0] if syllable else authored_pinyin)
    chars: list[str] = []
    for segment in _lesson_segments(lesson_json):
        text = segment.get("text", "") if isinstance(segment, Mapping) else segment.text
        for word in _CJK.findall(text):
            for char in word:
                if char not in chars:
                    chars.append(char)
    new_chars = [char for char in chars if char not in seen_chars]
    records = []
    for char in new_chars:
        record = resolve_char(char, loaders, pinyin_hints.get(char))
        record["lesson_words"] = [word for word in vocab_words if char in word]
        records.append(record)
    new_set = set(new_chars)
    prereqs = []
    prereq_chars = set()
    for record in records:
        roles = [(role, record[role]) for role in ("semantic", "phonetic") if record[role]]
        if not roles:
            roles = [("component", component) for component in _ids_components(record["decomp_ids"])]
        for role, component in roles:
            if (
                not component
                or component in seen_chars
                or component in new_set
                or component in prereq_chars
                or component == "？"
                or component in _IDS_OPERATORS
            ):
                continue
            prereq_chars.add(component)
            info = _record(_get(loaders, "radicals"), component)
            han_viet = _record(_get(loaders, "unihan"), component).get("han_viet")
            prereqs.append({
                "char": component,
                "role": role,
                "name_en": info.get("name_en", ""),
                "name_vi": info.get("name_vi", "") or han_viet or component,
            })
    seen_chars.update(new_set)
    return {"lesson_id": lesson_json.get("lesson_id") if isinstance(lesson_json, Mapping) else lesson_json.lesson_id, "characters": records, "components_prereq": prereqs}
