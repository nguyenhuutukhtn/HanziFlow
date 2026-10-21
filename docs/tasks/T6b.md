# T6b — Lesson metrics vs the model lesson + row-level revise
Status: todo (after T6a) · Suggested agent: continue T6a window · Size: M · Device check: user (none)

## Goal
Two cheap tools for the authoring loop: (1) compare a lesson's numbers against lesson 001 (the model lesson, user-approved 2026-10-21) by script instead of asking an AI "is it like lesson 1?"; (2) revise only flagged rows instead of regenerating a whole lesson.

## Read only
- What T6a added (`story/ledger.py`) and `story/bundle.py` (import validation path).
- `src/mandarin_course/story/` parser: how a story row is identified (first column, e.g. `scene_bag`, plus its section/activity).

## Do
1. `mandarin-course lesson-metrics <id> [--against 001]` (+ just recipe, `--json`): from the built lesson JSON (+ vocab, ledger's known words):
   - new words, new characters, words reused from earlier lessons;
   - zh sentence length in chars (avg, max), number of zh lines;
   - Vietnamese share (segments and estimated duration; estimate = chars × a per-language rate, documented constant);
   - repeats per new word in zh segments (min, avg; list words heard < 3 times);
   - zh tokens not in any known/new vocab (possible unintroduced words);
   - estimated total duration.
   Print one line per metric: value · model value · `ok`/`FLAG` (flag when outside ±30% of the model, or any word < 3 repeats, or any unintroduced token). ≤ 15 lines. Exit 0 always (it advises; import-lesson gates).
2. `mandarin-course export-rows <id> <row_ref>...` → `build/bundles/lesson_<id>.rows.md`: the condensed checklist (first 20 lines of `docs/authoring_checklist.md`), the ledger's cumulative section, each requested row with 2 neighbours for context, and the reply format: one fenced ```rows block with only the replaced rows, same refs.
3. `mandarin-course import-rows <id> <file>`: replace exactly those rows in `lessons/src/lesson_<id>.story.md` (save the previous version as import-lesson does), then run the same build → validate → audit → dry-run count path as `import-lesson`. Unknown ref or extra rows → error ≤ 20 lines, file untouched.
4. Tests: metrics on lesson 001 against itself = all `ok`; a synthetic lesson with a word heard twice → FLAG; export-rows/import-rows round-trip on a copy of lesson 001 changes only the target row (diff of the story file = that row).

## Acceptance
- `just check` passes; no new ⚠ file.
- `lesson-metrics 001 --against 001` prints only `ok`.
- Verify below exits 0.

## Verify
```
just check
mandarin-course lesson-metrics 001 --against 001 | tee /dev/stderr | grep -q FLAG && exit 1
mandarin-course export-rows 001 scene_bag >/dev/null && test -s build/bundles/lesson_001.rows.md
git diff --exit-code lessons/
```

## Out of scope
Skills (PM writes them after this), lesson 002 content, Kotlin, upload.
