import json

from mandarin_course.cli import main


def test_validate_does_not_construct_or_call_tts(tmp_path, lesson_data, monkeypatch, capsys):
    path = tmp_path / "lesson.json"
    path.write_text(json.dumps(lesson_data, ensure_ascii=False), encoding="utf-8")

    def forbidden(*args, **kwargs):
        raise AssertionError("TTS must not be touched by validate")

    monkeypatch.setattr("mandarin_course.cli.ElevenLabsTTS", forbidden)
    assert main(["validate", str(path)]) == 0
    assert "Valid:" in capsys.readouterr().out


def test_dry_run_does_not_construct_or_call_tts(tmp_path, lesson_data, monkeypatch, capsys):
    path = tmp_path / "lesson.json"
    path.write_text(json.dumps(lesson_data, ensure_ascii=False), encoding="utf-8")

    def forbidden(*args, **kwargs):
        raise AssertionError("TTS must not be touched by dry-run")

    monkeypatch.setattr("mandarin_course.cli.ElevenLabsTTS", forbidden)
    assert main(["generate", str(path), "--dry-run"]) == 0
    output = capsys.readouterr().out
    assert "Dry run only" in output
    assert "Need TTS:" in output

