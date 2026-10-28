# T-PR-fix — Finish T-PR (first attempt delivered ~5% of Acceptance)
Suggested agent: codex/gpt-5.6-sol (medium), fresh session · Size: M · Device check: none
Owns: same as `docs/tasks/T-PR.md` · Branch: `task/T-PR` (already checked out; the first attempt's uncommitted files are there — rewrite them freely)

## What went wrong
The first run hit the sandbox (`.git/index.lock` not writable, `check.sh` Gradle/web blocked) and
stopped: scripts are 7–15 line stubs, `tests/test_task_scripts.py` has 2 trivial tests. Every
Acceptance bullet of `docs/tasks/T-PR.md` is still open.

## Changes to T-PR.md (these override it)
- **Do NOT commit and do NOT run `scripts/check.sh`** (sandbox blocks both). Leave all changes
  uncommitted on `task/T-PR`; the PM commits the Owns paths after Verify and runs `check.sh`.
- Tests create their own temp git repos under pytest's `tmp_path` (that is writable); never touch this repo's `.git`.
- Do step 7 with this text instead: "Code tasks run on branch `task/<ID>` (PM creates it with
  `scripts/task_start.sh`). Leave your changes uncommitted; the PM commits the paths in your brief's
  `Owns:` after Verify. Never commit on `main`, never push. Content tasks: do not commit unless the brief says so."
- `task_pr.sh` therefore still requires a clean tree (PM has committed by then).

## Do
Everything in `docs/tasks/T-PR.md` "Do" 1–8 and every "Acceptance" bullet, with one test per bullet.

## Verify
```bash
source .venv/bin/activate
pytest -q -p no:warnings tests/test_task_scripts.py      # expect ≥ 8 tests, all pass
python3 scripts/code_delta.py main | head -20
grep -n "task/<ID>" AGENTS.md
```
Report: the pytest count line and the code_delta output.
