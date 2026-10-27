# Lesson 3 — author experiment log (Qwen author)

| Step | Who / model | Run min | Findings (block/fix/note) | Fix rounds | Quota delta | Notes |
|---|---|---|---|---|---|---|
| 2 topics | Codex luna medium | ~3 | — | 0 | Codex 5h ~0% | 8 topics; user chose #1 |

Qwen Lite plan: 93.4% remaining while L3-CORE was running (2026-10-27, baseline before/at step 3).
| 3 core | Qwen qwen3.8-max (CLI) | 15 (cap hit, 03 written at 14 min) | — | 0 | Qwen 93.4→91.5% (1.9%) | ~60k input tokens, 6 tool calls; cost is thinking/output, not CLI overhead; 04b not written |
| 4b facts | Qwen qwen3.8-max (CLI) | ~13 | — | 0 | Qwen 91.5→88.6% (2.9%) | 88 lines, F1–F19 + canonical answers + Nghe variants |
| 4 core review | DeepSeek (deepcode) | ~4 | 2 block / 12 fix / 10 note | — | DeepSeek ~0 USD shown (3.17) | block: 我给你钱→我转给你 (p15a, F14, V9); PM drops fix #9 (bands measured on examples only) |
| 5 fixes+story (combined) | Qwen qwen3.8-max (CLI) | 35 (cap hit) | — | failed | Qwen 88.6→85.5% (3.1%, wasted) | 2 provider retries, only thinking, no file written → split runs; cheap tier for fix-apply |
| 4 fixes (part A) | Qwen qwen3.8-flash (CLI) | ~3 | applied #1–8, #10 | 1 | Qwen 85.5→84.8% (0.7%) | PM script did punctuation (28 rows), Hán-Việt 付/吃, char list (96) |
| 5 story | Qwen qwen3.8-max (CLI) | ~20 | — | 0 | Qwen 84.8→80.3% (4.5%) | 35 lines, avg 10.3, 128 distinct chars, 15/15 core phrases |
| 6 lang review | Codex gpt-5.6-sol medium | ~2 | 2 block / 12 fix / 1 note | — | Codex 5h 0→6% | block: s13 contradicts F9 (刘洋 fetched), s24 换一个→换一份; many textbook narrator lines |
| 7 learner review | Claude Haiku subagent | ~2 | 0 block / 11 fix / 15 note | — | Claude plan (65k subagent tokens) | not fully blind; PM kept s1/s23 split, who-ordered-what (s8), pay confusion (s29/s31); dropped 点 ambiguity + once-only words (modes carry them) |
| 8 fix | Codex gpt-5.6-luna low | ~3 | applied 06 all block/fix + 3 of 07 | 1 | Codex 5h ~0 | 37 lines, avg 10.6, max 18, 141 distinct chars; who-gets-what put in s21 (not s8) |
| 11 fixes | Codex gpt-5.6-luna low | ~3 | applied both reviews except PM-kept items | 1 | Codex ~0 | missed Claude's Nghe V2 block → PM fixed l64 (1 line); PM demoted 2 stray ## headings |
