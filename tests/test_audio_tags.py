from pathlib import Path

import pytest

from mandarin_course.config import Settings
from mandarin_course.lesson_schema import DialogueTurn
from mandarin_course.tts import (
    CachedTTS,
    ElevenLabsTTS,
    cache_key,
    normalize_tts_tags,
    render_tts_text,
    supports_audio_tags,
    tag_usage,
)
from mandarin_course.transcript import render_transcript


def settings(tmp_path: Path, voices_path: Path) -> Settings:
    return Settings(
        "test", "eleven_v4", tmp_path / "cache", tmp_path / "out", tmp_path / "db",
        voices_path=voices_path,
    )


# 1. No tag -> text unchanged.
def test_no_tags_leaves_text_unchanged():
    assert render_tts_text("Mình cùng nghe một câu chuyện ngắn nhé.", [], "eleven_v4") == (
        "Mình cùng nghe một câu chuyện ngắn nhé."
    )


# 2. One tag -> "[tag] text".
def test_single_tag_prefixes_text():
    assert render_tts_text("Mình cùng nghe một câu chuyện ngắn nhé.", ["gentle"], "eleven_v4") == (
        "[gentle] Mình cùng nghe một câu chuyện ngắn nhé."
    )


# 3. Multiple tags -> "[tag1][tag2] text".
def test_multiple_tags_join_with_no_space_between_brackets():
    assert render_tts_text("hello", ["gentle", "encouraging"], "eleven_v4") == (
        "[gentle][encouraging] hello"
    )


# 4. Tag never appears in transcript/pinyin/translation.
def test_tags_do_not_leak_into_transcript(lesson_data):
    lesson_data["segments"][0]["tts_tags"] = ["gentle"]
    from mandarin_course.lesson_schema import Lesson

    lesson = Lesson.model_validate(lesson_data)
    rendered = render_transcript(lesson, full_reference=True)
    assert "[gentle]" not in rendered
    assert "gentle" not in rendered


# 5. Cache key changes when tags change; unchanged (empty) tags keep the old key shape.
def test_cache_key_changes_with_tags(lesson):
    base = lesson.segments[0]
    tagged = base.model_copy(update={"tts_tags": ["gentle"]})
    other_tag = base.model_copy(update={"tts_tags": ["curious"]})
    assert cache_key(base, "voice", "eleven_v4", "mp3") != cache_key(tagged, "voice", "eleven_v4", "mp3")
    assert cache_key(tagged, "voice", "eleven_v4", "mp3") != cache_key(other_tag, "voice", "eleven_v4", "mp3")


def test_untagged_segment_cache_key_is_unaffected_by_the_tts_tags_field(lesson):
    # A segment that never sets tts_tags (the default []) must hash exactly as it
    # did before this feature existed, so every already-cached clip stays valid.
    segment = lesson.segments[0]
    assert segment.tts_tags == []
    key = cache_key(segment, "voice", "eleven_v4", "mp3")
    explicit_empty = segment.model_copy(update={"tts_tags": []})
    assert cache_key(explicit_empty, "voice", "eleven_v4", "mp3") == key


def test_turns_without_tags_keep_the_pre_feature_cache_key_shape(lesson):
    dialogue = lesson.segments[0].model_copy(update={
        "speaker": None,
        "type": "dialogue",
        "turns": [DialogueTurn(speaker="anna", text="你好！")],
    })
    key = cache_key(dialogue, ["voice"], "eleven_v4", "mp3")
    rebuilt = dialogue.model_copy(update={
        "turns": [DialogueTurn(speaker="anna", text="你好！", tts_tags=[])],
    })
    assert cache_key(rebuilt, ["voice"], "eleven_v4", "mp3") == key


# 6 & 7. Segment and per-turn tags are applied to the actual TTS request text.
class _CapturingTextToSpeech:
    def __init__(self):
        self.calls = []

    def convert(self, *, text, voice_id, model_id, output_format, voice_settings):
        self.calls.append(text)
        return [b"audio"]


class _CapturingTextToDialogue:
    def __init__(self):
        self.calls = []

    def convert(self, *, inputs, model_id, output_format):
        self.calls.append(inputs)
        return [b"audio"]


class _FakeClient:
    def __init__(self):
        self.text_to_speech = _CapturingTextToSpeech()
        self.text_to_dialogue = _CapturingTextToDialogue()


def test_segment_tag_reaches_the_tts_request(tmp_path, lesson, voices_registry, monkeypatch):
    fake = _FakeClient()
    monkeypatch.setattr("elevenlabs.client.ElevenLabs", lambda api_key: fake)
    provider = ElevenLabsTTS(settings(tmp_path, voices_registry))
    segment = lesson.segments[0].model_copy(update={"tts_tags": ["gentle"]})
    provider.synthesize(segment, tmp_path / "out.mp3", lesson)
    assert fake.text_to_speech.calls == [f"[gentle] {segment.text}"]


def test_each_turn_gets_its_own_tag(tmp_path, lesson, voices_registry, monkeypatch):
    fake = _FakeClient()
    monkeypatch.setattr("elevenlabs.client.ElevenLabs", lambda api_key: fake)
    provider = ElevenLabsTTS(settings(tmp_path, voices_registry))
    dialogue = lesson.segments[0].model_copy(update={
        "speaker": None,
        "type": "dialogue",
        "turns": [
            DialogueTurn(speaker="anna", text="你好！", tts_tags=["curious"]),
            DialogueTurn(speaker="li_ming", text="你好！"),
        ],
    })
    provider.synthesize(dialogue, tmp_path / "out.mp3", lesson)
    sent = fake.text_to_dialogue.calls[0]
    assert sent[0]["text"] == "[curious] 你好！"
    assert sent[1]["text"] == "你好！"


# 8. Unsupported model: tags are dropped safely, text unchanged.
def test_unsupported_model_drops_tags_without_error():
    assert render_tts_text("hello", ["gentle"], "eleven_monolingual_v1") == "hello"


def test_supports_audio_tags_matrix():
    assert supports_audio_tags("eleven_v4") is True
    assert supports_audio_tags("eleven_v4_turbo") is True
    assert supports_audio_tags("eleven_v3") is True
    assert supports_audio_tags("eleven_monolingual_v1") is False


# 9. Empty or oversized tags, and too many tags, are rejected.
def test_empty_tag_is_rejected():
    with pytest.raises(ValueError, match="empty"):
        normalize_tts_tags([""])


def test_oversized_tag_is_rejected():
    with pytest.raises(ValueError, match="too long"):
        normalize_tts_tags(["x" * 33])


def test_invalid_characters_are_rejected():
    with pytest.raises(ValueError, match="unsupported characters"):
        normalize_tts_tags(["gentle!"])


def test_too_many_tags_is_rejected():
    with pytest.raises(ValueError, match="at most 4"):
        normalize_tts_tags(["a", "b", "c", "d", "e"])


def test_author_supplied_brackets_are_stripped():
    assert normalize_tts_tags(["[gentle]"]) == ["gentle"]


# dry-run tag usage report (segments-with-tags count + per-tag frequency).
def test_tag_usage_counts_segments_and_tags(lesson_data):
    from mandarin_course.lesson_schema import Lesson

    lesson_data["segments"][0]["tts_tags"] = ["gentle", "curious"]
    lesson = Lesson.model_validate(lesson_data)
    count, tags = tag_usage(lesson)
    assert count == 1
    assert tags == {"gentle": 1, "curious": 1}


def test_tag_usage_is_zero_for_a_lesson_with_no_tags(lesson):
    count, tags = tag_usage(lesson)
    assert count == 0
    assert tags == {}
