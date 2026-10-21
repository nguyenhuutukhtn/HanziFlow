# C2-fix — finish C2 and make it compile
Status: todo · Suggested agent: continue the C2 codex window, gpt-5.6-luna (medium) · Session: 01a15e1a-645b-7392-88d0-03e24f64ab76 · Size: M · Device check: user

## Callback channel
Before each major step run `bash scripts/agent_ask.sh C2-fix --inbox` — new notes are
instructions from the PM/user. Missing information? Run
`bash scripts/agent_ask.sh C2-fix "<question>"` and use its answer instead of guessing.

## Why
The C2 run never compiled (the sandbox could not write `~/.gradle`), so two files are
broken and three brief items were skipped. `bash scripts/check.sh` currently fails at
`:shared:compileKotlinJvm`.

## Must fix (compile blockers)
1. `kmp/shared/.../shared/Models.kt:196` — `CourseJson.characters` references
   `LessonCharacters` with no import. Add
   `import com.hanziflow.shared.characters.LessonCharacters`.
2. `kmp/android/.../data/CourseRepositoryImpl.kt` — the `downloadedFiles` expression is
   leftover garbage (`downloadedCatalog?.files?.let { decodeFromString(...).tracks }`).
   It must simply be `val downloadedFiles = downloadedCatalog?.files`.

## Must finish (skipped brief items, see docs/tasks/C2.md §Do 4–7)
3. **Lookup sheet actions are plain `Text`, not actions.** Make "Sao chép từ" (copies the
   word's Hanzi), "Sao chép câu" (copies the current line's Hanzi text) and "Chia sẻ đến…"
   (`Intent.ACTION_SEND`, `text/plain`, the Hanzi) real buttons/rows that work. Pass the
   line text in from `PlayerUiState`/the screen; use `LocalContext`/`ClipboardManager`.
   Keep `LookupSheet.kt` under 300 lines and keep the imports grouped like the rest of the
   file (alphabetical, no mid-file imports).
4. **"Chữ của bài" route is missing entirely.** Add it per C2 §Do 5: an action in
   `NowPlayingScreen` (header action or overflow) labelled "Chữ của bài" that opens a
   screen/sheet listing the lesson's characters (Hanzi + pinyin + Hán-Việt + vi meaning)
   and, in a second group titled "Bộ phận cần biết", the prerequisite components; tapping an
   item opens its `CharacterCard`. Hidden when `characterIndex == null`.
5. **Tests are missing on the Android side.** Add, per C2 §Do 7: a Compose UI test (JVM,
   semantics, in the style of `PlayerUiTest`) covering lookup chip → card and component chip
   → that component's card; and a `PlayerViewModel` test with `FakeCourseRepository` where
   `charactersFiles` is empty → `characterIndex == null` and no entry points are shown.
6. **Polish in `CharacterCard.kt`/`NowPlayingScreen.kt`:** add a `@Preview` for the card like
   the other screens have, use `FlowRow` for the lesson-word chips, use the spacing tokens
   (`ui/theme/Spacing`) instead of raw `24.dp`/`6.dp` where a token exists, and replace the
   fully-qualified `androidx.compose.material3.ModalBottomSheet` call with a normal import.

## Acceptance
Unchanged from `docs/tasks/C2.md` §Acceptance — all of it, including
`bash scripts/check.sh` passing and no new ⚠ over-300-line file in `docs/CODEMAP.md`.

## Verify
```bash
bash scripts/check.sh 2>&1 | tail -15
cd kmp && ./gradlew -q :shared:allTests --tests '*Character*' 2>&1 | tail -10
./gradlew -q :android:testDebugUnitTest --tests '*Character*' --tests '*PlayerUi*' --tests '*PlayerViewModel*' 2>&1 | tail -10
```
Run them yourself; Gradle is writable in this run. If Gradle still cannot write its cache,
stop and report that instead of guessing — do not finish without compiling.

## Out of scope
Same as C2: no stroke animation, no progress/SRS, no Pleco detection or "Chọn văn bản…",
no pipeline change, no commit.
