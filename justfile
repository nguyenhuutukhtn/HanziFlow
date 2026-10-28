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

# Start the web player locally (builds the shared JS facade first).
web:
    bash scripts/web_build.sh
    npm --prefix web run dev

web-build:
    bash scripts/web_build.sh

web-deploy:
    bash scripts/web_deploy.sh

# Mirror publish/ to Supabase: dry-run first, then asks before uploading. Run `just app` first when the catalog format changed.
upload:
    .venv/bin/mandarin-course upload --dry-run
    @read -p "Upload for real? [y/N] " ok && [ "$ok" = "y" ]
    .venv/bin/mandarin-course upload

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

import-modes ID:
    mandarin-course import-modes {{ID}}

modes ID:
    bash scripts/modes.sh {{ID}}

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

# Follow a running worker agent's live log (PM launches them; Ctrl-C only stops the tail).
watch ID:
    agent-watch {{ID}}

# Kill every running worker agent (codex / deepcode / qwen) — take the session back.
stop-agents:
    agent-stop

task-start ID:
    agent-task start {{ID}}

task-pr ID:
    agent-task pr {{ID}}

task-done ID:
    agent-task done {{ID}}

code-delta:
    .venv/bin/python3 scripts/code_delta.py

# Push a note to a running worker (it reads them at each checkpoint): just tell C2-fix "dùng Spacing.md"
tell ID +MSG:
    agent-tell {{ID}} {{MSG}}

# Answer the question a worker is blocked on (see build/logs/<ID>.ask).
answer ID +MSG:
    agent-answer {{ID}} {{MSG}}

# What is the worker waiting for, if anything?
asking ID:
    agent-asking {{ID}}

# Install agent-kit (commands, Agent Monitor extension, skill), then reload the window.
agent-monitor-install:
    bash ~/Documents/source_code/personal/agent-kit/install.sh

# Fake worker run (~15 s, no quota) to check the Agent Monitor panel: just agent-fake T-fake
agent-fake ID:
    agent-fake {{ID}}
