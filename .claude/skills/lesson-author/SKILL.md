---
name: lesson-author
description: Write or revise a lesson (lessons/src/lesson_<id>.story.md) with lesson 001 as the model — outline, one AI draft, import gates, metrics vs lesson 001, row-level fixes from review findings, Hán-Việt for new characters. Use when asked to "write lesson <id>", "draft lesson", "fix rows" or "apply review findings".
---

# Lesson author

The learner can't validate Chinese or Hán-Việt. Correctness comes from the gates, the metrics and a review by a **different model** (`lesson-review` skill). Your job: produce a source that passes the gates and sits close to lesson 001's metrics.

## Rules

- Edit only `lessons/src/lesson_<id>.story.md`, `lessons/src/bible.md` and the Vietnamese character data through `import-characters`. Never hand-edit `lessons/*.json` (built output), `publish/`, `cache/`.
- Never run `generate` without `--dry-run`, never `upload`. Paid TTS needs the user's explicit OK on the dry-run count.
- Work only from the bundles below; don't read other lessons' JSON.
- Outputs to the user/PM: ≤ 12 lines, AGENTS.md report template.

## 1. Outline (user approves before drafting)

1. `just ledger <id>` → read `build/ledger_<id>.md` (earlier lessons, known words/chars, recycle candidates, story bible).
2. Write `build/outlines/lesson_<id>.md`, ≤ 25 lines: story premise continuing the bible's threads, one new grammar pattern, 6–10 new words (each with why it fits the scene), 3+ recycle candidates to reuse, scenes in order.
3. Stop and show the outline. The user approves the story/topic, not the Chinese.

## 2. Draft (one shot)

1. `just export-lesson <id>` → `build/bundles/lesson_<id>.bundle.md` (checklist + ledger + format example). Self-contained; a chat model without repo access can draft from it.
2. Write the full story source as one ```story block following the outline and the bundle's checklist. Save the reply to `build/drafts/lesson_<id>.reply.md`.
3. `just import-lesson <id> build/drafts/lesson_<id>.reply.md`. On errors, fix only the named source lines and re-import. After 3 failing rounds, stop and report the remaining errors.

## 3. Metrics

`just lesson-metrics <id>` (compares against 001). For each `FLAG`:
- word heard < 3 times → add it to existing story/ask rows, not new scenes;
- unintroduced token → replace with a known/new word, or add it to the lesson's vocab on purpose;
- length/duration/Vietnamese share ±30% → trim or extend whole blocks.
A FLAG may stay if you can say why in one line in the report.

## 4. Characters

`just export-characters <id>` → author Hán-Việt + Vietnamese meaning for the new characters only → `just import-characters <id> <file>`. Mark every reading you are not sure of with `?` in your report (not in the data); the reviewer cross-checks all of them anyway.

## 5. Row fixes (after `lesson-review`)

For each finding, by row ref (`ref`, or `ref#n` when the ref repeats):
1. `just export-rows <id> <ref>...` → `build/bundles/lesson_<id>.rows.md` (rows + 2 neighbours + reply format).
2. Write only the replaced rows, same refs, one ```rows block; save to `build/drafts/lesson_<id>.rows.reply.md`.
3. `just import-rows <id> build/drafts/lesson_<id>.rows.reply.md` (rebuild + gates + dry-run count).
Never rewrite rows nobody flagged. Re-run metrics after the batch.

## 6. Bible

If the lesson adds cast, places, objects or a running thread, update `lessons/src/bible.md` (keep ≤ 40 lines, same `####` headings).

## Done when

Import passes, metrics FLAGs fixed or justified, characters imported, bible updated, review findings of severity `block`/`fix` applied. Report the dry-run TTS request count; the user decides on generating.
