"""Validation of the optional per-lesson ``lessons/<id>_notes.json`` text notes."""

from __future__ import annotations

from pathlib import Path
from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, ValidationError, model_validator

from .publish_timeline import PublishError


class NoteExample(BaseModel):
    model_config = ConfigDict(extra="forbid")
    hanzi: str = ""
    pinyin: str = ""
    meaning_vi: str = ""


class _Entry(BaseModel):
    model_config = ConfigDict(extra="forbid")
    id: str
    examples: list[NoteExample] = []

    def _require(self, *names: str) -> None:
        for name in names:
            if not str(getattr(self, name, "")).strip():
                raise ValueError(f"{self.id}: '{name}' must not be empty")
        if not self.examples:
            raise ValueError(f"{self.id}: needs at least one example")
        for index, example in enumerate(self.examples):
            for name in ("hanzi", "pinyin", "meaning_vi"):
                if not getattr(example, name).strip():
                    raise ValueError(f"{self.id}: example {index + 1} '{name}' must not be empty")


class NotePattern(_Entry):
    pattern: str = ""
    explain_vi: str = ""

    @model_validator(mode="after")
    def _check(self) -> "NotePattern":
        self._require("pattern", "explain_vi")
        return self


class NoteQuestion(_Entry):
    hanzi: str = ""
    pinyin: str = ""
    meaning_vi: str = ""
    how_to_answer_vi: str = ""

    @model_validator(mode="after")
    def _check(self) -> "NoteQuestion":
        self._require("hanzi", "pinyin", "meaning_vi", "how_to_answer_vi")
        return self


class NotesSidecar(BaseModel):
    model_config = ConfigDict(extra="forbid")
    schema_version: Literal[1]
    lesson_id: str
    status: Literal["draft", "reviewed"]
    patterns: list[NotePattern]
    questions: list[NoteQuestion]

    @model_validator(mode="after")
    def _unique_ids(self) -> "NotesSidecar":
        ids = [entry.id for entry in (*self.patterns, *self.questions)]
        for value in ids:
            if ids.count(value) > 1:
                raise ValueError(f"duplicate notes id: {value}")
        return self


def load_notes(path: Path, lesson_id: str) -> dict[str, Any] | None:
    """Return the validated notes dict, or ``None`` when the lesson has no notes source."""
    if not path.is_file():
        return None
    try:
        notes = NotesSidecar.model_validate_json(path.read_text(encoding="utf-8"))
    except ValidationError as exc:
        details = "; ".join(str(error["msg"]).removeprefix("Value error, ") if error["type"] == "value_error"
                            else f"{'.'.join(str(p) for p in error['loc'])}: {error['msg']}" for error in exc.errors())
        raise PublishError(f"Invalid notes file {path}: {details}") from exc
    if notes.lesson_id != lesson_id:
        raise PublishError(f"Notes lesson_id {notes.lesson_id!r} does not match {lesson_id!r}")
    return notes.model_dump(mode="json")


def notes_body(notes: dict[str, Any]) -> dict[str, Any]:
    """The package notes.json payload minus the envelope keys the publisher sets itself."""
    return {"patterns": notes["patterns"], "questions": notes["questions"]}
