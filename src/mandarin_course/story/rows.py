"""Export and import row-scoped story-source revisions."""

from __future__ import annotations

import re
import tempfile
from pathlib import Path

from ..config import Settings
from .bundle import BundleError, import_lesson
from .ledger import build_ledger

ROWS_BLOCK = re.compile(r"```rows\s*\n(.*?)```", re.DOTALL)


def command_export_rows(short_id: str, refs: list[str], lessons_dir: Path) -> int:
    path = export_rows(short_id, refs, lessons_dir)
    print(f"Wrote {path}")
    return 0


def command_import_rows(short_id: str, file_path: Path, lessons_dir: Path, settings: Settings) -> int:
    result = import_rows(short_id, file_path, lessons_dir, settings)
    print(result.report)
    return 0 if result.ok else 1


def _story_path(lessons_dir: Path, short_id: str) -> Path:
    return lessons_dir / "src" / f"lesson_{short_id}.story.md"


def _rows(path: Path) -> list[dict[str, object]]:
    lines = path.read_text(encoding="utf-8").splitlines(keepends=True)
    rows = []
    variant = section = activity = block = ""
    for index, raw in enumerate(lines):
        text = raw.strip()
        if text.startswith("# "):
            section = text[2:].strip()
            variant = section if section in {"guided", "practice"} else variant
            activity = ""
            block = ""
        elif text.startswith("## "):
            header = text[3:].split()
            block = header[0] if header else ""
            activity = " ".join(header[1:]) if block == "activity" else activity
        elif "|" in text and block != "tags" and not text.startswith((">", "<!--")):
            cells = [cell.strip() for cell in text.split("|")]
            if cells and cells[0]:
                rows.append({"ref": cells[0], "line": index, "text": raw, "cells": cells, "section": section, "variant": variant, "activity": activity, "block": block})
    occurrences: dict[str, int] = {}
    totals: dict[str, int] = {}
    for row in rows:
        ref = str(row["ref"])
        totals[ref] = totals.get(ref, 0) + 1
    for row in rows:
        ref = str(row["ref"])
        occurrences[ref] = occurrences.get(ref, 0) + 1
        row["occurrence"] = occurrences[ref]
        row["qualified_ref"] = f"{ref}#{occurrences[ref]}" if totals[ref] > 1 else ref
    return rows


def _find(path: Path, refs: list[str]) -> tuple[list[dict[str, object]], list[str]]:
    rows = _rows(path)
    by_ref: dict[str, list[dict[str, object]]] = {}
    for row in rows:
        by_ref.setdefault(str(row["ref"]), []).append(row)
    selected: list[dict[str, object]] = []
    errors: list[str] = []
    for ref in refs:
        match = re.fullmatch(r"(.+)#([1-9][0-9]*)", ref)
        if match:
            base, occurrence = match.group(1), int(match.group(2))
            choices = by_ref.get(base, [])
            if occurrence > len(choices):
                errors.append(f"unknown row ref '{ref}'")
            else:
                selected.append(choices[occurrence - 1])
            continue
        choices = by_ref.get(ref, [])
        if not choices:
            errors.append(f"unknown row ref '{ref}'")
        elif len(choices) > 1:
            qualified = ", ".join(f"{ref}#{index}" for index in range(1, len(choices) + 1))
            errors.append(f"ambiguous row ref '{ref}'; use {qualified}")
        else:
            selected.append(choices[0])
    return selected, errors


def export_rows(short_id: str, refs: list[str], lessons_dir: Path = Path("lessons")) -> Path:
    path = _story_path(lessons_dir, short_id)
    selected, missing = _find(path, refs)
    if missing:
        raise BundleError("; ".join(missing))
    all_rows = _rows(path)
    positions = {int(row["line"]): index for index, row in enumerate(all_rows)}
    checklist = Path("docs/authoring_checklist.md").read_text(encoding="utf-8").splitlines()[:20]
    lines = [f"# Rows: lesson_{short_id}", "", "## Checklist", "", *checklist, "", "## Cumulative ledger", "", build_ledger(lessons_dir, f"lesson_{short_id}").rstrip(), "", "## Rows and neighbours", ""]
    for row in selected:
        index = positions[int(row["line"])]
        lines.append(f"### {row['qualified_ref']} · {row['variant']} / {row['activity']} / {row['block']}")
        for neighbour in all_rows[max(0, index - 2):index + 3]:
            marker = "TARGET" if neighbour["ref"] == row["ref"] and int(neighbour["line"]) == int(row["line"]) else "context"
            lines.append(f"{marker}: {neighbour['text'].rstrip()}")
        lines.append("")
    reply_rows = []
    for row in selected:
        cells = str(row["text"]).rstrip().split("|", 1)
        reply_rows.append(f"{row['qualified_ref']} | {cells[1].strip()}")
    lines += ["## Reply format", "", "Replace only the requested rows, preserving each ref:", "", "```rows", *reply_rows, "```", ""]
    out = Path("build/bundles") / f"lesson_{short_id}.rows.md"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text("\n".join(lines), encoding="utf-8")
    return out


def _reply_rows(path: Path) -> list[str]:
    match = ROWS_BLOCK.search(path.read_text(encoding="utf-8"))
    if not match:
        raise BundleError("no ```rows``` block found")
    lines = [line.strip() for line in match.group(1).splitlines() if line.strip()]
    if not lines or any("|" not in line for line in lines):
        raise BundleError("rows block must contain pipe-delimited rows")
    return lines


def import_rows(short_id: str, file_path: Path, lessons_dir: Path, settings: Settings):
    story_path = _story_path(lessons_dir, short_id)
    replacements = _reply_rows(file_path)
    refs = [line.split("|", 1)[0].strip() for line in replacements]
    if len(set(refs)) != len(refs):
        raise BundleError("rows block contains duplicate refs")
    selected, missing = _find(story_path, refs)
    if missing:
        raise BundleError("; ".join(missing))
    if len(selected) != len(replacements):
        raise BundleError("rows block contains extra rows")
    source_lines = story_path.read_text(encoding="utf-8").splitlines(keepends=True)
    for row, replacement in zip(selected, replacements):
        replacement = f"{row['ref']} | {replacement.split('|', 1)[1].strip()}"
        newline = "\n" if str(row["text"]).endswith("\n") else ""
        source_lines[int(row["line"])] = replacement + newline
    candidate = "".join(source_lines)
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", suffix=".story.md") as temp:
        temp.write(candidate)
        temp.flush()
        return import_lesson(short_id, Path(temp.name), lessons_dir, settings)
