# Repeated steps become one command each (docs/workflow.md). Recipes are thin
# wrappers around scripts/ so they also run without `just` installed:
#   bash scripts/check.sh
#   bash scripts/lesson.sh <id>

set shell := ["bash", "-euo", "pipefail", "-c"]

# Full local check (pytest, kmp unit + architecture tests, codemap, file sizes); logs in build/logs/.
check:
    bash scripts/check.sh

# Fetch pinned offline character dictionaries and licenses.
fetch-character-data:
    bash scripts/fetch_character_data.sh

# Assemble and install the debug app on the connected device.
app:
    cd kmp && ./gradlew -q :android:assembleDebug :android:installDebug

# One lesson, start to finish: dry-run -> generate (asks before paid TTS) -> publish -> upload.
lesson ID:
    bash scripts/lesson.sh {{ID}}

# Write build/bundles/lesson_<ID>.bundle.md: story source + checklist + continuity, for any AI to write/revise.
export-lesson ID:
    mandarin-course export-lesson {{ID}}

lesson-metrics ID:
    mandarin-course lesson-metrics {{ID}}

export-rows ID +REF:
    mandarin-course export-rows {{ID}} {{REF}}

import-rows ID FILE:
    mandarin-course import-rows {{ID}} {{FILE}}

# What lessons before ID taught (words, chars, patterns, premise, bible) -> build/ledger_<ID>.md
ledger ID:
    mandarin-course ledger {{ID}}

# Import an AI's reply: build + validate + audit + dry-run count, never paid TTS.
import-lesson ID FILE:
    mandarin-course import-lesson {{ID}} {{FILE}}

export-characters ID:
    mandarin-course export-characters {{ID}}

import-characters ID FILE:
    mandarin-course import-characters {{ID}} {{FILE}}

# Record Roborazzi goldens, only after the user approved the screen (see android-ui-testing skill).
approve-screens:
    cd kmp && ./gradlew -q :android:recordRoborazziDebug

# Codex 5h/week %, DeepSeek balance, Claude hint
quota:
    scripts/quota.sh
