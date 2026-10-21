from __future__ import annotations

import json
from pathlib import Path

from mandarin_course.characters.resolve import resolve_char, resolve_lesson_characters
from mandarin_course.characters.data import Hsk30Loader
from mandarin_course.characters.bundle import _reply_rows


def _loaders():
    return {
        "mmah": {
            "妈": {"decomp_ids": "⿰女马", "semantic": "女", "phonetic": "马"},
            "吗": {"decomp_ids": "⿰口马", "semantic": "口", "phonetic": "马"},
        },
        "unihan": {"妈": {"han_viet": "mã"}},
        "hsk30": {"妈": 1},
        "cedict": {"妈": {"pinyin": "mā", "en_meaning": "mother, mama"}},
        "stroke": {"妈"},
        "characters_vi": {"妈": {"han_viet": "mã", "vi_meaning": None}},
        "radicals": {
            "女": {"name_en": "woman", "name_vi": "nữ"},
            "口": {"name_en": "mouth", "name_vi": "khẩu"},
        },
    }


def test_resolve_char_builds_schema_record():
    record = resolve_char("妈", _loaders())
    assert record == {
        "char": "妈",
        "pinyin": "mā",
        "han_viet": "mã",
        "hsk_level": 1,
        "decomp_ids": "⿰女马",
        "semantic": "女",
        "phonetic": "马",
        "en_meaning": "mother, mama",
        "vi_meaning": None,
        "phonetic_family": ["吗", "妈"],
        "stroke_available": True,
        "lesson_words": [],
    }


def test_resolve_lesson_characters_reads_real_lesson():
    lesson = json.loads(Path("lessons/lesson_001.json").read_text(encoding="utf-8"))
    result = resolve_lesson_characters(lesson, set(), _loaders())
    assert result["characters"]
    required = {
        "char", "pinyin", "han_viet", "hsk_level", "decomp_ids", "semantic",
        "phonetic", "en_meaning", "vi_meaning", "stroke_available", "lesson_words",
    }
    assert all(required <= set(entry) for entry in result["characters"])
    assert all(entry["vi_meaning"] is None for entry in result["characters"])


def test_resolve_lesson_characters_uses_vocab_words():
    lesson = {"lesson_id": "lesson_001", "segments": [{"language": "zh-CN", "text": "妈妈。"}]}
    result = resolve_lesson_characters(
        lesson, set(), _loaders(), {"words": [{"hanzi": "妈妈"}, {"hanzi": "吗"}]}
    )
    assert next(entry for entry in result["characters"] if entry["char"] == "妈")["lesson_words"] == ["妈妈"]


def test_cedict_pick_prefers_lesson_reading_and_marks_tones():
    loaders = _loaders()
    loaders["mmah"]["听"] = {"decomp_ids": "⿰口斤"}
    loaders["cedict"]["听"] = {
        "candidates": [
            {"pinyin": "yin3", "en_meaning": "smile (archaic)", "raw_meaning": "smile (archaic)"},
            {"pinyin": "ting1", "en_meaning": "to listen", "raw_meaning": "to listen"},
        ]
    }
    assert resolve_char("听", loaders, "tīng")["pinyin"] == "tīng"
    assert resolve_char("听", loaders, "tīng")["en_meaning"] == "to listen"


def test_cedict_drops_proper_noun_when_common_entry_remains():
    loaders = _loaders()
    loaders["cedict"]["也"] = {
        "candidates": [
            {"pinyin": "Ye3", "en_meaning": "surname Ye", "raw_meaning": "surname Ye"},
            {"pinyin": "ye3", "en_meaning": "also", "raw_meaning": "also"},
        ]
    }
    assert resolve_char("也", loaders, "yě")["en_meaning"] == "also"


def test_hsk_loader_uses_minimum_level_for_every_word_character(tmp_path):
    directory = tmp_path / "hsk30"
    directory.mkdir()
    (directory / "words.csv").write_text("Simplified,Level\n听,7\n听说,1\n", encoding="utf-8")
    assert Hsk30Loader(tmp_path).load()["听"] == 1


def test_ids_fallback_adds_components_when_mmah_has_no_roles():
    loaders = _loaders()
    loaders["mmah"]["你"] = {"decomp_ids": "⿰亻尔"}
    loaders["characters_vi"]["你"] = {"han_viet": "nhĩ", "vi_meaning": "bạn"}
    result = resolve_lesson_characters({"lesson_id": "lesson_001", "segments": [{"language": "zh-CN", "text": "你"}]}, set(), loaders)
    assert {entry["char"] for entry in result["components_prereq"]} == {"亻", "尔"}


def test_prereqs_are_unique_and_exclude_unknown_and_operators():
    loaders = _loaders()
    loaders["mmah"]["你"] = {"decomp_ids": "⿰亻？"}
    loaders["mmah"]["妈"] = {"decomp_ids": "⿰亻口"}
    result = resolve_lesson_characters(
        {"lesson_id": "lesson_001", "segments": [{"language": "zh-CN", "text": "你妈"}]},
        set(),
        loaders,
    )
    prereqs = result["components_prereq"]
    assert [entry["char"] for entry in prereqs] == ["亻", "口"]


def test_character_reply_requires_one_json_fence(tmp_path):
    path = tmp_path / "reply.md"
    path.write_text("```json\n{\"char\": \"你\"}\n```\n", encoding="utf-8")
    try:
        _reply_rows(path)
    except ValueError as exc:
        assert "array of objects" in str(exc)
    else:
        raise AssertionError("invalid character reply was accepted")
