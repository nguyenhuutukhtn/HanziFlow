# Project status

Read this first in a new session instead of past chat history. Keep it short; update it at the end of every task.

Last updated: 2026-10-20 (L1) · branch `app-publish-pipeline` (not merged to `main`)

## Working rules

- One task at a time, sequentially. No parallel agents.
- Agents report with the template in [AGENTS.md](../AGENTS.md) ("Task reports").
- Bring a task back for human/PM review only for design decisions or when blocked.
- Scripts first, AI as fallback: see [workflow.md](workflow.md).
- Every task (code or content) logs its token use in [token_log.md](token_log.md); review the log periodically to cut the most expensive steps.

## Done

- Content: Lesson 1 mini-story (guided track generated; practice track needs 2 paid TTS clips, skipped). Lesson 1 content audit still in progress.
- Python: `mandarin-course publish` → `publish/` (catalog, lesson, reference with tokens, items + clips, sentence-level timeline, 64 kbps mono). Draft vocab `lessons/lesson_001_vocab.json` awaits learner review.
- `kmp/shared`: contract models (tested against real publish output, JVM + JS), deterministic test generator + pass-rule session, transcript logic.
- `kmp/android`: prototype single screen — catalog sync, verified downloads, Media3 background playback (audio focus, noisy, wake lock, media-button resumption), karaoke transcript (4 views + Vietnamese, tap-to-seek, service-owned line loop with chip), long-press lookup.
- **G1**: Android refactored into `ui/library`, `ui/player` (stateless screens + per-screen `@HiltViewModel` + `@Preview`s), `data` (`CourseRepository`/`SettingsRepository`/empty `ProgressRepository` interfaces + impls), `playback/PlayerConnection` (sole MediaController wrapper) + Hilt DI (`HiltWorker` download worker, Konsist layering tests). No visual/behavioral change; verified on device (sync, download, play, tap-to-seek, loop set/clear, lookup, lock-screen controls).
- Docs: [app_architecture.md](app_architecture.md) (incl. code architecture), [ui/](ui/README.md) design spec.
- **D1**: Supabase migrations applied (progress tables + RLS + `updated_at` triggers, public `course` bucket with client read-only); `mandarin-course upload` mirrors `publish/` (immutable versions, catalog last) — 121 files uploaded, anonymous SHA-256 and anon-key RLS probes verified.
- Lesson 1 v2: chunk-buildup response gaps increased (1.8s chunks, 3s slow full line, 4.2s natural full line); guided audio regenerated from cache and published/uploaded as immutable v2. Public catalog and guided MP3 SHA-256 verified; superseded v1 removed from local `publish/` and Supabase Storage. Authoring audit now ignores vocab/other lesson sidecars.
- **G2a**: app shell — amber dark/light theme + bundled Noto Sans (Hanzi uses system sans), bottom nav (Học / Ôn tập / Cài đặt), persistent mini-player (play/pause + Nghe lại câu), Now Playing full-screen route with reused transcript/transport/loop chip, track display names; verified on device (tabs, seek-to-line-start, loop chip, lock-screen notification).
- **G2a.1**: headset/Bluetooth next/previous cue navigation in PlaybackService (shared `CueNavigation`, own timeline cache, notification actions); Supabase content URL via BuildConfig from `kmp/local.properties`; transcript punctuation baseline + 4dp token gaps + duplicate Vietnamese suppression; Roborazzi/Compose UI tests; verified on device (fresh Supabase sync/download/play, keyevents 87/88 with screen off and UI closed).
- **L1**: `publish` enriches each timeline cue with `language`/`type`/`section_id` (joined from the lesson JSON by line id, turns included) and derives top-level `sections` (one per run of consecutive same-`activity` segments); fails with a "regenerate" message if cue line ids/order no longer match the current lesson segments. `kmp/shared` `TimelineCue`/`Timeline` models gained the matching optional fields (old timelines still parse) + a new `TimelineSection` model; `commonTest/resources/contract/*` refreshed from a real publish and `ContractParsingTest` asserts sections are contiguous, cover the track, and every cue has one. Lesson 1 republished as v3 (guided track rebuilt from cache, 0 paid TTS) and uploaded; public catalog + guided MP3 SHA-256 verified against Supabase; superseded v2 removed from local `publish/` and Storage.

## Next (in order)

0. **Now (before G2b, which is paused):** listening features from real use — see app_architecture.md "Chinese-only listening" and "Section replay".
   - **V1 (Python):** auto-versioning in publish + prune superseded versions in upload (app_architecture.md "Versioning rule").
   - **T1:** justfile + short-by-default CLI output (docs/workflow.md).
   - **T2:** compact lesson story source + generic builder + export/import bundles for any AI (docs/workflow.md). Before L2 and before lesson 2.
   - then **L2 (Android):** Chinese-only mode in PlaybackService + toggle; section list, seek-bar markers, section loop up to 5× with Bỏ lặp / Sang phần sau.

1. **G2b** — Learn tab card states + lesson detail, lookup sheet actions (copy word/line, select text, send to), Settings tab (incl. MIUI battery/autostart help).
2. **G2c** — Review tab + test flow + result; pass-rule change in `kmp/shared` TestSession (passed = no first-try mistakes and no reveal); attempts stored in Room.
3. **D2** — Android sign-in + progress sync. Starts with a migration fix: lesson_progress merge monotonic in a trigger (status max started<listened<passed, keep earliest passed_at, never downgrade even if the incoming write is newer); drop the update policy on test_attempts (push = insert ignore-duplicates).
4. Known follow-ups: test question type balancing (C2: cloze dominates); merge multi-character names in tokenizer + vocab (A3: 李明 split into 李/明).

## Open decisions (user)

- None. Decided 2026-10-20: pass rule = first-try 100%; accent amber; track names "Học có hướng dẫn" / "Luyện nghe"; UI review changes adopted (see docs/ui/README.md "Review decisions").
