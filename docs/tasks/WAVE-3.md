# Wave 3 — PiP learning view, then Android

Planned 2026-10-22 after the Wave 2 device check (user: PiP works, but raw one-line text, pinyin always on, bad for learning characters).
Agents run one at a time, reset between tasks. PM reviews only at the ✅ checkpoints.

| Order | Task | Owns | Depends on | Suggested agent | Checkpoint after |
|---|---|---|---|---|---|
| 1 | [W3a](W3a.md) PiP = whole transcript (compact), `拼` toggle, tap to peek pinyin, card in PiP | `web/src/pip/`, `Transcript.tsx`, `styles.css`, tests | W2d-fix | Codex terra · medium (ask), fresh | ✅ user looks at PiP on device |
| 2 | S3 stroke animation upside down (Android, backlog) | brief at checkpoint 1 | — | Codex luna · low if Verify asserts a stroke position | — |
| 3 | Android redesign from `web/src/theme/tokens.css` | briefs at checkpoint 1 (needs a design pass with the user) | W2 look approved | Codex terra · medium (ask) | ✅ user approves the look |

Launch prompt (W3a, fresh):
```text
Read AGENTS.md and docs/tasks/W3a.md, do the task.
Before each major step run: bash scripts/agent_ask.sh W3a --inbox (new notes = instructions).
Missing information? Run: bash scripts/agent_ask.sh W3a "<question>" and use its answer instead of guessing.
```
