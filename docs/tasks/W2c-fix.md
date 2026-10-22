# W2c-fix — follow-mode works in a real browser; pill and markers in silences
Status: todo · Suggested agent: continue W2c window (codex/gpt-5.6-terra, medium) · Session: W2c · Size: S · Device check: user
Wave: 2 · Owns: `web/src/Transcript.tsx`, `web/src/screens/Player.tsx`, `web/src/player-context.tsx`, `web/src/styles.css`, `web/src/W2c.test.tsx` · Depends on: W2c · Checkpoint after: no (W2d follows in the same window)

## Findings (PM review of W2c, 2026-10-22)
1. **Scroll listener is on an element that never scrolls.** `.transcript` (`<ol>`) has no `overflow`; the page (window)
   scrolls. So `onScroll={stopFollowing}` never fires in Chrome: scrollbar drag, PageDown/Space-scroll, Home/End don't
   turn follow off (only wheel/touchmove do). The jsdom test passes because `fireEvent` works on any element.
2. **Programmatic-scroll guard is a fixed 350 ms.** A smooth `scrollIntoView` over a long distance lasts longer; the
   tail scroll events then turn follow off by themselves. Brief asked for `scrollend` (with a timeout fallback).
3. **Silences lose the anchor.** `activeRef` and the pill use `activeId` = speaking line only. In a gap
   (`speaking == -1`) the pill disappears and "Về câu đang phát" has no target — exactly UX2's moment.
4. **Pill arrow is always ↑**; brief: ↑/↓ by where the line is.
5. **Keyboard handling is split** between `Transcript.tsx` (←/→) and `Player.tsx` (Space, Shift+←/→, R). Cmd/Ctrl+R
   (reload) also triggers replay; arrows on the focused seek slider (`role=slider`) seek twice.

## Do
1. Listen for user scroll on the real scroller: `window` (`scroll`, `wheel`, `touchmove`, `keydown` for
   PageUp/PageDown/Home/End/Space-scroll keys are covered by `scroll`). Keep one `stopFollowing` that ignores scrolls while the
   programmatic flag is set.
2. Clear the programmatic flag on `scrollend` (window), fallback timeout 1000 ms if `scrollend` is unsupported.
3. Follow/pill anchor = `speaking` line, else `last` line (cuePlace). Ref + IntersectionObserver on that anchor.
4. Pill text "Về câu đang phát ↑" or "↓": compare anchor `getBoundingClientRect().top` with the viewport.
5. One keyboard handler (in `Player.tsx` or `usePlayer`) for all shortcuts; skip when any of `metaKey/ctrlKey/altKey`
   is held (Shift is ours), and when the target matches `input,textarea,select,[role=combobox],[role=slider],[contenteditable]`.

## Acceptance (vitest — write first)
- `window.dispatchEvent(new Event("scroll"))` with no programmatic scroll → follow off, pill visible.
- Programmatic scroll: call return-to-current, dispatch window `scroll` twice, then `scrollend` → follow still on.
- Gap position (speaking −1, last = k) with follow off → pill visible; click → `scrollIntoView` called on line k.
- `keydown` `r` with `metaKey: true` → no replay; `ArrowRight` with target `[role=slider]` → no sentence seek.

## Verify
```bash
npm --prefix web run typecheck && npm --prefix web test -- --run && npm --prefix web run build; echo "exit=$?"
bash scripts/check.sh
```
Paste the vitest summary and check.sh lines.

## Out of scope
PiP (W2d), any visual redesign, shared/Kotlin changes.
