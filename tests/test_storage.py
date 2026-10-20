from datetime import date

from mandarin_course.storage import ProgressStore


def test_register_and_record_learner_review(tmp_path, lesson):
    store = ProgressStore(tmp_path / "progress.db")
    store.register_lesson(lesson)
    assert len(store.due(date.today())) == 1
    store.record_review("hello", "good")
    assert store.due(date.today()) == []

