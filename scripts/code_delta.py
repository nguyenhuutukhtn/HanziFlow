#!/usr/bin/env python3
"""Print a compact, symbol-aware summary of ``base...HEAD``."""

from __future__ import annotations

import ast
import re
import subprocess
import sys
from collections import defaultdict
from dataclasses import dataclass


AREAS = ("src", "tests", "kmp", "web", "scripts", "docs", "other")
DATA_PATHS = (
    re.compile(r"^lessons/.*\.json$"),
    re.compile(r"^docs/CODEMAP\.md$"),
    re.compile(r"^publish/"),
    re.compile(r".*\.lock$"),
)
DECLARATION = re.compile(
    r"(?m)^[ \t]*(?:(?:export|default|public|private|protected|internal|abstract|open|"
    r"sealed|data|enum|annotation|inline|value|override|suspend|async|static)\s+)*"
    r"(?:(?:def|class|fun|object|interface|function)\s+([A-Za-z_$][\w$]*)|"
    r"const\s+([A-Za-z_$][\w$]*)\s*=\s*(?:async\s*)?\()"
)


@dataclass(frozen=True)
class Change:
    status: str
    old_path: str
    path: str


def git(*args: str, check: bool = True) -> str:
    result = subprocess.run(
        ["git", *args], text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False
    )
    if check and result.returncode:
        raise SystemExit(result.stderr.strip() or f"git {' '.join(args)} failed")
    return result.stdout


def changes(base: str) -> list[Change]:
    result = []
    for row in git("diff", "--name-status", "-M", f"{base}...HEAD").splitlines():
        fields = row.split("\t")
        status = fields[0][0]
        if status in {"R", "C"}:
            result.append(Change(status, fields[1], fields[2]))
        else:
            result.append(Change(status, fields[1], fields[1]))
    return result


def file_at(ref: str, path: str) -> str:
    return git("show", f"{ref}:{path}", check=False)


def python_symbols(text: str) -> dict[str, str]:
    try:
        tree = ast.parse(text)
    except SyntaxError:
        return {}
    lines = text.splitlines(keepends=True)
    found = {}
    nodes = sorted(
        (
            node
            for node in ast.walk(tree)
            if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef, ast.ClassDef))
        ),
        key=lambda node: node.lineno,
    )
    for node in nodes:
        end = getattr(node, "end_lineno", node.lineno)
        found[node.name] = "".join(lines[node.lineno - 1 : end])
    return found


def braced_symbols(text: str) -> dict[str, str]:
    """Extract Kotlin/JS/TS declarations and their brace bodies."""
    found = {}
    matches = list(DECLARATION.finditer(text))
    for index, match in enumerate(matches):
        name = match.group(1) or match.group(2)
        start = match.start()
        brace = text.find("{", match.end())
        next_start = matches[index + 1].start() if index + 1 < len(matches) else len(text)
        if brace < 0 or brace >= next_start:
            end = text.find("\n", match.end())
            found[name] = text[start : len(text) if end < 0 else end]
            continue
        depth = 0
        end = len(text)
        for position in range(brace, len(text)):
            if text[position] == "{":
                depth += 1
            elif text[position] == "}":
                depth -= 1
                if depth == 0:
                    end = position + 1
                    break
        found[name] = text[start:end]
    return found


def symbols(text: str, path: str) -> dict[str, str]:
    if path.endswith(".py"):
        return python_symbols(text)
    if path.endswith((".kt", ".js", ".jsx", ".ts", ".tsx")):
        return braced_symbols(text)
    return {}


def is_data(path: str) -> bool:
    return any(pattern.fullmatch(path) or pattern.match(path) for pattern in DATA_PATHS)


def counts(base: str, change: Change) -> tuple[str, str]:
    output = git("diff", "--numstat", "-M", f"{base}...HEAD", "--", change.old_path, change.path)
    row = output.splitlines()[0].split("\t") if output.strip() else ["0", "0"]
    return row[0], row[1]


def symbol_delta(base: str, change: Change) -> list[str]:
    if is_data(change.path):
        return []
    before = symbols(file_at(base, change.old_path), change.old_path)
    after = symbols(file_at("HEAD", change.path), change.path)
    return (
        [f"+{name}" for name in sorted(after.keys() - before.keys())]
        + [f"−{name}" for name in sorted(before.keys() - after.keys())]
        + [
            f"~{name}"
            for name in sorted(before.keys() & after.keys())
            if before[name] != after[name]
        ]
    )


def main() -> None:
    base = sys.argv[1] if len(sys.argv) > 1 else "main"
    grouped: dict[str, list[str]] = defaultdict(list)
    for change in changes(base):
        added, removed = counts(base, change)
        area = change.path.split("/", 1)[0]
        if area not in AREAS:
            area = "other"
        display = (
            f"{change.old_path} → {change.path}"
            if change.status in {"R", "C"}
            else change.path
        )
        details = symbol_delta(base, change)
        suffix = f" {' '.join(details)}" if details else ""
        grouped[area].append(f"{change.status} {display} +{added} −{removed}{suffix}")
    for area in AREAS:
        if grouped[area]:
            print(f"### {area}\n")
            print("\n".join(f"- {line}" for line in grouped[area]))
            print()


if __name__ == "__main__":
    main()
