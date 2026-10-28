# CH-forge — this repo uses agent-kit's PR commands
Suggested agent: codex/gpt-5.6-luna (low) · Session: new · Size: S · Device check: agent (Verify below)
Wave: AK1 (see agent-kit/docs/tasks/WAVE-AK1.md) · Owns: `.agent-kit.toml`, `scripts/task_start.sh`, `scripts/task_pr.sh`, `scripts/task_done.sh`, `scripts/pr_review.py`, `justfile`, `AGENTS.md`, `docs/tasks/README.md`, `docs/workflow.md`, `docs/CODEMAP.md` · Depends on: AK-forge merged + `bash ~/Documents/source_code/personal/agent-kit/install.sh` run · Checkpoint after: no

## Goal
Drop the repo's own PR scripts; `agent-task` / `agent-review` from agent-kit do the same job, driven by
`.agent-kit.toml`.

## Read only
`~/Documents/source_code/personal/agent-kit/docs/config.md`; `grep -rn 'task_start\|task_pr\|task_done\|pr_review' --include='*.md' --include=justfile --include='*.sh' .` (skip `build/`, `publish/`).

## Do
1. `.agent-kit.toml` (committed): forge github, base main, prefix `task/`, `check = "scripts/check.sh"`,
   `code_delta = "scripts/code_delta.py main"`, `regen_paths = ["docs/CODEMAP.md"]`, pm_dir `docs`,
   attribution default, allowed codex/deepseek/qwen/claude, review inline / "AI review".
2. `git rm` `scripts/task_start.sh`, `task_pr.sh`, `task_done.sh`, `pr_review.py`.
3. Replace every reference found by the grep with the agent-task/agent-review form
   (`scripts/task_start.sh X` → `agent-task start X`, `scripts/pr_review.py <PR> <file> --reviewer N` →
   `agent-review <PR> <file> --reviewer N`). Leave `.claude/skills/pm-session/SKILL.md` alone (CH-pm).
4. `scripts/check.sh` → CODEMAP regenerated.

## Acceptance
- `agent-kit config repo.check` prints `scripts/check.sh`; `agent-kit config --dump` exits 0.
- The grep above finds nothing outside `.claude/skills/pm-session/` and `docs/token_log.md`/`docs/STATUS.md` history lines.
- `scripts/check.sh` all steps ok.

## Verify
```
source .venv/bin/activate
agent-kit config repo.check && agent-kit config --dump >/dev/null && echo "config: ok"
grep -rln 'task_start\.sh\|task_pr\.sh\|task_done\.sh\|pr_review\.py' --include='*.md' --include=justfile --include='*.sh' . | grep -v -e '^./build/' -e pm-session -e token_log -e STATUS || echo "refs: ok"
scripts/check.sh
```

## Out of scope
pm-session overlay (CH-pm), agent-kit code.
