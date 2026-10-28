#!/usr/bin/env python3
"""Post one GitHub review, demoting out-of-diff findings to its body."""

from __future__ import annotations

import argparse
import json
import re
import subprocess
from pathlib import Path


FINDING = re.compile(r"^(.*):(\d+) — (.*)$")
HUNK = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@")


def right_side_lines(diff: str) -> dict[str, set[int]]:
    allowed: dict[str, set[int]] = {}
    path = None
    new_line = None
    for row in diff.splitlines():
        if row.startswith("+++ b/"):
            path = row[6:]
            allowed.setdefault(path, set())
        elif match := HUNK.match(row):
            new_line = int(match.group(1))
        elif path is not None and new_line is not None:
            if row.startswith("+") and not row.startswith("+++"):
                allowed[path].add(new_line)
                new_line += 1
            elif row.startswith("-") and not row.startswith("---"):
                continue
            elif not row.startswith("\\"):
                new_line += 1
    return allowed


def build_payload(findings: Path, reviewer: str, allowed: dict[str, set[int]]) -> dict:
    comments = []
    body_lines = [f"**{reviewer} review**"]
    for raw in findings.read_text(encoding="utf-8").splitlines():
        match = FINDING.match(raw)
        if not match:
            body_lines.append(raw)
            continue
        path, line_text, message = match.groups()
        line = int(line_text)
        if line in allowed.get(path, set()):
            comments.append({"path": path, "line": line, "side": "RIGHT", "body": message})
        else:
            body_lines.append(raw)
    return {"body": "\n".join(body_lines), "event": "COMMENT", "comments": comments}


def command_output(*command: str) -> str:
    return subprocess.check_output(command, text=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("number")
    parser.add_argument("findings", type=Path)
    parser.add_argument("--reviewer", default="Codex")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    diff = command_output("git", "diff", "--unified=0", "main...HEAD")
    payload = build_payload(args.findings, args.reviewer, right_side_lines(diff))
    encoded = json.dumps(payload, ensure_ascii=False)
    if args.dry_run:
        print(json.dumps(payload, ensure_ascii=False, indent=2))
        return

    owner_repo = command_output(
        "gh", "repo", "view", "--json", "nameWithOwner", "--jq", ".nameWithOwner"
    ).strip()
    subprocess.run(
        [
            "gh",
            "api",
            f"repos/{owner_repo}/pulls/{args.number}/reviews",
            "--method",
            "POST",
            "--input",
            "-",
        ],
        input=encoded,
        text=True,
        check=True,
    )


if __name__ == "__main__":
    main()
