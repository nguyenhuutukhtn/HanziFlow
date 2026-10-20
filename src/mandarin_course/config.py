"""Environment-backed configuration without exposing secrets."""

from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv

from .voice_registry import load_registry, short_language


@dataclass(frozen=True)
class Settings:
    api_key: str | None
    model_id: str
    cache_dir: Path
    output_dir: Path
    db_path: Path
    output_format: str = "mp3_44100_128"
    voices_path: Path | None = None

    @classmethod
    def from_env(cls, env_file: Path | None = None) -> "Settings":
        load_dotenv(dotenv_path=env_file, override=False)
        db_path = Path(os.getenv("MANDARIN_COURSE_DB_PATH", "data/progress.db"))
        voices_path = os.getenv("MANDARIN_COURSE_VOICES_PATH")
        return cls(
            api_key=os.getenv("ELEVENLABS_API_KEY") or None,
            model_id=os.getenv("ELEVENLABS_MODEL_ID", "eleven_v4"),
            cache_dir=Path(os.getenv("MANDARIN_COURSE_CACHE_DIR", "cache")),
            output_dir=Path(os.getenv("MANDARIN_COURSE_OUTPUT_DIR", "output")),
            db_path=db_path,
            voices_path=Path(voices_path) if voices_path else db_path.parent / "voices.json",
        )

    def resolved_voices_path(self) -> Path:
        return self.voices_path or Path("data/voices.json")

    def missing_tts_fields(self, languages: set[str]) -> list[str]:
        missing = []
        if not self.api_key:
            missing.append("ELEVENLABS_API_KEY")
        profiles = load_registry(self.resolved_voices_path())
        if not profiles:
            missing.append(f"voice registry at {self.resolved_voices_path()} (run `mandarin-course voices sync`)")
            return missing
        available = {short_language(profile.language) for profile in profiles}
        for language in sorted(languages):
            if short_language(language) not in available:
                missing.append(f"voices for {language} in registry (run `mandarin-course voices sync`)")
        return missing
