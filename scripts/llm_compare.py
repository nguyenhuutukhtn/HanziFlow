#!/usr/bin/env python3
"""Send the same 5 lesson-2 lines to every configured LLM provider and write the answers side by side.

Providers are enabled only when their key is set (env or .env). Stdlib only.

  python3 scripts/llm_compare.py --dry-run          # print the 5 lines + first rendered prompt, no calls
  python3 scripts/llm_compare.py --list-models gemini
  python3 scripts/llm_compare.py                    # run all lines x providers -> docs/reviews/llm_compare.md
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ENV_FILE = ROOT / ".env"
PROMPT_FILE = ROOT / "prompts" / "explain_line.md"
REFERENCE = ROOT / "publish" / "lessons" / "lesson_002" / "v4" / "reference.json"
OUTPUT = ROOT / "docs" / "reviews" / "llm_compare.md"
TIMEOUT_S = 60
PREFERRED_LINE = "大厅在楼里面，很大。"


@dataclass(frozen=True)
class Provider:
    name: str
    key_env: str
    base: str
    model_env: str
    model_default: str | None = None


PROVIDERS = [
    Provider("gemini", "GEMINI_API_KEY", "https://generativelanguage.googleapis.com/v1beta/openai/", "GEMINI_MODEL"),
    Provider("groq", "GROQ_API_KEY", "https://api.groq.com/openai/v1/", "GROQ_MODEL"),
    Provider("deepseek", "DEEPSEEK_API_KEY", "https://api.deepseek.com/v1/", "DEEPSEEK_MODEL", "deepseek-chat"),
]


def load_env(path: Path = ENV_FILE) -> dict[str, str]:
    """Parse KEY=VALUE lines; process environment overrides the file."""
    values: dict[str, str] = {}
    if path.exists():
        for raw in path.read_text(encoding="utf-8").splitlines():
            line = raw.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, _, value = line.partition("=")
            values[key.strip()] = value.strip().strip('"').strip("'")
    for p in PROVIDERS:
        for name in (p.key_env, p.model_env):
            if os.environ.get(name):
                values[name] = os.environ[name]
    return values


def load_prompt(path: Path = PROMPT_FILE) -> tuple[str, str]:
    """Return (system prompt, user template) from the prompt file."""
    text = path.read_text(encoding="utf-8")
    system, _, user = text.partition("---USER---")
    return system.replace("# System", "", 1).strip(), user.strip()


def render_prompt(template: str, values: dict[str, str]) -> str:
    out = template
    for key, value in values.items():
        out = out.replace("{{" + key + "}}", value)
    return out


def pick_lines(reference: dict) -> list[dict]:
    """Pick 5 Chinese lines from the lesson: the 大厅 line plus 4 of different lengths."""
    candidates = []
    for track, lines in reference["tracks"].items():
        for idx, line in enumerate(lines):
            if str(line.get("language", "")).startswith("zh") and line.get("text"):
                candidates.append((track, idx, line))

    target = next((c for c in candidates if c[2]["text"] == PREFERRED_LINE), None)
    if target is None:
        target = next((c for c in candidates if "大厅" in c[2]["text"]), None)
    if target is None:
        raise SystemExit("no line containing 大厅 in lesson 2 reference")

    rest = sorted((c for c in candidates if c[2]["text"] != target[2]["text"]),
                  key=lambda c: (len(c[2]["text"]), c[0], c[1]))
    chosen, seen = [], {target[2]["text"]}
    n = len(rest)
    for q in (0.15, 0.35, 0.55, 0.8):
        if n == 0:
            break
        start = int(q * (n - 1))
        for step in range(n):  # walk forward from the length quantile until a new text appears
            c = rest[(start + step) % n]
            if c[2]["text"] not in seen:
                chosen.append(c)
                seen.add(c[2]["text"])
                break
    picked = sorted([target] + chosen, key=lambda c: (c[0], c[1]))

    out = []
    for track, idx, line in picked[:5]:
        lines = reference["tracks"][track]
        before = [l["text"] for l in lines[max(0, idx - 2):idx] if l.get("text")]
        after = [l["text"] for l in lines[idx + 1:idx + 3] if l.get("text")]
        out.append({
            "track": track,
            "index": idx,
            "hanzi": line["text"],
            "pinyin": line.get("pinyin") or "",
            "meaning_vi": line.get("translation") or "",
            "context_before": " / ".join(before) or "(không có)",
            "context_after": " / ".join(after) or "(không có)",
        })
    return out


def http_json(method: str, url: str, headers: dict[str, str], body: dict | None = None,
              timeout: int = TIMEOUT_S) -> dict:
    data = json.dumps(body).encode("utf-8") if body is not None else None
    headers = {**headers, "User-Agent": "mandarin-course-llm-compare/1.0"}
    req = urllib.request.Request(url, data=data, method=method, headers=headers)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def redact(text: str, secrets: list[str]) -> str:
    for secret in secrets:
        if secret:
            text = text.replace(secret, "<redacted>")
    return text


def enabled_providers(env: dict[str, str]) -> list[tuple[Provider, str, str | None]]:
    """(provider, key, model) for each provider whose key is set. Model may be None (missing)."""
    out = []
    for p in PROVIDERS:
        key = env.get(p.key_env, "")
        if not key:
            continue
        out.append((p, key, env.get(p.model_env) or p.model_default))
    return out


def ask(provider: Provider, key: str, model: str, system: str, user: str) -> str:
    headers = {"Authorization": "Bearer " + key, "Content-Type": "application/json"}
    body = {
        "model": model,
        "messages": [{"role": "system", "content": system}, {"role": "user", "content": user}],
        "temperature": 0.3,
    }
    data = http_json("POST", provider.base + "chat/completions", headers, body, TIMEOUT_S)
    return data["choices"][0]["message"]["content"]


def list_models(provider: Provider, key: str) -> list[str]:
    headers = {"Authorization": "Bearer " + key}
    data = http_json("GET", provider.base + "models", headers, None, TIMEOUT_S)
    return sorted(str(m.get("id")) for m in data.get("data", []))


def run_compare(lines: list[dict], providers: list[tuple[Provider, str, str]], system: str, template: str) -> str:
    keys = [key for _, key, _ in providers]
    out = ["# LLM compare · lesson 2 · 5 lines", ""]
    for line in lines:
        user = render_prompt(template, {
            "hanzi": line["hanzi"], "pinyin": line["pinyin"], "meaning_vi": line["meaning_vi"],
            "context_before": line["context_before"], "context_after": line["context_after"],
        })
        out += [f"## {line['hanzi']}", "", f"_track {line['track']} · line {line['index']}_", ""]
        for provider, key, model in providers:
            started = time.monotonic()
            try:
                answer = ask(provider, key, model, system, user)
            except Exception as exc:  # one provider must not stop the others
                answer = "ERROR: " + redact(f"{type(exc).__name__}: {exc}", keys)
            elapsed = time.monotonic() - started
            out += [f"### {provider.name} · {model} · {elapsed:.1f}s", "", redact(answer, keys).strip(), ""]
    return "\n".join(out) + "\n"


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--dry-run", action="store_true", help="print lines and first prompt; no calls")
    parser.add_argument("--list-models", metavar="PROVIDER", help="print model ids for one provider")
    args = parser.parse_args(argv)

    env = load_env()

    if args.list_models:
        match = [p for p in PROVIDERS if p.name == args.list_models]
        if not match:
            print(f"unknown provider: {args.list_models}", file=sys.stderr)
            return 2
        key = env.get(match[0].key_env, "")
        if not key:
            print(f"set {match[0].key_env} in .env", file=sys.stderr)
            return 2
        try:
            for model_id in list_models(match[0], key):
                print(model_id)
        except Exception as exc:
            print("ERROR: " + redact(f"{type(exc).__name__}: {exc}", [key]), file=sys.stderr)
            return 1
        return 0

    reference = json.loads(REFERENCE.read_text(encoding="utf-8"))
    lines = pick_lines(reference)
    system, template = load_prompt()

    if args.dry_run:
        for i, line in enumerate(lines, 1):
            print(f"{i}. [{line['track']} #{line['index']}] {line['hanzi']}")
        print()
        print("--- first rendered user prompt ---")
        print(render_prompt(template, {
            "hanzi": lines[0]["hanzi"], "pinyin": lines[0]["pinyin"], "meaning_vi": lines[0]["meaning_vi"],
            "context_before": lines[0]["context_before"], "context_after": lines[0]["context_after"],
        }))
        names = [p.name for p, _, _ in enabled_providers(env)]
        print(f"\nproviders with keys set: {', '.join(names) or 'none'}")
        return 0

    providers = []
    for provider, key, model in enabled_providers(env):
        if not model:
            print(f"set {provider.model_env}; see --list-models {provider.name}", file=sys.stderr)
            continue
        providers.append((provider, key, model))
    if not providers:
        print("no provider enabled: set GEMINI_API_KEY / GROQ_API_KEY / DEEPSEEK_API_KEY in .env", file=sys.stderr)
        return 2

    report = run_compare(lines, providers, system, template)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(report, encoding="utf-8")
    print(f"wrote {OUTPUT.relative_to(ROOT)} ({len(lines)} lines x {len(providers)} providers)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
