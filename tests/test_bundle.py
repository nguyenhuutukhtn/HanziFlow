from __future__ import annotations

from pathlib import Path

import pytest

from mandarin_course.config import Settings
from mandarin_course.story.bundle import BundleError, export_lesson, extract_story_source, import_lesson

HEADER = "lesson_id: lesson_009\ntitle: T\nlevel: beginner\nversion: 1\npatterns: 是。\ngoals: g\n"
GUIDED = "# guided\nminutes: 5\n"


def _settings(tmp_path: Path) -> Settings:
    return Settings(
        api_key=None,
        model_id="eleven_v4",
        cache_dir=tmp_path / "cache",
        output_dir=tmp_path / "output",
        db_path=tmp_path / "data" / "progress.db",
        voices_path=tmp_path / "voices.json",
    )


def _lessons_dir_with(tmp_path: Path, lesson_id: str, story_text: str) -> Path:
    lessons_dir = tmp_path / "lessons"
    (lessons_dir / "src").mkdir(parents=True)
    (lessons_dir / "src" / f"{lesson_id}.story.md").write_text(story_text, encoding="utf-8")
    return lessons_dir


def test_export_lesson_001_has_sections_and_matching_story_block(tmp_path):
    story_text = Path("lessons/src/lesson_001.story.md").read_text(encoding="utf-8")
    lessons_dir = _lessons_dir_with(tmp_path, "lesson_001", story_text)

    path = export_lesson("001", lessons_dir)
    bundle = path.read_text(encoding="utf-8")

    assert "# Task: lesson_001" in bundle
    assert "## Authoring checklist" in bundle
    assert "## Already taught" in bundle
    assert "## Story source" in bundle
    assert extract_story_source(bundle) == story_text


def test_export_import_round_trip_leaves_story_source_byte_identical(tmp_path):
    story_text = Path("lessons/src/lesson_001.story.md").read_text(encoding="utf-8")
    lessons_dir = _lessons_dir_with(tmp_path, "lesson_001", story_text)

    bundle_path = export_lesson("001", lessons_dir)
    result = import_lesson("001", bundle_path, lessons_dir, _settings(tmp_path))

    assert result.ok, result.report
    assert (lessons_dir / "src" / "lesson_001.story.md").read_text(encoding="utf-8") == story_text
    assert (lessons_dir / "lesson_001.json").is_file()


def test_import_reports_broken_row_line_number(tmp_path):
    body = GUIDED + "## activity A\n## say\nok | 是。 | shì. | Phải.\nbad | 是。 | shì.\n"
    reply = tmp_path / "reply.md"
    reply.write_text(f"```story\n{HEADER + body}```\n", encoding="utf-8")
    lessons_dir = tmp_path / "lessons"
    (lessons_dir / "src").mkdir(parents=True)

    result = import_lesson("009", reply, lessons_dir, _settings(tmp_path))

    assert not result.ok
    assert "line 12:" in result.report


def test_import_rejects_lesson_id_mismatch(tmp_path):
    reply = tmp_path / "reply.md"
    reply.write_text(f"```story\n{HEADER + GUIDED}```\n", encoding="utf-8")
    lessons_dir = tmp_path / "lessons"
    (lessons_dir / "src").mkdir(parents=True)

    with pytest.raises(BundleError):
        import_lesson("002", reply, lessons_dir, _settings(tmp_path))
