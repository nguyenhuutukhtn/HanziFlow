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
    _enrich_timeline,
    _reference_lines,
    publish_lesson,
    render_publish_report,
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
    assert report.published is True
    assert report.version == 1
    first_catalog = (out / "catalog.json").read_bytes()
    second = publish_lesson(lesson_path, settings, out)
    assert second.published is False
    assert second.version == 1
    assert (out / "catalog.json").read_bytes() == first_catalog
    catalog = json.loads(first_catalog)
    guided = out / "lessons/lesson_001/v1/guided.mp3"
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
    # Every file in the package agrees on the version.
    manifest = json.loads((report.package_dir / "lesson.json").read_text())
    guided_timeline = json.loads((report.package_dir / "timeline_guided.json").read_text())
    practice_timeline = json.loads((report.package_dir / "timeline_practice.json").read_text())
    assert manifest["version"] == items["lesson_version"] == guided_timeline["lesson_version"] == practice_timeline["lesson_version"] == 1


def test_publish_ignores_the_lesson_json_version_field(tmp_path):
    """The authored `version` field is kept in the schema but never drives packaging."""
    lesson_path, settings = _write_fixture(tmp_path)
    lesson_data = json.loads(lesson_path.read_text(encoding="utf-8"))
    assert lesson_data["version"] == 2  # authored value, unrelated to the assigned package version
    report = publish_lesson(lesson_path, settings, tmp_path / "published")
    assert report.version == 1
    assert report.package_dir.name == "v1"


def test_publish_unchanged_reports_no_new_version(tmp_path):
    lesson_path, settings = _write_fixture(tmp_path)
    out = tmp_path / "published"
    first = publish_lesson(lesson_path, settings, out)
    assert render_publish_report(first).startswith("Published")

    second = publish_lesson(lesson_path, settings, out)

    assert second.published is False
    assert second.version == first.version == 1
    assert second.package_dir == first.package_dir
    assert render_publish_report(second) == "unchanged v1\n"
    # Nothing was written: still exactly one version directory.
    assert sorted(p.name for p in (out / "lessons/lesson_001").iterdir()) == ["v1"]


def test_publish_changed_content_bumps_to_next_version_and_keeps_the_old_one(tmp_path):
    lesson_path, settings = _write_fixture(tmp_path, practice_audio=True)
    out = tmp_path / "published"
    first = publish_lesson(lesson_path, settings, out)
    assert first.version == 1

    vocab_path = lesson_path.parent / "lesson_001_vocab.json"
    vocab = json.loads(vocab_path.read_text(encoding="utf-8"))
    vocab["words"][0]["meaning_vi"] = "changed meaning"
    vocab_path.write_text(json.dumps(vocab, ensure_ascii=False), encoding="utf-8")

    second = publish_lesson(lesson_path, settings, out)

    assert second.published is True
    assert second.version == 2
    assert second.package_dir.name == "v2"
    # The old version is kept locally until upload has verified the new one.
    assert (out / "lessons/lesson_001/v1").is_dir()
    assert (out / "lessons/lesson_001/v2").is_dir()
    catalog = json.loads((out / "catalog.json").read_text())
    assert catalog["lessons"][0]["version"] == 2
    assert catalog["lessons"][0]["base_url"] == "lessons/lesson_001/v2/"
    items = json.loads((second.package_dir / "items.json").read_text())
    assert items["lesson_version"] == 2
    manifest = json.loads((second.package_dir / "lesson.json").read_text())
    assert manifest["version"] == 2
    # Every file in the package must agree on the version — including both timelines.
    guided_timeline = json.loads((second.package_dir / "timeline_guided.json").read_text())
    assert guided_timeline["lesson_version"] == 2
    practice_timeline = json.loads((second.package_dir / "timeline_practice.json").read_text())
    assert practice_timeline["lesson_version"] == 2


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


def _timeline(cues: list[dict], duration_ms: int = 1000) -> dict:
    return {
        "schema_version": 1, "lesson_id": "lesson_999", "lesson_version": 1,
        "track": "guided", "duration_ms": duration_ms, "cues": cues,
    }


def test_enrich_timeline_joins_language_type_and_section_including_turns_and_repetitions(lesson_data):
    lesson_data["segments"] = [
        {"id": "a", "activity": "Nghe chuyện", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "你好。", "pinyin": "nǐ hǎo", "translation": "Xin chào.",
         "repetitions": 2},
        {"id": "exchange", "activity": "Nghe chuyện", "type": "dialogue", "language": "zh-CN",
         "text": "你好。你好！", "pinyin": "nǐ hǎo. nǐ hǎo!", "translation": "Xin chào.",
         "turns": [
             {"speaker": "anna", "text": "你好。", "pinyin": "nǐ hǎo.", "translation": "Xin chào."},
             {"speaker": "li_ming", "text": "你好！", "pinyin": "nǐ hǎo!", "translation": "Chào bạn!"},
         ]},
        {"id": "b", "activity": "Hỏi đáp", "type": "prompt", "language": "vi-VN",
         "speaker": "coach", "text": "Hỏi gì?", "pinyin": None, "translation": None},
    ]
    lesson = Lesson.model_validate(lesson_data)
    timeline = _timeline([
        {"line_id": "a", "start_ms": 0, "end_ms": 10},
        {"line_id": "a", "start_ms": 10, "end_ms": 20},
        {"line_id": "exchange_turn_1", "line_ids": ["exchange_turn_1", "exchange_turn_2"],
         "granularity": "segment", "start_ms": 20, "end_ms": 30},
        {"line_id": "b", "start_ms": 30, "end_ms": 40},
    ])

    enriched = _enrich_timeline(timeline, lesson, "guided", 3)

    assert enriched["lesson_version"] == 3
    assert [(cue["language"], cue["type"], cue["section_id"]) for cue in enriched["cues"]] == [
        ("zh-CN", "narration", "sec_01"),
        ("zh-CN", "narration", "sec_01"),
        ("zh-CN", "dialogue", "sec_01"),
        ("vi-VN", "prompt", "sec_02"),
    ]
    assert enriched["sections"] == [
        {"id": "sec_01", "title": "Nghe chuyện", "start_ms": 0, "end_ms": 30},
        {"id": "sec_02", "title": "Hỏi đáp", "start_ms": 30, "end_ms": 1000},
    ]


def test_enrich_timeline_sections_are_contiguous_and_cover_the_track(lesson_data):
    lesson_data["segments"] = [
        {"id": "a", "activity": "one", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "你好。", "pinyin": "nǐ hǎo", "translation": "Xin chào."},
        {"id": "b", "activity": "two", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "再见。", "pinyin": "zài jiàn", "translation": "Tạm biệt."},
        {"id": "c", "activity": "two", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "谢谢。", "pinyin": "xiè xiè", "translation": "Cảm ơn."},
        {"id": "d", "activity": "three", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "没事。", "pinyin": "méi shì", "translation": "Không có gì."},
    ]
    lesson = Lesson.model_validate(lesson_data)
    timeline = _timeline([
        {"line_id": "a", "start_ms": 0, "end_ms": 10},
        {"line_id": "b", "start_ms": 10, "end_ms": 20},
        {"line_id": "c", "start_ms": 20, "end_ms": 30},
        {"line_id": "d", "start_ms": 30, "end_ms": 40},
    ], duration_ms=40)

    enriched = _enrich_timeline(timeline, lesson, "guided", 1)

    sections = enriched["sections"]
    assert [section["id"] for section in sections] == ["sec_01", "sec_02", "sec_03"]
    assert [section["title"] for section in sections] == ["one", "two", "three"]
    assert sections[0]["start_ms"] == 0
    assert sections[-1]["end_ms"] == 40
    for earlier, later in zip(sections, sections[1:]):
        assert earlier["end_ms"] == later["start_ms"]
    cue_section_ids = {cue["section_id"] for cue in enriched["cues"]}
    assert cue_section_ids == {section["id"] for section in sections}


def test_enrich_timeline_fails_when_cue_has_no_matching_segment(lesson):
    timeline = _timeline([{"line_id": "ghost", "start_ms": 0, "end_ms": 10}])
    with pytest.raises(PublishError, match="no longer matches"):
        _enrich_timeline(timeline, lesson, "guided", 1)


def test_enrich_timeline_fails_when_segment_order_differs(lesson_data):
    lesson_data["segments"] = [
        {"id": "a", "activity": "x", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "你好。", "pinyin": "nǐ hǎo", "translation": "Xin chào."},
        {"id": "b", "activity": "x", "type": "narration", "language": "zh-CN",
         "speaker": "coach_zh", "text": "再见。", "pinyin": "zài jiàn", "translation": "Tạm biệt."},
    ]
    lesson = Lesson.model_validate(lesson_data)
    timeline = _timeline([
        {"line_id": "b", "start_ms": 0, "end_ms": 10},
        {"line_id": "a", "start_ms": 10, "end_ms": 20},
    ])
    with pytest.raises(PublishError, match="Regenerate the audio"):
        _enrich_timeline(timeline, lesson, "guided", 1)


def test_publish_writes_enriched_sections_and_cue_metadata(tmp_path):
    lesson_path, settings = _write_fixture(tmp_path, practice_audio=True)
    report = publish_lesson(lesson_path, settings, tmp_path / "published")
    timeline = json.loads((report.package_dir / "timeline_guided.json").read_text())
    assert timeline["sections"] == [
        {"id": "sec_01", "title": "a", "start_ms": 0, "end_ms": 80},
    ]
    for cue in timeline["cues"]:
        assert cue["section_id"] == "sec_01"
        assert cue["language"] == "zh-CN"
        assert cue["type"] in {"pattern", "prompt", "model_answer"}
