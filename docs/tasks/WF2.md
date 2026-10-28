# WF2 — PiP styled in production, Linux "always on top" hint, usable seek slider
Suggested agent: codex/gpt-5.6-luna (medium) · Session: new · Size: M · Device check: user
Wave: WF · Owns: `web/src/screens/Player.tsx`, `web/src/pip/`, `web/src/components/ui/slider.tsx`, `web/src/settings.ts`, `web/src/styles.css`, `web/src/WF2.test.tsx`, `web/scripts/pip-check.mjs` · Depends on: none · Checkpoint after: yes

## Goal

The floating window (Document PiP) looks and scrolls like in dev also in the deployed build; on Linux it tells
the learner how to keep it on top; the progress slider moves in useful steps from the keyboard.

## Read only

- `docs/reviews/web_review_linux.md` (P2 PiP stylesheet, P3 slider).
- STATUS "Open decisions" → "PiP always-on-top on Linux/Wayland" (why no web API can pin the window).
- `web/src/screens/Player.tsx` (`openPip` ~line 40–55, slider ~line 172), `web/src/pip/PipPlayer.tsx`,
  `web/src/components/ui/slider.tsx`, `web/src/settings.ts`.
- `web/src/W2d.test.tsx`, `web/src/W3a.test.tsx` (isolated PiP document test pattern).
- `web/scripts/smoke.mjs` (preview + Chrome launch pattern).

## Do

1. Tests first (`web/src/WF2.test.tsx`): PiP head receives the app CSS as `<style>` text when `cssRules` are
   readable, an absolute-URL `<link>` otherwise; hint visible on Linux UA and absent on macOS/Windows/Android UAs;
   hint dismissal persists; slider step and `aria-valuetext`.
2. Stylesheets: the cloned `<link href="/assets/…">` resolves against the PiP `about:blank` document and never
   loads. In `openPip`, for each `document.styleSheets` entry copy its `cssRules` text into a `<style>`; if reading
   rules throws (cross-origin, e.g. Google Fonts), append a `<link rel="stylesheet">` with the **absolute** `href`.
   Keep copying `<style>` nodes (dev).
3. Linux hint (`navigator.userAgentData?.platform` or `navigator.platform`/UA contains Linux, excluding Android):
   - a small pin button in the PiP controls, Linux only, `aria-label="Giữ cửa sổ trên cùng"`; tap toggles a short
     panel inside PiP: "Chuột phải lên thanh tiêu đề cửa sổ này → **Always on Top** (Luôn ở trên cùng)." and a
     second line "Tự động mỗi lần: chạy Chrome với `--ozone-platform=x11`.";
   - the panel opens by itself on the first PiP open, with "Đã hiểu" that sets a persisted setting
     (`settings.ts`, same storage as theme) so it doesn't auto-open again; the pin button still shows it.
   - No attempt to pin programmatically (impossible on Wayland).
4. Slider: keyboard step **5 s** (arrow keys), PageUp/PageDown **30 s**; `aria-valuetext` = `m:ss / m:ss`
   (e.g. `1:40 / 43:54`). Pointer seeking unchanged.
5. `web/scripts/pip-check.mjs`: production preview, `channel: "chrome"`, **headed** (`headless: false`; needs a
   desktop session, not part of `check.sh`), real lesson 1, real click on "Cửa sổ nổi". In the PiP document print
   and assert: `.float-text` computed `overflow-y` = `auto`; its `clientHeight` < PiP `innerHeight`; body font is
   not Times; after dispatching a click on line 300, within 2 s `.float-text.scrollTop > 0` and the active line's
   centre is within ±25% of the container centre. Exit 1 on any mismatch; ≤ 60 s; kill preview on exit.

## Acceptance

- `node web/scripts/pip-check.mjs` prints `overflow-y auto`, a `float-text` height smaller than the window, a
  non-Times font, and `follow ok` for line 300 (WR1 measured 15782 px / visible / Times before the fix).
- Linux UA: pin button + first-open panel with the exact Vietnamese text above; "Đã hiểu" persists across reload.
  macOS UA: no pin button.
- Slider: ArrowRight from 100 s → 105 s; PageUp → +30 s; `aria-valuetext` like `1:40 / 43:54`.
- Existing W2d/W3a PiP tests stay green.

## Verify

```sh
npm --prefix web run test -- --run WF2 W2d W3a 2>&1 | tail -5
npm --prefix web run build >/dev/null && node web/scripts/pip-check.mjs
scripts/check.sh
```

## Out of scope

Transcript rendering/follow logic (WF3), track switching (WF1), any native/Linux app, Chrome flags automation.

## Device check (user, Zorin Chrome, after merge + deploy or `just web`)

1. Open lesson 1 → "Cửa sổ nổi": PiP looks like the app (fonts, controls at the bottom, transcript scrolls).
2. First open shows the "Always on Top" panel; follow it (right-click title bar) → PiP stays above other apps.
3. "Đã hiểu", close and reopen PiP: panel doesn't auto-open; the pin button shows it again.
