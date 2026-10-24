# Wave 6 — single-session run (user, 2026-10-24: one strong agent does the whole wave)

Launch in a fresh Codex IDE window (stop any running B1 agent first; its partial work stays in the tree and is picked up).

```text
Read AGENTS.md, then docs/tasks/WAVE-6.md. You will do all of Wave 6 in this one session, in this order:
B1 → B2 → B5 → B6 → [PAUSE] → B3 → B4. Each task's brief is docs/tasks/<ID>.md; read each brief only when you start it.

Rules for the whole run:
- B1 may already be partly done in the working tree (git status): keep what fits the brief, finish the rest. B1 includes step 4 (Nghe shows the whole transcript, not one cue).
- Per task: write tests first, implement, run that brief's Verify until it passes, then commit only that task's files by exact path (never git add -A / commit -a), message "<ID>: <summary>" ending with the repo's usual Co-Authored-By line if one is configured. Do not move on with a failing Verify.
- Run Gradle for real (approve any permission prompt). Never claim Kotlin works without compiling it.
- scripts/check.sh must be all ok before each commit (if only the web smoke step times out, re-run it once).
- Update docs/STATUS.md and add one docs/token_log.md row per task (run scripts/quota.sh at start and end of the run; per-task delta is fine as an estimate).
- Never print secrets. Never run a non-dry-run generate, never run upload.
- Before each task run: bash scripts/agent_ask.sh WAVE6 --inbox (new notes = instructions).

PAUSE after B6 (stop and write in chat, then wait for my reply — do not continue on your own):
1. The B6 dry-run line for tu_vung (new requests, cached, characters).
2. Every invented example / uncertain meaning from B2 and B5 (by id / row ref).
3. "Ready for review + paid generate."
I will run the cross-vendor content review and the paid generate myself, then reply with fixes (if any) and "continue".
After my reply: apply any content fixes I paste (re-import modes if 09_modes.md changed), then B3 (publish lesson 1 next version + lesson 2 v3 locally, no upload) and B4.

Final report: one block per task in the AGENTS.md report template (≤ 12 lines each), plus the commit hashes, plus the user device checklist from WAVE-6.md.
```

## At the pause (user)

1. Content review on a different vendor (DeepSeek):
```bash
deepcode -x -p "Use the lesson-review skill rubric, but review only: the '## Từ vựng' section of content/lesson_002/09_modes.md (Chinese only, beginner, known characters in content/lesson_002/00_known.md), lessons/lesson_002_vocab.json, lessons/lesson_001_notes.json, lessons/lesson_002_notes.json. Check naturalness, correctness of pinyin/meaning_vi, whether explanations are simple enough, and any Vietnamese leaking into the Từ vựng spoken text. Note: the author avoided the story's A座/B座 because of an over-strict no-Latin check (PM error, Latin A/B is fine): flag rows v39, v69, v120-v124, v143 where restoring the story's A座/B座 would be more faithful. Also check the invented examples v10, v11, v22, v28, v38-v40, v69, v106, v122-v124, v143 and the explanation lines v71-v106. Report findings by row ref / id with a proposed fix; do not edit files. Keep it short." > build/logs/WAVE6.review.out 2>&1
```
2. Paste the findings to the agent (or to PM first if unsure which to accept).
3. Paid TTS (only after the review is applied): `.venv/bin/mandarin-course generate lessons/lesson_002_tu_vung.json --dry-run` → check the count → same command without `--dry-run`. Listen to the track.
4. Reply "continue" to the agent.
