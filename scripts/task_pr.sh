#!/usr/bin/env bash
set -euo pipefail

id=${1:?usage: task_pr.sh ID}
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$root"

[[ $(git branch --show-current) == "task/$id" ]] || {
    echo "refusing: must be on task/$id" >&2
    exit 1
}
[[ -z $(git status --porcelain) ]] || {
    echo "refusing: working tree is dirty" >&2
    exit 1
}

brief_path="docs/tasks/$id.md"
brief_commit=$(git log --reverse --format=%H HEAD -- "$brief_path" | head -n 1)
[[ -n $brief_commit ]] || {
    echo "missing brief history: $brief_path" >&2
    exit 1
}
brief=$(git show "$brief_commit:$brief_path")

pr_state=$(gh pr view "task/$id" --json state --jq .state 2>/dev/null || true)
if [[ $pr_state == MERGED || $pr_state == CLOSED ]]; then
    echo "refusing: PR for task/$id is $pr_state" >&2
    exit 1
fi

# check.sh regenerates docs/CODEMAP.md, which then belongs in the PR. The brief
# and PR state are validated first so a bad rerun cannot mutate local history.
check_output_file=$(mktemp)
if [[ -n ${TASK_PR_CHECK_CMD:-} ]]; then
    bash -c "$TASK_PR_CHECK_CMD" >"$check_output_file" 2>&1 || true
else
    "$root/scripts/check.sh" >"$check_output_file" 2>&1 || true
fi
if [[ $(git status --porcelain) == " M docs/CODEMAP.md" ]]; then
    git commit --quiet -m "$id: regenerate CODEMAP" -- docs/CODEMAP.md
fi

removed=()
for path in "$brief_path" $(git ls-files "docs/tasks/${id}-fix*.md"); do
    if git cat-file -e "HEAD:$path" 2>/dev/null; then
        git rm --quiet -- "$path"
        removed+=("$path")
    fi
done
if ((${#removed[@]})); then
    git commit --quiet -m "$id: remove brief"
fi
if ! push_output="$(git push --quiet -u origin "task/$id" 2>&1)"; then
    printf '%s\n' "$push_output" >&2
    exit 1
fi

section() {
    local name=$1
    printf '%s\n' "$brief" | awk -v heading="## $name" '
        index($0, heading) == 1 { inside=1; next }
        inside && /^## / { exit }
        inside { print }
    '
}

mkdir -p build/pr
body_file="build/pr/$id.md"
{
    echo "## Goal"
    section Goal
    echo "## Acceptance"
    section Acceptance
    echo "## Worker report"
    echo
    report="$(ls -t "build/logs/$id.out" "build/logs/$id"-fix*.out 2>/dev/null | head -1 || true)"
    if [[ -n "$report" ]]; then
        echo "From \`$report\` (latest run):"
        echo
        cat "$report"
        echo
    else
        echo "(no report file)"
    fi
    echo
    echo "## Code map delta"
    echo
    "$root/scripts/code_delta.py" main
    echo "## check.sh"
    echo
    echo '```'
    cat "$check_output_file"
    echo '```'
} >"$body_file"

heading=${brief%%$'\n'*}
title_text=${heading#* — }
if [[ $title_text == "$heading" ]]; then
    title_text=${heading#* - }
fi
title="$id — $title_text"
if url=$(gh pr view "task/$id" --json url --jq .url 2>/dev/null); then
    gh pr edit "task/$id" --title "$title" --body-file "$body_file" >/dev/null
else
    url=$(gh pr create --base main --head "task/$id" --title "$title" --body-file "$body_file")
fi
printf '%s\n' "$url"
