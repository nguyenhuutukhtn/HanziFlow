# Content rules for lesson authoring

Read this for any task that writes or edits lesson content, curriculum, or the lesson builder. It summarizes the two sources of truth, which win on any disagreement: [lesson_authoring_guidelines.md](lesson_authoring_guidelines.md) and [../curriculum/roadmap.md](../curriculum/roadmap.md).

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
- **Imitation needs a speaking gap, not a transition pause.** For absolute beginners,
  start at no less than 1.5s after each buildup chunk, 3s after the slow full line,
  and 4s after the natural full line; lengthen for longer utterances. Lesson 1 uses
  1.8/3.0/4.2s. Verify the generated timeline/audio and protect the gaps with a test.
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


Republishing an already published lesson follows the versioning rule in [architecture/content_package.md](architecture/content_package.md) (publish assigns the version; upload prunes superseded versions after verification). Do not bump the lesson JSON `version` by hand.

## Scope discipline

- Lesson 1 is the template. Do not author lesson 2 until this lesson has been heard and the fade feels right.
- Don't add adaptive/automatic familiarity tracking yet — the ledgers above are
  manual by design until the guided/practice player is stable.
- Don't pick a grammar point and construct a story around it (see "Current
  direction" above) — this is the single most common way this course accidentally
  reverts to feeling like a textbook.
