from __future__ import annotations

import json
import os
import shutil
import stat
import subprocess
from pathlib import Path

import pytest


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


def test_code_delta_handles_binary_and_non_ascii_paths(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    (repo / "image.png").write_bytes(b"\x89PNG\x00old")
    write(repo / "bài 1.py", "def old():\n    return 1\n")
    commit_all(repo, "base files")
    (repo / "image.png").write_bytes(b"\x89PNG\x00new\xff")
    write(repo / "bài 1.py", "def new():\n    return 2\n")
    commit_all(repo, "change files")

    result = run("python3", SCRIPTS / "code_delta.py", "HEAD^", cwd=repo)

    assert result.returncode == 0, result.stderr
    binary_line = next(line for line in result.stdout.splitlines() if "image.png" in line)
    assert not any(symbol in binary_line for symbol in ("+old", "+new", "~"))
    unicode_line = next(line for line in result.stdout.splitlines() if "bài 1.py" in line)
    assert "+new" in unicode_line and "−old" in unicode_line


def test_code_delta_reads_before_symbols_from_merge_base(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    write(repo / "a.py", "def bar():\n    return 1\n")
    commit_all(repo, "shared base")
    git(repo, "switch", "-c", "task/X1")
    write(repo / "a.py", "def bar():\n    return 2\n")
    commit_all(repo, "task change")
    git(repo, "switch", "main")
    write(repo / "a.py", "def bar():\n    return 1\n\ndef main_only():\n    return 3\n")
    commit_all(repo, "main advanced")
    git(repo, "switch", "task/X1")

    result = run("python3", SCRIPTS / "code_delta.py", "main", cwd=repo)

    assert result.returncode == 0, result.stderr
    line = next(line for line in result.stdout.splitlines() if "a.py" in line)
    assert "~bar" in line
    assert "main_only" not in line


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
    if not state.exists(): raise SystemExit(1)
    if 'state,headRefOid' in args: print(os.environ.get('FAKE_GH_PR_STATE', 'OPEN') + ' ' + os.environ.get('FAKE_GH_PR_HEAD', ''))
    elif 'state' in args: print(os.environ.get('FAKE_GH_PR_STATE', 'OPEN'))
    else: print('https://example.test/pr/1')
elif args[:2] == ['pr', 'create']:
    state.touch(); print('https://example.test/pr/1')
elif args[:2] == ['pr', 'edit']:
    print('edited')
elif args[:2] == ['repo', 'view']:
    print('owner/repo')
elif args[:1] == ['api']:
    print('{"html_url":"https://example.test/review/2"}')
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
    brief = "# X1 — Useful title\n\n## Goal\nShip the useful flow.\n\n## Acceptance (tests in temp repos)\n- It works.\n"
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
    call_lines = calls.splitlines()
    assert sum(line.startswith("pr create ") for line in call_lines) == 1
    assert sum(line.startswith("pr edit ") for line in call_lines) == 1
    assert git(repo, "ls-remote", "origin", "task/X1").stdout.strip()


def test_task_pr_validates_brief_before_mutation_or_check(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_pr.sh", "code_delta.py")
    commit_all(repo, "install scripts")
    add_origin(repo, tmp_path)
    git(repo, "switch", "-c", "task/X1")
    head = git(repo, "rev-parse", "HEAD").stdout.strip()
    marker = tmp_path / "check-ran"
    env = os.environ | {"TASK_PR_CHECK_CMD": f"touch {marker}"}

    result = run(repo / "scripts/task_pr.sh", "X1", cwd=repo, env=env)

    assert result.returncode != 0
    assert "brief history" in result.stderr
    assert git(repo, "rev-parse", "HEAD").stdout.strip() == head
    assert not marker.exists()
    assert not git(repo, "ls-remote", "origin", "task/X1").stdout.strip()


@pytest.mark.parametrize("pr_state", ["MERGED", "CLOSED"])
def test_task_pr_refuses_finished_pr_before_removal_or_push(tmp_path: Path, pr_state: str):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_pr.sh", "code_delta.py")
    commit_all(repo, "install scripts")
    add_origin(repo, tmp_path)
    git(repo, "switch", "-c", "task/X1")
    write(repo / "docs/tasks/X1.md", "# X1 — Test\n\n## Goal\nSafe.\n")
    commit_all(repo, "X1: brief")
    bin_dir = tmp_path / "bin"
    bin_dir.mkdir()
    fake_gh(bin_dir)
    state = tmp_path / "gh.state"
    state.touch()
    env = os.environ | {
        "PATH": f"{bin_dir}:{os.environ['PATH']}",
        "FAKE_GH_LOG": str(tmp_path / "gh.log"),
        "FAKE_GH_STATE": str(state),
        "FAKE_GH_PR_STATE": pr_state,
    }

    result = run(repo / "scripts/task_pr.sh", "X1", cwd=repo, env=env)

    assert result.returncode != 0
    assert pr_state in result.stderr
    assert (repo / "docs/tasks/X1.md").exists()
    assert not git(repo, "ls-remote", "origin", "task/X1").stdout.strip()


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


def test_pr_review_handles_header_like_content_and_unicode_path(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    write(repo / "bài 1.py", "first\n--old\nkeep\n")
    commit_all(repo, "base")
    git(repo, "switch", "-c", "task/X1")
    write(repo / "bài 1.py", "first\n++new\nadded\nkeep\n")
    commit_all(repo, "change tricky lines")
    write(repo / "findings.txt", "bài 1.py:2 — first\nbài 1.py:3 — second\n")

    result = run("python3", SCRIPTS / "pr_review.py", "1", "findings.txt", "--dry-run", cwd=repo)

    assert result.returncode == 0, result.stderr
    payload = json.loads(result.stdout)
    assert [(item["path"], item["line"]) for item in payload["comments"]] == [
        ("bài 1.py", 2),
        ("bài 1.py", 3),
    ]


def test_pr_review_posts_once_and_prints_only_review_url(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    git(repo, "switch", "-c", "task/X1")
    write(repo / "new.py", "value = 1\n")
    commit_all(repo, "new file")
    write(repo / "findings.txt", "new.py:1 — note\n")
    bin_dir = tmp_path / "bin"
    bin_dir.mkdir()
    fake_gh(bin_dir)
    log = tmp_path / "gh.log"
    env = os.environ | {
        "PATH": f"{bin_dir}:{os.environ['PATH']}",
        "FAKE_GH_LOG": str(log),
        "FAKE_GH_STATE": str(tmp_path / "gh.state"),
    }

    result = run("python3", SCRIPTS / "pr_review.py", "7", "findings.txt", cwd=repo, env=env)

    assert result.returncode == 0, result.stderr
    assert result.stdout.strip() == "https://example.test/review/2"
    assert sum(line.startswith("api ") for line in log.read_text().splitlines()) == 1


def test_task_done_keeps_unmerged_branch(tmp_path: Path):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_done.sh")
    commit_all(repo, "install task-done")
    add_origin(repo, tmp_path)
    git(repo, "switch", "-c", "task/X1")
    write(repo / "feature.txt", "unmerged\n")
    commit_all(repo, "unmerged work")
    git(repo, "push", "-u", "origin", "task/X1")
    bin_dir = tmp_path / "bin"
    bin_dir.mkdir()
    fake_gh(bin_dir)
    state = tmp_path / "gh.state"
    state.touch()
    env = os.environ | {
        "PATH": f"{bin_dir}:{os.environ['PATH']}",
        "FAKE_GH_LOG": str(tmp_path / "gh.log"),
        "FAKE_GH_STATE": str(state),
        "FAKE_GH_PR_STATE": "OPEN",
    }

    result = run(repo / "scripts/task_done.sh", "X1", cwd=repo, env=env)

    assert result.returncode != 0
    assert "not merged" in result.stderr
    assert "task/X1" in git(repo, "branch", "--list", "task/X1").stdout


@pytest.mark.parametrize("extra_local_commit", [False, True])
def test_task_done_deletes_branch_for_squash_merged_pr(tmp_path: Path, extra_local_commit: bool):
    repo = init_repo(tmp_path / "repo")
    install_scripts(repo, "task_done.sh")
    commit_all(repo, "install task-done")
    add_origin(repo, tmp_path)
    git(repo, "switch", "-c", "task/X1")
    write(repo / "feature.txt", "squashed elsewhere\n")
    commit_all(repo, "task work")
    git(repo, "push", "-u", "origin", "task/X1")
    bin_dir = tmp_path / "bin"
    bin_dir.mkdir()
    fake_gh(bin_dir)
    state = tmp_path / "gh.state"
    state.touch()
    env = os.environ | {
        "PATH": f"{bin_dir}:{os.environ['PATH']}",
        "FAKE_GH_LOG": str(tmp_path / "gh.log"),
        "FAKE_GH_STATE": str(state),
        "FAKE_GH_PR_STATE": "MERGED",
        "FAKE_GH_PR_HEAD": git(repo, "rev-parse", "task/X1").stdout.strip(),
    }
    if extra_local_commit:  # work committed after the PR was merged must survive
        write(repo / "later.txt", "not in the PR\n")
        commit_all(repo, "later work")

    result = run(repo / "scripts/task_done.sh", "X1", cwd=repo, env=env)

    if extra_local_commit:
        assert result.returncode != 0
        assert "task/X1" in git(repo, "branch", "--list", "task/X1").stdout
    else:
        assert result.returncode == 0, result.stderr
        assert not git(repo, "branch", "--list", "task/X1").stdout.strip()


def test_repository_documents_task_branch_and_recipes():
    agents = (ROOT / "AGENTS.md").read_text(encoding="utf-8")
    justfile = (ROOT / "justfile").read_text(encoding="utf-8")
    workflow = (ROOT / "docs/workflow.md").read_text(encoding="utf-8")
    tasks_readme = (ROOT / "docs/tasks/README.md").read_text(encoding="utf-8")
    assert "task/<ID>" in agents
    assert "Leave your changes uncommitted" in agents
    assert "Do not commit unless the task says so." not in agents
    assert "worker leaves changes uncommitted" in workflow
    assert "PM verifies and commits the brief's `Owns:` paths" in workflow
    assert "worker leaves changes uncommitted" in tasks_readme
    assert "PM verifies and commits the brief's `Owns:` paths" in tasks_readme
    for recipe in ("task-start ID:", "task-pr ID:", "task-done ID:", "code-delta:"):
        assert recipe in justfile
