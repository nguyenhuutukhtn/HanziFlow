#!/usr/bin/env bash
# Worker side of the callback channel. A worker agent that is missing information
# asks here instead of guessing or stopping:
#
#   bash scripts/agent_ask.sh <ID> "question"     # blocks until an answer arrives
#   bash scripts/agent_ask.sh <ID> --inbox        # prints new notes, never blocks
#
# The question goes to build/logs/<ID>.ask; the answer is read from
# build/logs/<ID>.answer (written by `just answer <ID> "..."`). Notes pushed with
# `just tell <ID> "..."` land in build/logs/<ID>.inbox and are printed by --inbox.
set -uo pipefail

ID="${1:?usage: agent_ask.sh <ID> \"question\" | agent_ask.sh <ID> --inbox}"
shift
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOGS="$ROOT/build/logs"
mkdir -p "$LOGS"
INBOX="$LOGS/$ID.inbox"
SEEN="$LOGS/$ID.inbox.seen"

if [[ "${1:-}" == "--inbox" ]]; then
    [[ -f "$INBOX" ]] || { echo "(no notes)"; exit 0; }
    seen=0
    [[ -f "$SEEN" ]] && seen=$(cat "$SEEN")
    total=$(wc -l <"$INBOX" | tr -d ' ')
    if (( total > seen )); then
        tail -n $(( total - seen )) "$INBOX"
        echo "$total" >"$SEEN"
    else
        echo "(no new notes)"
    fi
    exit 0
fi

QUESTION="$*"
ASK="$LOGS/$ID.ask"
ANSWER="$LOGS/$ID.answer"
rm -f "$ANSWER"
printf '%s\n' "$QUESTION" >"$ASK"
echo "ASK-PM: $QUESTION"   # shows up in the live log and in the PM's monitor

# Wait up to 15 minutes for an answer, then say so and let the agent decide.
for _ in $(seq 1 180); do
    if [[ -f "$ANSWER" ]]; then
        rm -f "$ASK"
        cat "$ANSWER"
        exit 0
    fi
    sleep 5
done
rm -f "$ASK"
echo "NO ANSWER within 15 minutes — state your assumption in the report and continue."
exit 0
