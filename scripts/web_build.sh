#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
source scripts/web_facade.sh
npm --prefix web ci --no-audit --no-fund --loglevel=error
npm --prefix web run build
echo "web build complete"
