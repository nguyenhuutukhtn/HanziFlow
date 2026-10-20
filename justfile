# Repeated steps become one command each (docs/workflow.md). Recipes are thin
# wrappers around scripts/ so they also run without `just` installed:
#   bash scripts/check.sh
#   bash scripts/lesson.sh <id>

set shell := ["bash", "-euo", "pipefail", "-c"]

# Full local check: pytest, kmp shared + android unit/architecture tests,
# codemap, file-size audit. Short output; full logs in build/logs/.
check:
    bash scripts/check.sh

# Assemble and install the debug app on the connected device.
app:
    cd kmp && ./gradlew -q :android:assembleDebug :android:installDebug

# One lesson, start to finish: dry-run -> generate (asks before paid TTS) -> publish -> upload.
lesson ID:
    bash scripts/lesson.sh {{ID}}

# Record Roborazzi goldens (only after the user has approved the screen once;
# see .claude/skills/android-ui-testing/SKILL.md).
approve-screens:
    cd kmp && ./gradlew -q :android:recordRoborazziDebug
