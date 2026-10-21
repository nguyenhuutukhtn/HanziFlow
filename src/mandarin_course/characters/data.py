"""Small, lazy loaders for the offline character data sources."""

from __future__ import annotations

import csv
import json
import re
from pathlib import Path
from typing import Any

_CACHE: dict[tuple[str, str], dict[str, Any] | set[str]] = {}
_CJK = re.compile(r"[一-鿿㐀-䶿]")
_HAN_VIET_GAPS = {
    "桌": "trác", "这": "giá", "谁": "thùy", "懂": "đổng", "者": "giả",
    "问": "vấn", "题": "đề", "遍": "biến", "猜": "sai",
}


def _read_lines(path: Path) -> list[str]:
    try:
        return path.read_text(encoding="utf-8").splitlines()
    except UnicodeDecodeError as exc:
        raise ValueError(f"Malformed character data file {path}: invalid UTF-8") from exc


def _cached(kind: str, path: Path, build):
    key = (kind, str(path.resolve()))
    if key not in _CACHE:
        _CACHE[key] = build()
    return _CACHE[key]


class MmahLoader:
    def __init__(self, data_dir: str | Path = "data"):
        self.path = Path(data_dir) / "mmah" / "dictionary.txt"

    def load(self) -> dict[str, dict[str, Any]]:
        def build():
            if not self.path.is_file():
                return {}
            result = {}
            for line_no, line in enumerate(_read_lines(self.path), 1):
                if not line.strip():
                    continue
                try:
                    row = json.loads(line)
                    char = row["character"]
                    etymology = row.get("etymology") or {}
                    result[char] = {
                        "decomp_ids": row.get("decomposition", ""),
                        "semantic": etymology.get("semantic"),
                        "phonetic": etymology.get("phonetic"),
                        "hint": etymology.get("hint"),
                        "etymology_type": etymology.get("type"),
                    }
                except (json.JSONDecodeError, KeyError, TypeError) as exc:
                    raise ValueError(f"Malformed MMAH data at {self.path}:{line_no}") from exc
            return result
        return _cached("mmah", self.path, build)  # type: ignore[return-value]


class UnihanLoader:
    def __init__(self, data_dir: str | Path = "data"):
        self.path = Path(data_dir) / "unihan" / "Unihan_Readings.txt"
        self.variants_path = Path(data_dir) / "unihan" / "Unihan_Variants.txt"

    def load(self) -> dict[str, dict[str, str | None]]:
        def build():
            if not self.path.is_file():
                return {}
            readings: dict[str, str] = {}
            variants: dict[str, list[str]] = {}
            paths = [self.path] + ([self.variants_path] if self.variants_path.is_file() else [])
            for path in paths:
              for line_no, line in enumerate(_read_lines(path), 1):
                if not line or line.startswith("#"):
                    continue
                fields = line.split("\t")
                if len(fields) != 3 or not fields[0].startswith("U+"):
                    raise ValueError(f"Malformed Unihan data at {path}:{line_no}")
                try:
                    char = chr(int(fields[0][2:], 16))
                except ValueError as exc:
                    raise ValueError(f"Malformed Unihan codepoint at {path}:{line_no}") from exc
                if fields[1] == "kVietnamese":
                    readings[char] = fields[2].split()[0]
                elif fields[1] == "kTraditionalVariant":
                    variants[char] = [value.removeprefix("U+") for value in fields[2].split()]
            result = {}
            for char in set(readings) | set(variants):
                variant_chars = [chr(int(value, 16)) for value in variants.get(char, [])]
                result[char] = {
                    "han_viet": readings.get(char) or next((readings.get(variant) for variant in variant_chars if readings.get(variant)), None) or _HAN_VIET_GAPS.get(char),
                    "traditional_variant": variant_chars[0] if variant_chars else None,
                }
            return result
        return _cached("unihan", self.path, build)  # type: ignore[return-value]


class Hsk30Loader:
    def __init__(self, data_dir: str | Path = "data"):
        self.path = Path(data_dir) / "hsk30"

    def load(self) -> dict[str, int]:
        def build():
            files = sorted(self.path.glob("*.csv")) if self.path.is_dir() else []
            result: dict[str, int] = {}
            for path in files:
                try:
                    with path.open(encoding="utf-8", newline="") as handle:
                        rows = list(csv.DictReader(handle))
                except (UnicodeDecodeError, csv.Error) as exc:
                    raise ValueError(f"Malformed HSK data file {path}") from exc
                for row in rows:
                    char = row.get("char") or row.get("character") or row.get("hanzi") or row.get("Simplified")
                    level = row.get("level") or row.get("hsk") or row.get("hsk_level") or row.get("Level")
                    if not char or not level:
                        continue
                    try:
                        for value in char.split("|"):
                            parsed = int(float(str(level).split("-", 1)[0]))
                            for character in value:
                                if "一" <= character <= "鿿":
                                    result[character] = min(parsed, result.get(character, parsed))
                    except ValueError as exc:
                        raise ValueError(f"Malformed HSK level in {path}: {level!r}") from exc
            return result
        return _cached("hsk30", self.path, build)  # type: ignore[return-value]


class CedictLoader:
    def __init__(self, data_dir: str | Path = "data"):
        self.path = Path(data_dir) / "cedict" / "cedict_ts.u8"

    def load(self) -> dict[str, dict[str, str]]:
        def build():
            if not self.path.is_file():
                return {}
            result: dict[str, dict[str, str]] = {}
            for line_no, line in enumerate(_read_lines(self.path), 1):
                if not line or line.startswith("#"):
                    continue
                match = re.match(r"^(\S+)\s+(\S+)\s+\[([^]]+)\]\s+/(.*)/$", line)
                if not match:
                    raise ValueError(f"Malformed CC-CEDICT data at {self.path}:{line_no}")
                traditional, simplified, pinyin, gloss = match.groups()
                if len(simplified) != 1:
                    continue
                clean = re.sub(r"\[[^]]*\]|\([^)]*\)|\b\d+\b", "", gloss)
                clean = "; ".join(part.strip(" ;") for part in clean.split("/") if part.strip(" ;"))
                candidate = {"pinyin": pinyin, "en_meaning": clean, "raw_meaning": gloss}
                result.setdefault(simplified, {"candidates": []})["candidates"].append(candidate)
                if len(traditional) == 1:
                    result.setdefault(traditional, {"candidates": []})["candidates"].append(candidate)
            for value in result.values():
                value.update(value["candidates"][0])
            return result
        return _cached("cedict", self.path, build)  # type: ignore[return-value]


class StrokeLoader:
    def __init__(self, data_dir: str | Path = "data"):
        self.path = Path(data_dir) / "hanzi_writer"

    def load(self) -> set[str]:
        def build():
            if not self.path.is_dir():
                return set()
            return {p.stem for p in self.path.glob("*.json") if len(p.stem) == 1}
        return _cached("stroke", self.path, build)  # type: ignore[return-value]


class RadicalsLoader:
    def __init__(self, data_dir: str | Path = "data"):
        self.path = Path(data_dir) / "radicals_vi.json"

    def load(self) -> dict[str, dict[str, str | None]]:
        def build():
            if not self.path.is_file():
                return {}
            try:
                rows = json.loads(self.path.read_text(encoding="utf-8"))
                return {row["char"]: {"name_en": row.get("name_en", ""), "name_vi": row.get("name_vi", "")} for row in rows}
            except (OSError, UnicodeDecodeError, json.JSONDecodeError, KeyError, TypeError) as exc:
                raise ValueError(f"Malformed radicals data file {self.path}") from exc
        return _cached("radicals", self.path, build)  # type: ignore[return-value]


class CharactersViLoader:
    def __init__(self, data_dir: str | Path = "data"):
        self.path = Path(data_dir) / "characters_vi.json"

    def load(self) -> dict[str, dict[str, str]]:
        def build():
            if not self.path.is_file():
                return {}
            try:
                value = json.loads(self.path.read_text(encoding="utf-8"))
                if not isinstance(value, dict):
                    raise TypeError("expected an object")
                return value
            except (OSError, UnicodeDecodeError, json.JSONDecodeError, TypeError) as exc:
                raise ValueError(f"Malformed authored character data file {self.path}") from exc
        return _cached("characters_vi", self.path, build)  # type: ignore[return-value]


class CharacterLoaders:
    """Default loader bundle; each source is parsed only when first requested."""

    def __init__(self, data_dir: str | Path = "data"):
        self.mmah = MmahLoader(data_dir)
        self.unihan = UnihanLoader(data_dir)
        self.hsk30 = Hsk30Loader(data_dir)
        self.cedict = CedictLoader(data_dir)
        self.stroke = StrokeLoader(data_dir)
        self.radicals = RadicalsLoader(data_dir)
        self.characters_vi = CharactersViLoader(data_dir)

    def as_dict(self) -> dict[str, Any]:
        return {name: getattr(self, name).load() for name in ("mmah", "unihan", "hsk30", "cedict", "stroke", "radicals", "characters_vi")}

    def missing_sources(self) -> list[str]:
        missing = [str(getattr(self, name).path) for name in ("mmah", "hsk30", "cedict", "stroke", "radicals") if not getattr(self, name).path.exists()]
        if not self.unihan.path.is_file():
            missing.append(str(self.unihan.path))
        if not self.unihan.variants_path.is_file():
            missing.append(str(self.unihan.variants_path))
        return missing
