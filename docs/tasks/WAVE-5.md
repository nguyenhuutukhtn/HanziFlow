# Wave 5 — Android plays `modes_v1` (lesson 2): mode picker + per-mode player

Planned 2026-10-23 (user: mobile before web). Re-scoped and resumed 2026-10-23 (user: learn lesson 2 on the phone — listening, reflex, repeating phrase by phrase; complex things later). Decisions: separate mode picker screen; cards show
"when to use" + duration, resume per mode; Nghe hides transcript; Phản xạ/Nói countdown, hidden answers; "Lặp ×2" per cue.
Deferred: % progress / done / suggested next, Supabase progress sync, web mode UI.
Nói recording **with pronunciation scoring** is deferred: research first, its own wave.
Agents run one at a time; user resets between tasks. Tier approved by the user: terra/sol · medium.

| Order | Task | Owns | Depends on | Suggested agent | Checkpoint after |
|---|---|---|---|---|---|
| 1 | [A1](A1.md) Contract: timeline `gaps` + shared `ModeInfo` (labels, when-to-use), lesson 2 → v2 (no TTS) | modes.py, publish_modes, lesson_002 JSON, kmp/shared models + fixtures, content_package.md | Wave 4 committed | Codex terra · medium, fresh | ✅ PM (contract) |
| 2 | [A2](A2.md) Android: modes lessons, mode picker (lite), resume per mode | data/, ui/modes, library, navigation, player (track open) | A1 | Codex terra · medium, fresh | — |
| 3 | [A3](A3.md) Player per mode: Nghe hidden, Phản xạ/Nói countdown | ui/player, playback (pause), shared gap helper | A1, A2 | Codex sol · medium, continue A2 window if < 150k | — |
| 4 | [A4](A4.md) "Lặp ×2" per cue for Nói/Phản xạ | playback loop, ui/player chip, shared repeat helper | A1, A3 | Codex sol · medium, continue A3 window if < 150k | ✅ PM, then user on device |

Done 2026-10-23: lesson 2 v1 uploaded (Android still hides it until A2).
After A1 checkpoint (user): `just upload` (lesson 2 v2 with gaps).

Device check (user, after A4, `just app`):
1. Library shows lesson 2 "你在哪儿？"; lesson 1 still opens straight into the player.
2. Open lesson 2: four cards with one-line descriptions and durations.
3. Nghe: no transcript; "Hiện câu" shows just the current sentence.
4. Phản xạ: after each question a ring counts down; the answer text appears only when spoken; "Cần thêm thời gian" pauses in the gap.
5. Nói: a ring counts down in each repeat gap.
6. Nói/Phản xạ with "Lặp ×2": each phrase + its gap plays twice, also with the screen off; headset next skips to the next phrase.
7. Leave a mode halfway, reopen it (also after killing the app): resumes where you left off.

Launch prompt (A1, fresh):
```text
Read AGENTS.md and docs/tasks/A1.md, do the task.
Before each major step run: bash scripts/agent_ask.sh A1 --inbox (new notes = instructions).
Missing information? Run: bash scripts/agent_ask.sh A1 "<question>" and use its answer instead of guessing.
```
