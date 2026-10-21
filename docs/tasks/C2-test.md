# C2-test — Android tests + preview for the character card
Status: todo · Suggested agent: Codex IDE extension, gpt-5.6-luna (low) · Session: new · Size: S/M · Device check: user

## Why
C2 + C2-fix landed the feature and it compiles — `bash scripts/check.sh` passes
(pytest, kmp-shared, android-unit, codemap, file sizes) and the shared contract test
prints the real values (`我 ngã`, `意 ý`, `答 đáp`, `示 thị`, `里 lý`). Only the Android-side
tests and the preview from `docs/tasks/C2.md` §Do 6–7 are missing, plus one UI gap below.

## Callback channel
Before each major step run `bash scripts/agent_ask.sh C2-test --inbox` — new notes are
instructions. Missing information? Run `bash scripts/agent_ask.sh C2-test "<question>"`
and use its answer instead of guessing.

## Read only
`AGENTS.md`, this brief, `docs/tasks/C2.md` (§Do 5–7, §Acceptance),
`.claude/skills/android-ui-testing/SKILL.md`, and the files you touch:
`kmp/android/src/{main/kotlin/com/hanziflow/ui/characters/CharacterCard.kt,
main/kotlin/com/hanziflow/ui/player/{NowPlayingScreen.kt,LookupSheet.kt},
test/kotlin/com/hanziflow/{ui/PlayerUiTest.kt,fakes/FakeCourseRepository.kt}}`.

## Do
1. **Compose UI test** (JVM, semantics, in the style of `PlayerUiTest`): long-press/open the
   lookup sheet for a word with character data → a chip per Hanzi is shown; tapping one opens
   the card with its Hán-Việt and Vietnamese meaning; tapping a component chip opens that
   component's card. Use test tags where a semantics match would be ambiguous.
2. **ViewModel test** with `FakeCourseRepository` (`charactersFiles` empty): `characterIndex`
   stays null, and a second test with a real fixture file: it is non-null and
   `charactersOf("我们")` has 2 entries. Assert the "Chữ của bài" entry point is hidden in the
   null case (via the UI test or the state, whichever is cheaper).
3. **`@Preview`** for `CharacterCard` (and `CharacterListSheet` if cheap), wrapped in
   `HanziFlowTheme`, with a small hand-made `CharacterEntry`/`CharacterIndex` fixture —
   matching how the other screens do previews.
4. **Check the "Chữ của bài" affordance actually exists in `NowPlayingScreen`** and is hidden
   when `characterIndex == null`; if the entry point was never wired to a visible action
   (header action or overflow item labelled "Chữ của bài"), add it now per C2 §Do 5.
5. Keep every touched file under 300 lines; `TranscriptPanel.kt` (337) is already over and must
   not grow. Record Roborazzi goldens to the build dir only; do not commit screenshots.
6. Update `docs/STATUS.md` (C2 done) and add a `docs/token_log.md` row. Do not commit.

## Acceptance
- The new tests fail if the chip row or the component step is broken (verify by temporarily
  breaking one assertion, then restore).
- `bash scripts/check.sh` passes, with no new file over 300 lines in `docs/CODEMAP.md`.
- `@Preview` renders the card in Android Studio / the preview test without a real repository.

## Verify
```bash
cd kmp && ./gradlew -q :android:testDebugUnitTest --tests '*Character*' --tests '*PlayerUi*' --tests '*PlayerViewModel*' 2>&1 | tail -10
cd .. && bash scripts/check.sh 2>&1 | tail -8
```
Paste both tails (≤ 15 lines) into the report.

## Out of scope
Stroke animation, progress/SRS, Pleco detection, "Chọn văn bản…", pipeline changes, commits.
