#!/usr/bin/env bash
set -euo pipefail

id=${1:?usage: task_done.sh ID}
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$root"

git switch main
git pull --ff-only
if ! git branch -d "task/$id"; then
    echo "refusing: task/$id is not merged into main" >&2
    exit 1
fi
git fetch --prune
