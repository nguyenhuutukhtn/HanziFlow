# T-PR-fix2 — Apply the DeepSeek review of PR #1
Suggested agent: see PM launch line · Size: S–M · Device check: none
Owns: same as `docs/tasks/T-PR.md` · Branch: `task/T-PR` (checked out). Leave changes uncommitted; no `check.sh`, no push.

## Findings to fix (all 11 in `build/review/T-PR.deepseek.txt`, also inline on PR #1)
1. **Data loss:** `task_done.sh` — `git branch -d` passes for a pushed-but-unmerged branch (upstream set).
   Delete only when `task/<ID>` is an ancestor of `origin/main` after fetch, or `gh pr view task/<ID> --json state` is `MERGED`
   (squash merges are not ancestors: accept MERGED); otherwise refuse, exit 1. Test: push the branch, PR not merged → branch kept.
2. `task_pr.sh`: validate the brief history before running check.sh or committing anything.
3. `task_pr.sh`: refuse (exit 1, no push) when `gh pr view task/<ID> --json state` is MERGED or CLOSED.
4. `task_pr.sh` `section()`: match headings by prefix (`## Acceptance (tests …)` must work). Test with that heading.
5. `code_delta.py`: binary files → counts only (no decode crash); `-c core.quotePath=false` for non-ASCII paths;
   symbols "before" from `git merge-base base HEAD`, not the base tip. Tests: a `.png` change, a path `bài 1.py`, main advanced after branching.
6. `pr_review.py`: parse the diff with hunk state (`diff --git`/`@@`), so `+++`/`---` content lines don't break numbering;
   strip the trailing tab and quoting from `+++ b/` paths (`core.quotePath=false`). Print only the review URL (`html_url`), not the JSON.
7. Tests: second `task_pr` run asserts exactly one `pr create`, one `pr edit`, and `git ls-remote origin task/X1` non-empty.
8. `docs/workflow.md:40` and `docs/tasks/README.md:5`: worker leaves changes uncommitted; PM commits `Owns:` paths.

## Verify
```bash
source .venv/bin/activate
pytest -q -p no:warnings tests/test_task_scripts.py     # all pass, ≥ 15 tests
python3 scripts/code_delta.py main | head -8
```
Report which test covers each numbered finding.
