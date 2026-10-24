from __future__ import annotations

import json
import shutil
import subprocess
from dataclasses import asdict
from pathlib import Path

import pytest

from mandarin_course.config import Settings
from mandarin_course import publish_modes
from mandarin_course.publish import PublishError, publish_lesson
from mandarin_course.voice_registry import VoiceProfile


class _TestCharacterLoaders:
    def missing_sources(self):
        return []

    def as_dict(self):
        class Authored(dict):
            def get(self, key, default=None):
                return super().get(key, {"han_viet": "test", "vi_meaning": "test"})

        return {"mmah": {}, "unihan": {}, "hsk30": {}, "cedict": {}, "stroke": set(), "radicals": {}, "characters_vi": Authored()}


@pytest.fixture(autouse=True)
def _test_character_data(monkeypatch):
    monkeypatch.setattr(publish_modes, "CharacterLoaders", _TestCharacterLoaders)


def _write_modes_fixture(tmp_path: Path) -> tuple[Path, Settings]:
    lessons, output, cache = (tmp_path / name for name in ("lessons", "output", "cache"))
    lessons.mkdir(); cache.mkdir(); (output / "lesson_002").mkdir(parents=True)
    for index, mode in enumerate(publish_modes.MODES):
        segments = [
            {"id": f"{mode}_wang", "activity": mode, "type": "narration", "language": "zh-CN", "speaker": "wang_fang", "text": "你好。", "pinyin": "nǐ hǎo.", "translation": "Xin chào.", **({"gap": "answer"} if mode == "phan_xa" else {"gap": "repeat"} if mode == "noi" else {})},
            {"id": f"{mode}_chen", "activity": mode, "type": "prompt", "language": "zh-CN", "speaker": "chen_dong", "text": "再见。", "pinyin": "zài jiàn.", "translation": "Tạm biệt."},
        ]
        data = {"lesson_id": "lesson_002", "title": "Modes fixture", "level": "absolute_beginner", "target_duration_minutes": 5,
                "communicative_goals": ["Test"], "new_patterns": ["你好。"], "version": 1, "format": "modes_v1", "variant": mode, "segments": segments}
        (lessons / f"lesson_002_{mode}.json").write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")
        audio = output / "lesson_002" / f"lesson_002_{mode}.mp3"
        subprocess.run(["ffmpeg", "-y", "-v", "error", "-f", "lavfi", "-i", f"sine=frequency={600 + index}:duration=0.08", str(audio)], check=True)
        (output / "lesson_002" / f"lesson_002_{mode}.timeline.json").write_text(json.dumps({
            "schema_version": 1, "lesson_id": "lesson_002", "lesson_version": 1, "track": mode, "duration_ms": 80,
            "cues": [{"line_id": segment["id"], "start_ms": offset, "end_ms": offset + 40} for offset, segment in ((0, segments[0]), (40, segments[1]))],
        }), encoding="utf-8")
    voices = tmp_path / "voices.json"
    voices.write_text(json.dumps([asdict(VoiceProfile("voice", "Voice", "zh", "beijing", "female"))]), encoding="utf-8")
    return lessons / "lesson_002_hieu.json", Settings(None, "eleven_v4", cache, output, tmp_path / "db.sqlite", voices_path=voices)


def test_publish_modes_package_and_versioning(tmp_path):
    lesson_path, settings = _write_modes_fixture(tmp_path)
    out = tmp_path / "published"
    first = publish_lesson(lesson_path, settings, out)
    assert first.published and first.version == 1
    assert {path.name for path in first.package_dir.iterdir()} == {
        "hieu.mp3", "tu_vung.mp3", "nghe.mp3", "phan_xa.mp3", "noi.mp3", "timeline_hieu.json", "timeline_tu_vung.json", "timeline_nghe.json",
        "timeline_phan_xa.json", "timeline_noi.json", "lesson.json", "reference.json", "items.json", "characters.json",
    }
    catalog = json.loads((out / "catalog.json").read_text())
    entry = catalog["lessons"][0]
    assert entry["format"] == "modes_v1"
    assert list(entry["tracks"]) == ["hieu", "tu_vung", "nghe", "phan_xa", "noi"]
    assert "guided" not in entry["tracks"]
    assert all(track["duration_ms"] > 0 for track in entry["tracks"].values())
    reference = json.loads((first.package_dir / "reference.json").read_text())
    assert list(reference["tracks"]) == list(publish_modes.MODES)
    assert json.loads((first.package_dir / "items.json").read_text())["words"] == []
    assert json.loads((first.package_dir / "timeline_hieu.json").read_text())["gaps"] == []
    assert json.loads((first.package_dir / "timeline_nghe.json").read_text())["gaps"] == []
    assert json.loads((first.package_dir / "timeline_phan_xa.json").read_text())["gaps"] == [{
        "start_ms": 40, "end_ms": 40, "kind": "answer", "after_line_id": "phan_xa_wang",
    }]
    assert json.loads((first.package_dir / "timeline_noi.json").read_text())["gaps"] == [{
        "start_ms": 40, "end_ms": 40, "kind": "repeat", "after_line_id": "noi_wang",
    }]
    assert publish_lesson(lesson_path, settings, out).published is False

    audio = settings.output_dir / "lesson_002" / "lesson_002_noi.mp3"
    subprocess.run(["ffmpeg", "-y", "-v", "error", "-f", "lavfi", "-i", "sine=frequency=999:duration=0.09", str(audio)], check=True)
    assert publish_lesson(lesson_path, settings, out).version == 2


def test_publish_modes_names_missing_audio(tmp_path):
    lesson_path, settings = _write_modes_fixture(tmp_path)
    (settings.output_dir / "lesson_002" / "lesson_002_nghe.mp3").unlink()
    with pytest.raises(PublishError, match="lesson_002_nghe.mp3"):
        publish_lesson(lesson_path, settings, tmp_path / "published")


LESSONS = Path(__file__).resolve().parents[1] / "lessons"


def test_modes_publish_words_and_notes_then_drops_notes_without_source(tmp_path):
    lesson_path, settings = _write_modes_fixture(tmp_path)
    vocab = {"schema_version": 1, "lesson_id": "lesson_002", "status": "draft", "words": [
        {"hanzi": "你好", "pinyin": "nǐ hǎo", "meaning_vi": "xin chào"}, {"hanzi": "再见", "pinyin": "zài jiàn", "meaning_vi": "tạm biệt"}]}
    (lesson_path.parent / "lesson_002_vocab.json").write_text(json.dumps(vocab, ensure_ascii=False), encoding="utf-8")
    shutil.copyfile(LESSONS / "lesson_002_notes.json", lesson_path.parent / "lesson_002_notes.json")
    out = tmp_path / "published"
    report = publish_lesson(lesson_path, settings, out)
    items = json.loads((report.package_dir / "items.json").read_text(encoding="utf-8"))
    assert [(w["hanzi"], w["tones"]) for w in items["words"]] == [("你好", [3, 3]), ("再见", [4, 4])]
    assert all(w["id"] and w["pinyin"] and w["meaning_vi"] for w in items["words"])
    notes = json.loads((report.package_dir / "notes.json").read_text(encoding="utf-8"))
    assert notes["lesson_version"] == 1 and notes["patterns"] and notes["questions"]
    entry = json.loads((out / "catalog.json").read_text(encoding="utf-8"))["lessons"][0]
    assert entry["files"]["notes"]["file"] == "notes.json"
    assert entry["files"]["notes"]["bytes"] == (report.package_dir / "notes.json").stat().st_size
    assert publish_lesson(lesson_path, settings, out).published is False
    (lesson_path.parent / "lesson_002_notes.json").unlink()
    again = publish_lesson(lesson_path, settings, out)
    assert again.published and again.version == 2 and not (again.package_dir / "notes.json").exists()
    assert "notes" not in json.loads((out / "catalog.json").read_text(encoding="utf-8"))["lessons"][0]["files"]


def test_real_lesson_2_reference_tokens_carry_vocab_item_ids():
    from mandarin_course.lesson_schema import load_lesson
    from mandarin_course.publish_timeline import _reference_lines
    from mandarin_course.publish_tokens import load_vocab, word_item_id

    vocab = load_vocab(LESSONS / "lesson_002_vocab.json", "lesson_002")
    ids = {w.hanzi: word_item_id(w.hanzi, w.pinyin) for w in vocab.words}
    failures: list[str] = []
    tokens = [t for mode in publish_modes.MODES
              for line in _reference_lines(load_lesson(LESSONS / f"lesson_002_{mode}.json"), ids, failures)
              for t in line.get("tokens") or []]
    assert not failures
    assert any(t["hanzi"] == "门口" and t.get("item_id") == ids["门口"] for t in tokens)
    assert all(t["item_id"] == ids[t["hanzi"]] for t in tokens if "item_id" in t)
