#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
bash scripts/web_build.sh
# Always deploy to the Pages production branch, not the current git branch (which makes a preview).
npx wrangler pages deploy web/dist --project-name hanziflow --branch "${PAGES_BRANCH:-hanzi-flow}" --commit-dirty=true
