# Wave 4 — lesson format `modes_v1` (lesson 2), pipeline → web

Planned 2026-10-22 from STATUS "Code needed for `modes_v1`". Content is committed (`5f19884`:
`content/lesson_002/09_modes.md`). Agents run one at a time, reset between tasks. PM reviews only at ✅.
W3a (PiP) device checkpoint is independent of this wave; its uncommitted web files are not touched before M5.

| Order | Task | Owns | Depends on | Suggested agent | Checkpoint after |
|---|---|---|---|---|---|
| 1 | [M1](M1.md) Contract: `format`, mode tracks, new speakers, `voice_genders` (Python schema + Kotlin models/facade + doc) | `lesson_schema.py`, `kmp/shared` models/facade/tests, Android call sites, `content_package.md` | — | Codex terra · medium (ask), fresh | ✅ PM (contract) |
| 2 | [M2](M2.md) `import-modes`: `09_modes.md` → 4 lesson JSON files | `story/modes.py`, `cli.py`, `audit.py`, `lessons/lesson_002_*.json`, `content/lesson_002/meta.json`, tests | M1 | Codex luna · low, fresh | — |
| 3 | [M3](M3.md) Voices + `generate` for `modes_v1` (dry-run only) | `voice_registry.py`, `cli.py` generate path, `transcript.py`, `justfile`/`scripts/modes.sh`, tests | M1, M2 | Codex luna · low, continue M2 window | ✅ PM, then **user** runs paid TTS |
| 4 | [M4](M4.md) `publish` for `modes_v1`: one track + timeline per mode, real fixture | new `publish_modes.py`, publish dispatch, kmp contract fixture + test | M3 + generated audio | Codex terra · medium (ask), fresh | ✅ PM (contract realized) |
| 5 | [M5](M5.md) Web: mode switcher via existing track toggle, default first track | `web/src/player-context.tsx`, `web/src/screens/`, tests | M4, W3a committed | Codex luna · low, fresh | ✅ user on device |

Between M3 and M4 (user): `just modes 002` (dry-run) → confirm count → generate the 4 files (paid).
After M4 (user): first `just app` on the phone (Android builds before M1 cannot parse a catalog lesson without `guided`), then `mandarin-course upload` when M5 is ready to try against the CDN (local publish works for dev).

Launch prompt (M1, fresh):
```text
Read AGENTS.md and docs/tasks/M1.md, do the task.
Before each major step run: bash scripts/agent_ask.sh M1 --inbox (new notes = instructions).
Missing information? Run: bash scripts/agent_ask.sh M1 "<question>" and use its answer instead of guessing.
```
