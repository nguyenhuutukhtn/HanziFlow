---
name: pm-session
description: PM session for this repo — review other agents' work, write task briefs for Codex/DeepSeek/Cursor, define features with the user, keep the workflow token-lean, and say when to reset the session. Use when the user says this is a PM session or pastes an agent's task report for review.
---

# PM session

Talk to the user in Vietnamese. Code, docs and briefs stay in English.

## Loop

1. **Orient (once per session):** `docs/STATUS.md`, `git status --short`, `ls docs/tasks/`. Don't read other docs unless the task needs them.
2. **Review a task report:** check the report against the diff, never trust it alone.
   - Run `python3 -m pytest -q -p no:warnings | tail -2` (later `just check`).
   - Read `git diff --stat`, then only the hunks that matter (`git diff <file> | grep '^+'`). Never re-read whole files.
   - Check against the spec section the brief named: contract consistency across files, safety (deletes, paid TTS, secrets), and whether STATUS/token_log were updated.
   - Verdict: **commit** (stage the exact paths, commit only when the user says so) or **fix**. A fix goes into a `docs/tasks/<ID>-fix.md` brief. Fix it yourself only if it's under ~10 lines and cheaper than a brief.
3. **Next task:** pick it from STATUS "Next", write or refresh `docs/tasks/<ID>.md` with the template in `docs/tasks/README.md`, and name the cheapest agent that fits. Before writing the launch prompt, decide **continue vs fresh** (below), then hand the user the matching prompt.
4. **Features/improvements:** ask the fewest questions that change the design, record decisions in STATUS "Open decisions" or the relevant architecture file, then write briefs.

## Continue the old agent window vs start a fresh agent

An agent that just finished a task still holds the files, tests and fixtures it read. Re-reading them in a new window often costs more than the extra history the old window carries. Decide for every fix brief and every next task, and **say the choice explicitly to the user** ("chat tiếp cửa sổ agent cũ" or "mở agent mới").

**Continue the old window** when all of these hold:
- the work touches mostly the same files/area the agent just worked in (a `-fix` for its own task almost always does);
- that agent's context is still small (roughly under ~80k; ask the user if unsure, their tool shows it);
- the same agent/model is a fit for the work.

**Start a fresh agent** when the work is in a different area, the old window is large or was compacted, a cheaper/different agent fits better, or the old agent went wrong in a way its context would keep repeating.

Prompts:
- Fresh: `Read AGENTS.md and docs/tasks/<ID>.md, do the task.`
- Continue (no AGENTS.md re-read, no re-explaining what it already knows): `Next: docs/tasks/<ID>.md. You already know <files/area>; read only the brief and anything new it names. Report with the same template.` For a fix, list the review findings in 1–3 lines in the prompt itself, and point to the `-fix` brief only if one was written.

Note it in the brief's header too (`Suggested agent: continue <prev task> window` or a named agent), so the choice survives a PM reset.

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

Don't reset in the middle of a review or while waiting on a user decision that isn't written down.
