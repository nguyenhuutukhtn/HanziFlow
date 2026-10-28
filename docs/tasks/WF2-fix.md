# WF2-fix — hint dismissal survives settings writes; Acceptance tests on the real wiring
Suggested agent: codex/gpt-5.6-sol (medium) · Session: new (escalated from luna) · Size: S · Device check: user
Wave: WF · Owns: same as `docs/tasks/WF2.md` · Depends on: WF2 (uncommitted in the working tree) · Checkpoint after: yes

## Context

WF2 code is in the working tree (`git diff`). PM already fixed `web/scripts/pip-check.mjs` (the API is
`window.documentPictureInPicture.window`); it now passes headed: `overflow-y auto`, `493 < 720`, `Be Vietnam Pro`,
`follow ok`. Don't touch that script.

## Defects found in PM review

1. **Dismissal is lost.** `App.tsx:32-41` keeps settings in React state, loaded once, and its effect writes the whole
   object on every change. PiP's "Đã hiểu" writes `pipAlwaysOnTopHintDismissed: true` straight to storage
   (`PipPlayer.tsx:110`), so the next theme/view/size change in the app (or PiP's `拼` toggle, if it goes through
   App's settings) writes `false` back and the panel auto-opens again. Fix: keep the flag out of the App-owned
   object — its own storage key read/written by a small helper in `settings.ts` — or route it through App's
   `setSettings`. Pick one, no both.
2. **Tests don't cover the Acceptance.** `WF2.test.tsx` renders a bare `<Slider>` with hand-passed props and tests
   helpers, not the wiring. Replace with tests that render the real components (follow the isolated PiP document
   pattern in `W2d.test.tsx` / `W3a.test.tsx`, and render the real `PlayerScreen` for the slider):
   - Slider in the Player: ArrowRight from 100 s → seek to 105 s; PageUp → +30 s; PageDown → −30 s (clamped at 0);
     `aria-valuetext` = `1:40 / 43:54` for position 100000 ms / duration 2634181 ms.
   - `openPip` stylesheet copy: a sheet with readable `cssRules` arrives in the PiP head as `<style>` text; a sheet
     whose `cssRules` throws arrives as `<link rel="stylesheet">` with an absolute `href`.
   - PiP with a Linux UA: pin button `Giữ cửa sổ trên cùng`, panel open on first render with the exact two lines
     from WF2.md; "Đã hiểu" closes it; a remount does not auto-open it; the pin button opens it again.
   - Regression for defect 1: dismiss, then change the theme through the app's settings path, remount PiP →
     panel stays closed.
   - macOS and Android UAs: no pin button, no panel.
   - Restore every `Navigator.prototype` property you override (the current test leaks `userAgent`).
3. `W2d.test.tsx` / `W3a.test.tsx` were loosened to `>= 5` controls. Make them exact again: 5 on a non-Linux UA
   (set it explicitly in those tests), and assert 6 with the pin button in the WF2 Linux test.

## Verify

```sh
npm --prefix web run test -- --run WF2 W2d W3a 2>&1 | tail -5   # all pass, WF2 ≥ 6 tests
npm --prefix web run test -- --run 2>&1 | tail -3               # full web suite green
npm --prefix web run build >/dev/null && echo build ok
```

PM runs `node web/scripts/pip-check.mjs` (headed) and `scripts/check.sh`.

## Out of scope

Anything not listed above; `pip-check.mjs`; styling of the panel.
