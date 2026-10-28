# WF1 — Track switch keeps the mapped position; replay works in gaps
Suggested agent: codex/gpt-5.6-sol (medium) · Session: new · Size: S/M · Device check: user
Wave: WF · Owns: `web/src/player-context.tsx`, `web/src/WF1.test.tsx`, `web/scripts/track-check.mjs` · Depends on: none · Checkpoint after: yes

## Goal

Switching lesson 1 between "Có hướng dẫn" (`guided`) and "Chỉ tiếng Trung" (`guided_zh`) continues at the same
sentence instead of restarting at 0:00, and "Nghe lại câu" / `R` in a silence replays the line just heard.

## Read only

- `docs/reviews/web_review_linux.md` (P1 track switch, P2 replay in gap) — repro steps and expected numbers.
- `web/src/player-context.tsx` (`changeTrack`, `replay`, `seek`, how `track`/`src`/session are rebuilt).
- `web/src/Transcript.tsx` only for how it calls `cuePlaceJson` (gap-aware "vừa đọc" line) — mirror it, don't edit.
- `web/scripts/follow-check.mjs` — copy its build/preview/Chrome launch pattern for the new script.
- `web/src/M5.test.tsx` — existing track/mode test setup to copy.

## Do

1. Write the tests first (`web/src/WF1.test.tsx`, jsdom): after `changeTrack` the new audio source, once it
   fires `loadedmetadata` (dispatch on the real `<audio>` element the provider renders), gets `currentTime` =
   `TrackSwitchJs.positionFor(old, new, ms) / 1000`; a `modes_v1` lesson (lesson 2 real fixture as in M5) still
   resets to 0; `replay` in a gap seeks to the start of the last-started cue.
2. Fix `changeTrack`: compute the mapped position before the switch, then apply it to the **new** media element
   after it has loaded (pending-seek applied on `loadedmetadata`/`canplay` of the new `src`, cleared after use and
   on a further track/lesson change). Keep play/pause state and `playbackRate` as they were.
3. Fix `replay`: use the same gap-aware lookup the transcript uses (last-started Chinese cue); inside a cue keep
   today's behaviour (seek to that cue's start).
4. Add `web/scripts/track-check.mjs` (production preview, `channel: "chrome"`, headless, real catalog): lesson 1,
   for start positions 500 s and 233.485 s switch guided→guided_zh and back; expected = `positionFor` computed in
   the page; print `track <from>→<to> start=<s> expected=<s> actual=<s>` per switch; then `R` at 80.3 s and 79.2 s.
   Exit 1 if any `|actual − expected| > 0.3 s`. Each run ≤ 60 s; kill preview on exit.

## Acceptance

- Lesson 1 at 500.000 s → `guided_zh`: `currentTime` ≈ **422.006 s** (±0.3), not 0.
- Lesson 1 switch back and the 233.485 s case also land on their mapped values (±0.3).
- Lesson 2 mode switch still resets to 0 (M5 tests stay green).
- `R` at 80.3 s (gap after `w_waimai`, cue 79.224–80.020) → **79.224 s**; at 80.0 s (inside the cue) → 79.224 s.
- Speed 0.8 survives the track switch; playing stays playing.

## Verify

```sh
npm --prefix web run test -- --run WF1 M5 2>&1 | tail -5
npm --prefix web run build >/dev/null && node web/scripts/track-check.mjs
scripts/check.sh
```

## Out of scope

Transcript rendering/follow (WF3), PiP and slider (WF2), Android, facade (`kmp/`) changes.
