"""Run the real `generate` path on lesson files with a fake TTS, in scratch dirs.

No network, no paid call, the real cache/ and output/ are untouched (the lesson's pinned
voice_cast.json is copied in). Catches crashes in generate before the user pays for TTS.
Usage: .venv/bin/python3 scripts/fake_generate.py lessons/lesson_002_hieu.json [...]
"""

import math
import os
import shutil
import struct
import sys
import tempfile
import wave
from pathlib import Path


class FakeTTS:
    def __init__(self, *args, **kwargs):
        pass

    def synthesize(self, segment, destination, lesson):
        destination = Path(destination)
        destination.parent.mkdir(parents=True, exist_ok=True)
        with wave.open(str(destination), "wb") as audio:
            audio.setparams((1, 2, 8000, 0, "NONE", "not compressed"))
            audio.writeframes(b"".join(struct.pack("<h", int(3000 * math.sin(i / 3))) for i in range(1600)))


def main(paths: list[str]) -> int:
    scratch = Path(tempfile.mkdtemp(prefix="fake_generate_"))
    os.environ["MANDARIN_COURSE_CACHE_DIR"] = str(scratch / "cache")
    os.environ["MANDARIN_COURSE_OUTPUT_DIR"] = str(scratch / "output")
    import mandarin_course.cli as cli
    from mandarin_course.config import Settings
    from mandarin_course.lesson_schema import load_lesson

    cli.ElevenLabsTTS = FakeTTS
    settings = Settings.from_env()
    for path in paths:
        lesson_id = load_lesson(path).lesson_id
        cast = Path("output") / lesson_id / "voice_cast.json"
        if cast.is_file():
            (scratch / "output" / lesson_id).mkdir(parents=True, exist_ok=True)
            shutil.copyfile(cast, scratch / "output" / lesson_id / "voice_cast.json")
        if cli.command_generate(Path(path), settings, False, True) != 0:
            print(f"FAIL {path}")
            return 1
    print(f"ok fake generate: {len(paths)} file(s), output in {scratch / 'output'}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
