#!/usr/bin/env bash
# Run a worker agent (codex / deepcode) with a live log, a wall-clock cap and a
# loop watchdog, so a run that goes sideways dies instead of burning quota.
#
#   bash scripts/agent_run.sh <ID> [--minutes N] -- <command...>
#
# Live log: build/logs/<ID>.log   (follow it with: just watch <ID>)
# Final message (codex -o): build/logs/<ID>.out
# Watchdog verdict, if it fired: build/logs/<ID>.killed
set -uo pipefail

ID="${1:?usage: agent_run.sh <ID> [--minutes N] -- <command...>}"
shift
MINUTES=20
if [[ "${1:-}" == "--minutes" ]]; then MINUTES="$2"; shift 2; fi
[[ "${1:-}" == "--" ]] && shift
[[ $# -gt 0 ]] || { echo "no command given"; exit 2; }

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOGS="$ROOT/build/logs"
mkdir -p "$LOGS"
LOG="$LOGS/$ID.log"
KILLED="$LOGS/$ID.killed"
rm -f "$KILLED"

"$@" >"$LOG" 2>&1 &
PID=$!
echo "$ID: pid $PID, cap ${MINUTES}m, log $LOG"

# Watchdog: wall-clock cap, plus "the same line repeated 25+ times in the last
# 200 lines" as a cheap stand-in for a stuck loop.
(
    deadline=$(( $(date +%s) + MINUTES * 60 ))
    while kill -0 "$PID" 2>/dev/null; do
        sleep 20
        if (( $(date +%s) > deadline )); then
            echo "wall-clock cap ${MINUTES}m reached" >"$KILLED"
            pkill -P "$PID" 2>/dev/null
            kill -TERM "$PID" 2>/dev/null
            break
        fi
        repeats=$(tail -200 "$LOG" 2>/dev/null | grep -v '^[[:space:]]*$' | sort | uniq -c | sort -rn | head -1 | awk '{print $1}')
        if [[ -n "${repeats:-}" ]] && (( repeats >= 25 )); then
            echo "loop watchdog: one line repeated ${repeats}x in the last 200 log lines" >"$KILLED"
            pkill -P "$PID" 2>/dev/null
            kill -TERM "$PID" 2>/dev/null
            break
        fi
    done
) &
WATCHDOG=$!

wait "$PID"
STATUS=$?
kill "$WATCHDOG" 2>/dev/null
if [[ -f "$KILLED" ]]; then
    echo "$ID: KILLED — $(cat "$KILLED")"
    exit 124
fi
echo "$ID: exit $STATUS"
exit "$STATUS"
