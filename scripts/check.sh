#!/usr/bin/env bash
# Full local check: python tests, kmp shared + android unit/architecture tests,
# codemap regeneration, file-size audit. One line per step; full logs in build/logs/.
# Run: ./scripts/check.sh   (also wired to `just check`)
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
LOG_DIR="build/logs"
mkdir -p "$LOG_DIR"

run_step() {
  local name="$1"
  shift
  local log="$LOG_DIR/$name.log"
  if "$@" >"$log" 2>&1; then
    echo "ok      $name"
  else
    echo "FAIL    $name (log: $log)"
    tail -n 30 "$log"
    exit 1
  fi
}

py=python3; [ -x .venv/bin/python3 ] && py=.venv/bin/python3  # agents' shells don't activate the venv
run_step pytest "$py" -m pytest -q -p no:warnings
run_step web bash -c "npm --prefix web run typecheck && npm --prefix web test -- --run"
run_step kmp-shared bash -c "cd kmp && ./gradlew -q :shared:allTests"
# android:testDebugUnitTest also runs the Konsist architecture/layering test
# (kmp/android/src/test/kotlin/com/hanziflow/architecture/LayeringTest.kt).
run_step android-unit bash -c "cd kmp && ./gradlew -q :android:testDebugUnitTest"
run_step codemap python3 scripts/codemap.py

oversize=$(grep '^- .*⚠' docs/CODEMAP.md | sed -E 's/^- `([^`]*)` \[([0-9]+)\].*/\1 (\2)/' | sed 's#.*/##' | paste -sd, - | sed 's/,/, /g' || true)
if [ -n "$oversize" ]; then
  echo "ok      file-size-audit (over 300 lines: $oversize)"
else
  echo "ok      file-size-audit (none over 300 lines)"
fi

# Crammed lines dodge the 300-line audit (W1c: App.tsx 29 lines, 9 KB).
longlines=$(grep -rlE '^.{201,}' web/src kmp/*/src/*/kotlin src --include='*.ts' --include='*.tsx' --include='*.kt' --include='*.py' 2>/dev/null | sed 's#.*/##' | paste -sd, - | sed 's/,/, /g' || true)
if [ -n "$longlines" ]; then
  echo "ok      long-line-audit (lines over 200 chars: $longlines)"
fi

echo "All checks passed."
