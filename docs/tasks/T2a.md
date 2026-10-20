# T2a — compact story source + generic lesson builder
Status: todo · Suggested agent: fresh agent (new area), Codex or Claude Sonnet · Size: L

## Goal
Lesson content is authored in one small, human/AI-editable story source; `lessons/lesson_XXX.json` (+ `_practice.json`) become build artifacts of a generic builder. Replaces the per-lesson script `scripts/build_lesson_001_story.py` (1321 lines). Spec: [workflow.md](../workflow.md) principle 3 + "Content loop". T2b (export/import bundles + condensed checklist) follows and builds on this format.

## Read only
`scripts/build_lesson_001_story.py` (to extract the reusable block logic vs Lesson 1 data), `src/mandarin_course/lesson_schema.py`, `docs/content_rules.md` "Hard rules", `scripts/lesson.sh`, `justfile`. Query `lessons/*.json` with short scripts, never Read them whole.

## Do
1. **Story source format** `lessons/src/lesson_XXX.story.md`: Markdown, parsed by our own small parser (no new dependency). Header block for lesson metadata (title, level, goals, patterns, coach phrases, minutes); one `## <block type>` section per activity block with its parameters; content rows as pipe rows `key | speaker | 中文 | pinyin | Vietnamese [| pause_ms]`. Pinyin stays explicit (no auto-generation). Defaults (pauses, speakers) live in the builder, not repeated per row. Keep it as short as the data allows; it is what an AI will read and write.
2. **Generic builder** `src/mandarin_course/story/` (parser, block types, assembly; each file < 300 lines): the block logic now in the script (story lines, gloss/teach, circle questions, buildup, shadow, retell, change-one-detail, practice variant, …) becomes block types parameterized by the source. Lesson-1-only data moves into `lessons/src/lesson_001.story.md`; any lesson-1-only logic that can't be generalized cheaply becomes data, not code.
3. CLI `mandarin-course build-lesson <story.md>` → writes the guided + practice lesson JSON; short output (segments per activity, estimated minutes), ≤ 20 lines. Validation errors: ≤ 20 lines, each with the source line number.
4. `scripts/lesson.sh <id>` runs `build-lesson` first when `lessons/src/lesson_<id>.story.md` exists.
5. Delete `scripts/build_lesson_001_story.py` once the acceptance below passes. Update `docs/content_rules.md` "Workflow for a new lesson" (2–3 lines) and CODEMAP (`python3 scripts/codemap.py`).

## Acceptance
- `build-lesson lessons/src/lesson_001.story.md` reproduces the current `lessons/lesson_001.json` and `lessons/lesson_001_practice.json` **byte-for-byte** (check with `git diff --exit-code lessons/`).
- `mandarin-course generate lessons/lesson_001.json --dry-run` → 0 new paid TTS; `mandarin-course publish lessons/lesson_001.json` → `unchanged v3`.
- Parser tests: a malformed row and an unknown block type each give an error naming the source line.
- Existing tests pass (`bash scripts/check.sh`).

## Verify
`bash scripts/check.sh`; the build + `git diff --exit-code lessons/`; dry-run; publish. No upload, no paid TTS.

## Out of scope
export/import bundles and the authoring checklist (T2b); lesson 2; any change to lesson 1 content (if a byte-identical rebuild needs a content change, stop and report); kmp/; upload.
