from __future__ import annotations

import hashlib
import json
import subprocess
from dataclasses import asdict
from pathlib import Path

import pytest

from mandarin_course.config import Settings
from mandarin_course.lesson_schema import Lesson, load_lesson
from mandarin_course.publish import (
    PublishError,
    _reference_lines,
    publish_lesson,
    sentence_item_id,
    tokenize_mandarin,
    word_item_id,
)
from mandarin_course.tts import cache_key, voice_ids_for
from mandarin_course.voice_registry import VoiceProfile


def test_tokenizer_aligns_words_names_and_punctuation():
    tokens = tokenize_mandarin("这是外卖吗？安娜！", "zhè shì wàimài ma? Ānnà!")
    assert tokens == [
        {"hanzi": "这", "pinyin": "zhè"},
        {"hanzi": "是", "pinyin": "shì"},
        {"hanzi": "外卖", "pinyin": "wàimài"},
        {"hanzi": "吗", "pinyin": "ma"},
        {"hanzi": "？"},
        {"hanzi": "安娜", "pinyin": "Ānnà"},
        {"hanzi": "！"},
    ]


def test_tokenizer_reports_bad_alignment():
    with pytest.raises(ValueError, match="does not align"):
        tokenize_mandarin("这是外卖。", "zhè shì kāfēi.")


def test_dialogue_turns_become_individual_reference_lines(lesson_data):
    lesson_data["segments"] = [{
        "id": "exchange", "activity": "a", "type": "dialogue", "language": "zh-CN",
        "text": "你好。你好！", "pinyin": "nǐ hǎo. nǐ hǎo!", "translation": "Xin chào.",
        "turns": [
            {"speaker": "anna", "text": "你好。", "pinyin": "nǐ hǎo.", "translation": "Xin chào."},
            {"speaker": "li_ming", "text": "你好！", "pinyin": "nǐ hǎo!", "translation": "Chào bạn!"},
        ],
    }]
    failures = []
    lines = _reference_lines(Lesson.model_validate(lesson_data), {}, failures)
    assert failures == []
    assert [line["id"] for line in lines] == ["exchange_turn_1", "exchange_turn_2"]
    assert [line["speaker"] for line in lines] == ["anna", "li_ming"]


def test_stable_item_ids_do_not_depend_on_authoring_order():
    words = [("外卖", "wàimài"), ("同事", "tóngshì")]
    forward = {hanzi: word_item_id(hanzi, pinyin) for hanzi, pinyin in words}
    reverse = {hanzi: word_item_id(hanzi, pinyin) for hanzi, pinyin in reversed(words)}
    assert forward == reverse
    assert forward["外卖"] == "w_外卖_wai4mai4"
    assert sentence_item_id("这是外卖。") == sentence_item_id("这是外卖。")


def _write_fixture(tmp_path: Path, *, practice_audio: bool = False, with_turns: bool = False) -> tuple[Path, Settings]:
    lessons = tmp_path / "lessons"
    output = tmp_path / "output"
    cache = tmp_path / "cache"
    lessons.mkdir()
    (output / "lesson_001").mkdir(parents=True)
    cache.mkdir()
    lesson = {
        "lesson_id": "lesson_001", "title": "Fixture", "level": "absolute_beginner",
        "target_duration_minutes": 5, "communicative_goals": ["Test"], "new_patterns": ["这是外卖。"],
        "version": 2, "variant": "guided", "segments": [
            {"id": "word", "activity": "a", "type": "pattern", "language": "zh-CN", "speaker": "coach_zh", "text": "外卖。", "pinyin": "wàimài.", "translation": "đồ ăn mang về"},
            {"id": "q", "activity": "a", "type": "prompt", "language": "zh-CN", "speaker": "coach_zh", "text": "这是外卖吗？", "pinyin": "zhè shì wàimài ma?", "translation": "Đây là đồ ăn mang về phải không?"},
            {"id": "q_a", "activity": "a", "type": "model_answer", "language": "zh-CN", "speaker": "coach_zh", "text": "是。", "pinyin": "shì.", "translation": "Phải."},
        ],
    }
    if with_turns:
        lesson["segments"].append({
            "id": "exchange", "activity": "story", "type": "dialogue", "language": "zh-CN",
            "text": "你好。你好！", "pinyin": "nǐ hǎo. nǐ hǎo!", "translation": "Xin chào.",
            "repetitions": 2,
            "turns": [
                {"speaker": "anna", "text": "你好。", "pinyin": "nǐ hǎo.", "translation": "Xin chào."},
                {"speaker": "li_ming", "text": "你好！", "pinyin": "nǐ hǎo!", "translation": "Chào bạn!"},
            ],
        })
    lesson_path = lessons / "lesson_001.json"
    lesson_path.write_text(json.dumps(lesson, ensure_ascii=False), encoding="utf-8")
    practice = {**lesson, "variant": "practice", "segments": lesson["segments"][:1]}
    (lessons / "lesson_001_practice.json").write_text(json.dumps(practice, ensure_ascii=False), encoding="utf-8")
    (lessons / "lesson_001_vocab.json").write_text(json.dumps({
        "schema_version": 1, "lesson_id": "lesson_001", "status": "draft",
        "words": [{"hanzi": "外卖", "pinyin": "wàimài", "meaning_vi": "đồ ăn mang về"}],
    }, ensure_ascii=False), encoding="utf-8")
    voices = tmp_path / "voices.json"
    voices.write_text(json.dumps([asdict(VoiceProfile("voice", "Voice", "zh", "beijing", "female"))]), encoding="utf-8")
    settings = Settings(None, "eleven_v4", cache, output, tmp_path / "db.sqlite", voices_path=voices)
    subprocess.run(["ffmpeg", "-y", "-v", "error", "-f", "lavfi", "-i", "sine=frequency=600:duration=0.08", str(output / "lesson_001" / "lesson_001_guided.mp3")], check=True)
    (output / "lesson_001" / "lesson_001_guided.timeline.json").write_text(json.dumps({
        "schema_version": 1, "lesson_id": "lesson_001", "lesson_version": 2,
        "track": "guided", "duration_ms": 80,
        "cues": [{"line_id": segment["id"], "start_ms": 0, "end_ms": 50} for segment in lesson["segments"]],
    }), encoding="utf-8")
    if practice_audio:
        subprocess.run(["ffmpeg", "-y", "-v", "error", "-f", "lavfi", "-i", "sine=frequency=700:duration=0.08", str(output / "lesson_001" / "lesson_001_practice.mp3")], check=True)
        (output / "lesson_001" / "lesson_001_practice.timeline.json").write_text(json.dumps({
            "schema_version": 1, "lesson_id": "lesson_001", "lesson_version": 2,
            "track": "practice", "duration_ms": 80,
            "cues": [{"line_id": "word", "start_ms": 0, "end_ms": 50}],
        }), encoding="utf-8")
    parsed = load_lesson(lesson_path)
    for segment in parsed.segments:
        ids = voice_ids_for(segment, settings, parsed)
        key = cache_key(segment, ids if segment.turns else ids[0], settings.model_id, settings.output_format)
        subprocess.run(["ffmpeg", "-y", "-v", "error", "-f", "lavfi", "-i", "sine=frequency=800:duration=0.05", str(cache / f"{key}.mp3")], check=True)
    return lesson_path, settings


def test_publish_is_deterministic_and_records_correct_digests(tmp_path):
    lesson_path, settings = _write_fixture(tmp_path, practice_audio=True)
    out = tmp_path / "published"
    report = publish_lesson(lesson_path, settings, out)
    first_catalog = (out / "catalog.json").read_bytes()
    publish_lesson(lesson_path, settings, out, force=True)
    assert (out / "catalog.json").read_bytes() == first_catalog
    catalog = json.loads(first_catalog)
    guided = out / "lessons/lesson_001/v2/guided.mp3"
    descriptor = catalog["lessons"][0]["tracks"]["guided"]
    assert descriptor["bytes"] == guided.stat().st_size
    assert descriptor["sha256"] == hashlib.sha256(guided.read_bytes()).hexdigest()
    timeline_path = report.package_dir / descriptor["timeline"]["file"]
    assert descriptor["timeline"]["bytes"] == timeline_path.stat().st_size
    assert descriptor["timeline"]["sha256"] == hashlib.sha256(timeline_path.read_bytes()).hexdigest()
    items = json.loads((report.package_dir / "items.json").read_text())
    clip = items["words"][0]["clip"]
    clip_path = report.package_dir / clip["file"]
    assert clip["bytes"] == clip_path.stat().st_size
    assert clip["sha256"] == hashlib.sha256(clip_path.read_bytes()).hexdigest()
    assert items["sentences"][0]["activity"] == "a"
    assert items["sentences"][0]["speaker"] == "coach_zh"
    assert items["qa"][0]["activity"] == "a"
    assert items["qa"][0]["speaker"] == "coach_zh"


def test_publish_refuses_existing_version_without_force(tmp_path):
    lesson_path, settings = _write_fixture(tmp_path)
    out = tmp_path / "published"
    publish_lesson(lesson_path, settings, out)
    with pytest.raises(PublishError, match="--force"):
        publish_lesson(lesson_path, settings, out)


def test_publish_omits_missing_practice_and_warns(tmp_path):
    lesson_path, settings = _write_fixture(tmp_path)
    report = publish_lesson(lesson_path, settings, tmp_path / "published")
    catalog = json.loads((tmp_path / "published/catalog.json").read_text())
    assert report.practice_present is False
    assert "practice" not in catalog["lessons"][0]["tracks"]
    assert any("Practice track omitted" in warning for warning in report.warnings)


def test_publish_fails_when_track_timeline_is_missing(tmp_path):
    lesson_path, settings = _write_fixture(tmp_path)
    (settings.output_dir / "lesson_001/lesson_001_guided.timeline.json").unlink()
    with pytest.raises(PublishError, match="guided track has no timeline"):
        publish_lesson(lesson_path, settings, tmp_path / "published")


def test_publish_accepts_grouped_turn_cues(tmp_path):
    lesson_path, settings = _write_fixture(tmp_path, with_turns=True)
    lesson = load_lesson(lesson_path)

    class CacheOnly:
        def synthesize(self, *args, **kwargs):
            raise AssertionError("fixture must build entirely from cache")

    from mandarin_course.audio_builder import build_audio
    from mandarin_course.tts import CachedTTS

    audio = settings.output_dir / "lesson_001/lesson_001_guided.mp3"
    build_audio(lesson, CachedTTS(CacheOnly(), settings), audio)
    report = publish_lesson(lesson_path, settings, tmp_path / "published")
    timeline = json.loads((report.package_dir / "timeline_guided.json").read_text())
    grouped = [cue for cue in timeline["cues"] if cue.get("granularity") == "segment"]
    assert len(grouped) == 2
    assert grouped[0]["line_ids"] == ["exchange_turn_1", "exchange_turn_2"]
