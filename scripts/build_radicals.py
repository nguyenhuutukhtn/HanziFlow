#!/usr/bin/env python3
"""Build the radical metadata from a pinned UCD skeleton and authored names."""

from __future__ import annotations

import json
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UCD = ROOT / "data" / "unihan" / "CJKRadicals.txt"
NAMES = ROOT / "data" / "radicals_vi_names.json"
OUTPUT = ROOT / "data" / "radicals_vi.json"


def _skeleton() -> list[dict]:
    rows = []
    for line_no, line in enumerate(UCD.read_text(encoding="utf-8").splitlines(), 1):
        if not line or line.startswith("#"):
            continue
        fields = [field.strip() for field in line.split(";")]
        if len(fields) != 3 or not fields[0] or "'" in fields[0] or not fields[1] or not fields[2]:
            continue
        try:
            index = int(fields[0])
            radical = chr(int(fields[1], 16))
            char = chr(int(fields[2], 16))
        except ValueError as exc:
            raise ValueError(f"Malformed CJKRadicals data at {UCD}:{line_no}") from exc
        name = unicodedata.name(radical, unicodedata.name(char, "")).removeprefix("KANGXI RADICAL ")
        rows.append({"char": char, "kangxi_index": index, "simplified_only": False, "name_en": name.lower()})
    if len(rows) != 214:
        raise ValueError(f"Expected 214 Kangxi radicals in {UCD}, found {len(rows)}")
    return rows


def build() -> None:
    names = json.loads(NAMES.read_text(encoding="utf-8"))
    old = {row["char"]: row for row in json.loads(OUTPUT.read_text(encoding="utf-8"))} if OUTPUT.is_file() else {}
    rows = _skeleton()
    known = {row["char"] for row in rows}
    extras = [char for char in names if char not in known]
    for row in rows:
        row["name_vi"] = names.get(row["char"], "")
    for char in extras:
        row = old.get(char, {})
        rows.append({
            "char": char,
            "kangxi_index": None,
            "simplified_only": True,
            "name_en": row.get("name_en", ""),
            "name_vi": names.get(char, ""),
        })
    missing = [row["char"] for row in rows if not row["name_vi"]]
    if missing:
        raise ValueError(f"Missing Vietnamese radical names: {', '.join(missing)}")
    OUTPUT.write_text(json.dumps(rows, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {OUTPUT} ({len(rows)} entries)")


if __name__ == "__main__":
    build()
