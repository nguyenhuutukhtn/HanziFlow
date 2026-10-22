# Wave 1 — W1 web player with an always-on-top window (Chrome on Mac + Linux)

Planned 2026-10-22 (user's top priority). Agents run one at a time, reset between tasks. PM reviews only at the ✅ checkpoints.

| Order | Task | Owns | Depends on | Suggested agent | Checkpoint after |
|---|---|---|---|---|---|
| 1 | [W1a](W1a.md) shared JS facade for the player | `kmp/shared/src/commonMain/.../web/`, its tests, `kmp/shared/build.gradle.kts` | — | Codex luna · low | ✅ (W1b/W1c build on its API) |
| 1b | [W1a-fix](W1a-fix.md) facade gaps: line text/language, per-track line list, track-switch | `web/PlayerJs.kt` + test | W1a | continue W1a window (luna · low) | PM re-runs Verify |
| 2 | [W1b](W1b.md) web app: library + player + transcript + build/deploy scripts | `web/`, `justfile`, `scripts/web_*.sh`, `.gitignore` | W1a | Codex terra · medium (ask) | — |
| 3 | [W1c](W1c.md) floating window (Document PiP) + media keys + character card + copy | `web/src/` | W1b | Codex terra · medium (ask) | ✅ end of wave, user device check |

Decisions behind the wave (user, 2026-10-21/22): Chrome on Mac **and** Linux, deployed to a static URL (Cloudflare Pages, per [overview.md](../architecture/overview.md) "Web app"), no sign-in (reads the public `course` bucket anonymously), learner reads Hanzi **with pinyin**, so that is the default view. Speed selector included in W1b (web `playbackRate`, ~10 lines). The Android version stays P4.

Launch prompts (fresh agent each time):
- `Read AGENTS.md and docs/tasks/W1a.md, do the task.` (then W1b, W1c)
- Append to each:
  ```text
  Before each major step run: bash scripts/agent_ask.sh <ID> --inbox (new notes = instructions).
  Missing information? Run: bash scripts/agent_ask.sh <ID> "<question>" and use its answer instead of guessing.
  ```
