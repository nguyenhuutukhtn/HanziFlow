"""Short build-lesson output: ≤ 20 lines, errors with source line numbers."""

from __future__ import annotations

import json
from collections import Counter
from pathlib import Path

from .assemble import BuildResult

MAX_LINES = 20


def activity_counts(lesson: dict) -> list[tuple[str, int]]:
    return list(Counter(segment["activity"] for segment in lesson["segments"]).items())


def render_errors(path: Path, result: BuildResult) -> tuple[str, str]:
    """Return (short report, full report for the log)."""
    errors = sorted(result.errors, key=lambda error: error.line)
    full = [f"{path.name}:{error.line}: {error.message}" for error in errors]
    lines = [f"{path}: {len(errors)} error(s)"] + [f"  {line}" for line in full[: MAX_LINES - 2]]
    if len(full) > MAX_LINES - 2:
        lines.append(f"  ... and {len(full) - (MAX_LINES - 2)} more")
    return "\n".join(lines), "\n".join(full)


def render_summary(result: BuildResult, as_json: bool = False) -> str:
    if as_json:
        return json.dumps([
            {
                "path": str(v.path), "segments": len(v.lesson["segments"]),
                "minutes": v.lesson["target_duration_minutes"],
                "brisk_minutes": round(v.brisk_seconds / 60, 1), "careful_minutes": round(v.careful_seconds / 60, 1),
                "activities": dict(activity_counts(v.lesson)),
            }
            for v in result.variants
        ], ensure_ascii=False)
    lines = []
    for v in result.variants:
        lines.append(
            f"{v.path}: {len(v.lesson['segments'])} segments, {v.lesson['target_duration_minutes']} min "
            f"(estimate brisk {v.brisk_seconds / 60:.1f}, careful {v.careful_seconds / 60:.1f})"
        )
        lines.append("  " + " · ".join(f"{name} {count}" for name, count in activity_counts(v.lesson)))
    return "\n".join(lines)
