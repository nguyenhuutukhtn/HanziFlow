"""Command-line interface for validation, generation, transcripts and reviews."""

from __future__ import annotations

import argparse
import dataclasses
import json
import sys
from pathlib import Path

from pydantic import ValidationError

from .audio_builder import AudioBuildError, build_audio, timeline_path_for
from .audit import build_report, load_authoring_lessons, render_report
from .config import Settings
from .lesson_schema import Lesson, load_lesson
from .publish import PublishError, publish_lesson, render_publish_report, render_publish_summary
from .storage import ProgressStore
from .transcript import write_transcripts
from .tts import CachedTTS, ElevenLabsTTS, TTSError, cache_key, supports_audio_tags, tag_usage, voice_ids_for
from .upload import StorageConfig, UploadError, upload_package
from .voice_registry import sync_registry

LOG_DIR = Path("build/logs")


def _write_log(name: str, content: str) -> Path:
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    path = LOG_DIR / f"{name}.log"
    path.write_text(content, encoding="utf-8")
    return path


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


def _plan_summary(lesson: Lesson, settings: Settings) -> dict:
    plan = _audio_plan(lesson, settings)
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


def _print_plan(lesson: Lesson, settings: Settings, as_json: bool = False) -> list[str]:
    summary = _plan_summary(lesson, settings)
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


def command_generate(path: Path, settings: Settings, dry_run: bool, assume_yes: bool, as_json: bool = False) -> int:
    lesson = load_lesson(path)
    missing = _print_plan(lesson, settings, as_json=as_json and dry_run)
    if dry_run:
        if not as_json:
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


def command_publish(path: Path, settings: Settings, out: Path, force: bool, as_json: bool = False) -> int:
    report = publish_lesson(path, settings, out, force)
    if as_json:
        payload = dataclasses.asdict(report)
        print(json.dumps(payload, default=str))
        return 0
    if not report.published:
        print(render_publish_report(report), end="")
        return 0
    log_path = _write_log(f"publish-{path.stem}", render_publish_report(report))
    print(render_publish_summary(report, log_path), end="")
    return 0


def command_upload(out: Path, dry_run: bool, prune: bool) -> int:
    config = StorageConfig.from_env()
    upload_package(out, config, dry_run=dry_run, prune=prune)
    return 0


REVIEW_PRINT_LIMIT = 15


def command_review(settings: Settings, as_json: bool = False) -> int:
    rows = ProgressStore(settings.db_path).due()
    if as_json:
        print(json.dumps([dict(row) for row in rows]))
        return 0
    if not rows:
        print("No review items are due. Generate a lesson or register its patterns first.")
        return 0
    print(f"{len(rows)} item(s) due:")
    for row in rows[:REVIEW_PRINT_LIMIT]:
        print(f"- {row['pattern_id']}: {row['pinyin']} — {row['translation']} (mistakes: {row['mistakes']})")
    if len(rows) > REVIEW_PRINT_LIMIT:
        log_path = _write_log(
            "review",
            "\n".join(f"{row['pattern_id']}: {row['pinyin']} — {row['translation']} (mistakes: {row['mistakes']})" for row in rows),
        )
        print(f"... and {len(rows) - REVIEW_PRINT_LIMIT} more; full list: {log_path}")
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
    generate.add_argument("--json", action="store_true", help="With --dry-run, print the plan as JSON")
    review = sub.add_parser("review", help="List learner-rated review items currently due")
    review.add_argument("--json", action="store_true", help="Print every due item as JSON")
    audit = sub.add_parser("audit", help="Report cross-lesson pattern continuity for lesson authoring")
    audit.add_argument("lessons_dir", type=Path, nargs="?", default=Path("lessons"))
    audit.add_argument("--up-to", type=str, default=None, help="Only include lessons up to and including this lesson_id")
    publish = sub.add_parser("publish", help="Package an already-generated lesson without calling TTS")
    publish.add_argument("lesson", type=Path)
    publish.add_argument("--out", type=Path, default=Path("publish"))
    publish.add_argument("--force", action="store_true", help="Bypass the unchanged check and always write a new version (testing only)")
    publish.add_argument("--json", action="store_true", help="Print the report as JSON")
    upload = sub.add_parser("upload", help="Upload published packages to Supabase Storage (service-role key required)")
    upload.add_argument("--out", type=Path, default=Path("publish"))
    upload.add_argument("--dry-run", action="store_true", help="List uploads and deletions; make no writes")
    upload.add_argument("--no-prune", action="store_true", help="Skip deleting superseded versions")
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
            return command_generate(args.lesson, settings, args.dry_run, args.yes, args.json)
        if args.command == "audit":
            return command_audit(args.lessons_dir, args.up_to)
        if args.command == "publish":
            return command_publish(args.lesson, settings, args.out, args.force, args.json)
        if args.command == "upload":
            return command_upload(args.out, args.dry_run, not args.no_prune)
        if args.command == "voices":
            return command_voices_sync(settings)
        return command_review(settings, args.json)
    except (ValidationError, ValueError) as exc:
        print(f"Lesson validation failed: {exc}", file=sys.stderr)
    except (TTSError, AudioBuildError, PublishError, UploadError, OSError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
