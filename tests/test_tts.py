from pathlib import Path

import pytest

from mandarin_course.config import Settings
from mandarin_course.lesson_schema import DialogueTurn
from mandarin_course.tts import CachedTTS, TTSError, api_voice_settings, cache_key, voice_ids_for


class FakeTTS:
    def __init__(self):
        self.calls = 0

    def synthesize(self, segment, destination: Path, lesson):
        self.calls += 1
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(b"audio")


class BrokenTTS:
    def synthesize(self, segment, destination: Path, lesson):
        raise TTSError("network failed")


def settings(tmp_path, voices_path: Path | None = None):
    return Settings(
        "test", "model", tmp_path / "cache", tmp_path / "out", tmp_path / "db",
        voices_path=voices_path or tmp_path / "no_such_voices.json",
    )


def test_cache_miss_then_hit(tmp_path, lesson, voices_registry):
    provider = FakeTTS()
    cached = CachedTTS(provider, settings(tmp_path, voices_registry))
    first = cached.get(lesson.segments[0], lesson)
    second = cached.get(lesson.segments[0], lesson)
    assert first == second
    assert provider.calls == 1


def test_cache_key_changes_with_text(tmp_path, lesson):
    segment = lesson.segments[0]
    changed = segment.model_copy(update={"text": "您好。"})
    assert cache_key(segment, "voice", "model", "mp3") != cache_key(changed, "voice", "model", "mp3")


def test_cache_key_ignores_gap_metadata(lesson):
    segment = lesson.segments[0]
    gapped = segment.model_copy(update={"gap": "answer"})
    assert cache_key(segment, "voice", "model", "mp3") == cache_key(gapped, "voice", "model", "mp3")


def test_tts_failure_does_not_create_success_file(tmp_path, lesson, voices_registry):
    cached = CachedTTS(BrokenTTS(), settings(tmp_path, voices_registry))
    with pytest.raises(TTSError, match="network"):
        cached.get(lesson.segments[0], lesson)
    assert not list((tmp_path / "cache").glob("*.mp3"))


def test_v4_uses_only_supported_voice_settings(lesson):
    values = api_voice_settings(lesson.segments[0], "eleven_v4")
    assert values == {"stability": 0.55, "similarity_boost": 0.75}


def test_multilingual_v2_keeps_speed_and_speaker_boost(lesson):
    values = api_voice_settings(lesson.segments[0], "eleven_multilingual_v2")
    assert values["speed"] == 1.0
    assert values["use_speaker_boost"] is True


def test_speaker_roles_resolve_distinct_voices(tmp_path, lesson, voices_registry):
    configured = settings(tmp_path, voices_registry)
    dialogue = lesson.segments[0].model_copy(update={
        "speaker": None,
        "type": "dialogue",
        "turns": [
            DialogueTurn(speaker="anna", text="你好！"),
            DialogueTurn(speaker="li_ming", text="你好！"),
        ],
    })
    voice_ids = voice_ids_for(dialogue, configured, lesson)
    assert set(voice_ids) == {"anna-voice", "li-ming-voice"}
    assert voice_ids[0] != voice_ids[1]
    assert cache_key(dialogue, voice_ids, "eleven_v4", "mp3") != cache_key(
        dialogue, list(reversed(voice_ids)), "eleven_v4", "mp3"
    )


def test_vietnamese_coach_uses_a_registry_voice(tmp_path, lesson, voices_registry):
    configured = settings(tmp_path, voices_registry)
    guide = lesson.segments[0].model_copy(update={
        "language": "vi-VN",
        "speaker": "coach",
        "type": "narration",
        "text": "Bây giờ hãy nghe.",
        "pinyin": None,
        "translation": None,
    })
    assert voice_ids_for(guide, configured, lesson) == ["vietnamese-coach"]


def test_coach_zh_resolves_a_mandarin_voice(tmp_path, lesson, voices_registry):
    configured = settings(tmp_path, voices_registry)
    guide = lesson.segments[0].model_copy(update={
        "speaker": "coach_zh",
        "type": "narration",
        "text": "现在听一遍。",
        "pinyin": "xiànzài tīng yī biàn",
        "translation": "Now listen once.",
    })
    voice_ids = voice_ids_for(guide, configured, lesson)
    # A single role with no sibling to share the pool with always gets the top-priority
    # (Beijing, for an absolute_beginner lesson) voice.
    assert voice_ids == ["anna-voice"]


def test_picking_is_deterministic_across_calls(tmp_path, lesson, voices_registry):
    configured = settings(tmp_path, voices_registry)
    first = voice_ids_for(lesson.segments[0], configured, lesson)
    second = voice_ids_for(lesson.segments[0], configured, lesson)
    assert first == second


def test_missing_voice_registry_is_reported(tmp_path):
    configured = settings(tmp_path)
    assert configured.missing_tts_fields({"vi-VN"}) == [
        f"voice registry at {configured.resolved_voices_path()} (run `mandarin-course voices sync`)"
    ]


def test_missing_language_in_registry_is_reported(tmp_path, voices_registry):
    configured = settings(tmp_path, voices_registry)
    assert configured.missing_tts_fields({"en-US"}) == [
        "voices for en-US in registry (run `mandarin-course voices sync`)"
    ]
    assert configured.missing_tts_fields({"vi-VN", "zh-CN"}) == []
