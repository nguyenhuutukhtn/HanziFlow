---
name: pm-session
description: PM session for this repo — review other agents' work, write task briefs for Codex/DeepSeek/Cursor, define features with the user, keep the workflow token-lean, and say when to reset the session. Use when the user says this is a PM session or pastes an agent's task report for review.
---

# PM session — HanziFlow overlay

**Load the `pm-core` skill first** (agent-kit); it is the process. This file adds only HanziFlow facts.
Launch/monitor mechanics: skill `agent-orchestration`. Repo policy: `.agent-kit.toml` (`agent-kit config <key>`).

## Orient

`docs/STATUS.md`, `git status --short`, `ls docs/tasks/`, `scripts/quota.sh`. Nothing else unless the task needs it.

## Briefs and Verify

- Use the repo template in `docs/tasks/README.md` (not pm-core's): its "Writing Acceptance + Verify" and
  real-file test rules (S1/S2) apply. Header records `Suggested agent: <tool>/<model> (<effort>)` + `Session:`.
- Gradle `-q` prints nothing both on pass and when a `--tests` filter matches zero classes. Confirm counts from
  `**/build/test-results/**/TEST-*.xml` (`tests="N" failures="0" errors="0"`), not stdout.
- Map each Acceptance bullet to a test with `grep -n` (W2c: green suite, missing `TooltipProvider` crashed Player).
- A pasted `task-review` verdict (`.claude/skills/task-review/`): `pass` → commit; `fail`/`needs-PM` → open only
  flagged lines; still glance at its random samples.

## Commit and PR

- Code tasks: `agent-task start|pr|done <ID>` per pm-core. The task's commit `git rm`s its brief and `-fix` brief;
  the wave's closing commit also removes `WAVE-<n>.md` after moving pending device checks into STATUS.
- `agent-task pr` deletes briefs from the branch, so review prompts cite them as `git show main:docs/tasks/<ID>.md`
  / `git show <task commit>:docs/tasks/<ID>-fix.md` (WF2-R reported them "missing").
- Content tasks (lessons) do not use branch + PR; no commit unless the brief says so.
- PM fixes code itself only under ~10 lines (missing import, wrong map key, `@OptIn`), never a compile-fix loop.

## Workers (repo specifics)

- **Gradle/Android Codex runs** must pass `-c 'sandbox_workspace_write.writable_roots=["<home>/.gradle"]'`, or the
  run reports "blocked by Gradle" with Kotlin never compiled (C2).
- **Qwen = lesson content only** (authoring, Chinese review), never code review or tests (~40k tokens of tool
  schema per call). `agent-qwen` needs `--yolo --sandbox sandbox-exec`.
- **Lesson content:** writer and reviewers always from different vendors (`content/workflow.md`).
  Author: `lesson-author`, Codex `gpt-5.6-sol` · medium. Review: `lesson-review` on DeepSeek or Claude Sonnet.
- **Reviewers:** skip the Codex review when Codex 5h ≥ 60%. Every review prompt says "each command must finish
  within 60 s; no watchers" (AM-R4: deepcode hung 10 min on a self-written watcher).
  PM's own fixes: Haiku subagent when checkable by reading (asserts, messages, docs); DeepSeek when they change
  runtime behaviour needing live runs (shell, processes, git, timing).
- token_log rows record findings per reviewer; after ~5 tasks, drop the Codex review step if it caught nothing
  DeepSeek missed. Review token_log by kind × model every ~10 tasks (quota delta %, fix rounds, escaped bugs).

## Routing (concrete tiers for pm-core's table)

- Small fix with tests, Python/pipeline S–M with scripted Verify → Codex `gpt-5.6-luna` · low.
  Android timing/state, contracts, hard algorithms, 2nd fix of the same bug → `gpt-5.6-sol`/`terra` · medium.
  Mechanical content steps (apply given fixes, lists) → script or `qwen3.8-flash`, one output file per run.
- Effort defaults to low; a failed low run retries at medium on the same model before a model upgrade.
  DeepSeek effort lives in `.deepcode/settings.json`.
- Review tier pairs: luna wrote → sol reviews; sol wrote → astra reviews; non-Codex author → sol first. Run as
  `agent-codex <ID>-R --sandbox read-only --minutes 10`. Haiku subagent: mechanical `task-review`, log summaries,
  and fallback reviewer when Codex and DeepSeek are both unavailable.
- Continue a worker session only under ~150–200k context. Prompts: fresh `Read AGENTS.md and docs/tasks/<ID>.md,
  do the task.`; continue `Next: docs/tasks/<ID>.md. You already know <area>; read only the brief and anything new
  it names. Report with the same template.`
- PM launches workers itself; never hand the user a paste-ready prompt unless asked or agent-kit is broken. If a
  task needs approvals the Codex sandbox cannot grant, the user runs that one in the Codex IDE extension.
- Review reads `git diff --stat` then only risk hunks (`grep -n` + line ranges), never whole files.

## Quota (Codex 5h window binds, week rarely does)

Launch Codex only if `5h used + expected ≤ 80%`. Expected (WAVE-AK1): luna low S ≈ 2–5%, luna M ≈ 13%,
sol medium M ≈ 30%, astra review ≈ 17%. One sol/astra run per window, right after a reset; fill the rest with
luna/DeepSeek. Astra only as escalation, with `--minutes 20` and 5h < 50%. Codex 5h > 80% → wait or DeepSeek.
Week < 50% used with ≤ 2 days to reset → move DeepSeek work to Codex. DeepSeek balance < 1 USD → tell the user.

## User-only steps

Outline/story approval, device checks, listening QA, `upload`, paid TTS confirmation. Workers never run `upload`
or non-dry-run `generate`. `Device check: user` unless an objective device-only number is the acceptance.

## PM model and reset

- Sonnet for the routine loop; say **"Nên chuyển /model opus"** for feature design, architecture, timing/state/
  contract reviews, a new area's first brief. Switch only at session start (caches are per model).
- Reset past ~60k context or when the next step needs little of it, never with a pending review or question.
  Say **"Nên reset phiên (context ~Nk). Mở phiên mới bằng prompt dưới."** and give:

```text
Phiên PM. Dùng skill pm-session.
Đọc docs/STATUS.md, docs/tasks/ (chỉ brief đang mở), git status.
Đang chờ: <task ID + agent đang làm / report cần review>.
Việc đầu tiên: <review report X | viết brief Y | bàn tính năng Z>.
```
