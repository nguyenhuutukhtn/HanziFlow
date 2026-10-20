# AGENTS.md — entry point for any AI agent in this repo

Codex, Cursor, Claude Code, DeepSeek or any other agent: read this, then only the files your task needs.

## What this is

Mandarin listening/speaking course for a Vietnamese-speaking learner.
- **Pipeline (Python, `src/mandarin_course/`, CLI `mandarin-course`):** lesson JSON → ElevenLabs TTS (cached) → FFmpeg → `publish` (versioned app package) → `upload` (Supabase Storage).
- **App (`kmp/`):** Kotlin Multiplatform `shared` logic + Android app (Compose, Media3). Web later. Supabase for auth, progress sync and lesson packages.

## Start here

- [`docs/STATUS.md`](docs/STATUS.md) — what's done, what's next, open decisions.
- [`docs/CODEMAP.md`](docs/CODEMAP.md) — one line per source file; open only the files you need.
- [`docs/workflow.md`](docs/workflow.md) — scripts first, AI as fallback.
- [`docs/tasks/`](docs/tasks/README.md) — one brief per task; if you were given a task ID, read `docs/tasks/<ID>.md` and work from it.
- By task type, read only:
  - **Lesson content / curriculum / builder:** [`docs/content_rules.md`](docs/content_rules.md).
  - **App or pipeline code:** the matching file in [`docs/architecture/`](docs/app_architecture.md) (index in `docs/app_architecture.md`).
  - **Android UI:** [`docs/ui/README.md`](docs/ui/README.md) + only the screen files you touch, and the skill [`.claude/skills/android-ui-testing/SKILL.md`](.claude/skills/android-ui-testing/SKILL.md).

## Hard limits

- Never print, log or write secret values (`.env`: ElevenLabs, Supabase keys). The service-role key and TTS keys never go into `kmp/` or any client.
- Never make paid TTS calls without the dry-run count and explicit user confirmation. `publish` never calls ElevenLabs.
- Do not commit unless the task says so. Other agents may share the working tree: stage only your own files.

## Token budget (app and pipeline tasks)

- Read [`docs/workflow.md`](docs/workflow.md): repeated steps are commands, AI is the
  fallback; command output is short by default; AI edits small sources, never
  generated files; humans do listening QA, visual approval and quick edits.

- Don't read large generated or data files unless the task needs them: `publish/`,
  `cache/`, `output/`, `**/build/`, `kmp/shared/src/commonTest/resources/`, and
  the full `lessons/*.json` (query them with a short script instead). These are
  listed in `.cursorignore`.
- Run Gradle with `-q` and show only the tail of failures. Use `just check` (pytest + kmp shared/android unit + architecture tests + codemap + file-size audit, one line per step) instead of re-deriving the command list; `just app` assembles+installs debug; `just lesson <id>` runs dry-run→generate→publish→upload for one lesson. `just`/`scripts/*.sh` work the same either way — `just` isn't required.
- UI changes in kmp/android: follow [`.claude/skills/android-ui-testing/SKILL.md`](.claude/skills/android-ui-testing/SKILL.md) (JVM Compose + Roborazzi screenshot tests; look at diffs only on failure). `just approve-screens` records goldens.
- Device checks: prefer text (`adb shell dumpsys media_session`, `uiautomator dump`
  grepped, logcat filtered) over screenshots; at most 3 screenshots per task,
  saved under `kmp/build/screenshots/`. Visual regressions belong in local
  screenshot tests, not repeated device captures.
- Do the verification yourself (build, tests, lint, architecture tests) before
  reporting; don't hand unverified work back for review.
- At the end of a task, update [`docs/STATUS.md`](docs/STATUS.md) (Done / Next /
  Open decisions) in a few lines, and add a row to [`docs/token_log.md`](docs/token_log.md).
- Tasks run strictly one at a time (code and content alike), so each token-log row
  measures one task.

## Task reports

Report in at most 12 lines, in this shape, and nothing else:

```text
Result: done | blocked (why) | partial (what's missing)
Verified: <commands run> → <pass/fail counts>; device: <what, how> or "not run"
Changed: <files or folders, one line>
Deviations: <from the prompt/spec, or "none">
Needs user: <decisions or manual steps, or "none">
Tokens: <input/output or total if your tool shows it, else "?">; biggest consumer: <what used most>
```
