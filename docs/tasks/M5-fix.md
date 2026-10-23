# M5-fix — per-lesson track memory + real-fixture tests
Status: todo · Suggested agent: continue M5 window (codex/gpt-5.6-luna, low) — user may escalate · Session: M5 · Size: S · Device check: user
Wave: 4 · Owns: `web/src/player-context.tsx`, `web/src/M5.test.tsx` · Depends on: M5 (uncommitted) · Checkpoint after: yes (user on web)

## Review findings (Haiku task-review + PM, 2026-10-23)
1. M5 added a single global `localStorage["hanziflow-track"]` (HEAD had no track persistence; the M5 brief wrongly implied it existed). User decision: **remember the track per lesson**.
2. `M5.test.tsx` builds an inline catalog instead of the real M4 fixture `kmp/shared/src/commonTest/resources/contract/catalog_modes.json`.
3. No assertion that selecting Nói requests `noi.mp3`.
4. No test that lesson 1 starts on `guided` (none of the existing tests cover it either).

## Do
1. Store the track per lesson: key `hanziflow-track:<lessonId>` (keep the try/catch). Drop the global key; no migration.
2. In `M5.test.tsx`, load lesson 2's catalog entry from `catalog_modes.json` (read the file, like the other tests load real data); lesson 1 may stay inline.
3. Tests (each a separate `it`):
   - lesson 2, empty storage → `hieu`; toggle labels `Hiểu, Nghe, Phản xạ, Nói`; select Nói → fetches end with `timeline_noi.json`, audio src ends with `noi.mp3`, position 0.
   - lesson 1, empty storage → `guided`.
   - saved `hanziflow-track:lesson_002 = noi` → lesson 2 opens on `noi`; lesson 1 with no key of its own still opens on `guided`.
   - saved `hanziflow-track:lesson_002 = practice` (invalid) → `hieu`.
   - switching on lesson 2 writes `hanziflow-track:lesson_002` and leaves the lesson 1 key untouched.

## Verify
```bash
source .venv/bin/activate
grep -q "catalog_modes.json" web/src/M5.test.tsx && grep -q "noi.mp3" web/src/M5.test.tsx || { echo "FAIL fixture/noi.mp3 not in test"; exit 1; }
grep -q '"hanziflow-track"' web/src/player-context.tsx && { echo "FAIL global key still used"; exit 1; }
cd web && npx vitest run src/M5.test.tsx && npx vitest run && cd .. && scripts/check.sh
```

## Out of scope
UI, styles, Android, other tests.

Then: update the M5 line in STATUS.md, add a token_log row (kind web, fix round 1), report with the AGENTS.md template, do not commit.
