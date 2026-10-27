#!/usr/bin/env bash
# Fake worker for `just agent-fake`: ~15 s of mixed codex-style events, no quota.
e() { printf '%s\n' "$1"; sleep 1; }
e '{"type":"thread.started","thread_id":"fake"}'
e '{"type":"turn.started"}'
e '{"type":"item.completed","item":{"type":"reasoning","text":"Reading the brief and planning the smallest change that satisfies the acceptance list."}}'
e 'plain line from a non-JSON worker'
e '{"type":"item.started","item":{"type":"command_execution","command":"ls kmp/shared"}}'
e '{"type":"item.completed","item":{"type":"command_execution","command":"ls kmp/shared","exit_code":0}}'
e '{"type":"item.completed","item":{"type":"file_change","changes":[{"path":"kmp/shared/Foo.kt","kind":"update"},{"path":"kmp/shared/Bar.kt","kind":"add"}]}}'
e '{"type":"item.started","item":{"type":"command_execution","command":"scripts/check.sh"}}'
e '{"type":"item.completed","item":{"type":"command_execution","command":"scripts/check.sh","exit_code":1}}'
e '{"type":"some.unknown.event","note":"hello from an unknown event"}'
e 'ASK-PM: should the button be primary or secondary?'
e '{"type":"item.completed","item":{"type":"agent_message","text":"All done, tests pass."}}'
e '{"type":"turn.completed","usage":{"input_tokens":1234,"output_tokens":567}}'
e 'not json {broken'
exit 0
