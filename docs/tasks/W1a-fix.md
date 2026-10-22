# W1a-fix — facade gaps W1b needs (brief omission, not a worker error)
Status: todo · Suggested agent: continue W1a window (codex/gpt-5.6-luna, low) · Session: W1a's · Size: S · Device check: none
Wave: 1 · Owns: `kmp/shared/src/commonMain/kotlin/com/hanziflow/shared/web/PlayerJs.kt`, `kmp/shared/src/commonTest/kotlin/com/hanziflow/shared/web/PlayerJsTest.kt` · Depends on: W1a · Checkpoint after: no (PM re-runs Verify, then W1b)

## Why
PM review of W1a: Verify passes, but the web transcript cannot be rendered from the facade alone.
Android (`android/.../ui/player/TranscriptPanel.kt:107-125, 217-225`) iterates the reference lines
of the **current track** and renders `line.text`, `line.pinyin`, `line.language`, `line.speaker`.
`lineJson` returns none of these, there is no ordered line list, and `nextLine` is taken from
guided+practice flattened (wrong at the guided→practice boundary).

## Do
1. `linesJson(track)`: ordered line ids of the reference track the session's timeline belongs to
   (`guided` and `guided_zh` → `reference.tracks.guided`; `practice` → `reference.tracks.practice`,
   `[]` when absent). Add a `track: String` constructor parameter (default `"guided"`).
2. `lineJson`: compute `nextLine` from that same track list, not the flattened one. Add fields
   `id`, `type`, `language`, `speaker`, `text`, `pinyin` (line-level, nullable) to the payload.
3. `@JsExport object TrackSwitchJs { fun positionFor(fromTimelineJson, toTimelineJson, positionMs: Double): Double }`
   wrapping `TrackSwitch.positionFor`.
No change to existing non-web classes.

## Acceptance (add to PlayerJsTest, concrete values read from the fixtures and hardcoded)
- `linesJson("guided")` size = `reference.tracks.guided.size` from the fixture, first id = the fixture's first guided id.
- `lineJson("scene_bag_vi", "HANZI_WITH_PINYIN", true, 0.0)` has `language` starting with `vi` and `text` equal to the fixture text.
- For the **last** guided line, `lineJson` does not use the first practice line as `nextLine` (assert via a line whose translation would otherwise be suppressed, or simply that a session with `track="practice"` gives `linesJson` = practice ids when the fixture has practice; if the fixture has no practice track, assert `[]`).
- `TrackSwitchJs.positionFor(timeline_guided, timeline_guided, 21500.0)` = 21500.0 (identity on same timeline), and on JS too.

## Verify
```bash
cd kmp && ./gradlew -q :shared:allTests; echo "exit=$?"
grep -o 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' shared/build/test-results/*/TEST-*web*.xml
grep -c "TrackSwitchJs\|linesJson" shared/build/dist/js/productionLibrary/HanziFlow-shared.d.ts
cd .. && bash scripts/check.sh
```
jvm and js `TEST-*web*.xml` must show failures="0" with tests ≥ 3.

## Out of scope
Web/React code, Android, existing shared classes.
