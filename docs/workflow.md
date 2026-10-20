# Workflow: scripts first, AI as fallback

Decided 2026-10-20. Applies to code and content. Goal: spend AI tokens only on creative or judgment work; everything repeatable is a local command.

## Principles

1. **Repeated steps are commands, not prompts.** From the second time a step is done it becomes a `just` recipe or CLI command (generate, publish, upload, build, install, verify, approve screenshots, export/import lesson bundles). AI is called only when a command fails and the human can't fix it quickly, or for creative/judgment work. `justfile` recipes (`just check`, `just app`, `just lesson <id>`, `just approve-screens`) wrap `scripts/*.sh`, so they also run without `just` installed.
2. **Output sized as AI input.** Every command prints a short summary by default (result, counts, failures; ≤ 20 lines), writes detail to a log file, and offers `--json`. An AI reads a log only on failure, and only via grep/tail.
3. **AI edits small sources, never generated files.** Lesson content lives in a compact story source; `lessons/lesson_XXX.json` is a build artifact. Feedback to an AI is a short list (`line_id` + note), not a whole lesson.
4. **Humans do what is fast for humans:** listening QA, visual approval (screenshots, Roborazzi), vocab/translation edits, one-line text fixes, device checks (Bluetooth, lock screen, MIUI settings), interactive logins, taste decisions, running known commands, committing a green task.
5. **No CI service.** All checks run on this machine (`just check`) when needed.
6. **AI-agnostic content work.** Any online AI can write or revise a lesson; nothing depends on one provider's connectors.

## Content loop (target)

```text
just export-lesson 001        → one self-contained Markdown bundle:
                                 story source + condensed authoring checklist +
                                 patterns/coach phrases already taught + listening feedback
   ↓ paste/upload to any AI (pick whichever writes best; switch freely)
just import-lesson 001 <file> → validate rules + build lesson JSON; on failure print
                                 ≤ 20 lines of rule violations to paste back to the same AI
just lesson 001                → dry-run, generate (asks before paid TTS), publish, upload (scripts/lesson.sh)
listen in the app → "Báo lỗi câu này" → feedback/lesson_001.tsv (line_id, note)
   ↓ next export includes the feedback mapped to story-source lines
```

Compare AIs by logging each lesson-writing run in [token_log.md](token_log.md) (kind = content, agent = the AI used) plus a one-line quality note.

## Planned tasks

- **V1** — auto-versioning in publish, prune superseded versions in upload.
- **T1** — `justfile` (check, app, lesson, approve-screens) and short-by-default CLI output with `--json` and log files.
- **T2** — compact lesson story source + one generic builder (replacing per-lesson Python builder scripts) + `export-lesson` / `import-lesson` bundles + condensed authoring checklist. Before writing lesson 2.
- **T3** — in-app "Báo lỗi câu này" (debug build) → feedback TSV → mapped back to story-source lines in the next export.
