# Task briefs

One file per task: `docs/tasks/<ID>.md`. The PM session writes it; any agent (Codex, DeepSeek, Cursor, Claude) executes it after reading only `AGENTS.md` + the brief + files the brief names. No chat history needed.

Lifecycle: `todo` → `in-progress` → `review` → `done` (set in the brief's `Status:` line). After `done`, the summary lives in [STATUS.md](../STATUS.md); delete the brief once the task is committed.

## Template

```markdown
# <ID> — <title>
Status: todo · Suggested agent: <tool>/<model> (<effort>) · Session: <id or new> · Size: S/M/L · Device check: user | agent (<the scripted check>)
Wave: <n or none> · Owns: <files/dirs this task may edit; `docs/STATUS.md` + `docs/token_log.md` are always implied> · Depends on: <IDs or none> · Checkpoint after: yes | no

## Goal
<1–3 sentences: what changes for the user/pipeline.>

## Read only
<files/sections; nothing else unless blocked>

## Do
1. ...

## Acceptance
- <observable checks>

## Verify
<exact commands, first line `source .venv/bin/activate`; the agent runs them and pastes the output (≤ 15 lines) into the report>

## Out of scope
<what not to touch>
```

Then: update STATUS.md + a token_log.md row, report with the AGENTS.md template, do not commit.

## Writing Acceptance + Verify

- Check **content, not only counts**: name concrete expected values (`听 = tīng, hsk 1`, `走 = tẩu`), not just "≥ 95% non-empty". Coverage passed on data that was wrong (C1-fix: 听 = "smile").
- Tests named in Acceptance are part of the deliverable, not polish (W2b: UI done, 0 of 5 tests → fix round). Workers write them before styling details; briefs for UI tasks say "write the Acceptance tests first". Don't ask jsdom to prove CSS layout (media queries, visibility): W2b's 390px test could only check that classes exist — leave layout to the user's visual check.
- Verify is a script that prints those values and exits non-zero on mismatch, so the PM re-runs it instead of reading code.
- Data tasks: sample at least 5 specific records spread across the dataset, not only the first entries.
- **Pipeline-generated data a client parses (S1/S2: 3 fix rounds):** the brief must require (1) a test that parses the REAL published file, or a mechanical extract of it (script-derived, include known odd entries, e.g. `懂` has float medians), never hand-picked or hand-written samples; (2) every new catalog/package field present in the contract fixtures copied from a real `publish/` run; (3) a model type distinct per file shape (catalog metadata `{sha256,bytes}` is not a package `Clip` that has `file`); (4) a network/parse failure of an optional feature must hide that feature only, never crash or drop other state.
- **API/facade for another client (W1a-fix):** list in the brief the existing consumer's call sites to mirror (e.g. Android `TranscriptPanel.kt:107-225`) and require every field/iteration they use; a facade that passes its own tests can still miss what the next task needs.
- **DOM events in jsdom (W2c-fix):** `fireEvent.scroll` passes on any element, even one that never scrolls in a browser. For scroll/resize/focus behaviour the brief names the real target (e.g. "the page scrolls: listen on `window`") and the test dispatches on that target.
- **Negative probes (W2d-fix):** a "break X → Verify prints FAIL …X…" check must be satisfiable only by the real error text. Forbid hard-coded messages that contain the expected keyword (W2d-fix's smoke printed "Tooltip provider error" for any missing element); PM re-runs the probe at review.
- **Pipeline commands (M3: `generate` crashed on a speaker label the unit tests never hit):** Verify runs the real CLI path on the real lesson files, not only the functions underneath. For `generate`: `.venv/bin/python3 scripts/fake_generate.py lessons/<files>` (fake TTS, scratch dirs, no cost) must print `ok fake generate`.
- **New content shapes vs shared parsers (M4: tokenizer rejected `A座` and erhua `哪儿` only at the real publish):** a contract/format brief that introduces new text shapes runs the real lesson files through every parser downstream (tokenizer, tones, characters) in its Verify, so gaps surface in the contract task, not two tasks later. A parser extension also lists the near-miss cases it must NOT change (M4: `儿子 ér zi` broke).
- **Existing behaviour the brief relies on (M5: "restore the saved track" — HEAD had none, the worker invented a global key):** PM greps HEAD for it before writing; if it does not exist, the brief either specifies it (key, scope) or drops it.
- **"Use the real fixture" in Acceptance (M5: test used an inline catalog):** Verify greps the test file for the fixture path and for each named expected value (e.g. `noi.mp3`), so a hand-written substitute fails loudly.
- **Tests near secrets (X1: a test read the real `.env` once keys existed and printed a key fragment):** script tests use `tmp_path` env files and `monkeypatch.delenv` every key name they assert on — the pipeline's `config.py` loads the real `.env` into `os.environ` during a full pytest run.
- Device check order for features that need downloaded data: confirm the data is uploaded (`curl` the public URL, HTTP 200) before blaming the app.

## Agent choice (cheapest that fits)

- Mechanical Python/test/docs edits with a precise brief → DeepSeek or Codex.
- Multi-file Kotlin/Compose, device checks → Cursor or Claude.
- Judgment (spec, review, curriculum) → PM session.
- Code review of a finished task: Codex in a fresh session on a different tier, read-only → DeepSeek last (see pm-session "Checkpoint review"). Lesson content: reviewers from a different vendor than the writer.
