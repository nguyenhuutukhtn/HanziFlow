from __future__ import annotations

import json
import os
import shutil
import stat
import subprocess
from pathlib import Path


ROOT = Path(__file__).parents[1]
SCRIPTS = ROOT / "scripts"


def run(*args: str | Path, cwd: Path, env: dict[str, str] | None = None):
    return subprocess.run(
        [str(arg) for arg in args], cwd=cwd, env=env, text=True, capture_output=True
    )


def git(repo: Path, *args: str, check: bool = True):
    result = run("git", *args, cwd=repo)
    if check:
        assert result.returncode == 0, result.stderr
    return result


def write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def init_repo(path: Path) -> Path:
    path.mkdir()
    git(path, "init", "-b", "main")
    git(path, "config", "user.name", "Task Tests")
    git(path, "config", "user.email", "tasks@example.test")
    write(path / ".gitignore", "build/\n")
    write(path / "README.md", "initial\n")
    git(path, "add", ".")
    git(path, "commit", "-m", "initial")
    return path


def commit_all(repo: Path, message: str) -> None:
    git(repo, "add", "-A")
    git(repo, "commit", "-m", message)


def install_scripts(repo: Path, *names: str) -> None:
    target = repo / "scripts"
    target.mkdir(exist_ok=True)
    for name in names:
        copied = target / name
        shutil.copy2(SCRIPTS / name, copied)
        copied.chmod(copied.stat().st_mode | stat.S_IXUSR)


def add_origin(repo: Path, tmp_path: Path) -> Path:
    bare = tmp_path / "origin.git"
    git(tmp_path, "init", "--bare", str(bare))
    git(repo, "remote", "add", "origin", str(bare))
    git(repo, "push", "-u", "origin", "main")
    return bare


def test_code_delta_reports_python_symbols_and_hides_data_symbols(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    write(repo / "a.py", "def old():\n    return 1\n\ndef bar():\n    return 1\n")
    write(repo / "lessons/x.json", '{"old": 1}\n')
    commit_all(repo, "base files")
    write(repo / "a.py", "def foo():\n    return 2\n\ndef bar():\n    return 3\n")
    write(repo / "lessons/x.json", '{"new": 2}\n')
    commit_all(repo, "change symbols")

    result = run("python3", SCRIPTS / "code_delta.py", "HEAD^", cwd=repo)

    assert result.returncode == 0, result.stderr
    python_line = next(line for line in result.stdout.splitlines() if "M a.py" in line)
    assert all(symbol in python_line for symbol in ("+foo", "−old", "~bar"))
    json_line = next(line for line in result.stdout.splitlines() if "lessons/x.json" in line)
    assert "+1 −1" in json_line
    assert not any(symbol in json_line for symbol in ("+new", "−old", "~"))


def test_code_delta_reports_added_kotlin_symbols(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    write(repo / "B.kt", "package demo\n")
    commit_all(repo, "kotlin base")
    write(repo / "B.kt", "package demo\n\nfun play() = Unit\nclass Queue\n")
    commit_all(repo, "kotlin additions")

    result = run("python3", SCRIPTS / "code_delta.py", "HEAD^", cwd=repo)

    assert result.returncode == 0, result.stderr
    line = next(line for line in result.stdout.splitlines() if "M B.kt" in line)
    assert "+play" in line
    assert "+Queue" in line


def test_task_start_refuses_dirty_tracked_tree(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_start.sh")
    write(repo / "docs/tasks/X1.md", "# X1 — Test\n")
    write(repo / "README.md", "dirty\n")

    result = run(repo / "scripts/task_start.sh", "X1", cwd=repo)

    assert result.returncode != 0
    assert "dirty" in result.stderr


def test_task_start_refuses_non_main_branch(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_start.sh")
    git(repo, "switch", "-c", "other")
    write(repo / "docs/tasks/X1.md", "# X1 — Test\n")

    result = run(repo / "scripts/task_start.sh", "X1", cwd=repo)

    assert result.returncode != 0
    assert "main" in result.stderr


def test_task_start_commits_only_brief_on_new_branch(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_start.sh")
    commit_all(repo, "install task-start")
    write(repo / "docs/tasks/X1.md", "# X1 — Test\n")

    result = run(repo / "scripts/task_start.sh", "X1", cwd=repo)

    assert result.returncode == 0, result.stderr
    assert git(repo, "branch", "--show-current").stdout.strip() == "task/X1"
    assert git(repo, "log", "-1", "--format=%s").stdout.strip() == "X1: brief"
    assert git(repo, "show", "--pretty=", "--name-only", "HEAD").stdout.splitlines() == [
        "docs/tasks/X1.md"
    ]


def fake_gh(path: Path) -> Path:
    script = path / "gh"
    write(
        script,
        """#!/usr/bin/env python3
import os, pathlib, sys
log = pathlib.Path(os.environ['FAKE_GH_LOG'])
stdin = sys.stdin.read()
with log.open('a', encoding='utf-8') as out:
    out.write(' '.join(sys.argv[1:]) + '\\n')
    if stdin:
        out.write('STDIN ' + stdin + '\\n')
state = pathlib.Path(os.environ['FAKE_GH_STATE'])
args = sys.argv[1:]
if args[:2] == ['pr', 'view']:
    if state.exists(): print('https://example.test/pr/1')
    else: raise SystemExit(1)
elif args[:2] == ['pr', 'create']:
    state.touch(); print('https://example.test/pr/1')
elif args[:2] == ['pr', 'edit']:
    print('edited')
else:
    raise SystemExit('unexpected gh command: ' + ' '.join(args))
""",
    )
    script.chmod(0o755)
    return script


def test_task_pr_pushes_removes_brief_builds_body_and_edits_existing_pr(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_pr.sh", "code_delta.py")
    commit_all(repo, "install scripts")
    add_origin(repo, tmp_path)
    git(repo, "switch", "-c", "task/X1")
    brief = "# X1 — Useful title\n\n## Goal\nShip the useful flow.\n\n## Acceptance\n- It works.\n"
    write(repo / "docs/tasks/X1.md", brief)
    commit_all(repo, "X1: brief")
    write(repo / "work.py", "def delivered():\n    return True\n")
    commit_all(repo, "X1: implementation")
    bin_dir = tmp_path / "bin"
    bin_dir.mkdir()
    fake_gh(bin_dir)
    log = tmp_path / "gh.log"
    env = os.environ | {
        "PATH": f"{bin_dir}:{os.environ['PATH']}",
        "FAKE_GH_LOG": str(log),
        "FAKE_GH_STATE": str(tmp_path / "gh.state"),
        "TASK_PR_CHECK_CMD": "printf 'ok fake\\n'",
    }

    first = run(repo / "scripts/task_pr.sh", "X1", cwd=repo, env=env)
    second = run(repo / "scripts/task_pr.sh", "X1", cwd=repo, env=env)

    assert first.returncode == 0, first.stderr
    assert second.returncode == 0, second.stderr
    assert first.stdout.strip() == "https://example.test/pr/1"
    assert not (repo / "docs/tasks/X1.md").exists()
    assert git(repo, "log", "-1", "--format=%s").stdout.strip() == "X1: remove brief"
    assert git(repo, "status", "--porcelain").stdout == ""
    body = (repo / "build/pr/X1.md").read_text(encoding="utf-8")
    assert all(f"## {heading}" in body for heading in ("Goal", "Acceptance", "Worker report", "Code map delta", "check.sh"))
    assert "Ship the useful flow." in body
    assert "ok fake" in body
    calls = log.read_text(encoding="utf-8")
    assert "pr create --base main --head task/X1" in calls
    assert "pr edit task/X1" in calls


def test_pr_review_dry_run_inlines_only_added_right_side_line(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    write(repo / "a.py", "first\nsecond\n")
    commit_all(repo, "base")
    git(repo, "switch", "-c", "task/X1")
    write(repo / "a.py", "first\nadded\nsecond\n")
    commit_all(repo, "add line")
    write(repo / "findings.txt", "a.py:2 — inline\na.py:1 — body only\nGeneral note\n")

    result = run(
        "python3", SCRIPTS / "pr_review.py", "1", "findings.txt", "--reviewer", "Ada", "--dry-run", cwd=repo
    )

    assert result.returncode == 0, result.stderr
    payload = json.loads(result.stdout)
    assert payload["comments"] == [
        {"path": "a.py", "line": 2, "side": "RIGHT", "body": "inline"}
    ]
    assert payload["body"].startswith("**Ada review**")
    assert "a.py:1 — body only" in payload["body"]
    assert "General note" in payload["body"]


def test_task_done_keeps_unmerged_branch(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_done.sh")
    commit_all(repo, "install task-done")
    add_origin(repo, tmp_path)
    git(repo, "switch", "-c", "task/X1")
    write(repo / "feature.txt", "unmerged\n")
    commit_all(repo, "unmerged work")

    result = run(repo / "scripts/task_done.sh", "X1", cwd=repo)

    assert result.returncode != 0
    assert "not merged" in result.stderr
    assert "task/X1" in git(repo, "branch", "--list", "task/X1").stdout


def test_repository_documents_task_branch_and_recipes():
    agents = (ROOT / "AGENTS.md").read_text(encoding="utf-8")
    justfile = (ROOT / "justfile").read_text(encoding="utf-8")
    assert "task/<ID>" in agents
    assert "Leave your changes uncommitted" in agents
    assert "Do not commit unless the task says so." not in agents
    for recipe in ("task-start ID:", "task-pr ID:", "task-done ID:", "code-delta:"):
        assert recipe in justfile
