"""Build the catalog-scoped Hanzi Writer stroke data package."""
from __future__ import annotations
import hashlib, json
from pathlib import Path

def _digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()

def build_stroke_pack(root: Path, *, echo=print) -> int:
    catalog_path = root / "catalog.json"
    catalog = json.loads(catalog_path.read_text(encoding="utf-8"))
    wanted = set()
    for lesson in catalog.get("lessons", []):
        characters_ref = lesson["files"].get("characters")
        if not characters_ref:
            continue
        base = root / lesson["base_url"]
        data = json.loads((base / characters_ref["file"]).read_text(encoding="utf-8"))
        wanted.update(row["char"] for row in data.get("characters", []))
        wanted.update(row["char"] for row in data.get("components_prereq", []))
    source = root.parent / "data" / "hanzi_writer"
    entries = {}
    for char in sorted(wanted):
        path = source / f"{char}.json"
        if path.is_file():
            row = json.loads(path.read_text(encoding="utf-8"))
            entries[char] = {"strokes": row["strokes"], "medians": row["medians"]}
    payload = json.dumps(entries, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode()
    old = catalog.get("stroke_pack") or {}
    version = int(old.get("version", 0))
    old_path = root / "stroke_pack" / f"v{version}" / "strokes.json" if version else None
    if old_path and old_path.is_file() and old_path.read_bytes() == payload:
        echo(f"Stroke pack: unchanged v{version} ({len(entries)} chars)")
        return version
    version += 1
    destination = root / "stroke_pack" / f"v{version}" / "strokes.json"
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_bytes(payload)
    catalog["stroke_pack"] = {"version": version, "files": {"strokes.json": {"sha256": _digest(destination), "bytes": len(payload)}}}
    catalog["catalog_version"] = int(catalog.get("catalog_version", 0)) + 1
    catalog_path.write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    echo(f"Stroke pack: wrote v{version} ({len(entries)} chars)")
    return version
