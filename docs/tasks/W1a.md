# W1a — shared JS facade for the web player
Status: todo · Suggested agent: codex/gpt-5.6-luna (low) · Session: new · Size: S · Device check: none
Wave: 1 · Owns: `kmp/shared/src/commonMain/kotlin/com/hanziflow/shared/web/`, `kmp/shared/src/commonTest/kotlin/com/hanziflow/shared/web/`, `kmp/shared/build.gradle.kts` (only if the browser library output needs it) · Depends on: none · Checkpoint after: yes

## Goal
The React web app (W1b) must reuse the shared contract parsing and transcript rules instead of re-implementing them in TypeScript. Expose them to JS through one small `@JsExport` facade that takes and returns JSON strings / primitives, following the pattern in `testgen/TestGenJs.kt`.

## Read only
- `kmp/shared/src/commonMain/kotlin/com/hanziflow/shared/testgen/TestGenJs.kt` (the pattern to copy)
- `Models.kt` (`ContractParser`, `CourseJson`, `Catalog`, `Timeline`, `Reference`, `Token`)
- `transcript/TranscriptCues.kt`, `transcript/TranscriptRendering.kt`, `transcript/CueNavigation.kt`, `transcript/SectionNavigation.kt`, `transcript/TrackSwitch.kt`, `characters/Characters.kt` (signatures only, `grep -n "fun "`)
- `kmp/shared/src/commonTest/kotlin/com/hanziflow/shared/ContractParsingTest.kt`: how fixtures under `commonTest/resources/contract/` are loaded (they run on JS too)

## Do
1. `web/PlayerJs.kt`: `@JsExport class LessonSessionJs(catalogJson, lessonId, timelineJson, referenceJson, charactersJson?)` (or a similar shape) exposing:
   - `lessonTitle()`, `audioPath(track)` (`base_url + track file`, relative to the content base URL), `timelinePath(track)`, `referencePath()`, `charactersPath()` (null when absent), and `tracksJson()` (names + display names);
   - `activeCueIndex(positionMs): Int` (−1 when none), `cueCount()`, `cueJson(i)`;
   - `lineJson(lineId, view, showVietnamese, positionMs)` → the `TranscriptRendering.decide` result as JSON: tokens `{hanzi, pinyin, showPinyin}`, `showHanzi`, `showPinyinLine`, `translation` / `showTranslation`, `lineVisible`, `placeholder`. `view` is the `TranscriptViewMode` name;
   - `seekTargetMs(lineId, positionMs)`, `nextCueStartMs(positionMs)`, `previousCueStartMs(positionMs)`, `sectionsJson()`;
   - `characterJson(char)` (`CharacterEntry` + its components; null when unknown).

   A static `CatalogJs.lessonsJson(catalogJson)` returns id/title/order/version for the library list.
2. Use `Double` for millisecond values crossing the JS boundary (JS `Long` is not a plain number). Make no change to existing classes.
3. Tests in `commonTest/.../web/` against the real fixtures (`catalog.json`, `timeline_guided.json`, `reference.json`, `characters.json`), with the concrete expected values below.

## Acceptance
- `CatalogJs.lessonsJson` on the fixture catalog → one lesson, `lesson_001`.
- For the fixture timeline, take cue #5 (`scene_bag_vi`, 21024–29902 ms in v7; read the actual values from the fixture and hardcode them in the test). Then `activeCueIndex(21500.0)` = 5, `activeCueIndex(-1.0)` = −1, and `seekTargetMs("scene_bag_vi", 0.0)` = its `start_ms`.
- `lineJson` for one zh line with `HANZI_WITH_PINYIN` returns tokens whose concatenated `hanzi` equals the line `text`. Punctuation tokens have `showPinyin=false`.
- `characterJson("我")` (or another char present in the fixture) has `han_viet` and `vi_meaning` equal to the fixture values. `characterJson("龘")` is null.
- These tests run on **both** JVM and JS (`shared:allTests`).
- `./gradlew -q :shared:jsBrowserProductionLibraryDistribution` (or whatever task produces the browser library; name it in the report) emits `.mjs`/`.js` + `.d.ts` containing `LessonSessionJs`.

## Verify
```bash
cd kmp && ./gradlew -q :shared:allTests; echo "exit=$?"
grep -o 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' shared/build/test-results/*/TEST-*web*.xml
grep -l "LessonSessionJs" -r shared/build/dist/js/ | head -3
cd .. && bash scripts/check.sh
```
Paste the output. Both a jvm and a js `TEST-*web*.xml` must show failures="0" with tests > 0.

## Out of scope
Any web/React code; changes to existing shared classes or the contract; Android.
