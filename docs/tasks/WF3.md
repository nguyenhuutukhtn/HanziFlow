# WF3 — Pinyin in "Hán tự + pinyin"; auto-follow survives long scrolls
Suggested agent: codex/gpt-5.6-sol (medium) · Session: new · Size: M · Device check: user
Wave: WF · Owns: `web/src/Transcript.tsx`, `web/src/styles.css`, `web/src/WF3.test.tsx`, `web/scripts/follow-check.mjs` (+ `docs/STATUS.md`, `docs/token_log.md`) · Depends on: WF2 merged · Checkpoint after: yes (wave end)

## Goal

The main transcript shows pinyin above each word in "Hán tự + pinyin" (desktop and mobile), and auto-follow no
longer switches itself off after jumping far (section markers, "Về câu đang phát").

## Read only

- `docs/reviews/web_review_linux.md` (P2 pinyin, P2 follow).
- `web/src/Transcript.tsx` (token rendering ~line 215–258; `returnToActive`/`programmed` flag ~line 85–140).
- Consumer to mirror for pinyin: `kmp/android/src/main/kotlin/com/hanziflow/ui/player/TranscriptPanel.kt:168-263`
  (per-token `showPinyin` from the facade decides ruby) and `kmp/shared/.../transcript/TranscriptRendering.kt`
  (view mode → `showPinyinLine` / token `showPinyin`).
- `web/src/W3a.test.tsx`, `web/src/W3b.test.tsx`, `web/scripts/follow-check.mjs`.

## Do

1. Tests first (`web/src/WF3.test.tsx`) using the real lesson-1 fixture the W3 tests use.
2. Pinyin: in the main transcript render `<ruby>…<rt>` for every token whose facade `token.showPinyin` is true
   (HANZI_WITH_PINYIN), regardless of `compact`. Keep PiP behaviour from W3a (its own `拼` toggle, peek when off)
   and HANZI_ONLY peek unchanged. Style ruby in `styles.css` so line height stays readable (no overlap with the
   previous line); no Vietnamese text change.
3. Follow: a programmatic scroll ends on `scrollend`, or when no `scroll` event arrived for 250 ms (debounced,
   restarted by every scroll event), or when the target is out of range; never on a fixed 1.5 s timer while scroll
   events are still arriving. User input (wheel/touch/keys/scrollbar) still opts out immediately. Apply the same to
   the PiP transcript container.
4. Extend `web/scripts/follow-check.mjs`: with CDP `Emulation.setCPUThrottlingRate(4)`, click section marker
   "Nghe lại" (and "Kể lại"); print `data-follow` at 1/2/3/5 s (all `on`), then play 20 s and assert the active
   line stays inside the visible transcript area. Exit 1 on mismatch. Keep its existing checks.

## Acceptance

- Lesson 1, default view: `.transcript rt` count > 0 (WR1 measured 0); a line like `我们` shows its pinyin; same at
  390×844. PINYIN_ONLY and HANZI_ONLY unchanged (W3a/W3b green).
- Throttled 4×, marker "Nghe lại": `data-follow=on` at 1/2/3/5 s, active line visible after 20 s of play
  (WR1: `off` from ~2 s, line at 934 px in an 800 px viewport).
- Wheel scroll still sets `data-follow=off` and shows the return pill; the pill restores follow.

## Verify

```sh
npm --prefix web run test -- --run WF3 W3a W3b 2>&1 | tail -5
npm --prefix web run build >/dev/null && node web/scripts/follow-check.mjs
scripts/check.sh
```

## Out of scope

Facade/`kmp/` changes, PiP styling (WF2), track switching (WF1), Android.

## Device check (user, Zorin Chrome)

1. Lesson 1 "Hán tự + pinyin": pinyin above the characters, lines don't overlap.
2. Click "Nghe lại" on the progress bar, play: the transcript keeps following without touching it.
3. Scroll up with the mouse: follow stops, "Về câu đang phát" brings it back.
