# Task briefs

One file per task: `docs/tasks/<ID>.md`. The PM session writes it; any agent (Codex, DeepSeek, Cursor, Claude) executes it after reading only `AGENTS.md` + the brief + files the brief names. No chat history needed.

Lifecycle: `todo` → `in-progress` → `review` → `done` (set in the brief's `Status:` line). After `done`, the summary lives in [STATUS.md](../STATUS.md); delete the brief once the task is committed.

## Template

```markdown
# <ID> — <title>
Status: todo · Suggested agent: <cheapest that fits> · Size: S/M/L

## Goal
<1–3 sentences: what changes for the user/pipeline.>

## Read only
<files/sections; nothing else unless blocked>

## Do
1. ...

## Acceptance
- <observable checks>

## Verify
<exact commands>

## Out of scope
<what not to touch>
```

Then: update STATUS.md + a token_log.md row, report with the AGENTS.md template, do not commit.

## Agent choice (cheapest that fits)

- Mechanical Python/test/docs edits with a precise brief → DeepSeek or Codex.
- Multi-file Kotlin/Compose, device checks → Cursor or Claude.
- Judgment (spec, review, curriculum) → PM session.
