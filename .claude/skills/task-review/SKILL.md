---
name: task-review
description: Mechanical review of a finished task against its brief (docs/tasks/<ID>.md) — run Verify + checks, scope and risk scan, random samples — and hand the PM a ≤ 12-line verdict. Use when asked to "review task <ID>" or "task-review <ID>". Does not fix anything.
---

# Task review (mechanical)

You check another agent's finished task so the PM doesn't have to read the diff. You are not the PM: no judgment calls on design, no fixes, no commits. Prefer a model different from the one that did the task.

## Steps

1. Read `docs/tasks/<ID>.md` only: Acceptance, Verify, Out of scope, Read only. Don't read AGENTS.md or source files.
2. Run every Verify command exactly as written. Record exit codes and the lines that show expected values. A missing or non-runnable Verify = `needs-PM`.
3. Run the project check (`scripts/check.sh`, or the one AGENTS.md names): keep only the one-line-per-step output.
4. `git status --short` + `git diff --stat`: list changed files that the brief neither names nor implies, and any file the brief's "Out of scope" names.
5. Risk scan on added lines only (`git diff -U0 | grep '^+'`), flag any hit:
   - deletes/prune/`rm`/`unlink`/`shutil.rmtree`, `--force`;
   - paid API calls (TTS, LLM) outside a dry-run path;
   - secret-looking values (`sk-`, `eyJ`, `service_role`, `KEY=` with a value);
   - contract/schema files (`*schema*`, contract fixtures, migrations), timing/threading code.
6. If the task produced data (JSON, CSV, generated content): print 5 **random** records (not ones named in Acceptance), one compact line each, for the PM to eyeball. Don't judge their correctness beyond obvious emptiness or placeholders (`?`, `？`, `TODO`, blank).
7. Compare the agent's report with what you saw: any claim (counts, "passed", "none") not backed by your output.

## Output (≤ 12 lines, nothing else)

```text
Verdict: pass | fail | needs-PM
Verify: <exit code(s)>; <key value lines, shortened>
Checks: <ok/FAIL per step on one line>
Scope: <unexpected or out-of-scope files, or "clean">
Risk: <flagged areas with file:line, or "none">
Samples: <up to 5 lines, or "n/a">
Report mismatch: <claims not backed by output, or "none">
```

`pass` = Verify and checks exit 0, scope clean, no risk flags, no mismatch. Any failure → `fail`. Risk flags or unexpected files with everything else green → `needs-PM`.
