# W3a — floating window (PiP) as a learning view: whole transcript, pinyin toggle, tap to peek
Status: todo · Suggested agent: codex/gpt-5.6-terra (medium), fresh window · Session: new · Size: M · Device check: user
Wave: 3 · Owns: `web/src/pip/`, `web/src/Transcript.tsx`, `web/src/styles.css` (PiP + peek rules only), `web/src/W3a.test.tsx` (new), `web/src/W2d.test.tsx` (update to the new layout) · Depends on: W2d-fix · Checkpoint after: ✅ user looks at PiP on device

## Why (user, 2026-10-22, after the Wave 2 device check)
PiP works but shows one raw-text sentence at a time with pinyin always on. The learner uses it at work to read along and
to learn characters: pause, guess the next line from the characters, peek pinyin only for the word they don't remember.

## Decisions (user + PM, 2026-10-22)
- PiP shows the **whole lesson transcript**, reusing `Transcript` (same follow mode, "Về câu đang phát", just-read / "Tiếp theo"
  states from W2c). Not a separate one-line renderer: delete the hand-built `.float-text` line in `PipPlayer.tsx`.
- **No Vietnamese in PiP**, whatever `player.vietnamese` is.
- **Pinyin toggle** button `拼` in the PiP control bar (`aria-pressed`). It sets the shared `player.view`:
  on → `HANZI_WITH_PINYIN`, off → `HANZI_ONLY` (from `HIDDEN`/`PINYIN_ONLY`, pressing it goes to `HANZI_WITH_PINYIN`). Shared state on purpose:
  the main Player follows it.
- In PiP, pinyin renders **above each word** (`<ruby>` with `token.pinyin`), not as a line below — maps sound to character.
  Add a `compact` prop to `Transcript` for this (ruby pinyin, no translation, tighter spacing); main Player keeps its current layout.
- **Tap to peek** (both PiP and main Player, it is the same component): when pinyin is off, the first tap on a word shows that
  word's pinyin above it and does **not** open the card; a second tap on the peeked word opens the character card. When pinyin is
  on, a tap opens the card (today's behaviour). Peeks clear when the anchor line (speaking, else last) changes or pinyin is turned on.
- Character card opens **inside the PiP document** (use the existing `PortalContainerProvider`).
- Controls stay pinned outside the scroller: previous, play/pause, next, replay, `拼`. Use the design-system `Button` / icons
  (`web/src/components/ui/`, `docs/ui/design_system.md`), no raw `‹ ›` text buttons. Keep the theme sync and PiP-local keyboard handler.

## Read only
`docs/ui/design_system.md`, `web/src/pip/PipPlayer.tsx`, `web/src/Transcript.tsx`, `web/src/facade.ts` (token type),
`web/src/W2d.test.tsx` (PiP-document test pattern to copy), how `screens/Player.tsx` wires `onCharacter` → card.

## Do
1. `Transcript`: `compact` prop + peek state as above. Keep the file ≤ 300 lines; extract a `TranscriptLine`/token component if needed.
2. `PipPlayer`: header (play state + lesson title), `<Transcript compact …>` in the scroll area, control bar with `拼`, card in PiP.
3. Styles in `styles.css` using tokens only; Hanzi in the CJK font; ruby pinyin ≥ 12px; muted text ≥ 4.5:1 in all 3 themes.
4. Tests (below), update `W2d.test.tsx` where the old one-line layout is asserted.

## Acceptance (each bullet = one named test in `W3a.test.tsx`, rendered into a separate PiP document like W2d)
- PiP lists every cue line of the fake lesson (count = the fake session's cue count) inside the scroll container; the 5 controls are outside it.
- Gap position: one line is marked just-read and the next one shows "Tiếp theo" (same markup as the main Player).
- `player.vietnamese = true` → no translation text in the PiP document.
- `拼` toggles `aria-pressed` and `player.view` `HANZI_WITH_PINYIN` ↔ `HANZI_ONLY`; with pinyin on, every token with pinyin has an `<rt>`.
- Pinyin off: click a token → its `<rt>` with that token's pinyin appears, no other `<rt>`, card not opened; click it again → card opens in the PiP document.
- Pinyin on: one click on a token opens the card.
- Peek clears after the position moves to the next line (rerender with a later `positionMs`).
- Main Player (non-compact) still renders pinyin as the line below, unchanged (existing tests stay green).
- `npm test` = 19 + new tests, all pass; `ok smoke`.

## Verify
```bash
npm --prefix web run typecheck && npm --prefix web test -- --run && npm --prefix web run build; echo "exit=$?"
node web/scripts/smoke.mjs; bash scripts/check.sh
```

## Out of scope
Pinyin per word in the main Player's non-compact layout, PiP window resize/size presets, Vietnamese toggle in PiP, Android, global hotkeys.
