# WAVE-WF — web player fixes from the Chrome/Linux review (WR1)

## Goal

Fix every bug in `docs/reviews/web_review_linux.md` and add the Linux PiP always-on-top hint (STATUS "Open
decisions", 2026-10-28), so the web player is reliable for daily office listening on Zorin (Chrome, Wayland).

## Priority (PM, from WR1 + code check)

| P | Bug | Task |
| --- | --- | --- |
| P1 | Track switch restarts at 0 instead of the mapped position (`player-context.tsx:135`) | WF1 |
| P1 | PiP unstyled in the production build: cloned `<link>` stylesheets never load (`screens/Player.tsx:49`); WR1 said P2, PM raised it: PiP is the main office use | WF2 |
| P2 | "Hán tự + pinyin" shows no pinyin in the main transcript (`Transcript.tsx:223`, escaped from W3a) | WF3 |
| P2 | Auto-follow turns off by itself after a long programmatic scroll (`Transcript.tsx:92`, escaped from W3b PM fallback) | WF3 |
| P2 | "Nghe lại câu"/`R` is a no-op in a gap (`player-context.tsx:119`) | WF1 |
| P3 | Progress slider: 1 ms keyboard step, raw-ms ARIA value (`screens/Player.tsx:172`) | WF2 |
| — | Linux PiP hint: right-click title bar → Always on Top | WF2 |

## Order

| # | ID | Suggested agent | Owns | Depends on | Checkpoint after |
| --- | --- | --- | --- | --- | --- |
| 1 | WF1 | codex/gpt-5.6-sol (medium) | `web/src/player-context.tsx`, `web/src/WF1.test.tsx`, `web/scripts/track-check.mjs` | none | yes (PR) |
| 2 | WF2 | codex/gpt-5.6-luna (medium) | `web/src/screens/Player.tsx`, `web/src/pip/`, `web/src/components/ui/slider.tsx`, `web/src/settings.ts`, `web/src/styles.css`, `web/src/WF2.test.tsx`, `web/scripts/pip-check.mjs` | none | yes (PR) |
| 3 | WF3 | codex/gpt-5.6-sol (medium) | `web/src/Transcript.tsx`, `web/src/styles.css`, `web/src/WF3.test.tsx`, `web/scripts/follow-check.mjs` | WF2 merged (shares `styles.css`) | yes (PR, wave end) |

Each task: `agent-task start <ID>` → worker → PM Verify + `check.sh` → `agent-task pr` → reviewers → user merges → `agent-task done`.
One sol run per 5h window: WF1 and WF3 in different windows, WF2 (luna) between them.

## Checkpoint review

- Reviewers per PR: Codex fresh session on another tier, read-only (sol wrote → luna; luna wrote → sol if the
  window has room, else luna fresh), then DeepSeek last with live runs (`npm --prefix web run build` + preview +
  the task's `web/scripts/*-check.mjs`; each command ≤ 60 s, no watchers).
- PM: rerun Verify, `scripts/check.sh` once, map each Acceptance bullet to a test, read state/timing hunks
  (`changeTrack`, `replay`, follow `programmed` flag).
- Device check (user, Zorin Chrome): see each brief's checklist.

## Completion

All three PRs merged; WR1 bugs re-run by the live scripts show expected values; STATUS "Done" gets one WAVE-WF
line, pending user checks move to STATUS; this file and `docs/tasks/WR1.md` are removed in the last task's commit.
