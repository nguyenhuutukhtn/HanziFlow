# Wave 2 — web redesign (v0 style) + player UX fixes

Planned 2026-10-22. Style reference: `docs/ui/v0_export/app/` (v0 prototype; copy its look and UX, not its fake logic). It is **local-only, never
committed** (gitignored) and gets deleted once the product UI is stable; W2b turns it into the durable design system
(`web/src/theme/tokens.css` + `docs/ui/design_system.md`) that all later agents (Android Wave 3 included) follow.
Agents run one at a time, reset between tasks. PM reviews only at the ✅ checkpoints.

| Order | Task | Owns | Depends on | Suggested agent | Checkpoint after |
|---|---|---|---|---|---|
| 1 | [W2a](W2a.md) shared cue "place" (speaking / last started / next) for silences | `TranscriptCues.kt`, `web/PlayerJs.kt` + tests | — | Codex luna · low | ✅ PM re-runs Verify (W2c/W2d use the API) |
| 2 | [W2b](W2b.md) web design system: tokens, 3 themes, shell, Library, Settings, code split | `web/` | — | Codex terra · medium (ask) | ✅ user approves the look (3 themes, desktop + phone) |
| 2b | [W2b-fix](W2b-fix.md) missing tests, Prettier, doc gaps | `web/`, `check.sh` web step | W2b | continue W2b window | PM re-runs Verify |
| 2c | [W2b2](W2b2.md) shadcn/ui component layer on our tokens (user: no browser-default controls) — partial: button/toggle-group/select/popover; rest moved to W2c | `web/`, `design_system.md` | W2b-fix | continue W2b window | ✅ user looks at controls |
| 3 | [W2c](W2c.md) Player screen in v0 style + UX2 (place in silences) + UX3 (free scroll, "Về câu đang phát") | `web/src/` player/transcript/card files | W2a, W2b | Codex terra · medium (ask) | — |
| 4 | [W2d](W2d.md) floating window (PiP) in v0 style + UX1 (controls always visible) | `web/src/` pip files | W2c | continue W2c window | ✅ end of wave, user device check |

Decisions (PM, 2026-10-22; user may veto):
- Android redesign (Compose theme from the same tokens) is Wave 3, after the web look is approved in W2b.
- Copy v0's visual style and component shapes; **render only real data**. No fake streak, minutes, progress ring
  or "Mới" badge until a data source exists (no sign-in on web yet).
- Speeds 1 / 0.8 / 0.7× only (user: 0.6/0.5 distort the audio on web), not v0's 0.75/1.25. No loop-sentence toggle (backlog).
- Model answer keeps the existing position-based reveal; only its two states get the v0 look.
- Brand name in the UI stays "HanziFlow" (v0 invented "nghe hán"); renaming is the user's call.
- Readability floors over v0's tiny sizes: UI text ≥ 12px, muted text ≥ 4.5:1 contrast on its surface,
  touch targets ≥ 44px at phone width, Hanzi in a CJK font (Noto Serif SC), not Georgia.

Launch prompts:
- Fresh: `Read AGENTS.md and docs/tasks/<ID>.md, do the task.`
- W2d: continue the W2c window: `Next: docs/tasks/W2d.md. You already know web/src player/transcript; read only the brief and anything new it names. Report with the same template.`
- Append to each:
  ```text
  Before each major step run: bash scripts/agent_ask.sh <ID> --inbox (new notes = instructions).
  Missing information? Run: bash scripts/agent_ask.sh <ID> "<question>" and use its answer instead of guessing.
  ```
