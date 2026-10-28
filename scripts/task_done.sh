#!/usr/bin/env bash
set -euo pipefail

id=${1:?usage: task_done.sh ID}
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$root"

git switch main
git pull --ff-only
git fetch --prune

merged=false
if git merge-base --is-ancestor "task/$id" origin/main 2>/dev/null; then
    merged=true
elif [[ $(gh pr view "task/$id" --json state,headRefOid --jq '.state + " " + .headRefOid' 2>/dev/null || true) == "MERGED $(git rev-parse "task/$id")" ]]; then
    merged=true  # squash merge: only when the local tip is exactly what the PR merged
fi
if [[ $merged != true ]]; then
    echo "refusing: task/$id is not merged into main" >&2
    exit 1
fi
git branch --unset-upstream "task/$id" 2>/dev/null || true
git branch -D "task/$id"
