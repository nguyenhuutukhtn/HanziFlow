"""Validated, content-driven lesson schema."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, model_validator

Language = Literal["zh-CN", "en-US", "vi-VN"]
Speaker = Literal["anna", "li_ming", "coach", "coach_zh"]
SegmentType = Literal[
    "narration", "dialogue", "pattern", "prompt", "model_answer", "context", "review"
]


class VoiceSettings(BaseModel):
    model_config = ConfigDict(extra="forbid")
    stability: float = Field(default=0.55, ge=0, le=1)
    similarity_boost: float = Field(default=0.75, ge=0, le=1)
    style: float = Field(default=0.0, ge=0, le=1)
    use_speaker_boost: bool = True
    speed: float = Field(default=1.0, ge=0.25, le=4.0)


class DialogueTurn(BaseModel):
    model_config = ConfigDict(extra="forbid")
    speaker: Speaker
    text: str = Field(min_length=1)
    pinyin: str | None = None
    translation: str | None = None
    tts_tags: list[str] = Field(default_factory=list)


class Segment(BaseModel):
    model_config = ConfigDict(extra="forbid")
    id: str = Field(min_length=1, pattern=r"^[a-z0-9_]+$")
    activity: str = Field(min_length=1)
    type: SegmentType
    language: Language
    speaker: Speaker | None = None
    text: str = Field(min_length=1)
    pinyin: str | None = None
    translation: str | None = None
    context: str | None = None
    repetitions: int = Field(default=1, ge=1, le=10)
    pause_before_ms: int = Field(default=0, ge=0, le=30_000)
    pause_after_ms: int = Field(default=0, ge=0, le=30_000)
    voice_settings: VoiceSettings = Field(default_factory=VoiceSettings)
    playback_speed: float = Field(default=1.0, ge=0.75, le=1.25)
    turns: list[DialogueTurn] = Field(default_factory=list)
    metadata: dict[str, str | int | float | bool] = Field(default_factory=dict)
    tts_tags: list[str] = Field(default_factory=list)

    @model_validator(mode="after")
    def require_mandarin_learning_fields(self) -> "Segment":
        if self.language == "zh-CN" and (not self.pinyin or not self.translation):
            raise ValueError("Mandarin segments require pinyin and translation")
        if self.speaker in {"anna", "li_ming"} and self.language != "zh-CN":
            raise ValueError("Anna and Li Ming must use Mandarin segments")
        if self.speaker == "coach" and self.language not in {"en-US", "vi-VN"}:
            raise ValueError("coach must use an English or Vietnamese segment")
        if self.speaker == "coach_zh" and self.language != "zh-CN":
            raise ValueError("coach_zh must use a Mandarin segment")
        if self.turns:
            if self.type not in {"dialogue", "context"}:
                raise ValueError("dialogue turns require a dialogue or context segment")
            if self.speaker is not None:
                raise ValueError("use either segment speaker or dialogue turns, not both")
            if self.language != "zh-CN":
                raise ValueError("Lesson dialogue turns currently require Mandarin")
            if any(turn.speaker in {"coach", "coach_zh"} for turn in self.turns):
                raise ValueError("Mandarin dialogue turns cannot use a coach speaker")
        return self


class Lesson(BaseModel):
    model_config = ConfigDict(extra="forbid")
    lesson_id: str = Field(pattern=r"^lesson_[0-9]{3}$")
    title: str = Field(min_length=1)
    level: Literal["absolute_beginner", "beginner"]
    target_duration_minutes: int = Field(ge=5, le=60)
    communicative_goals: list[str] = Field(min_length=1)
    prerequisites: list[str] = Field(default_factory=list)
    new_patterns: list[str] = Field(min_length=1, max_length=12)
    review_pattern_ids: list[str] = Field(default_factory=list)
    coach_phrases_introduced: list[str] = Field(default_factory=list)
    coach_phrases_reduced: list[str] = Field(default_factory=list)
    segments: list[Segment] = Field(min_length=1)
    version: int = Field(default=1, ge=1)
    variant: Literal["guided", "practice"] | None = None

    @model_validator(mode="after")
    def unique_segment_ids(self) -> "Lesson":
        ids = [segment.id for segment in self.segments]
        if len(ids) != len(set(ids)):
            raise ValueError("segment IDs must be unique")
        return self


def load_lesson(path: str | Path) -> Lesson:
    with Path(path).open(encoding="utf-8") as handle:
        return Lesson.model_validate(json.load(handle))
