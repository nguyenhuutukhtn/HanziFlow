# Task briefs

One file per task: `docs/tasks/<ID>.md`. The PM session writes it; any agent (Codex, DeepSeek, Cursor, Claude) executes it after reading only `AGENTS.md` + the brief + files the brief names. No chat history needed.

Lifecycle: `todo` → `in-progress` → `review` → `done` (set in the brief's `Status:` line). After `done`, the summary lives in [STATUS.md](../STATUS.md); delete the brief once the task is committed.

## Template

```markdown
# <ID> — <title>
Status: todo · Suggested agent: <tool>/<model> (<effort>) · Session: <id or new> · Size: S/M/L · Device check: user | agent (<the scripted check>)
Wave: <n or none> · Owns: <files/dirs this task may edit> · Depends on: <IDs or none> · Checkpoint after: yes | no

## Goal
<1–3 sentences: what changes for the user/pipeline.>

## Read only
<files/sections; nothing else unless blocked>

## Do
1. ...

## Acceptance
- <observable checks>

## Verify
<exact commands; the agent runs them and pastes the output (≤ 15 lines) into the report>

## Out of scope
<what not to touch>
```

Then: update STATUS.md + a token_log.md row, report with the AGENTS.md template, do not commit.

## Writing Acceptance + Verify

- Check **content, not only counts**: name concrete expected values (`听 = tīng, hsk 1`, `走 = tẩu`), not just "≥ 95% non-empty". Coverage passed on data that was wrong (C1-fix: 听 = "smile").
- Verify is a script that prints those values and exits non-zero on mismatch, so the PM re-runs it instead of reading code.
- Data tasks: sample at least 5 specific records spread across the dataset, not only the first entries.
- **Pipeline-generated data a client parses (S1/S2: 3 fix rounds):** the brief must require (1) a test that parses the REAL published file, or a mechanical extract of it (script-derived, include known odd entries, e.g. `懂` has float medians), never hand-picked or hand-written samples; (2) every new catalog/package field present in the contract fixtures copied from a real `publish/` run; (3) a model type distinct per file shape (catalog metadata `{sha256,bytes}` is not a package `Clip` that has `file`); (4) a network/parse failure of an optional feature must hide that feature only, never crash or drop other state.
- Device check order for features that need downloaded data: confirm the data is uploaded (`curl` the public URL, HTTP 200) before blaming the app.

## Agent choice (cheapest that fits)

- Mechanical Python/test/docs edits with a precise brief → DeepSeek or Codex.
- Multi-file Kotlin/Compose, device checks → Cursor or Claude.
- Judgment (spec, review, curriculum) → PM session.
- Mechanical review of a finished task → a cheap agent, ideally a different model than the author: `Use the task-review skill (.claude/skills/task-review/SKILL.md) for task <ID>.` Paste its verdict to the PM.
