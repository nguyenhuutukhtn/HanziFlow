#!/usr/bin/env bash
set -euo pipefail

id=${1:?usage: task_start.sh ID}
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$root"

[[ $(git branch --show-current) == main ]] || {
    echo "refusing: task-start must run on main" >&2
    exit 1
}
git diff --quiet && git diff --cached --quiet || {
    echo "refusing: tracked working tree is dirty" >&2
    exit 1
}
[[ -f "docs/tasks/$id.md" ]] || {
    echo "missing brief: docs/tasks/$id.md" >&2
    exit 1
}

if ! git pull --ff-only; then
    echo "warning: git pull skipped (no network or remote)" >&2
fi
git switch -c "task/$id"
git add -- "docs/tasks/$id.md"
git commit -m "$id: brief"
