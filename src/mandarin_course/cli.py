"""Command-line interface for validation, generation, transcripts and reviews."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from pydantic import ValidationError

from .audio_builder import AudioBuildError, build_audio, timeline_path_for
from .audit import build_report, load_authoring_lessons, render_report
from .config import Settings
from .lesson_schema import Lesson, load_lesson
from .publish import PublishError, publish_lesson, render_publish_report
from .storage import ProgressStore
from .transcript import write_transcripts
from .tts import CachedTTS, ElevenLabsTTS, TTSError, cache_key, supports_audio_tags, tag_usage, voice_ids_for
from .voice_registry import sync_registry


def _audio_plan(lesson: Lesson, settings: Settings) -> dict[str, int]:
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


def _print_plan(lesson: Lesson, settings: Settings) -> list[str]:
    plan = _audio_plan(lesson, settings)
    languages = {segment.language for segment in lesson.segments}
    missing = settings.missing_tts_fields(languages)
    print(f"Lesson: {lesson.lesson_id} — {lesson.title}")
    print(f"Model: {settings.model_id}; format: {settings.output_format}")
    print(f"Spoken segments: {plan['segments']}")
    print(f"Cache hits: {plan['cached_segments']} segment(s) / {plan['unique_cached_clips']} unique clip(s)")
    print(f"Need TTS: {plan['new_segments']} segment(s) / {plan['unique_new_requests']} unique paid request(s)")
    if missing:
        print("Missing configuration: " + ", ".join(missing))
    else:
        print("TTS configuration: present (secret values are not displayed)")
    segments_with_tags, tag_counts = tag_usage(lesson)
    print(f"Audio tags supported: {'yes' if supports_audio_tags(settings.model_id) else 'no'}")
    print(f"Segments with tags: {segments_with_tags}")
    if tag_counts:
        print("Tags: " + ", ".join(f"{tag}={count}" for tag, count in sorted(tag_counts.items())))
    return missing


def command_validate(path: Path) -> int:
    lesson = load_lesson(path)
    print(f"Valid: {lesson.lesson_id} ({len(lesson.segments)} segments)")
    return 0


def _audio_filename(lesson: Lesson) -> str:
    if lesson.variant:
        return f"{lesson.lesson_id}_{lesson.variant}.mp3"
    return f"{lesson.lesson_id}.mp3"


def command_transcript(path: Path, settings: Settings) -> int:
    lesson = load_lesson(path)
    destination = settings.output_dir / lesson.lesson_id
    practice, reference = write_transcripts(lesson, destination)
    print(f"Wrote {practice}")
    print(f"Wrote {reference}")
    return 0


def command_generate(path: Path, settings: Settings, dry_run: bool, assume_yes: bool) -> int:
    lesson = load_lesson(path)
    missing = _print_plan(lesson, settings)
    if dry_run:
        print("Dry run only: no API request was made.")
        return 0
    if missing:
        raise TTSError("Cannot generate until the missing configuration is resolved")
    plan = _audio_plan(lesson, settings)
    if plan["unique_new_requests"] and not assume_yes:
        answer = input(f"Create {plan['unique_new_requests']} new paid TTS clip(s)? Type 'yes' to continue: ")
        if answer.strip().lower() != "yes":
            print("Cancelled; no new TTS requests were made.")
            return 2
    output_dir = settings.output_dir / lesson.lesson_id
    write_transcripts(lesson, output_dir)
    tts = CachedTTS(ElevenLabsTTS(settings), settings)
    audio_path = output_dir / _audio_filename(lesson)
    duration = build_audio(lesson, tts, audio_path)
    store = ProgressStore(settings.db_path)
    store.register_lesson(lesson, generated=True)
    print(f"Created {audio_path} ({duration / 60:.2f} minutes)")
    timeline = json.loads(timeline_path_for(audio_path).read_text(encoding="utf-8"))
    drift_ms = abs(duration * 1000 - timeline["duration_ms"])
    print(f"Timeline: {timeline_path_for(audio_path)} ({len(timeline['cues'])} cues; drift {drift_ms:.1f} ms)")
    return 0


def command_audit(lessons_dir: Path, up_to: str | None) -> int:
    lessons = load_authoring_lessons(lessons_dir)
    report = build_report(lessons, up_to=up_to)
    print(render_report(report), end="")
    return 0


def command_voices_sync(settings: Settings) -> int:
    if not settings.api_key:
        raise TTSError("ELEVENLABS_API_KEY is not configured")
    destination = settings.resolved_voices_path()
    profiles = sync_registry(settings.api_key, destination)
    print(f"Saved {len(profiles)} voice(s) to {destination}")
    for profile in profiles:
        print(
            f"  {profile.name} [{profile.language}, {profile.accent or 'no accent tag'}, "
            f"{profile.gender or 'unknown gender'}] -> {profile.voice_id}"
        )
    return 0


def command_review(settings: Settings) -> int:
    rows = ProgressStore(settings.db_path).due()
    if not rows:
        print("No review items are due. Generate a lesson or register its patterns first.")
        return 0
    print(f"{len(rows)} item(s) due:")
    for row in rows:
        print(f"- {row['pattern_id']}: {row['pinyin']} — {row['translation']} (mistakes: {row['mistakes']})")
    print("Ratings are learner supplied; this MVP does not score spoken answers automatically.")
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="mandarin-course")
    parser.add_argument("--env-file", type=Path, help="Optional dotenv file (default: .env)")
    sub = parser.add_subparsers(dest="command", required=True)
    validate = sub.add_parser("validate", help="Validate a lesson JSON without calling TTS")
    validate.add_argument("lesson", type=Path)
    transcript = sub.add_parser("transcript", help="Create practice and full transcripts")
    transcript.add_argument("lesson", type=Path)
    generate = sub.add_parser("generate", help="Plan or generate one lesson")
    generate.add_argument("lesson", type=Path)
    generate.add_argument("--dry-run", action="store_true", help="Show cost-sensitive plan; make no API calls")
    generate.add_argument("--yes", action="store_true", help="Confirm paid requests non-interactively")
    sub.add_parser("review", help="List learner-rated review items currently due")
    audit = sub.add_parser("audit", help="Report cross-lesson pattern continuity for lesson authoring")
    audit.add_argument("lessons_dir", type=Path, nargs="?", default=Path("lessons"))
    audit.add_argument("--up-to", type=str, default=None, help="Only include lessons up to and including this lesson_id")
    publish = sub.add_parser("publish", help="Package an already-generated lesson without calling TTS")
    publish.add_argument("lesson", type=Path)
    publish.add_argument("--out", type=Path, default=Path("publish"))
    publish.add_argument("--force", action="store_true", help="Replace an existing package version (testing only)")
    voices = sub.add_parser("voices", help="Manage the local ElevenLabs voice registry")
    voices_sub = voices.add_subparsers(dest="voices_command", required=True)
    voices_sub.add_parser("sync", help="Fetch and cache available Mandarin/Vietnamese voices (not a paid TTS call)")
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    settings = Settings.from_env(args.env_file)
    try:
        if args.command == "validate":
            return command_validate(args.lesson)
        if args.command == "transcript":
            return command_transcript(args.lesson, settings)
        if args.command == "generate":
            return command_generate(args.lesson, settings, args.dry_run, args.yes)
        if args.command == "audit":
            return command_audit(args.lessons_dir, args.up_to)
        if args.command == "publish":
            print(render_publish_report(publish_lesson(args.lesson, settings, args.out, args.force)), end="")
            return 0
        if args.command == "voices":
            return command_voices_sync(settings)
        return command_review(settings)
    except (ValidationError, ValueError) as exc:
        print(f"Lesson validation failed: {exc}", file=sys.stderr)
    except (TTSError, AudioBuildError, PublishError, OSError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
