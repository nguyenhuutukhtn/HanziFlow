# W2d — floating window (PiP) in v0 style + UX1 (controls always visible)
Status: todo · Suggested agent: continue W2c window (terra · medium) · Session: <W2c session> · Size: M · Device check: user
Wave: 2 · Owns: `web/src/` PiP files (new `web/src/pip/` + the PiP open code in the Player), their CSS and tests · Depends on: W2c · Checkpoint after: yes (end of wave)

## Goal
The user's floating window only shows scrolling text; they cannot pause, go back or continue from it (user, 2026-10-22).
Make the PiP window a compact player in v0's `.floating-player` style whose controls are always visible.

## Read only
- `docs/ui/v0_export/app/page.tsx` `FloatingPlayer`; `globals.css` `.floating-player`, `.float-*` (`grep -n`)
- The PiP code W2c left in the Player, and W2c's place/marker components

## Do
1. Open the window at 360×200 (`requestWindow`); layout fills any size: top status row ("ĐANG PHÁT" / "TẠM DỪNG"),
   the current line large (tokens + pinyin per view mode, `--hanzi-size` scaled to fit), its Vietnamese line below when on,
   and a **controls row pinned to the bottom, never scrolled away**: previous sentence, play/pause, next sentence, "Nghe lại câu".
   Paused → the play button reads "Nghe tiếp".
2. UX2 inside PiP: in a silence show the `last` line with the "vừa đọc" marker and a one-line "Tiếp theo" preview of `next`.
3. Same keyboard shortcuts as the Player, handled in the PiP document.
4. Popovers/drawers inside PiP use W2b2's `PortalContainerContext` set to the PiP document body.
5. The PiP document gets the current theme class and font size, and follows changes while open.
6. Tapping a character in PiP opens the card inside PiP as a compact sheet (existing behaviour, new look).

## Acceptance (vitest) — write these tests first, before styling details
- Rendered PiP body contains the four controls outside any scroll container; paused state shows "Nghe tiếp".
- Gap position shows "vừa đọc" + "Tiếp theo"; theme class change in the main document is mirrored into the PiP document.

## Verify
```bash
npm --prefix web run typecheck && npm --prefix web test -- --run && npm --prefix web run build; echo "exit=$?"
bash scripts/check.sh
```

## Out of scope
Global hotkeys, Tauri wrapper, Android.

Device check (user), end of wave: open "Cửa sổ nổi", switch to another app, pause / next / previous / replay inside the
window, check the "vừa đọc"/"Tiếp theo" marks in a pause, switch theme in Settings with the window open.
