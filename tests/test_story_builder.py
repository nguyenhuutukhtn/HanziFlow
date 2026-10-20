import json
from pathlib import Path

from mandarin_course.cli import main
from mandarin_course.story import build_story, parse_story

HEADER = "lesson_id: lesson_009\ntitle: T\nlevel: beginner\nversion: 1\npatterns: 是。\ngoals: g\n"
GUIDED = "# guided\nminutes: 5\n"


def _errors(tmp_path: Path, body: str) -> list[str]:
    story = tmp_path / "lesson_009.story.md"
    story.write_text(HEADER + body, encoding="utf-8")
    errors = sorted(build_story(story, tmp_path).errors, key=lambda error: error.line)
    return [f"{error.line}: {error.message}" for error in errors]


def test_lesson_001_source_reproduces_committed_json(tmp_path):
    result = build_story("lessons/src/lesson_001.story.md", tmp_path)
    assert not result.errors
    for variant in result.variants:
        committed = Path("lessons") / variant.path.name
        built = json.dumps(variant.lesson, ensure_ascii=False, indent=2) + "\n"
        assert built == committed.read_text(encoding="utf-8")


def test_unknown_block_type_names_its_line():
    _, errors = parse_story(GUIDED + "## activity A\n## sing\nx | y\n")
    assert [(error.line, error.message) for error in errors] == [(4, "unknown block type 'sing'")]


def test_malformed_row_names_its_line(tmp_path):
    body = GUIDED + "## activity A\n## say\nok | 是。 | shì. | Phải.\nbad | 是。 | shì.\n"
    errors = _errors(tmp_path, body)
    assert len(errors) == 1 and errors[0].startswith("12: expected key | 中文 | pinyin | Vietnamese")


def test_unknown_set_and_speaker_name_their_lines(tmp_path):
    body = GUIDED + "## activity A\n## story prefix=s from=nope\n# data\n## lines x\nk | bob | 是。 | shì. | Phải.\n"
    errors = _errors(tmp_path, body)
    assert errors[0].startswith("10: unknown lines set 'nope'")
    assert errors[1].startswith("13: unknown speaker 'bob'")


def test_buildup_must_nest(tmp_path):
    body = (
        GUIDED + "## activity A\n## buildup prefix=b\n"
        "full | 这是谁的？ | zhè shì shuí de? | Của ai?\nc1 | 谁的 | shuí de | Của ai.\nc2 | 这是 | zhè shì | Đây là.\n"
    )
    errors = _errors(tmp_path, body)
    assert len(errors) == 1 and errors[0].startswith("13: buildup step does not contain the previous chunk")


def test_build_lesson_cli_writes_both_variants(tmp_path, capsys):
    body = (
        GUIDED + "## activity A\n## say\nhi | 你好。 | nǐ hǎo. | Xin chào.\n"
        "## ask prefix=q vi praise\nq1 | 是吗？ | shì ma? | Phải không? | 是 | 4500 | Tốt.\n"
        "# practice\nminutes: 5\n## activity A\n## zh\nagain | 再听一次。 | zài tīng yí cì. | Nghe lại lần nữa.\n"
    )
    story = tmp_path / "lesson_009.story.md"
    story.write_text(HEADER + body, encoding="utf-8")
    assert main(["build-lesson", str(story)]) == 0
    guided = json.loads((tmp_path / "lesson_009.json").read_text(encoding="utf-8"))
    ids = [segment["id"] for segment in guided["segments"]]
    assert ids == ["hi", "hi_vi", "q_q1", "q_q1_vi", "q_q1_a", "q_q1_av", "q_q1_ok", "q_q1_ok_vi"]
    assert json.loads((tmp_path / "lesson_009_practice.json").read_text(encoding="utf-8"))["target_duration_minutes"] == 5
    assert len(capsys.readouterr().out.splitlines()) == 4
