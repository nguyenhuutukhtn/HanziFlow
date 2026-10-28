import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).parents[1]
PYTHON = ROOT / ".venv/bin/python"


def run_import(lesson_id: str = "002") -> None:
    subprocess.run(
        [str(PYTHON), "-m", "mandarin_course.cli", "import-modes", lesson_id],
        cwd=ROOT, env={"PYTHONPATH": "src"}, check=True, capture_output=True, text=True,
    )


def test_real_modes_counts_and_samples():
    run_import()
    lessons = {
        mode: json.loads((ROOT / f"lessons/lesson_002_{mode}.json").read_text())
        for mode in ("hieu", "tu_vung", "nghe", "phan_xa", "noi")
    }
    assert {mode: len(lesson["segments"]) for mode, lesson in lessons.items()} == {
        "hieu": 122, "tu_vung": 157, "nghe": 69, "phan_xa": 121, "noi": 43
    }
    tu_vung = lessons["tu_vung"]["segments"]
    assert not any(s["language"] == "vi-VN" for s in tu_vung)
    assert sum(1 for s in tu_vung if s.get("gap") == "answer") == 28
    assert tu_vung[3]["id"] == "tu_vung_v4" and tu_vung[3]["gap"] == "answer"
    segments = {s["id"]: s for lesson in lessons.values() for s in lesson["segments"]}
    assert segments["phan_xa_q2"]["pause_after_ms"] == 3420
    assert segments["phan_xa_q2"]["gap"] == "answer"
    assert segments["phan_xa_q25"]["pause_after_ms"] == 5680
    assert segments["noi_s2"]["pause_after_ms"] == 2260
    assert segments["noi_s2"]["gap"] == "repeat"
    assert segments["noi_s43"]["pause_after_ms"] == 6460
    assert segments["hieu_h1"]["speaker"] == "coach"
    assert segments["hieu_h4"]["speaker"] == "coach_zh"
    assert segments["nghe_l3"]["speaker"] == "wang_fang"
    assert all("gap" not in segment for segment in lessons["hieu"]["segments"])
    assert all("gap" not in segment for segment in lessons["nghe"]["segments"])


def test_import_is_deterministic():
    run_import()
    first = {p.name: p.read_bytes() for p in (ROOT / "lessons").glob("lesson_002_*.json")}
    run_import()
    assert first == {p.name: p.read_bytes() for p in (ROOT / "lessons").glob("lesson_002_*.json")}


def test_real_lesson_003_modes_counts_and_acceptance_samples():
    run_import("003")
    lessons = {
        mode: json.loads((ROOT / f"lessons/lesson_003_{mode}.json").read_text())
        for mode in ("hieu", "tu_vung", "nghe", "phan_xa", "noi")
    }
    assert {mode: len(lesson["segments"]) for mode, lesson in lessons.items()} == {
        "hieu": 161, "tu_vung": 108, "nghe": 119, "phan_xa": 146, "noi": 58
    }
    assert sum(1 for s in lessons["tu_vung"]["segments"] if s.get("gap") == "answer") == 16
    assert sum(1 for s in lessons["phan_xa"]["segments"] if s.get("gap") == "answer") == 48
    assert sum(1 for s in lessons["noi"]["segments"] if s.get("gap") == "repeat") == 56
    assert all("gap" not in s for mode in ("hieu", "nghe") for s in lessons[mode]["segments"])
    assert lessons["hieu"]["segments"][0]["speaker"] == "coach"
    assert lessons["hieu"]["segments"][0]["language"] == "vi-VN"
    assert lessons["tu_vung"]["segments"][0]["text"] == "今天我们听几个词。"
    assert lessons["nghe"]["segments"][0]["text"] == "我们听故事。"
    assert lessons["phan_xa"]["segments"][0]["text"] == "我来问，你来回答。"
    assert lessons["noi"]["segments"][0]["text"] == "跟我说。"
    for mode in ("tu_vung", "nghe", "phan_xa", "noi"):
        assert lessons[mode]["segments"][0]["speaker"] == "coach_zh"
        assert all(s["language"] == "zh-CN" for s in lessons[mode]["segments"])
    for lesson in lessons.values():
        assert lesson["format"] == "modes_v1"
        assert lesson["voice_genders"] == {"coach_zh": "male", "zhang_jing": "female", "liu_yang": "male"}
        assert len({s["id"] for s in lesson["segments"]}) == len(lesson["segments"])
        assert all(s["language"] == "zh-CN" for s in lesson["segments"] if s["speaker"] != "coach")
    speakers = {s["speaker"] for lesson in lessons.values() for s in lesson["segments"]}
    assert speakers <= {"coach", "coach_zh", "zhang_jing", "liu_yang"}
    nghe_speakers = {s["speaker"] for s in lessons["nghe"]["segments"]}
    assert {"zhang_jing", "liu_yang"} <= nghe_speakers
