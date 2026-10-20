"""Shared helper: write full detail behind a short CLI summary (docs/workflow.md)."""

from __future__ import annotations

from pathlib import Path

LOG_DIR = Path("build/logs")


def write_log(name: str, content: str) -> Path:
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    path = LOG_DIR / f"{name}.log"
    path.write_text(content, encoding="utf-8")
    return path
