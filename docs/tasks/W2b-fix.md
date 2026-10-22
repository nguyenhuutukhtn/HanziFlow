# W2b-fix — missing W2b tests, Prettier, design-system doc gaps
Status: todo · Suggested agent: continue W2b window (terra · medium) · Session: <W2b session> · Size: S · Device check: none
Wave: 2 · Owns: `web/` (not `web/public/shared/`), `docs/ui/design_system.md`, `scripts/check.sh` (web step only) · Depends on: W2b · Checkpoint after: yes (PM re-runs Verify)

## Goal
W2b reported partial: none of its five acceptance tests exist, four `web/src` files still have lines over 200 chars,
and `docs/ui/design_system.md` misses `--space-2/3/4`. Close those; no UI changes.

## Do
1. Prettier (pin an exact version as devDependency), `.prettierrc` `{ "printWidth": 120 }`, scripts `format` and
   `format:check` (over `src/**/*.{ts,tsx,css}`). Run `format` once. In `scripts/check.sh` extend the web step with
   `npm --prefix web run format:check`.
2. Write the W2b acceptance tests (vitest):
   - switching theme in Settings sets the `<html>` class and it survives a remount (storage);
   - `localStorage` that throws on get/set still renders with the default `light` theme;
   - Library renders catalog lessons sorted by `order` (fixture with orders 3,1,2 → 1,2,3);
   - the Settings default view mode (e.g. `HANZI_ONLY`) is the Player's initial view mode;
   - at 390px the bottom nav renders and the sidebar does not (matchMedia mock or class check).
3. Add `--space-2`, `--space-3`, `--space-4` to `docs/ui/design_system.md`.

## Acceptance
- vitest: at least 8 tests (3 old + 5 new), all pass.
- `grep -rlE '^.{201,}' web/src` prints nothing; `format:check` passes.
- Every token in `tokens.css` appears in the doc (Verify loop prints nothing).

## Verify
```bash
npm --prefix web run typecheck && npm --prefix web run format:check && npm --prefix web test -- --run && npm --prefix web run build; echo "exit=$?"
grep -rlE '^.{201,}' web/src; for t in $(grep -oE -- '--[a-z0-9-]+:' web/src/theme/tokens.css | tr -d : | sort -u); do grep -q -- "$t" docs/ui/design_system.md || echo "missing: $t"; done
bash scripts/check.sh
```

## Out of scope
Player restyle/structure (`usePlayer`, `content.ts`) and splitting `Player.tsx` (343 lines) — that is W2c.
