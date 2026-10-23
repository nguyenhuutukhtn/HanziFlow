"""ElevenLabs TTS adapter and deterministic audio cache."""

from __future__ import annotations

import hashlib
import json
import re
from collections import Counter
from pathlib import Path
from typing import Protocol

from .config import Settings
from .lesson_schema import Lesson, Segment
from .voice_registry import load_registry, preferred_accent_for_level, resolve_roles_with_pinning


class TTSError(RuntimeError):
    pass


class TTSProvider(Protocol):
    def synthesize(self, segment: Segment, destination: Path, lesson: Lesson) -> None: ...


def api_voice_settings(segment: Segment, model_id: str) -> dict[str, float | bool]:
    """Return only settings supported by the selected ElevenLabs model family."""
    values = segment.voice_settings
    if model_id in {"eleven_v4", "eleven_v4_turbo"}:
        return {
            "stability": values.stability,
            "similarity_boost": values.similarity_boost,
        }
    return values.model_dump()


AUDIO_TAG_MODELS = {"eleven_v3", "eleven_v4", "eleven_v4_turbo"}
_TAG_CHARS = re.compile(r"^[A-Za-z0-9 _-]+$")
MAX_TTS_TAGS = 4
MAX_TAG_LENGTH = 32


def supports_audio_tags(model_id: str) -> bool:
    return model_id in AUDIO_TAG_MODELS


def normalize_tts_tags(tags: list[str]) -> list[str]:
    """Clean and validate inline audio-direction tags (not a prompt): strip any
    brackets the author already typed, reject empty/oversized/invalid tags, and
    cap the count. Raises on anything invalid rather than silently dropping it,
    so a bad tag is caught at authoring time, not discovered after a paid TTS call.
    """
    if len(tags) > MAX_TTS_TAGS:
        raise ValueError(f"at most {MAX_TTS_TAGS} tts_tags are allowed, got {len(tags)}")
    cleaned: list[str] = []
    for tag in tags:
        value = tag.strip().removeprefix("[").removesuffix("]").strip()
        if not value:
            raise ValueError("tts_tags cannot contain an empty tag")
        if len(value) > MAX_TAG_LENGTH:
            raise ValueError(f"tts_tags entry too long (max {MAX_TAG_LENGTH}): {value!r}")
        if not _TAG_CHARS.match(value):
            raise ValueError(f"tts_tags entry has unsupported characters: {value!r}")
        cleaned.append(value)
    return cleaned


def render_tts_text(text: str, tags: list[str], model_id: str) -> str:
    """Build the string actually sent to ElevenLabs. Tags are inline audio
    direction (e.g. `[gentle]`), never a system/instruction prompt, and never
    appear in the transcript, pinyin, or translation — only here, at the TTS
    request boundary.
    """
    if not tags or not supports_audio_tags(model_id):
        return text
    prefix = "".join(f"[{tag}]" for tag in normalize_tts_tags(tags))
    return f"{prefix} {text}"


def tag_usage(lesson: Lesson) -> tuple[int, dict[str, int]]:
    """Best-effort, non-raising count of tag usage for the `generate --dry-run`
    report. Uses raw (lowercased, bracket-stripped) tag text rather than the
    strict `normalize_tts_tags`, so a lesson with an invalid tag still produces
    a report instead of crashing the dry-run; the strict check still applies
    for real at TTS-request time.
    """
    segments_with_tags = 0
    counts: Counter[str] = Counter()

    def collect(tags: list[str]) -> bool:
        if not tags:
            return False
        for tag in tags:
            value = tag.strip().removeprefix("[").removesuffix("]").strip().lower()
            if value:
                counts[value] += 1
        return True

    for segment in lesson.segments:
        used = False
        if segment.turns:
            for turn in segment.turns:
                used = collect(turn.tts_tags) or used
        else:
            used = collect(segment.tts_tags) or used
        if used:
            segments_with_tags += 1
    return segments_with_tags, dict(counts)


def voice_ids_for(segment: Segment, settings: Settings, lesson: Lesson) -> list[str]:
    speakers = [turn.speaker for turn in segment.turns] if segment.turns else [segment.speaker]
    roles = [speaker or f"default-{segment.language}" for speaker in speakers]
    profiles = load_registry(settings.resolved_voices_path())
    prefer_accent = preferred_accent_for_level(lesson.level, segment.language)
    cast_path = settings.output_dir / lesson.lesson_id / "voice_cast.json"
    assignment = resolve_roles_with_pinning(
        profiles,
        language=segment.language,
        lesson_id=lesson.lesson_id,
        version=lesson.version,
        roles=roles,
        prefer_accent=prefer_accent,
        cast_path=cast_path,
        role_genders=lesson.voice_genders,
    )
    voice_ids: list[str] = []
    for speaker, role in zip(speakers, roles, strict=True):
        profile = assignment.get(role)
        if not profile:
            label = speaker or segment.language
            raise TTSError(f"No voice available in the registry for {label} ({segment.language})")
        voice_ids.append(profile.voice_id)
    return voice_ids


def cache_key(
    segment: Segment, voice_id: str | list[str], model_id: str, output_format: str
) -> str:
    payload = {
        "text": segment.text,
        "language": segment.language,
        "model_id": model_id,
        "output_format": output_format,
        "voice_settings": segment.voice_settings.model_dump(mode="json"),
    }
    if segment.tts_tags:
        # Only added when non-empty, so every pre-existing (untagged) segment's
        # cache key is byte-for-byte identical to before this feature existed.
        payload["tts_tags"] = segment.tts_tags
    if isinstance(voice_id, str):
        # Preserve cache compatibility for existing single-speaker clips.
        payload["voice_id"] = voice_id
    else:
        payload["voice_ids"] = voice_id
        # Keep the historical {speaker, text} shape when no tag is present,
        # preserving the cache key for every pre-existing turn.
        payload["turns"] = []
        for turn in segment.turns:
            # Pinyin and translation are publish/reference metadata, not TTS
            # inputs, so they must not invalidate an existing spoken-audio cache.
            value = {"speaker": turn.speaker, "text": turn.text}
            if turn.tts_tags:
                value["tts_tags"] = turn.tts_tags
            payload["turns"].append(value)
    canonical = json.dumps(payload, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    return hashlib.sha256(canonical.encode("utf-8")).hexdigest()


class ElevenLabsTTS:
    def __init__(self, settings: Settings):
        self.settings = settings

    def synthesize(self, segment: Segment, destination: Path, lesson: Lesson) -> None:
        voice_ids = voice_ids_for(segment, self.settings, lesson)
        if not self.settings.api_key:
            raise TTSError("TTS credentials are incomplete; run generate --dry-run for details")
        try:
            from elevenlabs import VoiceSettings as ElevenVoiceSettings
            from elevenlabs.client import ElevenLabs

            client = ElevenLabs(api_key=self.settings.api_key)
            if segment.turns:
                audio = client.text_to_dialogue.convert(
                    inputs=[
                        {
                            "text": render_tts_text(turn.text, turn.tts_tags, self.settings.model_id),
                            "voice_id": voice_id,
                        }
                        for turn, voice_id in zip(segment.turns, voice_ids, strict=True)
                    ],
                    model_id=self.settings.model_id,
                    output_format=self.settings.output_format,
                )
            else:
                audio = client.text_to_speech.convert(
                    text=render_tts_text(segment.text, segment.tts_tags, self.settings.model_id),
                    voice_id=voice_ids[0],
                    model_id=self.settings.model_id,
                    output_format=self.settings.output_format,
                    voice_settings=ElevenVoiceSettings(
                        **api_voice_settings(segment, self.settings.model_id)
                    ),
                )
            destination.parent.mkdir(parents=True, exist_ok=True)
            temporary = destination.with_suffix(".part")
            with temporary.open("wb") as handle:
                for chunk in audio:
                    handle.write(chunk)
            if not temporary.exists() or temporary.stat().st_size == 0:
                raise TTSError("ElevenLabs returned empty audio")
            temporary.replace(destination)
        except TTSError:
            raise
        except Exception as exc:
            destination.with_suffix(".part").unlink(missing_ok=True)
            raise TTSError(f"ElevenLabs TTS failed: {type(exc).__name__}: {exc}") from exc


class CachedTTS:
    def __init__(self, provider: TTSProvider, settings: Settings):
        self.provider = provider
        self.settings = settings

    def path_for(self, segment: Segment, lesson: Lesson) -> Path:
        voice_ids = voice_ids_for(segment, self.settings, lesson)
        key = cache_key(
            segment,
            voice_ids if segment.turns else voice_ids[0],
            self.settings.model_id,
            self.settings.output_format,
        )
        return self.settings.cache_dir / f"{key}.mp3"

    def is_cached(self, segment: Segment, lesson: Lesson) -> bool:
        try:
            path = self.path_for(segment, lesson)
        except TTSError:
            return False
        return path.is_file() and path.stat().st_size > 0

    def get(self, segment: Segment, lesson: Lesson) -> Path:
        path = self.path_for(segment, lesson)
        if not (path.is_file() and path.stat().st_size > 0):
            self.provider.synthesize(segment, path, lesson)
        if not (path.is_file() and path.stat().st_size > 0):
            raise TTSError(f"TTS did not create usable audio for segment {segment.id}")
        return path
