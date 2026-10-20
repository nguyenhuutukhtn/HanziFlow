# AGENTS.md — rules for any AI tool authoring content in this repo

This file is the entry point for Codex, Cursor, Claude Code, or any other AI agent
asked to write or edit lesson content, curriculum, or the generation pipeline here.
It is a summary. The two files it points to are the actual source of truth — read
them before writing a lesson, not just this file:

- [`docs/lesson_authoring_guidelines.md`](docs/lesson_authoring_guidelines.md) — content/authoring rules.
- [`curriculum/roadmap.md`](curriculum/roadmap.md) — curriculum sequencing, level structure, Vietnamese fade-out schedule.

If this file ever disagrees with those two, the two win — update this file to match
rather than trusting it blindly.

## What this is

An offline-first Mandarin listening/speaking course for a Vietnamese-speaking learner.
Audio is generated from JSON lesson files (`lessons/*.json`, validated by
`src/mandarin_course/lesson_schema.py`) via ElevenLabs TTS, assembled with FFmpeg.
There is no UI yet; everything is a CLI (`mandarin-course ...`) run against lesson JSON.

App work (Android first, then web; Kotlin Multiplatform `shared/` logic; Supabase
for auth, progress sync, and lesson package storage) follows
[`docs/app_architecture.md`](docs/app_architecture.md). Read it before touching
`publish`, `kmp/`, `web/`, or `supabase/`. Clients never receive TTS or Supabase
admin credentials, and `publish` never calls ElevenLabs. For app work, start with
[`docs/STATUS.md`](docs/STATUS.md) (what's done, what's next, open decisions)
and, for UI, [`docs/ui/README.md`](docs/ui/README.md) plus only the screen files
your task touches.

## Current direction — read this before writing any new lesson

The course pivoted away from scripted two-character textbook dialogue (greetings,
ordering coffee, etc.) after real listener feedback. **Do not write new lessons in
that old shape.** `lessons/lesson_001.json` is the mini-story template. Copy its
shape: one narrator, short character lines, meaning mapped once, then three Q&A
passes (Vietnamese, guided Chinese, Chinese-only). The builder is
`scripts/build_lesson_001_story.py`.

The current method is a **mini-story** format (Effortless English / TPRS-style):

1. **Story first, grammar tracked, not taught.** Pick or write a story because it is
   good and realistic, not because it contains a grammar point. Check the grammar it
   happens to use *after* writing it, against what earlier lessons already covered.
   Never start from a grammar point and build a story to contain it.
2. **Circling Q&A is the main drill**, not full-sentence recall: yes/no, either/or
   (`A还是B`), short wh-questions about concrete story facts, so the learner
   recognizes and guesses before they can produce.
3. **Point-of-view retelling** repeats the story with one variable changed (subject,
   location, color, quantity, a flipped fact). **Never change tense/aspect as a
   retelling variable before that marker's own lesson has introduced it** (e.g. no
   past-tense retelling before `了` lands in week 12) — that would teach an untaught
   structure through the back door.
4. **Narrator shape is the Lesson 1 template:** one narrator, plus short lines
   from the people in the story. Do not reopen a scripted two-person dialogue.

## Hard rules (schema- and pipeline-level, not just style)

- **Coach narration is bilingual from the start, not just target vocabulary.**
  Scene-setting, transitions, instructions, and explanations are first spoken in
  simple Mandarin (by a stable Mandarin-coach voice, speaker `coach_zh`), then
  restated in Vietnamese (speaker `coach`). Fade the Vietnamese as the line becomes
  familiar — see "Gradual reduction of Vietnamese support" in the guidelines doc.
  Chinese and Vietnamese are always separate TTS segments; never send mixed
  Vietnamese–Mandarin text to one voice.
- **Do not insert praise after every retrieval line.** One or two varied cues per
  activity, not a cycling list fired on every answer — this was a real bug found in
  the Lesson 1 trial script and is now an explicit rule.
- **Voice IDs are not stored in `.env` or lesson JSON.** Run `mandarin-course voices
  sync` once (manually, re-run only when the account's voice library changes) to
  populate `data/voices.json`. Generation picks a voice per lesson/role from that
  registry deterministically (same lesson regenerates with the same cast).
  Lessons 1–2 (`absolute_beginner`) get top priority toward a Beijing Mandarin
  voice automatically — don't hardcode an accent in the story text or JSON.
- **`new_patterns`/`review_pattern_ids`** (dialogue content) and
  **`coach_phrases_introduced`/`coach_phrases_reduced`** (coach narration) are two
  separate manual ledgers — don't conflate them. Run `mandarin-course audit` before
  finalizing a lesson's Vietnamese fade decisions; it flags reused patterns that
  were never introduced, duplicates, and the reuse map.
- **Backward buildup** for a hard line must nest: each step contains the previous
  chunk, then the full line (`什么 → 叫什么 → 你叫什么？`). A chunk that doesn't
  contain the prior step is chunk practice, not buildup — don't mislabel it.
- **`tts_tags`** (on a `Segment`, or per-turn when using `turns`) are inline
  ElevenLabs delivery tags (`["gentle"]` → `[gentle] text`), sent to TTS only —
  never in `text`/transcript/pinyin/translation. Use sparingly (scene openings,
  reassurance, genuine emotional beats), never on every line. See "Audio tags
  for delivery" in the guidelines doc.

## Workflow for a new lesson

1. `mandarin-course validate lessons/<file>.json` — schema check, no API calls.
2. `mandarin-course generate lessons/<file>.json --dry-run` — shows cache hits vs.
   new paid TTS requests and missing configuration, still no API calls.
3. `mandarin-course audit lessons` — cross-lesson pattern/coach-phrase continuity
   check (read-only, no API calls).
4. `mandarin-course generate lessons/<file>.json` — confirms paid request count
   before calling ElevenLabs.
5. Run `pytest` (project root) before considering any change done. Lesson-content
   tests in `tests/test_lesson_001_story.py` assert on Lesson 1's actual text.

## Scope discipline

- Lesson 1 is the template. Do not author lesson 2 until this lesson has been heard and the fade feels right.
- Don't add adaptive/automatic familiarity tracking yet — the ledgers above are
  manual by design until the guided/practice player is stable.
- Don't pick a grammar point and construct a story around it (see "Current
  direction" above) — this is the single most common way this course accidentally
  reverts to feeling like a textbook.

## Token budget (app and pipeline tasks)

- Don't read large generated or data files unless the task needs them: `publish/`,
  `cache/`, `output/`, `**/build/`, `kmp/shared/src/commonTest/resources/`, and
  the full `lessons/*.json` (query them with a short script instead). These are
  listed in `.cursorignore`.
- Run Gradle with `-q` and show only the tail of failures.
- Device checks: prefer text (`adb shell dumpsys media_session`, `uiautomator dump`
  grepped, logcat filtered) over screenshots; at most 3 screenshots per task,
  saved under `kmp/build/screenshots/`. Visual regressions belong in local
  screenshot tests, not repeated device captures.
- Do the verification yourself (build, tests, lint, architecture tests) before
  reporting; don't hand unverified work back for review.
- At the end of a task, update [`docs/STATUS.md`](docs/STATUS.md) (Done / Next /
  Open decisions) in a few lines.

## Task reports

Report in at most 12 lines, in this shape, and nothing else:

```text
Result: done | blocked (why) | partial (what's missing)
Verified: <commands run> → <pass/fail counts>; device: <what, how> or "not run"
Changed: <files or folders, one line>
Deviations: <from the prompt/spec, or "none">
Needs user: <decisions or manual steps, or "none">
```
