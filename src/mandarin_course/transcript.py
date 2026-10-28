"""Learner-facing Markdown transcripts."""

from __future__ import annotations

from pathlib import Path

from .lesson_schema import Lesson, Segment


def _entry(segment: Segment, *, show_translation: bool, show_hanzi: bool) -> list[str]:
    labels = {
        "anna": "Anna", "li_ming": "Li Ming", "wang_fang": "Wang Fang", "chen_dong": "Chen Dong",
        "zhang_jing": "Zhang Jing", "liu_yang": "Liu Yang",
        "coach": "Coach", "coach_zh": "Narrator",
    }
    lines: list[str] = []
    if segment.turns:
        ordered_speakers = list(dict.fromkeys(turn.speaker for turn in segment.turns))
        lines.append("Speakers: " + " ↔ ".join(labels[name] for name in ordered_speakers))
    elif segment.speaker:
        lines.append(f"Speaker: {labels[segment.speaker]}")
    lines.append(f"**{segment.pinyin or segment.text}**")
    if show_translation and segment.translation:
        lines.append(f"_{segment.translation}_")
    if show_hanzi and segment.language == "zh-CN":
        lines.append(f"<details><summary>Show Hanzi</summary>{segment.text}</details>")
    if segment.context:
        lines.append(f"Context: {segment.context}")
    return lines


def render_transcript(lesson: Lesson, *, full_reference: bool = False, show_hanzi: bool = False) -> str:
    if lesson.format == "modes_v1":
        label = {"hieu": "Hiểu", "tu_vung": "Từ vựng", "nghe": "Nghe", "phan_xa": "Phản xạ", "noi": "Nói"}[lesson.variant]
        title = f"{label} reference" if full_reference else f"{label} transcript"
    elif lesson.variant == "guided":
        title = "Guided reference" if full_reference else "Guided transcript"
    elif lesson.variant == "practice":
        title = "Listening reference" if full_reference else "Listening transcript"
    else:
        title = "Full reference" if full_reference else "Practice transcript"
    if full_reference:
        note = "Backup copy. Listen to the audio. Pinyin is only a way to find a line again."
    elif any(segment.language == "vi-VN" for segment in lesson.segments):
        note = "Backup copy. Chinese lines are pinyin. Vietnamese lines are what the guide says. The lesson is the audio."
    else:
        note = "Listen first. This copy is pinyin only, so the English gloss does not sit under the sound."
    lines = [f"# {lesson.title} — {title}", "", note, ""]
    activity: str | None = None
    hidden_answers = False
    for segment in lesson.segments:
        if segment.activity != activity:
            activity = segment.activity
            lines.extend([f"## {activity}", ""])
        if segment.type == "model_answer" and not full_reference:
            if not hidden_answers:
                lines.extend(["> Your turn—answer aloud before the model plays.", ""])
                hidden_answers = True
            continue
        hidden_answers = False
        lines.extend(_entry(segment, show_translation=full_reference, show_hanzi=show_hanzi))
        lines.append("")
    return "\n".join(lines).rstrip() + "\n"


def write_transcripts(lesson: Lesson, output_dir: Path) -> tuple[Path, Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    if lesson.format == "modes_v1":
        practice = output_dir / f"transcript_{lesson.variant}.md"
        reference = output_dir / f"transcript_full_{lesson.variant}.md"
    elif lesson.variant == "practice":
        practice = output_dir / "transcript_practice.md"
        reference = output_dir / "transcript_full_practice.md"
    else:
        practice = output_dir / "transcript.md"
        reference = output_dir / "transcript_full.md"
    practice.write_text(render_transcript(lesson), encoding="utf-8")
    reference.write_text(render_transcript(lesson, full_reference=True, show_hanzi=True), encoding="utf-8")
    return practice, reference
