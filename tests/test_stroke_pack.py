from __future__ import annotations

import json
from pathlib import Path

from mandarin_course.characters.stroke_pack import build_stroke_pack


def test_build_stroke_pack_unions_and_skips_missing_characters_file(tmp_path: Path):
    root = tmp_path / "publish"
    lesson_dir = root / "lessons" / "lesson_001" / "v1"
    lesson_dir.mkdir(parents=True)
    data_dir = tmp_path / "data" / "hanzi_writer"
    data_dir.mkdir(parents=True)
    characters = {"characters": [{"char": "我", "stroke_available": True}], "components_prereq": [{"char": "扌"}]}
    (lesson_dir / "characters.json").write_text(json.dumps(characters), encoding="utf-8")
    for char in ("我", "扌"):
        (data_dir / f"{char}.json").write_text(json.dumps({"strokes": [f"M {char}"], "medians": [[[1, 2]]], "radStrokes": []}), encoding="utf-8")
    catalog = {"catalog_version": 1, "lessons": [{"id": "lesson_001", "base_url": "lessons/lesson_001/v1/", "files": {"characters": {"file": "characters.json"}}}, {"id": "lesson_002", "base_url": "lessons/lesson_002/v1/", "files": {}}]}
    (root / "catalog.json").write_text(json.dumps(catalog), encoding="utf-8")

    messages: list[str] = []
    build_stroke_pack(root, echo=messages.append)
    pack = json.loads((root / "stroke_pack/v1/strokes.json").read_text(encoding="utf-8"))
    assert set(pack) == {"我", "扌"}
    build_stroke_pack(root, echo=messages.append)
    assert any("unchanged v1" in message for message in messages)
