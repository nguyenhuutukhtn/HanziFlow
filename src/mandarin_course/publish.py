"""Public publishing API; implementation is split into focused modules."""

from pathlib import Path

from .config import Settings
from .lesson_schema import load_lesson
from .publish_core import PublishReport, publish_lesson as publish_linear_lesson
from .publish_modes import publish_modes_lesson
from .publish_render import render_publish_report, render_publish_summary
from .publish_timeline import PublishError, _enrich_timeline, _reference_lines
from .publish_tokens import (
    load_vocab,
    numbered_pinyin,
    sentence_item_id,
    tokenize_mandarin,
    tones_from_pinyin,
    word_item_id,
)

__all__ = [
    "PublishError", "PublishReport", "publish_lesson", "render_publish_report",
    "render_publish_summary", "load_vocab", "tokenize_mandarin", "tones_from_pinyin",
    "numbered_pinyin", "word_item_id", "sentence_item_id", "_enrich_timeline",
    "_reference_lines",
]


def publish_lesson(lesson_path: Path, settings: Settings, out: Path, force: bool = False) -> PublishReport:
    """Publish either the original linear package or a four-track modes package."""
    if load_lesson(lesson_path).format == "modes_v1":
        return publish_modes_lesson(lesson_path, settings, out, force)
    return publish_linear_lesson(lesson_path, settings, out, force)
