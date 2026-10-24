# Wave 7 — "Giải thích" a line with AI, in-app chat, cached per line

Feature decided with the user 2026-10-24 (STATUS "Open decisions" → "Sentence explain + chat"):
personal app, one user. Long-press any Chinese transcript line (every mode, hidden lines included)
→ menu (Giải thích / Sao chép) → bottom sheet with a ChatGPT-style explanation in Vietnamese →
chat box for follow-ups. App calls an OpenAI-compatible chat API directly (key in
`kmp/local.properties`, AGENTS.md exception). Whole thread saved locally by lesson + `line_id`;
reopening the line shows it with no API call. No pre-generation.

| Order | Task | Owns | Depends on | Agent | Checkpoint after |
|---|---|---|---|---|---|
| 1 | [X1](X1.md) prompt + provider comparison script | `prompts/`, `scripts/llm_compare.py`, `tests/test_llm_compare.py` | none | Claude Haiku subagent | no (Haiku task-review) |
| 2 | [X2](X2.md) Android data layer: config, chat client, Room thread store, ViewModel | `kmp/android/.../ai/`, `CourseDatabase.kt`, `build.gradle.kts`, `libs.versions.toml`, tests | X1 (prompt file) | Claude Sonnet subagent · medium | yes — X3 builds on it |
| 3 | [X3](X3.md) Android UI: line menu, explain sheet, chat input | `kmp/android/.../ui/explain/`, `TranscriptPanel.kt` (long-press only), screenshot tests | X2 | Claude Sonnet subagent · medium | yes — end of wave |

User steps in between: create Gemini + Groq keys (`.env` for X1, `kmp/local.properties` for the
app), run `.venv/bin/python3 scripts/llm_compare.py` and pick the provider, then the device check
after X3.
