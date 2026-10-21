#!/usr/bin/env bash
# Print remaining AI quota in a few lines (PM checks it before assigning a task).
# Codex: last rate_limits event in the newest ~/.codex session log.
# DeepSeek: balance via API (key read from ~/.deepcode/settings.json, never printed).
# Claude: ~/.claude/quota.json if something writes it, else a hint to run /usage.
set -uo pipefail

python3 - <<'EOF'
import glob, json, os, time, urllib.request

def when(ts):
    return time.strftime("%a %d/%m %H:%M", time.localtime(ts)) if ts else "?"

def find_limits(node):
    if isinstance(node, dict):
        if isinstance(node.get("rate_limits"), dict):
            return node["rate_limits"]
        node = list(node.values())
    if isinstance(node, list):
        for child in node:
            found = find_limits(child)
            if found:
                return found
    return None

logs = sorted(glob.glob(os.path.expanduser("~/.codex/sessions/*/*/*/*.jsonl")), key=os.path.getmtime)
codex = None
for path in reversed(logs[-5:]):
    for line in reversed(open(path, encoding="utf-8").read().splitlines()):
        if '"rate_limits"' in line:
            found = find_limits(json.loads(line))
            if found:
                codex = (found, os.path.getmtime(path))
                break
    if codex:
        break
if codex:
    rl, seen = codex
    p, s = rl.get("primary") or {}, rl.get("secondary") or {}
    print(f"codex   5h {p.get('used_percent','?')}% (reset {when(p.get('resets_at'))}) · week {s.get('used_percent','?')}% (reset {when(s.get('resets_at'))}) · as of {when(seen)}")
else:
    print("codex   no session log yet")

try:
    env = json.load(open(os.path.expanduser("~/.deepcode/settings.json"))).get("env", {})
    req = urllib.request.Request(env.get("BASE_URL", "https://api.deepseek.com").rstrip("/") + "/user/balance",
                                 headers={"Authorization": "Bearer " + env["API_KEY"]})
    info = json.load(urllib.request.urlopen(req, timeout=10))["balance_infos"][0]
    print(f"deepseek balance {info['total_balance']} {info['currency']}")
except Exception as exc:
    print(f"deepseek balance unavailable ({type(exc).__name__})")

claude = os.path.expanduser("~/.claude/quota.json")
if os.path.exists(claude):
    print("claude  " + open(claude, encoding="utf-8").read().strip()[:200])
else:
    print("claude  run /usage in Claude Code (no local quota file)")
EOF
