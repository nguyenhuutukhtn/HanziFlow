"""Read-only authoring audit for cross-lesson pattern continuity.

This does not change the lesson schema's runtime behavior or TTS generation.
It only reads the manual authoring ledgers already declared on each lesson:
dialogue content (`new_patterns` / `review_pattern_ids`) and coach
instructional narration (`coach_phrases_introduced` / `coach_phrases_reduced`).
"""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path

from .lesson_schema import Lesson, load_lesson


@dataclass(frozen=True)
class LessonPatterns:
    lesson_id: str
    title: str
    new_patterns: list[str]
    review_pattern_ids: list[str]
    coach_phrases_introduced: list[str]
    coach_phrases_reduced: list[str]


@dataclass
class PatternLedger:
    introduced_by: dict[str, str] = field(default_factory=dict)
    duplicate_new: dict[str, list[str]] = field(default_factory=dict)
    unintroduced_reuse: dict[str, list[str]] = field(default_factory=dict)
    reused_in: dict[str, list[str]] = field(default_factory=dict)


@dataclass
class AuditReport:
    lessons: list[LessonPatterns] = field(default_factory=list)
    dialogue: PatternLedger = field(default_factory=PatternLedger)
    coach_phrases: PatternLedger = field(default_factory=PatternLedger)


def load_authoring_lessons(lessons_dir: Path) -> list[Lesson]:
    """Load one canonical lesson per lesson_id, ordered by lesson_id.

    Only files named exactly after their own lesson_id (e.g. `lesson_001.json`)
    are canonical. Practice-variant files declare the same lesson_id and the
    same authoring ledger as their guided counterpart, and draft files (e.g.
    `lesson_001_mau.json`) are not the authored ledger, so both are skipped.
    """
    by_id: dict[str, Lesson] = {}
    for path in sorted(lessons_dir.glob("lesson_*.json")):
        lesson = load_lesson(path)
        if lesson.variant == "practice" or path.stem != lesson.lesson_id:
            continue
        by_id[lesson.lesson_id] = lesson
    return [by_id[key] for key in sorted(by_id)]


def _build_ledger(lessons: list[Lesson], new_attr: str, reuse_attr: str) -> PatternLedger:
    ledger = PatternLedger()
    for lesson in lessons:
        for pattern in getattr(lesson, reuse_attr):
            ledger.reused_in.setdefault(pattern, []).append(lesson.lesson_id)
            if pattern not in ledger.introduced_by:
                ledger.unintroduced_reuse.setdefault(lesson.lesson_id, []).append(pattern)
        for pattern in getattr(lesson, new_attr):
            if pattern in ledger.introduced_by:
                ledger.duplicate_new.setdefault(pattern, [ledger.introduced_by[pattern]]).append(lesson.lesson_id)
            else:
                ledger.introduced_by[pattern] = lesson.lesson_id
    return ledger


def build_report(lessons: list[Lesson], up_to: str | None = None) -> AuditReport:
    if up_to is not None:
        lessons = [lesson for lesson in lessons if lesson.lesson_id <= up_to]
    report = AuditReport(
        lessons=[
            LessonPatterns(
                lesson.lesson_id,
                lesson.title,
                lesson.new_patterns,
                lesson.review_pattern_ids,
                lesson.coach_phrases_introduced,
                lesson.coach_phrases_reduced,
            )
            for lesson in lessons
        ],
        dialogue=_build_ledger(lessons, "new_patterns", "review_pattern_ids"),
        coach_phrases=_build_ledger(lessons, "coach_phrases_introduced", "coach_phrases_reduced"),
    )
    return report


def _render_ledger(lines: list[str], title: str, ledger: PatternLedger) -> None:
    lines.append(f"{title} patterns introduced so far: {len(ledger.introduced_by)}")
    lines.append("")
    if ledger.unintroduced_reuse:
        lines.append(f"{title}: reused/reduced with no earlier introduction:")
        for lesson_id, patterns in ledger.unintroduced_reuse.items():
            lines.append(f"  {lesson_id}: {', '.join(patterns)}")
    else:
        lines.append(f"{title}: reused/reduced with no earlier introduction: none")
    lines.append("")
    if ledger.duplicate_new:
        lines.append(f"{title}: duplicate introductions:")
        for pattern, lesson_ids in ledger.duplicate_new.items():
            lines.append(f"  {pattern}: {', '.join(lesson_ids)}")
    else:
        lines.append(f"{title}: duplicate introductions: none")
    lines.append("")
    lines.append(f"{title}: reuse map (pattern → lessons that declared it again):")
    if ledger.reused_in:
        for pattern, lesson_ids in ledger.reused_in.items():
            introduced = ledger.introduced_by.get(pattern, "unintroduced")
            lines.append(f"  {pattern} (introduced {introduced}): reused in {', '.join(lesson_ids)}")
    else:
        lines.append("  (none)")
    lines.append("")


def render_report(report: AuditReport) -> str:
    lines: list[str] = []
    lines.append("Lessons audited (in order):")
    for lesson in report.lessons:
        lines.append(f"  {lesson.lesson_id} — {lesson.title}")
        lines.append(f"    dialogue new: {', '.join(lesson.new_patterns) or '(none)'}")
        lines.append(f"    dialogue review: {', '.join(lesson.review_pattern_ids) or '(none)'}")
        lines.append(f"    coach introduced: {', '.join(lesson.coach_phrases_introduced) or '(none)'}")
        lines.append(f"    coach reduced: {', '.join(lesson.coach_phrases_reduced) or '(none)'}")
    lines.append("")
    _render_ledger(lines, "Dialogue", report.dialogue)
    _render_ledger(lines, "Coach phrases", report.coach_phrases)
    return "\n".join(lines).rstrip() + "\n"
