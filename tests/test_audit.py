import json

from mandarin_course.audit import build_report, load_authoring_lessons
from mandarin_course.cli import main
from mandarin_course.lesson_schema import Lesson


def _write(tmp_path, name, data):
    (tmp_path / name).write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")


def _base(lesson_id, **overrides):
    data = {
        "lesson_id": lesson_id,
        "title": "Test",
        "level": "absolute_beginner",
        "target_duration_minutes": 5,
        "communicative_goals": ["Test"],
        "prerequisites": [],
        "new_patterns": ["你好"],
        "review_pattern_ids": [],
        "version": 1,
        "segments": [
            {
                "id": "s1",
                "activity": "Test",
                "type": "pattern",
                "language": "zh-CN",
                "text": "你好。",
                "pinyin": "nǐ hǎo",
                "translation": "Hello.",
            }
        ],
    }
    data.update(overrides)
    return data


def test_load_authoring_lessons_skips_practice_and_drafts(tmp_path):
    _write(tmp_path, "lesson_001.json", _base("lesson_001", variant="guided"))
    _write(tmp_path, "lesson_001_practice.json", _base("lesson_001", variant="practice"))
    _write(tmp_path, "lesson_001_mau.json", _base("lesson_001", variant="guided"))

    lessons = load_authoring_lessons(tmp_path)
    assert [lesson.lesson_id for lesson in lessons] == ["lesson_001"]


def test_build_report_tracks_introductions_and_reuse(tmp_path):
    lessons = [
        Lesson.model_validate(_base("lesson_001", new_patterns=["你好", "再见"])),
        Lesson.model_validate(
            _base("lesson_002", new_patterns=["谢谢"], review_pattern_ids=["你好"])
        ),
    ]
    report = build_report(lessons)
    assert report.dialogue.introduced_by == {"你好": "lesson_001", "再见": "lesson_001", "谢谢": "lesson_002"}
    assert report.dialogue.reused_in == {"你好": ["lesson_002"]}
    assert report.dialogue.unintroduced_reuse == {}
    assert report.dialogue.duplicate_new == {}


def test_build_report_flags_unintroduced_review(tmp_path):
    lessons = [
        Lesson.model_validate(_base("lesson_001", new_patterns=["你好"])),
        Lesson.model_validate(
            _base("lesson_002", new_patterns=["谢谢"], review_pattern_ids=["再见"])
        ),
    ]
    report = build_report(lessons)
    assert report.dialogue.unintroduced_reuse == {"lesson_002": ["再见"]}


def test_build_report_flags_duplicate_new_pattern(tmp_path):
    lessons = [
        Lesson.model_validate(_base("lesson_001", new_patterns=["你好"])),
        Lesson.model_validate(_base("lesson_002", new_patterns=["你好"])),
    ]
    report = build_report(lessons)
    assert report.dialogue.duplicate_new == {"你好": ["lesson_001", "lesson_002"]}


def test_build_report_up_to_filters_later_lessons(tmp_path):
    lessons = [
        Lesson.model_validate(_base("lesson_001", new_patterns=["你好"])),
        Lesson.model_validate(_base("lesson_002", new_patterns=["谢谢"])),
    ]
    report = build_report(lessons, up_to="lesson_001")
    assert report.dialogue.introduced_by == {"你好": "lesson_001"}


def test_build_report_tracks_coach_phrases_separately_from_dialogue(tmp_path):
    lessons = [
        Lesson.model_validate(
            _base(
                "lesson_001",
                new_patterns=["你好"],
                coach_phrases_introduced=["再见"],
            )
        ),
        Lesson.model_validate(
            _base(
                "lesson_002",
                new_patterns=["谢谢"],
                coach_phrases_reduced=["再见"],
            )
        ),
    ]
    report = build_report(lessons)
    assert report.coach_phrases.introduced_by == {"再见": "lesson_001"}
    assert report.coach_phrases.reused_in == {"再见": ["lesson_002"]}
    assert report.coach_phrases.unintroduced_reuse == {}
    # Dialogue ledger must stay unaware of 再见 since it was never a dialogue pattern.
    assert "再见" not in report.dialogue.introduced_by


def test_build_report_flags_coach_phrase_reduced_without_introduction(tmp_path):
    lessons = [
        Lesson.model_validate(_base("lesson_001", new_patterns=["你好"])),
        Lesson.model_validate(
            _base("lesson_002", new_patterns=["谢谢"], coach_phrases_reduced=["再见"])
        ),
    ]
    report = build_report(lessons)
    assert report.coach_phrases.unintroduced_reuse == {"lesson_002": ["再见"]}


def test_cli_audit_reports_without_tts(tmp_path, monkeypatch, capsys):
    _write(tmp_path, "lesson_001.json", _base("lesson_001", new_patterns=["你好"], variant="guided"))
    _write(
        tmp_path,
        "lesson_002.json",
        _base("lesson_002", new_patterns=["谢谢"], review_pattern_ids=["你好"], variant="guided"),
    )

    def forbidden(*args, **kwargs):
        raise AssertionError("TTS must not be touched by audit")

    monkeypatch.setattr("mandarin_course.cli.ElevenLabsTTS", forbidden)
    assert main(["audit", str(tmp_path)]) == 0
    output = capsys.readouterr().out
    assert "lesson_001" in output
    assert "lesson_002" in output
    assert "Dialogue patterns introduced so far: 2" in output
