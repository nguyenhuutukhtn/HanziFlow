# W2a — shared cue "place": speaking / last started / next
Status: todo · Suggested agent: codex/gpt-5.6-luna (low) · Session: new · Size: S · Device check: user
Wave: 2 · Owns: `kmp/shared/src/commonMain/kotlin/com/hanziflow/shared/transcript/TranscriptCues.kt`, `kmp/shared/src/commonMain/kotlin/com/hanziflow/shared/web/PlayerJs.kt`, their tests · Depends on: none · Checkpoint after: yes

## Goal
In the silence after a sentence, `activeCueIndex` returns null/-1, so the web transcript highlights nothing and the
learner loses their place (user, 2026-10-22). Add one pure lookup that says where the learner is even in a gap,
so W2c/W2d can mark "vừa đọc" (last line) and "Tiếp theo" (next line). Additive only: nothing existing changes.

## Read only
- `TranscriptCues.kt` (pattern: `activeCueIndex`, binary search over cues sorted by `start_ms`)
- `PlayerJs.kt` lines 85–135 (`activeCueIndex`, `nextCueStartMs`) and its existing test file
- A real timeline fixture already in `kmp/shared/src/commonTest/resources/` (pick one; don't read the whole file — `grep -o '"start_ms":[0-9]*,"end_ms":[0-9]*' | head`)

## Do
1. `TranscriptCues.placeAt(cues, positionMs: Long): CuePlace` with
   `data class CuePlace(val speaking: Int?, val lastStarted: Int?, val next: Int?)`:
   - `speaking` = same as `activeCueIndex` (start ≤ pos < end);
   - `lastStarted` = greatest index with `startMs <= pos` (equals `speaking` while speaking; null before the first cue);
   - `next` = smallest index with `startMs > pos` (null after the last cue start). Binary search, no linear scan.
2. Facade: `LessonSessionJs.cuePlaceJson(positionMs: Double): String` → `{"speaking":i,"last":i,"next":i}` with `-1` for null.
3. Tests (common, so JVM and JS both run them), cues `[0–1000], [1500–2500], [4000–5000]`:
   | pos | speaking | lastStarted | next |
   |---|---|---|---|
   | 500 | 0 | 0 | 1 |
   | 1000 | null | 0 | 1 |
   | 1200 | null | 0 | 1 |
   | 3000 | null | 1 | 2 |
   | 4000 | 2 | 2 | null |
   | 6000 | null | 2 | null |
   plus cues starting at 100, pos 50 → (null, null, 0); empty list → all null.
   One facade test on the real fixture: a position inside a real gap between two consecutive cues (end of cue k < start of cue k+1)
   → `{"speaking":-1,"last":k,"next":k+1}`; print k and the two times in the test name.

## Acceptance
- All table rows pass on JVM and JS; existing tests unchanged and green.

## Verify
```bash
cd kmp && ./gradlew -q :shared:jvmTest :shared:jsTest; echo "exit=$?"
grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' shared/build/test-results/jvmTest/TEST-*TranscriptCues*.xml shared/build/test-results/jvmTest/TEST-*PlayerJs*.xml
cd .. && bash scripts/check.sh
```
Paste the counts; `failures="0" errors="0"` and the new tests counted.

## Out of scope
Android UI (Wave 3), web UI (W2c/W2d), changing `activeCueIndex`.
