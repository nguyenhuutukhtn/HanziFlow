"""SQLite progress store. Ratings are learner supplied; no automatic scoring."""

from __future__ import annotations

import sqlite3
from datetime import date, datetime
from pathlib import Path

from .lesson_schema import Lesson
from .review import Rating, next_review


class ProgressStore:
    def __init__(self, path: Path):
        path.parent.mkdir(parents=True, exist_ok=True)
        self.connection = sqlite3.connect(path)
        self.connection.row_factory = sqlite3.Row
        self.connection.executescript("""
            CREATE TABLE IF NOT EXISTS lessons (
                lesson_id TEXT PRIMARY KEY, title TEXT NOT NULL, generated_at TEXT
            );
            CREATE TABLE IF NOT EXISTS patterns (
                pattern_id TEXT PRIMARY KEY, lesson_id TEXT NOT NULL, hanzi TEXT NOT NULL,
                pinyin TEXT NOT NULL, translation TEXT NOT NULL, stage INTEGER NOT NULL DEFAULT 0,
                next_review TEXT NOT NULL, mistakes INTEGER NOT NULL DEFAULT 0
            );
            CREATE TABLE IF NOT EXISTS review_history (
                id INTEGER PRIMARY KEY, pattern_id TEXT NOT NULL, reviewed_at TEXT NOT NULL,
                rating TEXT NOT NULL, note TEXT
            );
        """)

    def register_lesson(self, lesson: Lesson, generated: bool = False) -> None:
        generated_at = datetime.now().isoformat(timespec="seconds") if generated else None
        with self.connection:
            self.connection.execute(
                "INSERT INTO lessons VALUES (?, ?, ?) ON CONFLICT(lesson_id) DO UPDATE SET title=excluded.title, generated_at=COALESCE(excluded.generated_at, lessons.generated_at)",
                (lesson.lesson_id, lesson.title, generated_at),
            )
            for segment in lesson.segments:
                if segment.type == "pattern" and segment.pinyin and segment.translation:
                    self.connection.execute(
                        "INSERT OR IGNORE INTO patterns(pattern_id, lesson_id, hanzi, pinyin, translation, next_review) VALUES (?, ?, ?, ?, ?, ?)",
                        (segment.id, lesson.lesson_id, segment.text, segment.pinyin, segment.translation, date.today().isoformat()),
                    )

    def due(self, today: date | None = None) -> list[sqlite3.Row]:
        target = (today or date.today()).isoformat()
        return list(self.connection.execute("SELECT * FROM patterns WHERE next_review <= ? ORDER BY next_review, pattern_id", (target,)))

    def record_review(self, pattern_id: str, rating: Rating, note: str | None = None) -> None:
        row = self.connection.execute("SELECT stage, mistakes FROM patterns WHERE pattern_id=?", (pattern_id,)).fetchone()
        if row is None:
            raise KeyError(f"Unknown pattern: {pattern_id}")
        stage, due = next_review(row["stage"], rating)
        mistakes = row["mistakes"] + (1 if rating == "again" else 0)
        with self.connection:
            self.connection.execute("UPDATE patterns SET stage=?, next_review=?, mistakes=? WHERE pattern_id=?", (stage, due.isoformat(), mistakes, pattern_id))
            self.connection.execute("INSERT INTO review_history(pattern_id, reviewed_at, rating, note) VALUES (?, ?, ?, ?)", (pattern_id, datetime.now().isoformat(timespec="seconds"), rating, note))

