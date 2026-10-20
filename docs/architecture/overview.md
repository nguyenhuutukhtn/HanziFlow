# Architecture — overview

> Part of the architecture docs; index: [../app_architecture.md](../app_architecture.md).

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

## Web app (after Android)

- TypeScript + React + Vite, single-page app behind sign-in, hosted on Cloudflare Pages.
- Online only: reads and writes Supabase directly with supabase-js and streams audio from Storage URLs. No service worker or local database.
- Uses the `shared/` JS output for test generation and the pass rule.
- Media Session API for media keys and headset controls.
- Same screens as Android, minus downloads.

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
