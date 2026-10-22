# W2d-fix — finish W2d: PiP acceptance tests + browser smoke step
Status: todo · Suggested agent: codex/gpt-5.6-terra (medium), fresh window · Session: new · Size: S · Device check: user
Wave: 2 · Owns: `web/src/pip/`, `web/src/Transcript.tsx` (fallback only), `scripts/web_build.sh`, `scripts/web_facade.sh` (new), `web/src/*.test.tsx` (new PiP test file), `web/scripts/smoke.mjs` (new), `web/package.json`, `scripts/check.sh` web step · Depends on: W2d (partial) · Checkpoint after: ✅ end of wave 2

## State left by W2d (partial, 2026-10-22)
- Done: `web/src/pip/PipPlayer.tsx` (PiP on shared `usePlayer()` state), wiring, styles; section-marker test in `App.test.tsx`;
  `@playwright/test` in devDependencies (range `^1.54.2`, not pinned; no browser downloaded).
- PM fix already in: `components/ui/tooltip.tsx` wraps its own `TooltipProvider` (the real Player crashed without it).
- Missing: W2d Acceptance tests (none written) and the smoke step.

## Read only
`docs/tasks/W2d.md` (Goal + Acceptance), `web/src/pip/PipPlayer.tsx`, `web/src/player-context.tsx`, `web/src/W2c.test.tsx` (fake session pattern to copy), `scripts/check.sh` web step.

## Do
1. **PiP tests** (new `web/src/W2d.test.tsx`, copy the W2c fake session): render PipPlayer into a separate document
   (`document.implementation.createHTMLDocument()` as the PiP window's document), then assert W2d Acceptance:
   four controls (play/pause, previous, next, replay) not inside the scroll container; paused shows "Nghe tiếp";
   gap position shows "vừa đọc" + "Tiếp theo"; changing the theme class on the main `<html>` updates the PiP `<html>`.
   Events go to the PiP document's elements, not the main document.
2. **Smoke step** `web/scripts/smoke.mjs`: use `playwright-core` API with `channel: "chrome"` (the system Chrome — no browser
   download; if Chrome is missing print `skip smoke: no chrome` and exit 0). Start `vite preview` on a free port after
   `build`, open the Player route of one lesson that exists in the built/test catalog (ask via agent_ask if no lesson is
   reachable offline), fail on any `pageerror` or `console.error`, click one section marker, then print exactly
   `ok smoke` or `FAIL smoke: <first error>` (exit 1). No screenshots. Call it from the `check.sh` web step.
3. Pin Playwright to an exact version (no `^`).
4. **Stale facade (PM, 2026-10-22):** `web/public/shared/` was built before W2a, so the real page had no `cuePlaceJson`
   and UX2 silently did nothing (tests use a fake session). (a) `Transcript.tsx`: drop the silent `cuePlaceJson?.() ?? {…}`
   fallback; if the facade lacks a function the web uses, `console.error` once (smoke then fails). (b) `smoke.mjs` first runs
   the gradle `jsBrowserProductionLibraryDistribution` + copy step from `scripts/web_build.sh` (extract it into
   `scripts/web_facade.sh` and call it from both), so smoke always tests the current facade.

## Acceptance
- `npm test` count = 17 + the new PiP tests, all pass.
- Temporarily revert the tooltip Provider fix → `node web/scripts/smoke.mjs` prints `FAIL smoke: …Tooltip…`; restore → `ok smoke`. Say in the report that you did this.

## Verify
```bash
npm --prefix web run typecheck && npm --prefix web test -- --run && npm --prefix web run build; echo "exit=$?"
node web/scripts/smoke.mjs; bash scripts/check.sh
```

## Out of scope
New PiP features or styling, Android, global hotkeys.
