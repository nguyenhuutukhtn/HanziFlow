from __future__ import annotations

import json
import shutil
from pathlib import Path

from mandarin_course.config import Settings
from mandarin_course.story.metrics import compare_metrics
from mandarin_course.story.bundle import BundleError
from mandarin_course.story.rows import export_rows, import_rows


def _settings(tmp_path: Path) -> Settings:
    return Settings(
        api_key=None, model_id="eleven_v4", cache_dir=tmp_path / "cache",
        output_dir=tmp_path / "output", db_path=tmp_path / "data" / "progress.db",
        voices_path=tmp_path / "voices.json",
    )


def test_metrics_model_lesson_is_all_ok():
    result = compare_metrics(Path("lessons"), "lesson_001", "lesson_001")
    assert set(result["status"].values()) == {"ok"}


def test_metrics_flags_word_heard_twice(tmp_path):
    lessons = tmp_path / "lessons"
    lessons.mkdir()
    (lessons / "lesson_001_vocab.json").write_text(json.dumps({"words": [{"hanzi": "妈妈"}]}), encoding="utf-8")
    (lessons / "lesson_001.json").write_text(json.dumps({"lesson_id": "lesson_001", "segments": [{"language": "zh-CN", "text": "妈妈"}]}), encoding="utf-8")
    (lessons / "lesson_002_vocab.json").write_text(json.dumps({"words": [{"hanzi": "爸爸"}]}), encoding="utf-8")
    (lessons / "lesson_002.json").write_text(json.dumps({"lesson_id": "lesson_002", "segments": [{"language": "zh-CN", "text": "爸爸爸爸"}]}), encoding="utf-8")
    result = compare_metrics(lessons, "lesson_002", "lesson_001")
    assert result["metrics"]["repeats_min"] == 2
    assert result["status"]["low_repeat_words"] == "FLAG"


def test_export_import_rows_changes_only_target_row(tmp_path):
    lessons = tmp_path / "lessons"
    shutil.copytree("lessons", lessons)
    story = lessons / "src" / "lesson_001.story.md"
    before = story.read_text(encoding="utf-8").splitlines()
    bundle = export_rows("001", ["scene_bag"], lessons)
    target = next(line for line in bundle.read_text(encoding="utf-8").splitlines() if line.startswith("scene_bag |"))
    replacement = target.replace("| 700", "| 701")
    reply = tmp_path / "rows.md"
    reply.write_text(f"```rows\n{replacement}\n```\n", encoding="utf-8")
    result = import_rows("001", reply, lessons, _settings(tmp_path))
    assert result.ok, result.report
    after = story.read_text(encoding="utf-8").splitlines()
    changed = [index for index, (old, new) in enumerate(zip(before, after)) if old != new]
    assert len(changed) == 1
    assert before[changed[0]].startswith("scene_bag |")


def test_repeated_row_ref_is_qualified_and_writes_bare_ref(tmp_path):
    lessons = tmp_path / "lessons"
    shutil.copytree("lessons", lessons)
    story = lessons / "src" / "lesson_001.story.md"
    before = story.read_text(encoding="utf-8").splitlines()
    bundle = export_rows("001", ["name_ts#3"], lessons)
    text = bundle.read_text(encoding="utf-8")
    assert "### name_ts#3 ·" in text
    target = next(line for line in text.splitlines() if line.startswith("name_ts#3 |"))
    reply = tmp_path / "rows.md"
    reply.write_text(f"```rows\n{target.replace('| 不是', '| 是')}\n```\n", encoding="utf-8")
    result = import_rows("001", reply, lessons, _settings(tmp_path))
    assert result.ok, result.report
    after = story.read_text(encoding="utf-8").splitlines()
    changed = [index for index, (old, new) in enumerate(zip(before, after)) if old != new]
    assert changed == [323]
    assert after[323].startswith("name_ts |")


def test_bare_repeated_row_ref_lists_qualified_choices_without_writing(tmp_path):
    lessons = tmp_path / "lessons"
    shutil.copytree("lessons", lessons)
    story = lessons / "src" / "lesson_001.story.md"
    before = story.read_bytes()
    reply = tmp_path / "rows.md"
    reply.write_text("```rows\nname_ts | replacement\n```\n", encoding="utf-8")
    try:
        import_rows("001", reply, lessons, _settings(tmp_path))
    except BundleError as exc:
        assert "name_ts#1" in str(exc) and "name_ts#2" in str(exc) and "name_ts#3" in str(exc)
    else:
        raise AssertionError("ambiguous bare ref was accepted")
    assert story.read_bytes() == before
