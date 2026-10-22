# W2b — web design system: tokens, 3 themes, shell, Library, Settings, code split
Status: todo · Suggested agent: codex/gpt-5.6-terra (medium), ask first · Session: new · Size: L · Device check: user
Wave: 2 · Owns: `web/` (not `web/public/shared/`), `docs/ui/design_system.md` · Depends on: none · Checkpoint after: yes (user approves the look)

## Goal
The web app looks like the v0 prototype the user approved: warm, calm "education" style, 3 themes (Sáng default,
Giấy, Tối), sidebar on desktop and bottom nav on phone. This task lays the foundation; the Player screen keeps
its current behaviour and gets restyled in W2c. Also split the compressed one-line code into readable files.

## Read only
- `docs/tasks/WAVE-2.md` "Decisions" (real data only, readability floors, speeds, brand)
- `docs/ui/v0_export/app/globals.css` lines 1–60 (tokens + themes) and the classes for shell, library, settings
  (`grep -n` for `.sidebar`, `.lesson-card`, `.setting`, `.theme-`, `.mobile-`)
- `docs/ui/v0_export/app/page.tsx`: `Library`, `SettingsPage`, `Page` (shell). Ignore its mock data and Next files.
- `web/src/App.tsx`, `web/src/Transcript.tsx`, `web/src/styles.css`, existing tests

## Do
1. **Split** `App.tsx` into files without changing behaviour, e.g. `src/app/Shell.tsx`, `src/screens/Library.tsx`,
   `src/screens/Player.tsx` (current Player + Body moved as-is), `src/screens/Settings.tsx`, `src/components/CharacterCard.tsx`,
   `src/settings.ts`. Normal formatting: no line over 200 chars anywhere in `web/src` (check.sh lists offenders).
2. **Tokens** `src/theme/tokens.css`: port v0's `:root`, `.theme-paper`, `.theme-dark` colour, radius and spacing variables
   (no Tailwind needed; plain CSS like now). Theme = class on `<html>`; stored in `localStorage` `hanziflow.theme`
   (wrap every storage access in try/catch; default `light`). Adjust `--color-content-muted` per theme only as much as
   needed to reach 4.5:1 on `--color-surface`; write the before/after values in the report.
3. **Fonts**: Google Fonts `<link>` in `index.html` for Noto Serif SC (Hanzi, `display=swap`) and Be Vietnam Pro (UI);
   headings may keep a serif per v0. Hanzi tokens use `--font-hanzi`.
4. **Shell** per v0: sidebar (brand "HanziFlow", nav Thư viện / Đang học / Cài đặt) on desktop; header + bottom nav at
   ≤ 720px. "Đang học" opens the last opened lesson (keep it in memory; disabled when none).
5. **Library** per v0 lesson cards using only real catalog fields (order, title, plus duration if the catalog has it — check,
   don't invent). No streak/minutes/progress/"Mới" (no data yet).
6. **Settings** per v0: theme picker with a live preview tile per theme; default view mode (Ẩn / Pinyin / Hán tự + pinyin /
   Hán tự → `HIDDEN`, `PINYIN_ONLY`, `HANZI_WITH_PINYIN`, `HANZI_ONLY`); Hanzi size Nhỏ / Vừa / Lớn → `--hanzi-size`
   22 / 28 / 34px. All persisted like the theme. The Player's initial view mode comes from this setting.
7. **Design system doc** `docs/ui/design_system.md` (≤ 120 lines): token table per theme (name → value, what it is for),
   type scale, spacing, radius, shadows, and component rules (card, segmented control, switch, pill button, chip, sheet/popover,
   active/past/coach line). Later agents (incl. Android) build from this file + `tokens.css`, not from `docs/ui/v0_export/`,
   which will be deleted.
8. Floors: UI text ≥ 12px, touch targets ≥ 44px at ≤ 720px, `:focus-visible` ring in `--accent`.

## Acceptance
- New tests (vitest): switching theme sets the `<html>` class and survives a remount (storage); storage that throws
  still renders with the default theme; Library renders catalog lessons sorted by order; the Settings default view mode
  is the Player's initial mode; at 390px the bottom nav is rendered and the sidebar is not (matchMedia mock or class check).
- `docs/ui/design_system.md` exists and every token in `tokens.css` appears in it.
- `scripts/check.sh` long-line audit names no `web/src` file. Existing web tests still pass (moved, not deleted).

## Verify
```bash
npm --prefix web run typecheck && npm --prefix web test -- --run && npm --prefix web run build; echo "exit=$?"
bash scripts/check.sh
```
Paste the vitest summary line (files/tests passed) and the check.sh lines.

## Out of scope
Player screen styling and behaviour (W2c), PiP (W2d), Android, deploy (user runs `just web-deploy`).

Device check (user): `just web-deploy` or `npm --prefix web run dev`, look at Library + Settings in all 3 themes on
desktop and at phone width; approve or list changes before W2c starts.
