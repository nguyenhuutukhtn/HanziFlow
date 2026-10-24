#!/usr/bin/env bash
set -euo pipefail

id="${1:?usage: scripts/modes.sh <ID>}"
root="$(cd "$(dirname "$0")/.." && pwd)"
python="${PYTHON:-$root/.venv/bin/python}"
[[ -x "$python" ]] || python=python3
export PYTHONPATH="$root/src${PYTHONPATH:+:$PYTHONPATH}"

"$python" - "$root" "$id" <<'PY'
import json
import subprocess
import sys
from pathlib import Path

root, lesson_id = Path(sys.argv[1]), sys.argv[2]
total = 0
for mode in ("hieu", "tu_vung", "nghe", "phan_xa", "noi"):
    result = subprocess.run(
        [sys.executable, "-m", "mandarin_course.cli", "generate",
         f"lessons/lesson_{lesson_id}_{mode}.json", "--dry-run", "--json"],
        cwd=root, check=True, capture_output=True, text=True,
    )
    plan = json.loads(result.stdout)
    total += plan["unique_new_requests"]
    lesson = json.loads((root / f"lessons/lesson_{lesson_id}_{mode}.json").read_text())
    chars = sum(len(segment["text"]) for segment in lesson["segments"])
    print(f"{mode}: {plan['unique_new_requests']} new, {plan['unique_cached_clips']} cached, {chars} chars")

print(f"total (upper bound, shared clips counted per mode): {total} new")
settings = __import__("mandarin_course.config", fromlist=["Settings"]).Settings.from_env()
registry = {p.voice_id: p for p in __import__("mandarin_course.voice_registry", fromlist=["load_registry"]).load_registry(settings.resolved_voices_path())}
cast_path = settings.output_dir / f"lesson_{lesson_id}" / "voice_cast.json"
cast = json.loads(cast_path.read_text()) if cast_path.is_file() else {}
roles = sorted((key, voice_id) for key, voice_id in cast.items() if key.startswith("zh-CN:"))
roles += sorted((key, voice_id) for key, voice_id in cast.items() if key.startswith("vi-VN:"))
label = ", ".join(f"{role.split(':', 1)[1]} → {registry[voice_id].name} ({registry[voice_id].gender})" for role, voice_id in roles)
print(f"cast: {label}")
PY
