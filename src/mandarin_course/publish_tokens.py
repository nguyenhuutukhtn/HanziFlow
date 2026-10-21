from __future__ import annotations

import hashlib
import re
import unicodedata
from pathlib import Path
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, model_validator
from pypinyin import Style, lazy_pinyin

from .publish_timeline import PublishError


class VocabWord(BaseModel):
    model_config = ConfigDict(extra="forbid")
    hanzi: str = Field(min_length=1)
    pinyin: str = Field(min_length=1)
    meaning_vi: str = Field(min_length=1)


class VocabSidecar(BaseModel):
    model_config = ConfigDict(extra="forbid")
    schema_version: Literal[1]
    lesson_id: str = Field(pattern=r"^lesson_[0-9]{3}$")
    status: Literal["draft", "reviewed"]
    words: list[VocabWord]

    @model_validator(mode="after")
    def unique_words(self) -> "VocabSidecar":
        values = [word.hanzi for word in self.words]
        if len(values) != len(set(values)):
            raise ValueError("vocabulary words must have unique hanzi")
        return self


def load_vocab(path: Path, lesson_id: str) -> VocabSidecar:
    try:
        vocab = VocabSidecar.model_validate_json(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exc:
        raise PublishError(f"Vocabulary sidecar is missing: {path}") from exc
    if vocab.lesson_id != lesson_id:
        raise PublishError(
            f"Vocabulary sidecar lesson_id {vocab.lesson_id!r} does not match {lesson_id!r}"
        )
    return vocab


_PINYIN_WORD = re.compile(r"[A-Za-z\u00c0-\u024f\u1e00-\u1eff\u0300-\u036f\u00fc\u00dc:]+[1-5]?")
_HANZI = re.compile(r"[\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff]")
_TONE_MARKS = {
    **{char: 1 for char in "āēīōūǖĀĒĪŌŪǕ"},
    **{char: 2 for char in "áéíóúǘÁÉÍÓÚǗ"},
    **{char: 3 for char in "ǎěǐǒǔǚǍĚǏǑǓǙ"},
    **{char: 4 for char in "àèìòùǜÀÈÌÒÙǛ"},
}


def _plain_pinyin(value: str) -> str:
    value = value.replace("u:", "ü").replace("U:", "Ü")
    decomposed = unicodedata.normalize("NFD", value)
    return "".join(char for char in decomposed if not unicodedata.combining(char)).lower().replace("ü", "v")


def _hanzi_syllables(text: str) -> list[str]:
    chars = [char for char in text if _HANZI.fullmatch(char)]
    return lazy_pinyin(chars, style=Style.NORMAL, strict=False)


def _pinyin_words(pinyin: str) -> list[str]:
    return _PINYIN_WORD.findall(pinyin)


def tokenize_mandarin(text: str, pinyin: str, item_ids: dict[str, str] | None = None) -> list[dict[str, str]]:
    """Align author-separated pinyin words to Hanzi spans; never guess boundaries."""
    item_ids = item_ids or {}
    words = _pinyin_words(pinyin)
    chars = list(text)
    hanzi_positions = [index for index, char in enumerate(chars) if _HANZI.fullmatch(char)]
    syllables = _hanzi_syllables(text)
    if len(syllables) != len(hanzi_positions):
        raise ValueError("pypinyin could not produce one syllable per Hanzi character")

    spans: list[tuple[int, int, str]] = []
    syllable_at = 0
    for word in words:
        target = _plain_pinyin(word.rstrip("12345"))
        joined = ""
        start = syllable_at
        while syllable_at < len(syllables) and len(joined) < len(target):
            joined += _plain_pinyin(syllables[syllable_at])
            syllable_at += 1
        if joined != target:
            raise ValueError(f"{word!r} does not align at Hanzi syllable {start + 1}")
        spans.append((start, syllable_at, word))
    if syllable_at != len(syllables):
        raise ValueError(f"pinyin covers {syllable_at} of {len(syllables)} Hanzi syllables")

    tokens: list[dict[str, str]] = []
    char_at = 0
    for start, end, word in spans:
        first = hanzi_positions[start]
        last = hanzi_positions[end - 1]
        while char_at < first:
            if not chars[char_at].isspace():
                tokens.append({"hanzi": chars[char_at]})
            char_at += 1
        hanzi = "".join(char for char in chars[first : last + 1] if _HANZI.fullmatch(char))
        token = {"hanzi": hanzi, "pinyin": word}
        if hanzi in item_ids:
            token["item_id"] = item_ids[hanzi]
        tokens.append(token)
        char_at = last + 1
    while char_at < len(chars):
        if not chars[char_at].isspace():
            tokens.append({"hanzi": chars[char_at]})
        char_at += 1
    return tokens


def tones_from_pinyin(hanzi: str, pinyin: str) -> list[int]:
    tokens = tokenize_mandarin(hanzi, pinyin)
    tones: list[int] = []
    for token in tokens:
        authored = token.get("pinyin")
        if not authored:
            continue
        source = authored.rstrip("12345")
        explicit = int(authored[-1]) if authored[-1:].isdigit() else None
        generated = _hanzi_syllables(token["hanzi"])
        offset = 0
        plain = _plain_pinyin(source)
        for index, syllable in enumerate(generated):
            width = len(_plain_pinyin(syllable))
            piece = source[offset : offset + width]
            offset += width
            marked = next((_TONE_MARKS[c] for c in piece if c in _TONE_MARKS), None)
            tones.append(marked or (explicit if index == len(generated) - 1 else None) or 5)
        if offset != len(plain):
            raise ValueError(f"cannot extract tones from {pinyin!r}")
    return tones


def numbered_pinyin(hanzi: str, pinyin: str) -> str:
    """Canonical tone-number pinyin used in cross-lesson stable item IDs."""
    tokens = tokenize_mandarin(hanzi, pinyin)
    numbered: list[str] = []
    for token in tokens:
        authored = token.get("pinyin")
        if not authored:
            continue
        syllables = _hanzi_syllables(token["hanzi"])
        tones = tones_from_pinyin(token["hanzi"], authored)
        numbered.extend(
            f"{_plain_pinyin(syllable)}{tone}"
            for syllable, tone in zip(syllables, tones, strict=True)
        )
    return "".join(numbered)


def word_item_id(hanzi: str, pinyin: str) -> str:
    return f"w_{hanzi}_{numbered_pinyin(hanzi, pinyin)}"


def sentence_item_id(text: str) -> str:
    digest = hashlib.sha256(text.encode("utf-8")).hexdigest()[:12]
    return f"s_{digest}"


def _strip_punctuation(text: str) -> str:
    return "".join(char for char in text if _HANZI.fullmatch(char))
