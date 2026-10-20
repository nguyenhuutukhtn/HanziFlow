# Project status

Read this first in a new session instead of past chat history. Keep it short; update it at the end of every task.

Last updated: 2026-10-20 · branch `app-publish-pipeline` (not merged to `main`)

## Working rules

- One task at a time, sequentially. No parallel agents.
- Agents report with the template in [AGENTS.md](../AGENTS.md) ("Task reports").
- Bring a task back for human/PM review only for design decisions or when blocked.

## Done

- Content: Lesson 1 mini-story (guided track generated; practice track needs 2 paid TTS clips, skipped). Lesson 1 content audit still in progress.
- Python: `mandarin-course publish` → `publish/` (catalog, lesson, reference with tokens, items + clips, sentence-level timeline, 64 kbps mono). Draft vocab `lessons/lesson_001_vocab.json` awaits learner review.
- `kmp/shared`: contract models (tested against real publish output, JVM + JS), deterministic test generator + pass-rule session, transcript logic.
- `kmp/android`: prototype single screen — catalog sync, verified downloads, Media3 background playback (audio focus, noisy, wake lock, media-button resumption), karaoke transcript (4 views + Vietnamese, tap-to-seek, service-owned line loop with chip), long-press lookup.
- **G1**: Android refactored into `ui/library`, `ui/player` (stateless screens + per-screen `@HiltViewModel` + `@Preview`s), `data` (`CourseRepository`/`SettingsRepository`/empty `ProgressRepository` interfaces + impls), `playback/PlayerConnection` (sole MediaController wrapper) + Hilt DI (`HiltWorker` download worker, Konsist layering tests). No visual/behavioral change; verified on device (sync, download, play, tap-to-seek, loop set/clear, lookup, lock-screen controls).
- Docs: [app_architecture.md](app_architecture.md) (incl. code architecture), [ui/](ui/README.md) design spec.
- **D1**: Supabase migrations applied (progress tables + RLS + `updated_at` triggers, public `course` bucket with client read-only); `mandarin-course upload` mirrors `publish/` (immutable versions, catalog last) — 121 files uploaded, anonymous SHA-256 and anon-key RLS probes verified.
- **G2a**: app shell — amber dark/light theme + bundled Noto Sans (Hanzi uses system sans), bottom nav (Học / Ôn tập / Cài đặt), persistent mini-player (play/pause + Nghe lại câu), Now Playing full-screen route with reused transcript/transport/loop chip, track display names; verified on device (tabs, seek-to-line-start, loop chip, lock-screen notification).

## Next (in order)

1. **G2a follow-up** — headset/Bluetooth next/previous = next cue / replay current cue (app_architecture.md "Headset and Bluetooth controls"); the shell itself is done.
2. **G2b** — Learn tab card states + lesson detail, lookup sheet actions (copy word/line, select text, send to), Settings tab (incl. MIUI battery/autostart help).
3. **G2c** — Review tab + test flow + result; pass-rule change in `kmp/shared` TestSession (passed = no first-try mistakes and no reveal); attempts stored in Room.
4. **D2** — Android sign-in + progress sync. Starts with a migration fix: lesson_progress merge monotonic in a trigger (status max started<listened<passed, keep earliest passed_at, never downgrade even if the incoming write is newer); drop the update policy on test_attempts (push = insert ignore-duplicates).
5. Known follow-ups: test question type balancing (C2: cloze dominates); merge multi-character names in tokenizer + vocab (A3: 李明 split into 李/明).

## Open decisions (user)

- None. Decided 2026-10-20: pass rule = first-try 100%; accent amber; track names "Học có hướng dẫn" / "Luyện nghe"; UI review changes adopted (see docs/ui/README.md "Review decisions").
