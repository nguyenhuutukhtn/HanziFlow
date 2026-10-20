"""Small, transparent spaced-repetition policy."""

from __future__ import annotations

from datetime import date, timedelta
from typing import Literal

Rating = Literal["again", "hard", "good", "easy"]
BASE_INTERVALS = (1, 2, 3, 5, 7)


def next_review(current_stage: int, rating: Rating, today: date | None = None) -> tuple[int, date]:
    today = today or date.today()
    if rating == "again":
        new_stage = 0
    elif rating == "hard":
        new_stage = max(0, current_stage)
    elif rating == "good":
        new_stage = current_stage + 1
    else:
        new_stage = current_stage + 2
    if new_stage < len(BASE_INTERVALS):
        days = BASE_INTERVALS[new_stage]
    else:
        days = round(BASE_INTERVALS[-1] * (1.7 ** (new_stage - len(BASE_INTERVALS) + 1)))
    return new_stage, today + timedelta(days=days)

