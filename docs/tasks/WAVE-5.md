# Wave 5 — Android plays `modes_v1` (lesson 2): mode picker + per-mode player

Planned 2026-10-23 (user: mobile before web). **Paused 2026-10-23** behind W3b; user re-ranks priorities before A1 launches. Decisions: separate mode picker screen; cards show
"when to use", progress, suggested next; Nghe hides transcript; Phản xạ countdown + hidden answers.
Nói recording **with pronunciation scoring** is deferred: research first, its own wave.
Agents run one at a time; user resets between tasks. Tier approved by the user: terra/sol · medium.

| Order | Task | Owns | Depends on | Suggested agent | Checkpoint after |
|---|---|---|---|---|---|
| 1 | [A1](A1.md) Contract: timeline `gaps` + shared `ModeInfo`, lesson 2 → v2 (no TTS) | modes.py, publish_modes, lesson_002 JSON, kmp/shared models + fixtures, content_package.md | Wave 4 committed | Codex terra · medium, fresh | ✅ PM (contract) |
| 2 | [A2](A2.md) Android: modes lessons, mode picker, local progress | data/, ui/modes, library, navigation, player (track open) | A1 | Codex terra · medium, fresh | — |
| 3 | [A3](A3.md) Player per mode: Nghe hidden, Phản xạ countdown | ui/player, playback (pause), shared gap helper | A1, A2 | Codex sol · medium, continue A2 window if < 150k | ✅ PM, then user on device |

Before A1 (user): `just app` on the phone, then `mandarin-course upload --dry-run` → `upload` (lesson 2 v1 to the CDN; web can show it).
After A1 checkpoint (user): `upload` again (lesson 2 v2 with gaps).

Device check (user, after A3, `just app`):
1. Library shows lesson 2 "你在哪儿？"; lesson 1 still opens straight into the player.
2. Open lesson 2: four cards with one-line descriptions and durations; "Gợi ý" on Hiểu.
3. Play Hiểu to the end (or seek near the end): back on the picker it shows "Đã xong", "Gợi ý" moves to Nghe.
4. Nghe: no transcript; "Hiện câu" shows just the current sentence.
5. Phản xạ: after each question a ring counts down; the answer text appears only when spoken; "Cần thêm thời gian" pauses in the gap.
6. Leave a mode halfway, reopen it: resumes where you left off. Kill the app: progress is still there.
7. Lock screen / headset: mode name shown, next/previous still work.

Launch prompt (A1, fresh):
```text
Read AGENTS.md and docs/tasks/A1.md, do the task.
Before each major step run: bash scripts/agent_ask.sh A1 --inbox (new notes = instructions).
Missing information? Run: bash scripts/agent_ask.sh A1 "<question>" and use its answer instead of guessing.
```
