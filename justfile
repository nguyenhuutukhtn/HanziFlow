# Repeated steps become one command each (docs/workflow.md). Recipes are thin
# wrappers around scripts/ so they also run without `just` installed:
#   bash scripts/check.sh
#   bash scripts/lesson.sh <id>

set shell := ["bash", "-euo", "pipefail", "-c"]

# Full local check (pytest, kmp unit + architecture tests, codemap, file sizes); logs in build/logs/.
check:
    bash scripts/check.sh

# Assemble and install the debug app on the connected device.
app:
    cd kmp && ./gradlew -q :android:assembleDebug :android:installDebug

# One lesson, start to finish: dry-run -> generate (asks before paid TTS) -> publish -> upload.
lesson ID:
    bash scripts/lesson.sh {{ID}}

# Write build/bundles/lesson_<ID>.bundle.md: story source + checklist + continuity, for any AI to write/revise.
export-lesson ID:
    mandarin-course export-lesson {{ID}}

# Import an AI's reply: build + validate + audit + dry-run count, never paid TTS.
import-lesson ID FILE:
    mandarin-course import-lesson {{ID}} {{FILE}}

# Record Roborazzi goldens, only after the user approved the screen (see android-ui-testing skill).
approve-screens:
    cd kmp && ./gradlew -q :android:recordRoborazziDebug
