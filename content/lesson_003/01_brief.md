# 01 — Lesson 3 content brief

Process and prompts: `content/workflow.md` (read "Rules for every step", "Independent
lessons", "Vocabulary balance"). Format type: `modes_v1` (like lesson 2).

## Learner

- Vietnamese speaker, beginner. Lessons 1–10 are independent and studied in any order:
  assume someone who has heard about one lesson of Mandarin (greetings, 我/你/是/不/吗/在,
  numbers). No checks against other lessons; repeating their words is fine.
- **Communication course, not grammar:** no grammar limits (了, 过, 就, 能… all fine);
  content is judged by how natural and common the spoken Chinese is.
- Meaning comes fast via Hán-Việt; **characters are the slow part**.
- Listens eyes-off most of the time (headphones, at work), opens the app only when lost.

## Constraints

- Story shape: one narrator (`coach_zh`) + two characters, mostly their dialogue, short
  narrator lines, present time.
- **Learning unit = phrase**: core items are reusable chunks people really say.
- Sentence length: average ~8–12 characters, longest ~20; long sentences are built from
  phrases already heard on their own.
- Vocabulary tiers and bands: `content/workflow.md` → "Vocabulary balance" (guidance, not gates).

## Chosen topic

**Lunch delivery, wrong dish — 午饭点错菜** (user, 2026-10-27; idea from `02_topics.md` #1).

Cast (new voices, speaker ids for the pipeline):
- 张静 Zhāng Jìng (Trương Tĩnh), female, `zhang_jing` — doesn't eat spicy food.
- 刘洋 Liú Yáng (Lưu Dương), male, `liu_yang` — orders lunch for both on a delivery app.
- Narrator `coach_zh`: male voice this lesson (lesson 1 male, lesson 2 female).
- No third speaker; anything the restaurant/courier says is reported by the characters or narrator.

Outline (the author fixes the details in `04b_facts.md`): colleagues at the office. 刘洋
orders 外卖 for both. It arrives; one box is a very spicy dish 张静 did not order. They work
out which box is whose, what was ordered vs what came, and solve it (swap, call the
restaurant, or another natural fix — author decides, one solution only).
- Key patterns to repeat: 你点的是什么 / 我点的是… / 送错了 / 太辣了 / 不吃辣 / 换一个 / A还是B.
- Facts for circling Q&A: who ordered, what each person ordered, what arrived, spicy or not,
  how many boxes, price/time if natural, how it was solved.

## Modes

Like lesson 2; any order after the first Hiểu.

| Mode | Content | Vietnamese | Length |
|---|---|---|---|
| Hiểu | story + meaning + core word gloss | yes (only here) | 6–8 min |
| Từ vựng | core words/phrases explained in simple Chinese | no | 6–10 min |
| Nghe | story + 3–4 one-detail retellings | no, narration included | 6–10 min |
| Phản xạ | circling Q&A with answer gaps | no | 6–10 min |
| Nói | chunk build-up + shadowing with imitation gaps | no | 5–8 min |

Each Chinese-only mode opens with one fixed short line naming the mode, so it works cold.

## Author experiment (user, 2026-10-27)

Lesson 2: ChatGPT/Codex wrote, review→fix went on too long. Lesson 3: **Qwen (qwen3.8-max,
Qwen Code) writes every author step**; reviewers are other vendors (DeepSeek, Codex sol,
Claude). After this lesson PM compares vs lesson 2 to pick the best author model.

| Step | Who |
|---|---|
| 3 core + 4b facts (one run) | Qwen |
| 4 core review ★ | DeepSeek |
| 5 story | Qwen |
| 6 language review | Codex sol |
| 7 learner role-play | Claude (subagent) + the user reads |
| 8 fix ★ | Codex luna (fix-apply, user 2026-10-27) |
| 9 modes | Qwen |
| 10 Hán-Việt | Qwen (A) + DeepSeek (B) |
| 11 final review ★ | DeepSeek + Claude (Codex applied step-8 fixes, so it does not review), outside reader for flagged lines |

Record per step in `content/lesson_003/log.md`: author run minutes, block/fix findings per
reviewer, fix rounds, quota delta (Qwen % of month, DeepSeek USD, Codex %).
