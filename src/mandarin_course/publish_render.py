from __future__ import annotations

from pathlib import Path

from .publish_core import PublishReport


def render_publish_report(report: PublishReport) -> str:
    """Full detail (every file + size): written to a log file, not the default terminal output."""
    if not report.published:
        return f"unchanged v{report.version}\n"
    lines = [f"Published {report.package_dir} (v{report.version})", "Files:"]
    lines.extend(f"  {name}: {size} bytes" for name, size in report.files)
    lines.extend([
        f"Total package size: {report.total_bytes} bytes",
        f"Words with clips ({len(report.words_with_clips)}): {', '.join(report.words_with_clips) or 'none'}",
        f"Words without clips ({len(report.words_without_clips)}): {', '.join(report.words_without_clips) or 'none'}",
        f"Sentences without clips ({len(report.sentences_without_clips)}): {', '.join(report.sentences_without_clips) or 'none'}",
        f"QA without clips ({len(report.qa_without_clips)}): {', '.join(report.qa_without_clips) or 'none'}",
        f"Practice track: {'present' if report.practice_present else 'not present'}",
    ])
    lines.extend(f"Warning: {warning}" for warning in report.warnings)
    return "\n".join(lines) + "\n"


def render_publish_summary(report: PublishReport, log_path: Path | None = None) -> str:
    """Short default output: counts only; `render_publish_report`'s full file list goes to a log."""
    if not report.published:
        return f"unchanged v{report.version}\n"
    lines = [
        f"Published v{report.version}: {len(report.files)} file(s), {report.total_bytes:,} bytes",
        f"Without clips — words: {len(report.words_without_clips)}, sentences: {len(report.sentences_without_clips)}, qa: {len(report.qa_without_clips)}",
        f"Practice track: {'present' if report.practice_present else 'not present'}",
    ]
    if report.warnings:
        lines.append(f"Warning(s): {len(report.warnings)} (see log)")
    if log_path is not None:
        lines.append(f"Detail: {log_path}")
    return "\n".join(lines) + "\n"
