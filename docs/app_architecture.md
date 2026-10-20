# App Architecture

Supersedes the former `android_app_handoff.md` (Android-only, no accounts, Firebase Hosting).
Decisions below were made on 2026-10-20.

Course content and teacher behavior follow [Lesson Audio Design Principles](lesson_design_principles.md). The apps play, test, and track that content; they do not replace the teacher script with generic UI exercises.

## Product decisions

| Topic | Decision |
|---|---|
| Users | Personal use first. Commercial release is possible later, so nothing may block it (no hardcoded single user, no secrets in clients). |
| Platforms | **Android app first** (learn anywhere: commute, street, office). **Web app second**, for computers (replaces separate Windows/macOS/Linux apps). iOS possibly later. |
| Offline | Android: fully offline after a lesson is downloaded. Web: online only. |
| Priority | Runtime performance and battery over developer convenience. Code is written by AI agents, so language familiarity is not a factor. |
| Sync | Progress only: which lesson the learner has reached and which lesson tests passed. Audio and playback position are not synced. |
| Backend | Supabase (Auth, Postgres, Storage): relational progress data with row-level security, and file storage on the free tier (Firebase Storage needs the Blaze plan). |
| Tests | Randomly generated on every attempt from the lesson's item bank, so answers cannot be memorized. Pass requires 100%. |
| Word lookup | Tap a word to look it up (pre-segmented tokens from the pipeline), not free text selection. |

## System overview

```text
Python pipeline (this repo, src/)
  generate  -> TTS + FFmpeg (paid, explicit)
  publish   -> versioned lesson packages + catalog.json (never calls TTS)
  upload    -> Supabase Storage (explicit step)

shared/  (Kotlin Multiplatform, pure logic, no I/O)
  models · test generator · pass rule
     ├── JVM target -> android/
     └── JS target  -> web/ (exported .d.ts)

android/  Kotlin + Compose + Media3 + Room + WorkManager   (offline, syncs progress)
web/      TypeScript + React + Vite, supabase-js           (online, reads/writes Supabase directly)

Supabase: Auth · Postgres (lesson_progress, test_attempts, RLS) · Storage (public lesson packages)
Web hosting: Cloudflare Pages
```

Why `shared/` exists: Android must generate tests offline and the web generates tests too. Both write to the same progress tables, so test generation and the pass rule must be one implementation. Sync/merge logic is Android-only and lives in `android/`.

Pronunciation scoring (later) is independent of this choice: on-device models run through onnxruntime (Android native, web via WASM), or scoring calls a cloud API.

## Repository layout

```text
src/mandarin_course/   Python authoring + generation + publish (existing)
lessons/               lesson JSON (existing)
publish/               publish output (git-ignored), mirrors the Storage bucket
kmp/                   Gradle root
├── shared/            KMP module: jvm + js(IR) targets
└── android/           Android application module
web/                   Vite + React app, consumes kmp/shared JS build
supabase/              migrations/, seed, config
```

## Lesson package contract

The Python project remains the authoring and audio-generation system. `publish` writes:

```text
publish/
├── catalog.json
└── lessons/
    └── lesson_001/
        └── v2/
            ├── guided.mp3
            ├── practice.mp3        # optional while practice audio is not generated
            ├── timeline_guided.json
            ├── timeline_practice.json  # present only with practice.mp3
            ├── lesson.json
            ├── reference.json
            ├── items.json
            └── clips/<sha256>.mp3
```

Use versioned paths. Never overwrite a published version in place; CDN and devices may have cached it. Upload versioned files with a one-year `cacheControl`; upload `catalog.json` with no-cache.

`mandarin-course publish lessons/<lesson_id>.json` reads only local lesson files,
generated tracks, the voice registry/cast, and `cache/<cache_key>.mp3`. It never calls
ElevenLabs or any other network API. The guided track is required. The authored
`lessons/<lesson_id>_practice.json` may contribute a reference transcript, but its
track is omitted from the catalog with a warning until the practice MP3 exists.
Vocabulary is authored separately in `lessons/<lesson_id>_vocab.json` with
`schema_version`, matching `lesson_id`, `status` (`draft` or `reviewed`), and a
`words` array containing `hanzi`, `pinyin`, and `meaning_vi`; publish validates this
sidecar before packaging.

### catalog.json

The only file a client checks to discover new or updated content. Every downloadable artifact carries byte size and SHA-256 so Android can detect incomplete or corrupt downloads.

```json
{
  "schema_version": 1,
  "catalog_version": 1,
  "lessons": [
    {
      "id": "lesson_001",
      "version": 2,
      "order": 1,
      "title": "...",
      "level": "absolute_beginner",
      "base_url": "lessons/lesson_001/v2/",
      "tracks": {
        "guided":   { "file": "guided.mp3",   "bytes": 1639675, "duration_ms": 204889, "sha256": "...",
          "timeline": { "file": "timeline_guided.json", "bytes": 4200, "sha256": "..." } },
        "practice": { "file": "practice.mp3", "bytes": 921125, "duration_ms": 112500, "sha256": "...",
          "timeline": { "file": "timeline_practice.json", "bytes": 2300, "sha256": "..." } }
      },
      "files": {
        "manifest":  { "file": "lesson.json",    "bytes": 12400, "sha256": "..." },
        "reference": { "file": "reference.json", "bytes": 8900,  "sha256": "..." },
        "items":     { "file": "items.json",     "bytes": 6100,  "sha256": "..." }
      }
    }
  ]
}
```

`base_url` is relative to the bucket's public root, which each client gets from config. Clip files are listed with their size and SHA-256 inside `items.json`.
Catalog output is deterministic: entries are sorted by the numeric part of the
lesson ID, and publishing identical inputs does not change `catalog.json`.
Published tracks are mono 44.1 kHz MP3 at 64 kbps. Every published track must have
a timeline whose cue IDs all exist in that track's reference lines.

### reference.json

Full transcript for the reference view: per line speaker, pinyin, Hanzi, Vietnamese meaning, and a **token list** (`hanzi`, `pinyin`, `item_id` if the token is a lesson item). Clients render each token as a tappable word; the lookup popup shows pinyin and meaning and plays the item clip. Segmentation is done once in the pipeline, never in the client.
Dialogue segments with `turns` produce one reference line per turn; their pinyin
and translation are authored on each turn so publish never has to infer a split.

### items.json (test item bank)

Raw material for test generation, not a fixed test.

```json
{
  "schema_version": 1,
  "lesson_id": "lesson_001",
  "lesson_version": 2,
  "words": [
    { "id": "w_买_mai3", "hanzi": "买", "pinyin": "mǎi", "tones": [3], "meaning_vi": "mua",
      "clip": { "file": "clips/<sha>.mp3", "bytes": 9100, "sha256": "..." } }
  ],
  "sentences": [
    { "id": "s_<text-sha256-prefix>", "activity": "...", "speaker": "coach_zh",
      "hanzi": "...", "pinyin": "...", "meaning_vi": "...", "clip": { "...": "..." },
      "tokens": [ { "hanzi": "买", "pinyin": "mǎi", "item_id": "w_买_mai3" } ] }
  ],
  "qa": [
    {
      "id": "r1_food",
      "activity": "...",
      "speaker": "coach_zh",
      "question": { "hanzi": "...", "pinyin": "...", "meaning_vi": "...", "clip": { "...": "..." } },
      "answer": { "hanzi": "...", "pinyin": "...", "meaning_vi": "...", "clip": { "...": "..." } }
    }
  ]
}
```

`qa` pairs come from consecutive authored Mandarin `prompt` → `model_answer`
segments, not from new writing. Word clips are available when an existing
non-dialogue segment exactly speaks that word after punctuation is stripped;
sentence and Q&A clips likewise come only from their existing cached segment audio.
Publish normalizes available clips to mono 44.1 kHz MP3 at 64 kbps with loudness
normalization and content-addresses them by the SHA-256 of the output bytes. A
missing cache clip leaves the item without `clip` and produces a report warning; it
never triggers TTS. Any item needing new TTS must be produced separately by
`generate` (paid, with the usual dry run), never by `publish`.
Word IDs are derived from Hanzi plus canonical tone-number pinyin, so the same word
has the same ID across lessons and vocabulary ordering. Sentence IDs use a stable
short SHA-256 prefix of the complete Hanzi text.

## Timeline contract (karaoke transcript)

Each published audio track has a timeline, `timeline_<track>.json`, listed in `catalog.json` under that track (`"timeline": {"file", "bytes", "sha256"}`).

- **Sentence level (MVP):** one cue per spoken line and per repetition, with
  `start_ms`/`end_ms` and the `reference.json` line ID it displays. A `turns`
  segment is one composite cached clip, so its cue uses the first turn as `line_id`,
  lists every turn in `line_ids`, and sets `granularity` to `"segment"`. Ordinary
  cues omit both optional fields and default to line granularity. Times are recorded
  by `build_audio` while it assembles the track (measured clip durations after
  `atempo`, plus inserted silences), never estimated from text length. Rebuilding
  audio from cache to get a timeline costs no TTS request.
- The timeline's `duration_ms` is the measured-parts end (including all silence).
  Generation compares it with the encoded MP3 duration from ffprobe and fails when
  absolute drift exceeds 150 ms.
- **Word level (later):** optional per-token `start_ms`/`end_ms` inside a cue, which
  also provides the timestamps needed to split grouped composite cues into per-turn
  timing. Planned once Lesson 1 has finished its content audit and is regenerated:
  switch TTS to ElevenLabs' with-timestamps endpoint (store the alignment next to
  each cached clip), and use local forced alignment for clips already in the cache.
  Character times must be divided by the segment's `playback_speed`.

```json
{
  "schema_version": 1,
  "lesson_id": "lesson_001",
  "lesson_version": 2,
  "track": "guided",
  "duration_ms": 204889,
  "cues": [
    { "line_id": "r1_food", "start_ms": 12400, "end_ms": 14800 }
  ]
}
```

## Test generation (shared/)

- Input: item banks of the target lesson and every earlier lesson, a seed, and `generator_version`. Output is deterministic for the same input on JVM and JS. A checked-in set of seed → expected-test vectors runs against both targets.
- Each attempt uses a fresh random seed; the seed is stored with the attempt.
- Question types:
  - listen → choose Hanzi / pinyin / Vietnamese meaning;
  - see Hanzi → choose pinyin;
  - identify the tone;
  - fill the missing word in a story sentence;
  - true/false or either/or about a story fact.
- At least half the questions target the current lesson; the rest review earlier lessons.
- Distractors are chosen deliberately, never uniformly at random: same syllable with a different tone (`mǎi`/`mài`), confusable initials and finals (`zh/z`, `sh/s`, `ü/u`), then other taught words of the same kind. Never use a word the learner has not been taught.
- Answer position is shuffled per question.

**Pass rule (decided 2026-10-20):** an attempt passes only if every question is answered correctly on the first try and no answer was revealed. Mistakes don't end the attempt: a wrong answer shows the correct one, and a different question of the same type on the same item is requeued at the end so the learner still practices it, but that attempt is recorded as not passed. The result screen shows first-try correct / total. A lesson counts as passed when it has at least one passed attempt.

## Android app

Stack:

- Kotlin, Jetpack Compose
- Jetpack Media3 / ExoPlayer in a `MediaLibraryService`, with a `MediaController` in the UI. The Activity never owns the player, so playback survives leaving the UI and locking the screen.
- Room for content, download, and progress records, plus a sync outbox
- WorkManager + OkHttp for downloads and sync
- supabase-kt (Auth, Postgrest)

### Download behavior

1. Fetch `catalog.json` on startup and when the user taps Sync.
2. Compare remote lesson version and SHA-256 with Room.
3. Show available downloads; do not auto-download every lesson. Optional "Wi-Fi only".
4. Download each file to a `.part` path and verify byte size and SHA-256.
5. Rename atomically only after every artifact for the lesson is valid; then mark the lesson available offline.
6. Never delete an older playable version until the replacement is fully verified. A failed or cancelled update must leave the previous version playable. Provide retry, cancel, and delete.

### Playback

- Two tracks per lesson: **guided** and **practice**. "Continue listening" defaults to the last track and position. A daily-playlist mode queues practice tracks with no interaction.
- Play, pause, seek ±, previous/next lesson; lock-screen, notification, and headset controls via Media3.
- Speed 0.8×–1.1× locally.
- Restore last lesson and position (local only).

### Karaoke transcript

The player shows the transcript in sync with the audio: the current line is highlighted and auto-scrolls; tapping a line seeks to it; a single line can be looped for shadowing. Sentence level in the MVP, word level later (see Timeline contract).

Display is one dataset with several views, switched instantly, never separate audio or separate files:

- text hidden (listen only)
- pinyin only
- Hanzi with pinyin above (ruby)
- Hanzi only
- Vietnamese meaning on/off, independently of the above

The learner chooses the default view in Settings, and can change it per track (guided / practice) and on the fly in the player. Recommended default is text hidden or pinyin only, because audio is the main channel (see [lesson design principles](lesson_design_principles.md)), but it is a preference, not a lock. Opening the text is normal and never penalized: if a line still can't be heard or said correctly after many listens, looking at it is the right move. The app does not count opening the text against progress.

One rule is not configurable: a `model_answer` line is never shown before the audio reaches it, so the answer gap for speaking aloud is preserved whatever view is selected.

### Progress sync

Room is the source of truth; the app is fully usable offline once signed in.

- `test_attempts` rows are immutable and keyed by a client-generated UUID. Push is an idempotent upsert.
- `lesson_progress` is one row per lesson; merge is last-write-wins on `updated_at`, except that `passed_at` is never cleared once set.
- Local writes go into an outbox. Sync runs on startup, on network regain (WorkManager constraint), and after each test: push the outbox, then pull rows with `updated_at` greater than the last pull cursor.
- Sign-in is email + password (or email OTP). No OAuth redirect flows in the MVP.

### Navigation and MVP screens

Music-app structure (Spotify / Zing MP3 style), because listening is the main activity and must never be interrupted by moving around the app:

- **Bottom navigation, three tabs:**
  1. **Learn**: lesson library (available, downloading, downloaded, update available, passed), continue-listening card on top, download/cancel/delete/retry.
  2. **Review**: lesson tests (generated questions, requeue on mistakes, result) and attempt history.
  3. **Settings**: default transcript view per track, Vietnamese default, Wi-Fi-only downloads, storage used, account, last sync time; catalog URL in debug builds only.
- **Mini-player** docked above the bottom navigation on every tab whenever something is loaded: title, track, play/pause, progress. Switching tabs never stops playback.
- **Now Playing**: tapping or swiping up the mini-player opens a full-screen player; the karaoke transcript fills most of it, like synced lyrics in a music app. Compact controls: play/pause, ±10 s, seek bar, speed, guided/practice switch, transcript view, Vietnamese toggle, loop state.

Visual design follows [`docs/ui/`](ui/README.md) (start at its README, then read only the files for the screens a task touches); screens must not be designed ad hoc inside feature tasks.

### Word lookup and copy

- Tap a word (token with `item_id`) → in-app lookup sheet: Hanzi, pinyin, Vietnamese meaning, play the word clip when available.
- The lookup sheet also offers: **Copy**, **Open in Pleco** (Pleco's URL scheme, only if installed), and **Send to…** (Android `ACTION_PROCESS_TEXT` / share, so any installed dictionary or translator can receive it).
- Chinese text in the transcript is selectable (select a word or a whole line) so the learner can copy any span, not only lesson items; the system selection menu then shows installed dictionary/translator apps.
- Gestures must not conflict: line seek, word lookup, and text selection each need a distinct, discoverable interaction; the UI design decides which.

### MVP acceptance criteria

- A new lesson arrives through Sync without reinstalling the APK.
- A downloaded lesson plays, and its test can be taken, in airplane mode.
- Audio continues with the screen locked; lock-screen and headset play/pause work.
- Playback resumes near the saved position after restarting the app.
- A failed or cancelled update leaves the previous version playable.
- The highlighted transcript line matches the spoken line (including at 0.8× and 1.1× speed); tapping a line seeks to it.
- The default transcript view follows Settings; a `model_answer` line never appears before its audio.
- Two attempts at the same lesson test produce different questions.
- A test passed offline shows as passed on another device after both sync.
- No ElevenLabs key, Supabase service-role key, or other admin credential ships in the APK.

## Code architecture (Android)

Goal: the UI can be redesigned or rewritten without touching business logic, and the logic is testable without a device. Follows Google's recommended app architecture (UI → optional domain → data), with unidirectional data flow. No extra ceremony beyond that.

```text
ui/<feature>/        Composables (stateless: state in, events out) + one ViewModel per screen
                     ViewModel exposes StateFlow<UiState>, takes events (onEvent / functions)
domain/              use cases only where logic spans several repositories or the player
                     (StartTestAttempt, FinishTestAttempt, SyncProgress, ResolveContinueListening)
data/                repositories (interfaces + implementations): Course, Progress, Settings, Auth
                     data sources: Room, OkHttp downloads, file store, Supabase
playback/            PlaybackService (owns ExoPlayer, loop, resumption) +
                     PlayerConnection: wraps MediaController, exposes StateFlow<PlaybackState>
                     and commands; the only place UI-side code talks to Media3
kmp/shared/          pure platform-independent rules (contract models, test generator,
                     pass rule, transcript decisions); also used by the web app
```

Rules:

- Dependency direction is one-way: `ui → domain → data`, `ui → playback (PlayerConnection)`, everything may use `shared`. `data` and `playback` never import `ui`. Composables never touch repositories, Room, OkHttp, or `MediaController` directly.
- Composables are stateless and previewable; screen state is one immutable `UiState` per screen. Navigation and one-off effects (snackbar, open URL) go through the ViewModel as events.
- Logic that the web app will also need goes into `kmp/shared`, not into Android ViewModels.
- The service is the source of truth for playback state (position, track, loop); ViewModels only mirror it via `PlayerConnection`.
- Dependency injection: Hilt.
- Single `:android` module with these packages for now; split into Gradle modules only when build time or ownership requires it. Package boundaries are enforced by architecture tests (Konsist) so AI-written code can't quietly break the layering.

## Web app (after Android)

- TypeScript + React + Vite, single-page app behind sign-in, hosted on Cloudflare Pages.
- Online only: reads and writes Supabase directly with supabase-js and streams audio from Storage URLs. No service worker or local database.
- Uses the `shared/` JS output for test generation and the pass rule.
- Media Session API for media keys and headset controls.
- Same screens as Android, minus downloads.

## Supabase

```sql
create table lesson_progress (
  user_id      uuid not null references auth.users on delete cascade,
  lesson_id    text not null,
  status       text not null check (status in ('started', 'listened', 'passed')),
  passed_at    timestamptz,
  updated_at   timestamptz not null default now(),
  primary key (user_id, lesson_id)
);

create table test_attempts (
  id                 uuid primary key,          -- generated on the client
  user_id            uuid not null references auth.users on delete cascade,
  lesson_id          text not null,
  lesson_version     int  not null,
  generator_version  int  not null,
  seed               bigint not null,
  question_count     int  not null,
  mistakes           int  not null,
  passed             boolean not null,
  started_at         timestamptz not null,
  finished_at        timestamptz not null,
  client             text not null check (client in ('android', 'web')),
  device_id          text not null,
  updated_at         timestamptz not null default now()
);

alter table lesson_progress enable row level security;
alter table test_attempts  enable row level security;
-- each table: select/insert/update only where user_id = auth.uid(); no delete policy on test_attempts
```

Migrations live in `supabase/migrations/`. Lesson packages go in a public Storage bucket `course`; only the local upload step (service-role key from `.env`) writes to it.

Free-tier notes: projects pause after 7 days without activity (data is kept; resume from the dashboard). Android keeps working offline while paused. The Storage free tier (1 GB, 5 GB egress/month) is enough for personal use; if it stops being enough, move packages to Cloudflare R2 by changing the base URL.

## Phases

1. Python: `publish` (catalog, reference with tokens, items + clips, sentence-level timelines) and `upload`.
2. `shared/`: models, test generator, pass rule, seed test vectors.
3. Supabase project, migrations, RLS.
4. Android MVP (above, including the sentence-level karaoke transcript).
5. Web app.
6. After the Lesson 1 audit and regeneration: word-level timelines (with-timestamps TTS + local forced alignment).
7. Later: pinyin/tone module, Hanzi recognition, spaced repetition, pronunciation scoring, iOS.

## Out of scope for the MVP

Word-level karaoke, spaced repetition, pronunciation scoring, AI chat, gamification, handwriting/stroke order, web offline mode.

## Open questions

- Confirm the pass rule (requeue vs. fail on first mistake).
- Which item clips can be cut from existing TTS clips, and how many need new paid TTS.
- Before going commercial: ElevenLabs commercial licence for published audio, licence of any bundled dictionary data, Supabase paid tier.

## References

- Android Media3 background playback: https://developer.android.com/media/media3/session/background-playback
- Kotlin Multiplatform: https://kotlinlang.org/docs/multiplatform.html
- supabase-kt: https://github.com/supabase-community/supabase-kt
- Supabase pricing and limits: https://supabase.com/pricing
