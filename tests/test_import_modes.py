import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).parents[1]
PYTHON = ROOT / ".venv/bin/python"


def run_import() -> None:
    subprocess.run(
        [str(PYTHON), "-m", "mandarin_course.cli", "import-modes", "002"],
        cwd=ROOT, env={"PYTHONPATH": "src"}, check=True, capture_output=True, text=True,
    )


def test_real_modes_counts_and_samples():
    run_import()
    lessons = {
        mode: json.loads((ROOT / f"lessons/lesson_002_{mode}.json").read_text())
        for mode in ("hieu", "nghe", "phan_xa", "noi")
    }
    assert {mode: len(lesson["segments"]) for mode, lesson in lessons.items()} == {
        "hieu": 122, "nghe": 68, "phan_xa": 121, "noi": 43
    }
    segments = {s["id"]: s for lesson in lessons.values() for s in lesson["segments"]}
    assert segments["phan_xa_q2"]["pause_after_ms"] == 3420
    assert segments["phan_xa_q25"]["pause_after_ms"] == 5680
    assert segments["noi_s2"]["pause_after_ms"] == 2260
    assert segments["noi_s43"]["pause_after_ms"] == 6460
    assert segments["hieu_h1"]["speaker"] == "coach"
    assert segments["hieu_h4"]["speaker"] == "coach_zh"
    assert segments["nghe_l3"]["speaker"] == "wang_fang"


def test_import_is_deterministic():
    run_import()
    first = {p.name: p.read_bytes() for p in (ROOT / "lessons").glob("lesson_002_*.json")}
    run_import()
    assert first == {p.name: p.read_bytes() for p in (ROOT / "lessons").glob("lesson_002_*.json")}
