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
- Docs: [app_architecture.md](app_architecture.md) (incl. code architecture), [ui/](ui/README.md) design spec.
- Supabase project created; keys in `.env` (git-ignored).

## Next (in order)

1. **G1** — refactor Android into ui/domain/data/playback layers + Hilt + Konsist, no visual change.
2. **D1** — Supabase migrations + RLS + `course` bucket; `mandarin-course upload`.
3. **G2** — rebuild UI per `docs/ui/` (bottom nav, mini-player, Now Playing, Learn, Review/test flow, Settings, lookup copy/share) — needs the open decisions below.
4. **D2** — Android sign-in + progress sync.
5. Known follow-ups: test question type balancing (C2: cloze dominates); merge multi-character names in tokenizer + vocab (A3: 李明 split into 李/明).

## Open decisions (user)

- Pass rule: (a) pass only if every question is right on the first try, wrong answers still requeued for learning — recommended; or (b) pass when the attempt is completed.
- Accent color (spec proposes amber).
- Track names: keep "Guided / Luyện tập" or e.g. "Học có hướng dẫn / Luyện nghe".
- UI spec review changes listed in [ui/README.md](ui/README.md) "Pending review changes".
