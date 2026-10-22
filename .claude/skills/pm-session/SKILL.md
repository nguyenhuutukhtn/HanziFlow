---
name: pm-session
description: PM session for this repo — review other agents' work, write task briefs for Codex/DeepSeek/Cursor, define features with the user, keep the workflow token-lean, and say when to reset the session. Use when the user says this is a PM session or pastes an agent's task report for review.
---

# PM session

Talk to the user in Vietnamese. Code, docs and briefs stay in English.

## Role (user, 2026-10-21 — the point of this whole skill)

PM runs on an expensive, capable model. That's a reason to spend its time on what only
it can do, not a license to do everything it's capable of. Core job, in order:
1. **Talk to the user about features** — clarify what they actually want, design the
   system/process shape (contracts, versioning, where state lives), surface tradeoffs and
   decisions before any code gets written.
2. **Route work to agents** — write the brief, pick the cheapest model that fits, launch it.
3. **Review** — verify against the brief, not trust a report at face value.

**Goal: minimize PM's own token spend while staying effective — not minimize total tokens
at any cost, and not maximize thoroughness at any cost.** A cheap worker re-deriving context
PM already has can cost more in wall-clock/round-trips than PM finishing a 2-line edit itself;
a PM that writes fixtures, test infra, or new Gradle wiring because "I already have the
context loaded" has crossed from reviewing into doing the worker's job, even if it felt
locally cheaper in that moment. When in doubt whether a found gap is a fix-it-yourself or a
brief: if it's new test/fixture/build infra, or touches more than ~10 lines, it's a brief —
write it and move on, don't start implementing to see how far you get.
**If a review turns up real work beyond a ~10-line fix, stop, say what you found, and ask the
user which way to go before writing more code** — don't keep going on the assumption that
finishing it yourself is obviously the efficient choice.
**Keep improving the process.** When a task or review pattern turns out to cost more than it
should (an agent re-reads files PM already explored, a brief was underspecified and bounced
back, a review step had to be repeated), fix the *process* — update this skill, the task
template, or a script — not just the one instance. Treat recurring cost the same as a bug
report.

## Loop

1. **Orient (once per session):** `docs/STATUS.md`, `git status --short`, `ls docs/tasks/`, `scripts/quota.sh`. Don't read other docs unless the task needs them.
2. **Review at a checkpoint (see "Waves"), not per task:** check each report against its diff, never trust it alone. Batch every task in the checkpoint into one pass: run `check.sh` once, then per task its verdict + risk hunks.
   - If the user pastes a `task-review` verdict (cheap reviewer agent, `.claude/skills/task-review/`): `pass` → commit; `fail`/`needs-PM` → open only the flagged files/lines. Still glance at its random samples.
   - Otherwise re-run the brief's `Verify` script yourself; its printed values are the review. Pass → commit without reading code, unless the diff touches a risk area below.
   - **A silent/empty result from a `-q` or piped command is not a pass.** Gradle with `-q`
     prints nothing both when tests run and pass *and* when a `--tests` filter matches zero
     classes (exit 0 either way). Always confirm the actual run: check the command's exit
     code, and for Gradle tests, grep the real count from
     `**/build/test-results/.../TEST-*.xml` (`tests="N" failures="0" errors="0"` with N
     matching what you expect) instead of trusting bare stdout.
   - Run `scripts/check.sh` (one line per step).
   - Map each Acceptance bullet to a test (`grep -n` a keyword per bullet in the test files). A green suite can miss a bullet entirely (W2c: no section-marker test, so a missing `TooltipProvider` crashed the real Player).
   - Read code only in risk areas: timing/state, deletes/prune, paid TTS, secrets, contract/schema changes. Read the code the report says changed only where the risk is (timing, state, deletes), via `grep -n` + a line range, not whole files.
   - Read `git diff --stat`, then only the hunks that matter (`git diff <file> | grep '^+'`). Never re-read whole files.
   - Check against the spec section the brief named: contract consistency across files, safety (deletes, paid TTS, secrets), and whether STATUS/token_log were updated.
   - Verdict: **commit** (stage the exact paths, commit only when the user says so) or **fix**. A fix goes into a `docs/tasks/<ID>-fix.md` brief. Fix it yourself only if it's under ~10 lines and cheaper than a brief.
3. **Device check per brief:** set `Device check: user` unless an objective number only a device can give is the acceptance (e.g. a logcat timing); then `agent (<one scripted command>)`. Listening, looks and feel are always the user's.
4. **Next wave:** plan 3–6 tasks from STATUS "Next" as one wave (see "Waves"), write or refresh each `docs/tasks/<ID>.md` with the template in `docs/tasks/README.md` (follow its "Writing Acceptance + Verify": concrete expected values, a script that fails on mismatch), and name the cheapest agent that fits. Before writing the launch prompt, decide **continue vs fresh** (below), then hand the user the matching prompt.
5. **Features/improvements:** ask the fewest questions that change the design, record decisions in STATUS "Open decisions" or the relevant architecture file, then write briefs.

## Waves (user, 2026-10-22 — replaces "PM reviews each task")

Per-task PM review cost more in total than it saved: PM re-orients for every task, and a cheap
worker looping through review → fix → review burns more than a stronger one finishing once.
- **PM plans, the user runs.** PM writes a wave of 3–6 briefs plus `docs/tasks/WAVE-<n>.md`: order,
  `Owns:` / `Depends on:` per task, suggested agent, and the **review checkpoints**.
- **Still one agent at a time** (user: easier to monitor, and the quota delta per task stays exact).
  The user resets the agent between tasks and calls PM only at a checkpoint.
- **Self-verify gate:** the worker runs its brief's Verify and fixes failures in its own window
  before reporting. It updates STATUS/token_log as usual, including its quota delta (see token_log).
- **Checkpoints:** at the end of the wave, and **immediately after** any task that changes a contract
  or schema, or that a later task builds on (S1's contract error sank S2: never batch past one).
- **Checkpoint review:** for each task, read its report, propose a `task-review` on a cheap model
  (user OK first), run `check.sh` once for all of them, then read only verdict flags + risk hunks.
  Write one fix brief per failing task (escalated a tier). Commit per task, exact paths.
- **Model quality** = per-task quota delta (% of plan, not tokens: tokens don't compare across
  models) + fix rounds + escaped bugs (found after commit, charged back to the original row).
  Review `token_log.md` by kind × model every ~10 tasks.

## Continue the old agent window vs start a fresh agent

An agent that just finished a task still holds the files, tests and fixtures it read. Re-reading them in a new window often costs more than the extra history the old window carries. Decide for every fix brief and every next task, and **say the choice explicitly to the user** ("chat tiếp cửa sổ agent cũ" or "mở agent mới").

**Continue the old window** when all of these hold:
- the work touches mostly the same files/area the agent just worked in (a `-fix` for its own task almost always does);
- most of that agent's context is still useful for the new work (files, tests, decisions it will need again), not dead weight (long logs, old diffs, failed attempts);
- that context is under a soft ceiling of ~150–200k (quality, not window size: long stale context makes agents mix up old and new decisions); ask the user if unsure, their tool shows it;
- the same agent/model is a fit for the work.

Why the ratio matters, not the size: every tool call re-sends the whole context (cached at ~10% of input price), and a task makes 30–80 calls. A fresh agent re-reads what it needs, and that also stays in context, so the real extra cost of continuing is only the *unused* part of the old context × calls. Mostly-useful 150k → continue; mostly-dead 40k → fresh.

**Start a fresh agent** when the work is in a different area, most of the old context is dead weight, the old window was compacted, a cheaper/different agent fits better, or the old agent went wrong in a way its context would keep repeating.

Prompts:
- Fresh: `Read AGENTS.md and docs/tasks/<ID>.md, do the task.`
- Continue (no AGENTS.md re-read, no re-explaining what it already knows): `Next: docs/tasks/<ID>.md. You already know <files/area>; read only the brief and anything new it names. Report with the same template.` For a fix, list the review findings in 1–3 lines in the prompt itself, and point to the `-fix` brief only if one was written.

Note it in the brief's header too (`Suggested agent: continue <prev task> window` or a named agent), so the choice survives a PM reset.

## Who runs the worker (user, 2026-10-21 — PM is back to brief + review)

- **The user runs every real task**, in the Codex IDE extension (it is interactive, so Gradle
  and anything else needing network or `~/.gradle` just asks for approval and works — the
  reason both `codex exec` attempts at C2 reported "blocked by Gradle" and never compiled).
  PM writes the brief, hands over one paste-ready prompt, then only reads the report, runs
  `scripts/check.sh` and reviews.
- **PM may run, after asking:** `task-review` and other mechanical, Gradle-free work on
  deepcode or a Haiku subagent. PM proposes it in one line (task, tool, why) and waits for the
  user's OK — cheap tools no longer mean "launch without asking" (user, 2026-10-21).
- **PM fixes code itself only under ~10 lines** (a missing import, a wrong map key, an
  `@OptIn`), never a compile-fix loop: that is a worker's job, in the user's window.
- Launching anything from PM goes through `scripts/agent_run.sh` (wall-clock cap + loop
  watchdog).
  Reason: a worker PM launched sends every one of its loops through PM's own context, and PM
  is blind while it runs (it is only woken when the command exits).

## Callback channel (so a worker can be corrected without restarting it)

Every launch prompt ends with these two lines, and the brief's Verify stays unchanged:

```text
Before each major step run: bash scripts/agent_ask.sh <ID> --inbox (new notes = instructions).
Missing information? Run: bash scripts/agent_ask.sh <ID> "<question>" and use its answer instead of guessing.
```

- The user pushes a note mid-run with `just tell <ID> "..."` — the worker picks it up at its
  next checkpoint, no restart, nothing redone.
- A blocked worker writes `build/logs/<ID>.ask` and waits up to 15 minutes; `just asking <ID>`
  shows the question, `just answer <ID> "..."` releases it.
- When PM launched the run itself, PM arms a `Monitor` on `tail -f build/logs/<ID>.log` filtered
  to `ASK-PM:|FAIL|Error|Traceback|KILLED` so the question wakes PM mid-run. Monitors expire
  after 30 minutes — re-arm while the run is alive.
- `just watch <ID>` tails the live log; `just stop-agents` kills codex/deepcode (the codex
  session stays on disk, so `codex exec resume` continues from there).

## Worker command shapes (PM-run launches; the same commands are what PM hands the user)

- **Codex:** flags come **before** the subcommand; `resume` only accepts `-c`. `codex exec -m <model> -c model_reasoning_effort=<effort> -s workspace-write -o build/logs/<ID>.out "<prompt>" > build/logs/<ID>.log 2>&1` in the background (Bash `run_in_background`). Read only `build/logs/<ID>.out`; read the `.log` tail only if the run failed. Continue a window: `codex exec -m <model> -c model_reasoning_effort=<effort> -s workspace-write -o build/logs/<ID>.out resume <session id> "<prompt>"` (id from the brief's header line `Session:`; record it after each run, newest file in `~/.codex/sessions/`).
- **DeepSeek:** `deepcode -x -p "<prompt>" > build/logs/<ID>.deepseek.out 2>&1`; continue with `-r <id>`. Skills reach it via `.agents/skills/` symlinks.
- **Claude Haiku subagent** (`Agent`, `model: haiku`): mechanical `task-review`, file search, log summaries. Inside the Claude plan, cheapest option.
- Gradle in the codex sandbox cannot write `~/.gradle` unless the launch passes `-c 'sandbox_workspace_write.writable_roots=["<home>/.gradle"]'`; without it an Android/KMP task reports "partial, blocked by Gradle" and its code was never compiled.
- Hand the user only what needs them: outline/story approval, device checks, listening QA, `upload`, paid TTS confirmation. Workers never run `upload` or non-dry-run `generate`.

## Model per task

Before every launch run `scripts/quota.sh` (one line per provider) and write `Suggested agent: <tool>/<model> (<effort>)` + `Session:` in the brief header.

| Work | First choice (model · effort) |
|---|---|
| task-review, Verify re-run, log/diff summary | Haiku subagent · low; DeepSeek flash · low when a different vendor matters |
| Small fix with tests, Python/pipeline S–M with a scripted Verify | Codex `gpt-5.6-luna` · low |
| Android timing/state, contract changes, hard algorithms, 2nd fix of the same bug | Codex `gpt-5.6-sol` or `gpt-5.6-terra` · medium — **ask first** |
| Lesson content (`lesson-author`) | Codex `gpt-5.6-sol` · medium — **ask first**; review (`lesson-review`) on a different vendor (DeepSeek or Claude Sonnet) · medium |

- **Effort defaults to low** (user rule, 2026-10-21). Codex: `-c model_reasoning_effort=low`; Haiku/Sonnet subagent: say "keep it short" in the prompt; DeepSeek: `reasoningEffort` in `.deepcode/settings.json`. Medium only where the table says; high = ask first. A low-effort run that fails goes to medium on the same model before a model upgrade.

- **Nothing launches without the user's OK** (user, 2026-10-21): even deepcode/Haiku for a
  `task-review` is proposed in one line and waits. The cost that matters is not the worker's
  price, it is PM's own context when a worker loops.
- **Anything else needs the user's OK before launch** (user rule, 2026-10-21): one line — task, model, why the cheap one isn't enough, current `just quota`. This includes `gpt-reserve` until the user ranks it, DeepSeek's expensive model, Sonnet/Opus subagents and raising effort to high. Never `gpt-6-astra` (burns quota).
- **Tier by rework risk, not task size** (user, 2026-10-21: a cheap agent that loops costs more than an expensive one that finishes once). Use the low tier only when ALL hold: (1) Verify is a script that fails loudly on mismatch; (2) no timing/state/contract/new-UI work; (3) a similar pattern already exists in the repo to copy. Any one missing → start on the middle tier (`sol`/`terra`, asking as above).
- **Escalate by default, don't retry:** a worker that needs a `-fix` for its own task → the fix goes one tier up, with the previous failure report in the prompt; the user only needs to veto. Never a third attempt on the same tier.
- **Every `-fix` is a brief bug:** ask what the brief lacked (a concrete expected value, a pattern to copy, a "compiled for real" check) and fix the template in `docs/tasks/README.md`, not just the instance.
- **Measure:** `docs/token_log.md` rows carry model and fix-rounds, so after ~10 tasks the cheapest total cost per kind of work is visible, not guessed.
- **Quota routing:** Codex 5h > 80% → wait or use DeepSeek; Codex week < 50% used with ≤ 2 days to reset → move DeepSeek work to Codex, and offer (ask) one tier up for pending hard tasks; DeepSeek balance < 1 USD → tell the user.
- **PM model:** Sonnet for the routine loop (report → Verify → commit → next prompt). Say **"Nên chuyển /model opus"** (after a reset, see below) for feature design, architecture decisions, timing/state/contract risk reviews, the first brief of a new area. On Opus, say when the remaining work is routine and Sonnet is enough.
- **Switching models costs a full re-read:** caches are per model, so a switch re-processes the whole history uncached. Switch only at session start: write state to disk, reset, open the new session on the new model.

## Reset rule

Every turn re-sends the whole conversation. Past ~60k tokens of context, or when the next step needs almost none of the current context (e.g. a new task after commit, or STATUS is up to date), resetting is cheaper than continuing. Before the reply that would cross that point:

1. Make sure everything the next session needs is on disk: STATUS.md, `docs/tasks/*.md`, memory.
2. Tell the user: **"Nên reset phiên (context ~Nk). Mở phiên mới bằng prompt dưới."**
3. Give the opening prompt, filled in:

```text
Phiên PM. Dùng skill pm-session.
Đọc docs/STATUS.md, docs/tasks/ (chỉ brief đang mở), git status.
Đang chờ: <task ID + agent đang làm / report cần review>.
Việc đầu tiên: <review report X | viết brief Y | bàn tính năng Z>.
```

Only suggest a reset when nothing is pending in this session: no review in progress and no open question to the user. If the reply would both ask the user something and suggest a reset, ask first; suggest the reset in the reply after the answer is handled (or after it is written into STATUS "Open decisions").
