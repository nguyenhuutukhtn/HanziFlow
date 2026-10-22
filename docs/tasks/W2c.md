# W2c — Player screen in v0 style + UX2 (place in silences) + UX3 (free scroll)
Status: todo · Suggested agent: codex/gpt-5.6-terra (medium), ask first · Session: new · Size: L · Device check: user
Wave: 2 · Owns: `web/src/screens/Player*`, `web/src/components/` (Transcript, PlayerBar, CharacterCard and new player parts), their CSS and tests · Depends on: W2a, W2b2 · Checkpoint after: no (W2d continues this window)

## Goal
The Player looks like v0's player and fixes two problems the user hit (2026-10-22): (UX2) in the silence after a
sentence nothing is marked, so the learner loses their place and can't pause to guess the next line; (UX3) the
transcript yanks back down on every new line even after the user scrolled up to read.

## Read only
- `docs/tasks/WAVE-2.md` "Decisions"
- `docs/ui/v0_export/app/page.tsx`: `Transcript`, `Player`, `CharacterCard`; `globals.css` classes `.player-`, `.track-switch`,
  `.transcript`, `.coach-line`, `.active-line`, `.past-line`, `.segmented`, `.vi-toggle`, `.model-answer`, `.seek`, `.character-card`
  (`grep -n`, not the whole file)
- W2b's `web/src` files you touch; facade: `cuePlaceJson` (W2a), `lineJson`, `sectionsJson` (`{id,title,start_ms,end_ms}`), `characterJson`

## Do
0. **Structure first (no new libraries — no Redux/TanStack/Zustand):** domain logic stays in the shared facade; the web
   is a view. Extract (a) `usePlayer()` — one owner of the `<audio>` element, position, playing, speed, track, view mode,
   follow-mode and the commands (toggle, seek, prev/next sentence, replay), exposed via a React context so the main
   Player, the PiP window (W2d), media keys and keyboard all read the same state; (b) `src/content.ts` — the only
   module that builds URLs and fetches catalog/timeline/reference/characters (the future R2 move and offline cache
   change only this file). Components get state from the context, not long prop chains.
   Controls use the shadcn components from W2b2 per the rule in `docs/ui/design_system.md` (no native select/checkbox).
   W2b2 shipped only `button`, `toggle-group`, `select`, `popover` (+ `portal-container`). Add the rest the Player needs as
   standard shadcn files in `web/src/components/ui/` (Radix deps are already installed): `switch` (Tiếng Việt),
   `slider` (seek), `tooltip` (section markers), `drawer` (card bottom sheet; add `vaul`, pinned), `dropdown-menu` only if
   used. Every Radix `Portal` uses `PortalContainerContext`. Add one rule line per new component to `design_system.md`
   "Components" (when to use it), and reuse `--text-body/secondary/caption` for all Vietnamese text.
1. **Layout** per v0: back + lesson title; track switch as a segmented control (labels from `tracksJson` `display_name`);
   transcript card; fixed bottom player bar. Desktop may keep v0's right column only for a keyboard-shortcut card (no fake stats).
2. **Lines**: zh lines as tokens with pinyin above (existing render decisions, `--hanzi-size`); `vi-VN` lines styled as v0's
   coach line (smaller, muted, accent left border); model answer placeholder vs revealed in v0's two looks (reveal stays
   position-based). View modes as a 4-option segmented control incl. "Ẩn" (`HIDDEN` → v0's soft bar per line); "Tiếng Việt" switch.
3. **Player bar**: seek bar with section markers (`sectionsJson`, tooltip = title, click = seek to section start), time labels,
   previous sentence / play-pause / next sentence, "Nghe lại câu", speed (1 / 0.8 / 0.7× — drop 0.6/0.5, they distort on web), "Cửa sổ nổi" button.
   Keyboard: Space play/pause, ←/→ previous/next **sentence**, Shift+←/→ ∓10 s, R replay; ignored in inputs/selects.
4. **UX2** from `cuePlaceJson(position)`: `speaking` line = active style (v0 `.active-line`); when `speaking` is -1, the `last`
   line keeps a "vừa đọc" style (accent marker, no fill, label "vừa đọc" for screen readers) and the `next` line shows a small
   "Tiếp theo" hint; lines before `last` are faded. Grouped cues mark all their lines as today.
5. **UX3**: auto-follow is on by default. A user scroll (wheel, touch, scrollbar, keyboard — anything we didn't start) turns it
   off; our own `scrollIntoView` must not (guard with a flag cleared on `scrollend`/timeout). While off and the current line is
   outside the viewport (IntersectionObserver), show a floating pill "Về câu đang phát" with ↑/↓ by direction; click → scroll to
   it, follow on. Tapping a line to seek also turns follow on.
6. **Character card** per v0: popover next to the tapped token on desktop, bottom sheet at ≤ 720px; char, pinyin, Hán-Việt,
   Vietnamese meaning, HSK chip (hidden when null), decomposition, components (tap → that char), lesson words, "Sao chép".
   No "Xem nét" on web yet.

## Acceptance (vitest, with a fake session) — write these tests first, before styling details
- Gap position → `last` line has the "vừa đọc" marker and `next` has "Tiếp theo"; speaking position → only the active style.
- A user `wheel`/`scroll` event → follow off and the pill appears; clicking it calls `scrollIntoView` and hides the pill;
  a programmatic scroll does not turn follow off.
- `HIDDEN` renders soft bars and no Hanzi; a `vi-VN` line has the coach class; ←/→ seek to previous/next cue start.
- Section marker click seeks to `start_ms`. Card renders as sheet at 390px and popover on desktop.

## Verify
```bash
npm --prefix web run typecheck && npm --prefix web test -- --run && npm --prefix web run build; echo "exit=$?"
bash scripts/check.sh
```
Paste the vitest summary and check.sh lines.

## Out of scope
PiP window (W2d), loop-sentence toggle, stroke animation on web, shared/Kotlin changes (ask if `cuePlaceJson` is missing something).
