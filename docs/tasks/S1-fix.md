# S1-fix — stroke pack: close test gaps found in review
Status: todo · Suggested agent: continue the S1 Codex window (same files, same context) · Session: continue S1 · Size: S · Device check: none (no device-dependent change)

## Why
PM review of S1 found 2 real bugs, already fixed directly (both under ~10 lines, in the
working tree now, not committed):
- `kmp/shared/.../characters/StrokeData.kt`: `StrokePack` was `data class StrokePack(val
  strokes: Map<String, StrokeEntry>)`, but the actual pack file on disk is a **flat**
  `{char: entry}` object — no `"strokes"` wrapper key. Changed to `typealias StrokePack =
  Map<String, StrokeEntry>`. Also added the missing `CourseJson.strokePack(value: String):
  StrokePack` parser (the brief asked for one, it was never added).
- `src/mandarin_course/characters/stroke_pack.py`: unconditional
  `lesson["files"]["characters"]["file"]` crashes `build-stroke-pack` entirely for **any**
  lesson that doesn't yet have `characters.json` (a near-term case — lesson_002 may not have
  one authored yet). Guarded with `characters_ref = lesson["files"].get("characters"); if not
  characters_ref: continue`.
- Removed a stray duplicate `return 0` in `cli.py`'s `command_build_stroke_pack`.

What's still missing (not fixed by PM — this brief is to close it):
1. **No test exercises `CourseJson.strokePack(...)`** against the real pack shape. Add a small
   fixture: 2–3 real entries from `publish/stroke_pack/v2/strokes.json` (e.g. `我`, `们`, `扌`)
   copied into `kmp/shared/src/commonTest/resources/contract/strokes.json`, registered in
   `kmp/shared/build.gradle.kts`'s `fixtureFiles` list (same mechanism as the other fixtures —
   read that file's `generateFixtureData` task to see the pattern), and a test (in
   `CharacterContractTest.kt` or a new small file) asserting `CourseJson.strokePack(...)`
   parses the flat map directly (no wrapper), has the expected chars, and each has a non-empty
   `strokes` list.
2. **No Python test for `build_stroke_pack`** (the union-of-chars logic, the
   unchanged-on-rerun digest compare, and the just-fixed missing-`characters.json` guard). Add
   one to `tests/` (follow the existing `tests/test_characters.py`/`test_publish.py` style —
   `tmp_path` fixtures, no real network/TTS): a fake `catalog.json` + `characters.json` with a
   known small char set, assert the pack's char set matches, assert a second call with no
   input change reports `unchanged`, and assert a lesson entry **without** a `files.characters`
   key is skipped instead of raising.
3. **No test for `CourseRepositoryImpl.strokePackFile()`'s caching behavior.** Checked during
   review: this codebase has no `MockWebServer` dependency and no existing test for
   `LessonDownloadWorker`'s network path either — untested IO is the existing convention here,
   verified by device check instead. **Do not add a new test dependency for this** — skip it,
   this item is informational only (explains why it's not in the acceptance list below).

## Read only
This brief, `kmp/shared/build.gradle.kts`, `kmp/shared/src/commonTest/kotlin/com/hanziflow/shared/{ContractParsingTest.kt,CharacterContractTest.kt}`,
`kmp/shared/src/commonMain/kotlin/com/hanziflow/shared/characters/StrokeData.kt`,
`src/mandarin_course/characters/stroke_pack.py`, `tests/test_characters.py` (style reference only).

## Acceptance
- A shared test parses a real (small, fixture-sized) stroke pack payload via
  `CourseJson.strokePack` and asserts concrete chars/stroke counts — not just "non-null".
- A Python test builds a stroke pack from a fake catalog with one lesson missing
  `files.characters` and asserts it doesn't raise and simply excludes that lesson's chars.
- `bash scripts/check.sh` passes; no new file over 300 lines in `docs/CODEMAP.md`.

## Verify
```bash
cd kmp && ./gradlew -q :shared:allTests --tests '*Stroke*' 2>&1 | tail -10
cd .. && python3 -m pytest tests/ -k stroke -q 2>&1 | tail -10
bash scripts/check.sh 2>&1 | tail -8
```
Per the "no silent pass" rule in `.claude/skills/pm-session/SKILL.md`: grep the real test count
from `kmp/shared/build/test-results/.../TEST-*StrokeData*.xml` (or wherever the new test class
lands) — a `--tests` filter matching zero classes looks identical to a pass otherwise.

## Out of scope
- `CourseRepositoryImpl.strokePackFile()` network/caching tests (see item 3 above — skip).
- S2 (renderer/UI) — this brief is pipeline + shared only.
- Committing.
