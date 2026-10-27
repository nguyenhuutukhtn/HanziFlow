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

# Run registry for the editor extension (tools/agent-monitor): atomic write.
RUNFILE="$LOGS/$ID.run.json"
AGENT="$(basename "$1")"
STARTED="$(date +%Y-%m-%dT%H:%M:%S%z)"
json_esc() { local v="${1//\\/\\\\}"; v="${v//\"/\\\"}"; printf '%s' "$v"; }
write_run() { # status [ended]
    local tmp="$RUNFILE.tmp.$$" ended=""
    [[ -n "${2:-}" ]] && ended=",\"ended\":\"$2\""
    printf '{"id":"%s","agent":"%s","pid":%s,"started":"%s","log":"%s","status":"%s"%s}\n' \
        "$(json_esc "$ID")" "$(json_esc "$AGENT")" "$PID" "$STARTED" "$(json_esc "$LOG")" "$(json_esc "$1")" "$ended" >"$tmp" \
        && mv "$tmp" "$RUNFILE"
}
write_run running
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
    write_run "killed: $(head -1 "$KILLED")" "$(date +%Y-%m-%dT%H:%M:%S%z)"
    echo "$ID: KILLED — $(cat "$KILLED")"
    exit 124
fi
write_run "exit $STATUS" "$(date +%Y-%m-%dT%H:%M:%S%z)"
echo "$ID: exit $STATUS"
exit "$STATUS"
