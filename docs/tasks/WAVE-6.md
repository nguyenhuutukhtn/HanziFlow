# Wave 6 — lesson 2 device feedback: fixes, copy, text notes

Source: user device check of Wave 5 (2026-10-24): stale lesson 1 title in lesson 2, Nghe reveal shows only pinyin, no word tap in lesson 2 (no vocab, `items.json` empty for `modes_v1`), no way to copy a sentence, no written explanations.
Decisions (user, 2026-10-24): copy = long-press a line, **hanzi only** (to paste into a translator); explanations as **text in the app** ("nghe không hiểu mở app coi được liền"), not new audio.

| # | Task | Owns (short) | Depends on | Agent | Checkpoint |
|---|---|---|---|---|---|
| 1 | [B1](B1.md) title bug, hanzi always shown, long-press copy | ui/player | — | Codex sol · medium, fresh | — |
| 2 | [B2](B2.md) lesson 2 vocab + notes (lessons 1, 2) | lessons/*_vocab, *_notes | — | Codex sol · medium, fresh | ✅ `lesson-review` on DeepSeek (different vendor), then PM |
| 3 | [B3](B3.md) publish words + notes.json, shared model, republish | pipeline, contract docs, kmp/shared | B2 | Codex sol · medium, fresh | ✅ PM (contract change) |
| 4 | [B4](B4.md) "Ghi chú" screen + word tap in lesson 2 | data, ui/notes, ui/player, ui/modes | B1, B3 | Codex sol · medium, continue B1 window if < 150k | ✅ PM, then user |

B1 and B2 are independent: run B1 → B2 (one agent at a time), then call PM for the B2 content review.

Device check (user, after B4): `just upload` (lesson 1 new version + lesson 2 v3; dry-run first), `just app`, then:
1. Lesson 1 → lesson 2: header shows "你在哪儿？".
2. Nghe "Hiện câu": hanzi + pinyin of the current line.
3. Long-press a line → "Đã sao chép"; paste in a translator: hanzi only.
4. Lesson 2 Hiểu: tap a word → meaning sheet.
5. "Ghi chú" (player header and mode picker): Mẫu câu / Câu hỏi / Từ vựng read well; audio keeps playing.
6. Lesson 1 "Ghi chú": 7 patterns.

Launch prompt (B1, fresh):
```text
Read AGENTS.md and docs/tasks/B1.md, do the task. Work through to the Acceptance list; do not report partial unless genuinely blocked. Run Gradle for real (approve any permission prompt); never report Kotlin work without compiling.
Before each major step run: bash scripts/agent_ask.sh B1 --inbox (new notes = instructions).
Missing information? Run: bash scripts/agent_ask.sh B1 "<question>" and use its answer instead of guessing.
```
