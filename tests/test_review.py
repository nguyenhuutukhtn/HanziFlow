from datetime import date

from mandarin_course.review import next_review


def test_review_intervals_start_at_one_two_three_five_seven_days():
    today = date(2026, 1, 1)
    stages_and_days = [next_review(stage, "good", today) for stage in (-1, 0, 1, 2, 3)]
    assert [(stage, (due - today).days) for stage, due in stages_and_days] == [
        (0, 1), (1, 2), (2, 3), (3, 5), (4, 7)
    ]


def test_again_resets_schedule_and_easy_advances_two_steps():
    today = date(2026, 1, 1)
    assert next_review(4, "again", today) == (0, date(2026, 1, 2))
    assert next_review(0, "easy", today) == (2, date(2026, 1, 4))

