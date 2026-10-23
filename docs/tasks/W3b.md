# W3b — Web follow: keep the active line centered (main window + PiP)
Status: todo · Suggested agent: codex/gpt-5.6-sol (medium) · Session: new · Size: S–M · Device check: user
Wave: 3 (follow-up, runs before Wave 5) · Owns: `web/src/Transcript.tsx`, `web/src/styles.css` (transcript/scroll padding only), `web/src/pip/PipPlayer.tsx` (scroll container only), `web/scripts/smoke.mjs` (or a new `web/scripts/follow-check.mjs`), a new `web/src/W3b.test.tsx` · Depends on: none · Checkpoint after: yes (PM, then user on web)

## Goal
User report (2026-10-23): while playing, the highlighted line sits at the **bottom** of the screen, in both the main window and PiP. To read the next sentence's Hanzi the user has to pause and scroll by hand. Wanted: with auto-follow on, the active line stays at the vertical center of the visible transcript area, so the next lines are always readable below it.

## Read only
- `web/src/Transcript.tsx:33-105` (follow state, `returnToActive` uses `scrollIntoView({block: "center", behavior: "smooth"})`, the `programmed` flag + 1 s timer, `stopFollowing` on capture-phase scroll/wheel/touchmove, `scrollend`)
- `web/src/styles.css` around `.transcript-wrap` (:393), the fixed player bar (:455), the PiP scroller (`overflow: hidden auto`, :580) and `.compact-transcript`
- `web/scripts/smoke.mjs` (real-browser pattern: Playwright, real catalog lesson)

## Diagnose first (write the finding in the report, 1–2 lines)
The code already asks for `center`, so find out in a **real browser** (Playwright, not jsdom) why the line ends up at the bottom. Likely suspects, check each:
1. Follow silently turns off: a smooth scroll still running when the next cue starts, or `scrollend`/the 1 s timer clearing `programmed` mid-scroll, so the scroll's own events hit `stopFollowing`.
2. `center` is measured against the whole viewport, while the fixed bottom player bar (and in PiP the pinned controls) cover the lower part, so "center" lands visually low; or the scroll container has no bottom padding, so lines near the end cannot be centered.
3. In PiP, `scrollIntoView` scrolls the wrong element or the window instead of the PiP scroller.

## Do
1. Fix the cause. The target is the center of the **visible** transcript area (viewport minus fixed bar / pinned controls). Use `scroll-padding` / explicit `scrollTo` on the real scroll container, whichever is simpler, and enough bottom padding that even the last line can be centered.
2. Programmatic scrolls must never turn follow off; only user input (wheel, touch, keyboard scroll, scrollbar drag) does. The existing return pill and opt-out behaviour stay as they are.
3. Real-browser check script: open the real lesson 1 in Playwright (desktop 1280×800 and mobile 390×844), play/seek across ≥ 5 consecutive cues, and after each cue change + settle assert the active line's center is within ±10 % of the visible area's center and follow is still on. Same check inside the PiP document if Playwright can open it; otherwise test `PipPlayer`'s scroller in jsdom by asserting the scroll call targets the PiP scroller with the right offset, and say so in the report.
4. Add the script to `check.sh` (next to smoke) so a regression fails loudly.

## Acceptance
- Report names the real cause (one of the suspects or another).
- Real-browser script prints one line per cue, e.g. `cue 3 offset +2% follow=on`, and exits non-zero if any offset exceeds ±10 % or follow turns off. Passes at both sizes.
- jsdom test (`W3b.test.tsx`): a programmatic follow scroll followed by its own `scroll` events leaves follow on; a `wheel` event turns it off (existing behaviour).
- Existing web tests stay green, none edited except imports.

## Verify
```bash
source .venv/bin/activate
cd web && npx vitest run && node scripts/follow-check.mjs && cd .. && scripts/check.sh
```

## Out of scope
Android, transcript visuals other than scroll position/padding, new settings (e.g. choosing the follow position).

Device check (user, `just web`): play lesson 1 in the main window and in PiP; the highlighted line stays mid-screen and the next sentence is readable without scrolling; scroll by hand → follow stops and the pill appears; tap the pill → back to center.

Then: update STATUS.md + a token_log.md row, report with the AGENTS.md template, do not commit.
