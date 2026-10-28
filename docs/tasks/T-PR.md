# T-PR — Task branches + GitHub PR review flow
Suggested agent: codex/gpt-5.6-luna (low), launched by PM via `agent-codex T-PR` · Session: new · Size: M · Device check: none
Wave: none · Owns: `scripts/task_start.sh`, `scripts/task_pr.sh`, `scripts/task_done.sh`, `scripts/code_delta.py`, `scripts/pr_review.py`, `tests/test_task_scripts.py`, `.gitattributes`, `justfile` (new recipes only), `AGENTS.md` (commit rule only), `docs/tasks/README.md` (lifecycle paragraph only), `docs/workflow.md` (one short section) · Depends on: none · Checkpoint after: yes
Branch: `task/T-PR` (PM created it; commit your work there, never on `main`, never push)

## Goal
Each code task becomes a GitHub PR the user reads and merges on github.com
(repo `nguyenhuutukhtn/HanziFlow`, auto-delete branch on merge is on). The PR description
shows what was asked, what the worker reported, a **code map delta** (which files and which
functions/classes were added, removed or changed), and the `check.sh` result. Reviewers post
findings as inline comments on the PR.

## Read only
- `scripts/check.sh` (output shape: one `ok|FAIL <step>` line per step), `scripts/codemap.py` (style only)
- `docs/tasks/README.md` (template + lifecycle), `AGENTS.md` "Hard limits"
- `justfile` (recipe style)

## Do
1. `scripts/task_start.sh <ID>`: fail unless on `main` with no tracked changes and `docs/tasks/<ID>.md`
   exists; `git pull --ff-only` (skip with a warning when there is no network/remote);
   `git switch -c task/<ID>`; commit only the brief: `git add docs/tasks/<ID>.md`, message `<ID>: brief`.
2. `scripts/code_delta.py [base]` (default base `main`): Markdown for the diff `base...HEAD`, one
   line per changed file, grouped by top-level area (`src/`, `tests/`, `kmp/`, `web/`, `scripts/`,
   `docs/`, other). Each line: status `A|M|D|R`, path, `+added −removed`, then symbols:
   `+name` defined only after, `−name` defined only before, `~name` existing symbol whose body
   changed (from the hunk's enclosing function: use `git diff -U0 --function-context` or hunk
   headers with `.gitattributes` `*.py diff=python`, `*.kt diff=kotlin`). Symbols = Python
   `def`/`class`, Kotlin `fun`/`class`/`object`/`interface`, TS/JS `function`/`const X = (`/`export`.
   Generated or data files (`lessons/*.json`, `docs/CODEMAP.md`, `**/*.lock`, `publish/`)
   show counts only, no symbols. Pure stdlib Python.
3. `scripts/task_pr.sh <ID>`: on branch `task/<ID>`, clean tree required. Last commit
   `git rm docs/tasks/<ID>.md` (+ `<ID>-fix.md` if present), message `<ID>: remove brief`.
   Push `-u origin task/<ID>`. Body (write to `build/pr/<ID>.md`):
   `## Goal` + `## Acceptance` copied from the brief as it was in the first branch commit;
   `## Worker report` from `build/logs/<ID>.out` if present, else "(no report file)";
   `## Code map delta` from `code_delta.py main`; `## check.sh` = output of `scripts/check.sh`
   in a code block (run it; a FAIL line does not stop the PR, it is shown). Title `<ID> — <brief H1 title after the dash>`.
   `gh pr create --base main --head task/<ID>`; if a PR for that head exists, `gh pr edit` the
   body instead. Print only the PR URL.
4. `scripts/pr_review.py <PR number> <findings file> [--reviewer NAME]`: findings file lines
   `path:line — text` (blank lines and other lines go to the review body). Posts ONE review via
   `gh api repos/{owner}/{repo}/pulls/<n>/reviews` with `event=COMMENT`, inline comments for lines
   present on the RIGHT side of the PR diff, and lines not in the diff appended to the body as
   `path:line — text`. Body starts `**<NAME> review**`. `--dry-run` prints the JSON payload instead.
5. `scripts/task_done.sh <ID>`: `git switch main`, `git pull --ff-only`, delete local `task/<ID>`
   with `git branch -d` (refuse if not merged; print why), `git fetch --prune`.
6. `justfile`: `task-start ID`, `task-pr ID`, `task-done ID`, `code-delta`.
7. `AGENTS.md` line 26 becomes: "Code tasks run on branch `task/<ID>` (PM creates it with
   `scripts/task_start.sh`): commit your work there, stage only your own files, never commit on
   `main`, never push. Content tasks: do not commit unless the brief says so."
8. `docs/tasks/README.md` lifecycle paragraph + `docs/workflow.md`: the flow in ≤ 8 lines
   (start → worker commits → PM verify → task_pr → reviewers via pr_review → user merges on web → task_done).

## Acceptance (tests in `tests/test_task_scripts.py`, temp git repos + a fake `gh` on PATH that logs argv and stdin; never call real GitHub)
- `code_delta.py` on a temp repo where `a.py` gains `def foo`, loses `def old`, and `def bar`'s
  body changes, and `lessons/x.json` changes: prints a line containing `M a.py`, `+foo`, `−old`,
  `~bar`, and the json line has counts but no `+`/`−`/`~` symbol entries.
- Kotlin case: `B.kt` adding `fun play()` and a new `class Queue` → `+play`, `+Queue`.
- `task_start.sh` refuses on a dirty tree and on a non-main branch (non-zero exit, message names the reason); on success branch `task/X1` exists with one commit touching only `docs/tasks/X1.md`.
- `task_pr.sh` on the temp repo (bare repo as `origin`): pushes, removes the brief in the last
  commit, fake `gh` receives `pr create` with `--base main --head task/X1`, and the body file has
  the four `##` sections with the brief's Goal text; a second run calls `gh pr edit`, not create.
  Use an env var (e.g. `TASK_PR_CHECK_CMD`) so tests replace `check.sh` with `echo ok fake`.
- `pr_review.py --dry-run` with one finding on an added line and one on an untouched line: payload
  has exactly 1 inline comment (`path`, `line`, `side: RIGHT`) and the other finding in `body`.
- `task_done.sh` refuses to delete an unmerged branch (exit ≠ 0, branch still exists).
- `AGENTS.md` contains `task/<ID>`; no remaining "Do not commit unless the task says so." for code tasks.

## Verify
```bash
source .venv/bin/activate
pytest -q -p no:warnings tests/test_task_scripts.py
python3 scripts/code_delta.py main | head -20     # on this branch: shows your own scripts as A with +symbols
git log --oneline main..HEAD                      # first commit "T-PR: brief"
grep -n "task/<ID>" AGENTS.md
scripts/check.sh
```
Do NOT run `task_pr.sh` for real (PM runs it to open this task's PR). Do NOT push.

## Out of scope
- Real GitHub calls in tests; CI workflows; branch protection.
- Changing `scripts/check.sh` or `codemap.py`.
- `.claude/skills/*` (PM updates its skill).
