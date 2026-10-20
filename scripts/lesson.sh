#!/usr/bin/env bash
# One lesson, start to finish: dry-run count -> ask before paid TTS (generate's own
# existing confirmation; never auto-yes) -> generate -> publish -> upload.
# Run: ./scripts/lesson.sh 001   (also wired to `just lesson 001`)
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

ID="${1:?Usage: scripts/lesson.sh <lesson id, e.g. 001>}"
LESSON="lessons/lesson_${ID}.json"

mandarin-course generate "$LESSON" --dry-run
mandarin-course generate "$LESSON"
mandarin-course publish "$LESSON"
mandarin-course upload
