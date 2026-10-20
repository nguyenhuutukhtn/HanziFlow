"""Cache/cost plan for a lesson's TTS segments: `generate --dry-run` and bundle import."""

from __future__ import annotations

import json

from .config import Settings
from .lesson_schema import Lesson
from .tts import TTSError, cache_key, supports_audio_tags, tag_usage, voice_ids_for


def audio_plan(lesson: Lesson, settings: Settings) -> dict[str, int]:
    hits = 0
    misses = 0
    unique_hits: set[str] = set()
    unique_misses: set[str] = set()
    for segment in lesson.segments:
        try:
            voice_ids = voice_ids_for(segment, settings, lesson)
        except TTSError:
            misses += 1
            unique_misses.add(f"missing:{segment.id}")
            continue
        key = cache_key(
            segment,
            voice_ids if segment.turns else voice_ids[0],
            settings.model_id,
            settings.output_format,
        )
        path = settings.cache_dir / f"{key}.mp3"
        if path.is_file() and path.stat().st_size > 0:
            hits += 1
            unique_hits.add(key)
        else:
            misses += 1
            unique_misses.add(key)
    return {
        "segments": len(lesson.segments),
        "cached_segments": hits,
        "new_segments": misses,
        "unique_cached_clips": len(unique_hits),
        "unique_new_requests": len(unique_misses),
    }


def plan_summary(lesson: Lesson, settings: Settings) -> dict:
    plan = audio_plan(lesson, settings)
    languages = {segment.language for segment in lesson.segments}
    missing = settings.missing_tts_fields(languages)
    segments_with_tags, tag_counts = tag_usage(lesson)
    return {
        "lesson_id": lesson.lesson_id,
        "title": lesson.title,
        "model_id": settings.model_id,
        "output_format": settings.output_format,
        **plan,
        "missing_configuration": missing,
        "audio_tags_supported": supports_audio_tags(settings.model_id),
        "segments_with_tags": segments_with_tags,
        "tag_counts": tag_counts,
    }


def print_plan(lesson: Lesson, settings: Settings, as_json: bool = False) -> list[str]:
    summary = plan_summary(lesson, settings)
    if as_json:
        print(json.dumps(summary))
        return summary["missing_configuration"]
    print(f"Lesson: {summary['lesson_id']} — {summary['title']}")
    print(f"Model: {summary['model_id']}; format: {summary['output_format']}")
    print(f"Spoken segments: {summary['segments']}")
    print(f"Cache hits: {summary['cached_segments']} segment(s) / {summary['unique_cached_clips']} unique clip(s)")
    print(f"Need TTS: {summary['new_segments']} segment(s) / {summary['unique_new_requests']} unique paid request(s)")
    if summary["missing_configuration"]:
        print("Missing configuration: " + ", ".join(summary["missing_configuration"]))
    else:
        print("TTS configuration: present (secret values are not displayed)")
    print(f"Audio tags supported: {'yes' if summary['audio_tags_supported'] else 'no'}")
    print(f"Segments with tags: {summary['segments_with_tags']}")
    if summary["tag_counts"]:
        print("Tags: " + ", ".join(f"{tag}={count}" for tag, count in sorted(summary["tag_counts"].items())))
    return summary["missing_configuration"]
