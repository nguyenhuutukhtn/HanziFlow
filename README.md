# HanziFlow

A local-first Python CLI that turns validated lesson JSON into a Mandarin listening-and-speaking course. Lesson content, TTS, FFmpeg assembly, transcripts, caching, and learner-managed review history are separate so later lessons do not require changes to the core code.

The MVP includes a week-by-week outline and Lesson 1, the takeout mini-story. That lesson is the template for later ones. It does not claim to assess pronunciation or know whether an answer was correct; review ratings are supplied by the learner.

## What the MVP does

- validates lesson content with Pydantic before any paid request;
- reports cache hits and unique clips that would need TTS;
- asks for explicit confirmation before uncached paid generation;
- calls the official ElevenLabs Python SDK and caches by text, language, voice, model, output format, and voice settings;
- normalizes clips, inserts each configured pause, and creates an offline MP3 with FFmpeg;
- writes a spoiler-light practice transcript and a full reference transcript;
- stores generated lessons, learned patterns, learner ratings, mistakes, and due dates in SQLite;
- tests without an ElevenLabs key and never calls TTS in unit tests.

## Requirements (macOS)

- Python 3.11 or newer
- FFmpeg and `ffprobe`
- an ElevenLabs account only when generating real speech

Install system prerequisites with Homebrew if needed:

```bash
brew install python@3.11 ffmpeg
python3 --version
ffmpeg -version
```

## Install

From the project root:

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -e '.[dev]'
cp .env.example .env
```

The `.env` file, audio cache, generated output, and SQLite database are ignored by Git.

## Configure ElevenLabs

Open the [ElevenLabs Voice Library](https://elevenlabs.io/app/voice-library), audition voices, and choose IDs available to your account. Select one voice that speaks natural Mainland Standard Mandarin and one clear English voice. Do not guess or copy the example voice IDs from documentation.

Fill in `.env`:

```dotenv
ELEVENLABS_API_KEY=your_key
ELEVENLABS_MANDARIN_VOICE_ID=your_mandarin_voice_id
ELEVENLABS_MANDARIN_VOICE_2_ID=your_second_mandarin_voice_id
ELEVENLABS_ENGLISH_VOICE_ID=your_english_voice_id
ELEVENLABS_MODEL_ID=eleven_v4
```

The implementation follows the current official `text_to_speech.convert` API. It defaults to `eleven_v4`, ElevenLabs' highest-quality model, and sends only the voice settings supported by that model family. Model availability and account pricing can change, so check [the model reference](https://elevenlabs.io/docs/overview/models/) and [text-to-speech API reference](https://elevenlabs.io/docs/api-reference/text-to-speech/convert) before a large run.

## Safe workflow

Activate the environment first:

```bash
source .venv/bin/activate
```

Validate Lesson 1 without TTS:

```bash
python -m mandarin_course.cli validate lessons/lesson_001.json
```

Create both transcripts without TTS:

```bash
python -m mandarin_course.cli transcript lessons/lesson_001.json
```

This writes:

- `output/lesson_001/transcript.md` — practice version; pinyin only, model answers hidden;
- `output/lesson_001/transcript_full.md` — reference with Vietnamese glosses and optional Hanzi details.

Always inspect the paid-request plan first:

```bash
python -m mandarin_course.cli generate lessons/lesson_001.json --dry-run
```

The plan reports spoken segments, segment-level cache hits, and the number of unique uncached TTS requests. A dry run never constructs the TTS client or calls the API.

Generate only this lesson:

```bash
python -m mandarin_course.cli generate lessons/lesson_001.json
```

The CLI asks you to type `yes` before making any uncached paid requests. In a trusted automated workflow, `--yes` supplies that confirmation:

```bash
python -m mandarin_course.cli generate lessons/lesson_001.json --yes
```

On success, listen offline at `output/lesson_001/lesson_001_guided.mp3`. The phone copy is `output/dien_thoai/04_bai1_chuyen.mp3`. The command prints the duration measured by `ffprobe`; it never reports success for an empty or failed output.

## Supabase setup

Progress rows and published lesson packages live in one Supabase project. Migrations are in `supabase/migrations/`; the public `course` bucket is writable only with the service-role key.

Fill `.env` (git-ignored, never commit these values):

```dotenv
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=
```

Link the project and apply migrations from your own terminal; the CLI prompts for the database password and this repo never stores it:

```bash
source .venv/bin/activate
supabase login
set -a && source .env && set +a
supabase link --project-ref "$(printf '%s' "${SUPABASE_URL}" | sed -E 's#https://([^.]+)\..*#\1#')"
supabase db push
```

Package, then mirror `publish/` into Storage (the catalog goes last; versioned files are never overwritten):

```bash
mandarin-course publish lessons/lesson_001.json
mandarin-course upload --dry-run
mandarin-course upload
```

Clients read `<SUPABASE_URL>/storage/v1/object/public/course/catalog.json` and use `SUPABASE_ANON_KEY` only; the service-role key stays on this machine.

## Cache behavior

Each cached clip is content-addressed. Running the same lesson with the same voices and settings reuses it. Changing text, language, model, output format, voice ID, or a TTS voice setting creates a new key. `playback_speed` is applied locally by FFmpeg, so adjusting it rebuilds the lesson without another paid TTS request. Old cache files are safe to leave in place.

Lesson 1 uses four speaker roles: `anna` and `li_ming` are Mandarin character voices, `coach_zh` is the Mandarin narrator, and `coach` is the Vietnamese narrator. Drills stay individual clips so answer pauses stay exact.

Eleven v4 does not expose a Speed setting. To slow a segment without changing pitch, set `"playback_speed": 0.9` in lesson JSON. Keep normal conversations near `1.0`; use roughly `0.88`–`0.95` only for focused imitation. Large slowdowns sound processed and are intentionally rejected by the schema.

To intentionally rebuild every clip, move the contents of `cache/` somewhere safe and run the dry-run again. Do not delete the entire project directory.

## Tests

```bash
source .venv/bin/activate
pytest
```

Tests cover valid and invalid JSON, duplicate IDs, bad pauses, Mandarin learning fields, audio order, real silence assembly through FFmpeg, cache hits/misses, cache invalidation, transcript spoiler handling, TTS failures, SQLite review data, and CLI commands that must not call TTS.

## Review schedule

After successful audio generation, Lesson 1 patterns are registered in `data/progress.db`:

```bash
python -m mandarin_course.cli review
```

The initial schedule uses 1, 2, 3, 5, and 7 days, then expands by about 1.7x. The storage layer accepts `again`, `hard`, `good`, and `easy` learner ratings; a future interactive command can expose rating input. `again` also increments the recorded mistake count.

## Lesson data

`scripts/build_lesson_001_story.py` writes `lessons/lesson_001.json`. That file is the Lesson 1 template: Anna and Lý Minh each think the takeout is the other's, and it belongs to a colleague. Mandarin is stored in simplified Hanzi for TTS, with pinyin and a Vietnamese gloss for transcripts.

Each question-answer exercise is explicit: a `prompt` segment has its own answer pause and points to a later `model_answer` in metadata. The builder never invents answers or timing. To add a lesson, copy the schema, give every segment a unique ID, validate it, create transcripts, and dry-run before generation.

## Troubleshooting

- **Missing configuration:** copy `.env.example` to `.env` and fill all three ElevenLabs values. The CLI never prints their contents.
- **401/403 from ElevenLabs:** verify the key, account permissions, and that each voice ID is available to the account.
- **429 or rate limit:** stop and retry later. Completed cache files remain reusable; partial files are removed.
- **Voice has the wrong accent:** audition Mainland Mandarin voices and replace `ELEVENLABS_MANDARIN_VOICE_ID` or `ELEVENLABS_MANDARIN_VOICE_2_ID`; dry-run will correctly show only the resulting cache misses.
- **`ffmpeg` not found:** run `brew install ffmpeg`, then open a new shell.
- **Validation error:** the message identifies the invalid field; common causes are missing pinyin/translation on Mandarin, a negative pause, or duplicate segment IDs.
- **Generated duration differs from target:** TTS voice pacing affects actual length. The measured output duration is authoritative; adjust segment pauses or lesson activities and regenerate only the invalidated clips.

## Project map

```text
curriculum/roadmap.md          week-by-week sequence
lessons/lesson_001.json        Lesson 1 mini-story template
src/mandarin_course/config.py  environment configuration
src/mandarin_course/lesson_schema.py
src/mandarin_course/tts.py     ElevenLabs adapter and cache
src/mandarin_course/audio_builder.py
src/mandarin_course/transcript.py
src/mandarin_course/storage.py
src/mandarin_course/review.py
src/mandarin_course/cli.py
tests/                         offline unit/integration tests
```

## Next extension: AI-assisted curriculum authoring

Keep AI authoring outside the audio builder. Add a generator that consumes the curriculum, the validated pattern inventory, previous exposure counts, and due-review items; require it to emit this same lesson schema. Then apply deterministic checks for vocabulary budget, introduced-before-used patterns, pinyin completeness, answer timing, and review coverage. A human should preview and approve the JSON before the existing dry-run/generation pipeline is allowed to spend TTS credits.
