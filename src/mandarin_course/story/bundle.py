"""Export/import lesson story-source bundles (docs/workflow.md "Content loop").

`export-lesson` writes one self-contained Markdown bundle any AI can read and
reply to; `import-lesson` takes that reply back, builds it, and reports
rule violations or a dry-run cost estimate.
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

from ..audit import build_report, load_authoring_lessons
from ..config import Settings
from ..generate_plan import audio_plan
from ..lesson_schema import Lesson, load_lesson
from ..logs import write_log
from .assemble import BuildResult, build_story, write_story
from .ledger import build_ledger

BUNDLES_DIR = Path("build/bundles")
CHECKLIST_PATH = Path("docs/authoring_checklist.md")
MAX_VIOLATIONS = 20
MAX_SUMMARY_LINES = 10


class BundleError(Exception):
    pass


def _lesson_id(short_id: str) -> str:
    return f"lesson_{short_id}"


def _story_path(lessons_dir: Path, short_id: str) -> Path:
    return lessons_dir / "src" / f"{_lesson_id(short_id)}.story.md"


def _story_id(path: Path) -> str:
    return path.name.removesuffix(".story.md")


def _previous_story(lessons_dir: Path, short_id: str) -> Path | None:
    lesson_id = _lesson_id(short_id)
    candidates = sorted(
        (p for p in (lessons_dir / "src").glob("lesson_*.story.md") if _story_id(p) < lesson_id), key=_story_id
    )
    return candidates[-1] if candidates else None


def _already_taught(lessons_dir: Path, short_id: str) -> list[str]:
    lesson_id = _lesson_id(short_id)
    lessons = [lesson for lesson in load_authoring_lessons(lessons_dir) if lesson.lesson_id < lesson_id]
    if not lessons:
        return [build_ledger(lessons_dir, lesson_id).rstrip()]
    report = build_report(lessons)
    lines = []
    for lesson in report.lessons:
        lines.append(
            f"{lesson.lesson_id}: patterns new=[{', '.join(lesson.new_patterns)}] "
            f"review=[{', '.join(lesson.review_pattern_ids)}]; "
            f"coach new=[{', '.join(lesson.coach_phrases_introduced)}] "
            f"reduced=[{', '.join(lesson.coach_phrases_reduced)}]"
        )
    return lines + ["", build_ledger(lessons_dir, lesson_id).rstrip()]


def _task_header(short_id: str, exists: bool, example_id: str | None, note: str | None) -> str:
    lesson_id = _lesson_id(short_id)
    if exists:
        task = f"Revise lesson {lesson_id}'s story source below."
    elif example_id:
        task = (
            f"Write lesson {lesson_id}'s story source from scratch (no source exists yet). "
            f"The story source below is lesson {example_id}'s — a format example only, do not copy its content."
        )
    else:
        task = f"Write lesson {lesson_id}'s story source from scratch (no source and no earlier lesson to use as a format example)."
    lines = [
        f"# Task: {lesson_id}",
        "",
        task,
        "",
        "Reply with exactly one fenced block, ```story ... ```, containing the complete story source "
        f"for {lesson_id}. Nothing else is needed.",
    ]
    if note:
        lines += ["", f"Note from the author: {note}"]
    return "\n".join(lines)


def export_lesson(short_id: str, lessons_dir: Path = Path("lessons"), note: str | None = None) -> Path:
    story_path = _story_path(lessons_dir, short_id)
    exists = story_path.is_file()
    example_path = story_path if exists else _previous_story(lessons_dir, short_id)
    example_id = _story_id(example_path) if example_path else None
    story_text = example_path.read_text(encoding="utf-8") if example_path else ""
    checklist = CHECKLIST_PATH.read_text(encoding="utf-8") if CHECKLIST_PATH.is_file() else ""

    parts = [_task_header(short_id, exists, None if exists else example_id, note)]
    parts.append("## Authoring checklist\n\n" + checklist.strip())
    parts.append("## Already taught\n\n" + "\n".join(_already_taught(lessons_dir, short_id)))
    story_heading = "## Story source" if exists else f"## Story source (format example, do not copy: {example_id or 'none'})"
    # No separator is added between `story_text` and the closing fence, so a byte-identical
    # story source round-trips through export -> extract_story_source -> import.
    parts.append(f"{story_heading}\n\n```story\n{story_text}```")

    bundle = "\n\n".join(parts).rstrip() + "\n"
    BUNDLES_DIR.mkdir(parents=True, exist_ok=True)
    out_path = BUNDLES_DIR / f"{_lesson_id(short_id)}.bundle.md"
    out_path.write_text(bundle, encoding="utf-8")
    return out_path


def command_export_lesson(short_id: str, lessons_dir: Path, note: str | None = None) -> int:
    path = export_lesson(short_id, lessons_dir, note)
    line_count = len(path.read_text(encoding="utf-8").splitlines())
    print(f"Wrote {path} ({line_count} lines)")
    return 0


_STORY_BLOCK = re.compile(r"```story\s*\n(.*?)```", re.DOTALL)


def extract_story_source(text: str) -> str:
    match = _STORY_BLOCK.search(text)
    if match:
        return match.group(1)
    stripped = text.strip()
    head = "\n".join(stripped.splitlines()[:20])
    if re.search(r"(?m)^lesson_id:\s*\S+", head):
        return stripped + "\n"
    raise BundleError("no ```story``` block found, and the file doesn't start like a story source")


@dataclass
class ImportResult:
    ok: bool
    report: str


def _violations_report(label: str, lines: list[str]) -> tuple[str, str]:
    full = "\n".join(lines)
    header = f"{label}: {len(lines)} issue(s)"
    body = [header] + [f"  {line}" for line in lines[: MAX_VIOLATIONS - 1]]
    if len(lines) > MAX_VIOLATIONS - 1:
        body.append(f"  ... and {len(lines) - (MAX_VIOLATIONS - 1)} more")
    return "\n".join(body), full


def _audit_issues(lessons_dir: Path, lesson_id: str) -> list[str]:
    lessons = load_authoring_lessons(lessons_dir)
    report = build_report(lessons)
    issues = []
    for label, ledger in (("dialogue", report.dialogue), ("coach phrase", report.coach_phrases)):
        for pattern, lesson_ids in ledger.duplicate_new.items():
            if lesson_id in lesson_ids:
                issues.append(f"audit: {label} '{pattern}' introduced more than once ({', '.join(lesson_ids)})")
        for owner, patterns in ledger.unintroduced_reuse.items():
            if owner == lesson_id:
                issues.append(f"audit: {label} reused/reduced with no earlier introduction: {', '.join(patterns)}")
    return issues


def import_lesson(short_id: str, file_path: Path, lessons_dir: Path, settings: Settings) -> ImportResult:
    lesson_id = _lesson_id(short_id)
    text = file_path.read_text(encoding="utf-8")
    content = extract_story_source(text)
    match = re.search(r"(?m)^lesson_id:\s*(\S+)", content)
    if not match or match.group(1) != lesson_id:
        found = match.group(1) if match else "(missing)"
        raise BundleError(f"story's lesson_id '{found}' does not match the requested id '{short_id}' ({lesson_id})")

    story_path = _story_path(lessons_dir, short_id)
    if story_path.is_file():
        BUNDLES_DIR.mkdir(parents=True, exist_ok=True)
        (BUNDLES_DIR / f"{lesson_id}.prev.story.md").write_text(story_path.read_text(encoding="utf-8"), encoding="utf-8")
    story_path.parent.mkdir(parents=True, exist_ok=True)
    story_path.write_text(content, encoding="utf-8")

    result: BuildResult = build_story(story_path, lessons_dir)
    if result.errors:
        lines = [f"line {error.line}: {error.message}" for error in sorted(result.errors, key=lambda e: e.line)]
        short, full = _violations_report(f"{story_path}: build failed", lines)
        log_path = write_log(f"import-lesson-{short_id}", full)
        return ImportResult(False, f"{short}\nFull list: {log_path}")

    write_story(result)
    for variant in result.variants:
        load_lesson(variant.path)  # re-validate the written JSON

    issues = _audit_issues(lessons_dir, lesson_id)
    if issues:
        short, full = _violations_report(f"{story_path}: audit failed", issues)
        log_path = write_log(f"import-lesson-{short_id}", full)
        return ImportResult(False, f"{short}\nFull list: {log_path}")

    summary_lines = []
    for variant in result.variants:
        summary_lines.append(
            f"{variant.path}: {len(variant.lesson['segments'])} segments, {variant.lesson['target_duration_minutes']} min"
        )
    guided = next(v for v in result.variants if v.name == "guided")
    plan = audio_plan(Lesson.model_validate(guided.lesson), settings)
    summary_lines.append(f"Dry run: {plan['unique_new_requests']} new paid TTS request(s) for {lesson_id} (no API call made).")
    return ImportResult(True, "\n".join(summary_lines[:MAX_SUMMARY_LINES]))


def command_import_lesson(short_id: str, file_path: Path, lessons_dir: Path, settings: Settings) -> int:
    result = import_lesson(short_id, file_path, lessons_dir, settings)
    print(result.report)
    return 0 if result.ok else 1
