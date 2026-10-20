# Token log

One row per task (code or content), filled at the end of the task. Tasks run one at a time so each row is a clean measurement. Purpose: find which kinds of work burn tokens and change the process, not to account precisely.

How to read the numbers:
- Claude Code: `/cost` (or `/usage`) at the end of the session; one session per task.
- Cursor: Settings → Usage for the request range.
- DeepSeek: platform usage page for the day/time range.
- If the agent can't see its usage, it writes `?` and the user fills it in.

Columns: **Kind** = code / content / review / design · **Tokens** = input / output (or total, or cost) · **Biggest consumer** = what used most (reading big files, screenshots, retries, long lesson JSON, regeneration loops…) · **Fix** = process change to try next time.

| Date | Task | Kind | Agent / model | Tokens | Wall time | Biggest consumer | Fix |
|---|---|---|---|---|---|---|---|
| 2026-10-20 | L1 timeline sections | code | Claude Sonnet 5.5 | ? | ? | Reading the full timeline_guided.json (797 cues) once to understand cue/segment ordering before writing the enrich function | Query large JSON with a short script (jq/python) instead of Read; only needed a handful of sample cues |
| 2026-10-21 | V1 auto-versioning + prune | code | Claude Sonnet 5.5 | ? | ? | Designing+rewriting the mock-Storage test fixture (HEAD/POST/GET/list/delete handlers) to cover verify+prune, and updating existing tests whose assumptions the new behavior intentionally broke (hardcoded v2 paths, `--force` semantics) | Keep a small reusable fake-Storage helper module instead of rebuilding it per upload-related task |
| 2026-10-21 | V1-fix timeline version | code | Claude Sonnet 5.5 | ? | ? | Small, scoped task brief (docs/tasks/V1-fix.md) kept this cheap: read only the two named files plus the test file, no re-exploration needed | None — this is the shape to aim for (small task doc, narrow read list) |
| 2026-10-21 | T1 justfile + short output | code | Claude Sonnet 5.5 | ? | ? | `./gradlew -q tasks --all` dumped to a file then grepped (cheap) vs. the two full `check.sh` dry runs (pytest + shared:allTests + android:testDebugUnitTest) needed to verify line counts and the FAIL path for real, which is inherently slow (Gradle) rather than token-heavy | Continuing the same window (knew cli.py/publish.py/upload.py already) avoided re-reading them, as the brief suggested |
