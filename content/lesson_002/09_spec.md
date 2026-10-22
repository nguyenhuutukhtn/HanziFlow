# 09 spec — four modes for lesson 2

Input: `03_core.md` (approved script, section "Script"), `01_brief.md` ("Modes"),
`00_known.md` (lesson 1 words/characters). Output: `09_modes.md` with sections
`## Hiểu`, `## Nghe`, `## Phản xạ`, `## Nói`, `## Coach words`, `## Open doubts`.

## Rules for all modes

1. **Every Chinese line counts** — narration, instructions, questions, answers,
   explanations. Allowed words only:
   (a) words in the 03_core.md script;
   (b) lesson 1 words (`00_known.md` "Known words");
   (c) at most **6 new coach words for all four modes together**, listed under
       `## Coach words` (word | pinyin | Vietnamese | Hán-Việt), each used repeatedly.
   Anything else: say it another way.
2. No tense/grammar restrictions (user, 2026-10-22): Vietnamese and Chinese grammar are close enough that the learner understands structures without lessons. Write whatever natural, common spoken Chinese needs (了, 过, 就, 能…); limits are on new words/characters only.
3. Instructions prefer lesson 1 words: 我们听故事。/ 你回答。/ 跟我说。/ 再听一遍。/ 再说一遍。
4. Line format (one per line, inside a ``` block per section):
   `ref | speaker | 中文 | pinyin (with tone sandhi) | Tiếng Việt`
   speaker: narrator / wang_fang / chen_dong / coach_vi (Hiểu only) / gap.
   A `gap` line: `qN.g | gap | — | — | —` (learner answers or repeats there).
5. Vietnamese **only in Hiểu**. In the other modes the Tiếng Việt column is still filled
   (for the on-screen transcript) but no `coach_vi` lines.

## Hiểu (6–8 min) — refs h…
1. coach_vi intro, 2–3 Vietnamese sentences.
2. Script line by line: the Chinese line, then coach_vi saying its meaning.
3. Gloss every new word/phrase of the script: the Chinese word, coach_vi short meaning
   (with Hán-Việt when it helps guessing), an example line from the script.
4. Short Vietnamese explanation of 在 + place / 在哪儿 with 3 script examples.
5. Script once more, Chinese only.

## Nghe (6–10 min, Chinese only) — refs l…
1. Fixed short opener (e.g. 我们听故事。).
2. Script once.
3. Third-person retelling by narrator (no dialogue): 王芳在……，她……
4. 3–4 retellings changing ONE detail each, opened by a short "this time" line using
   allowed words. Ideas: 陈东在五楼 instead of 八楼; 王芳有卡; 咖啡店在A座旁边 (so she is
   at the right building); 陈东 is the one at the wrong building. Short, logically consistent.
5. Script once more.

## Phản xạ (6–10 min, Chinese only) — refs q…
1. Fixed short opener (e.g. 你回答。).
2. 40–50 questions circling the story facts: yes/no (吗, 是不是), choice (还是:
   王芳在A座还是B座？), short wh- (谁, 哪儿, 什么, 哪个, 几). Recycle 还是, 谁, 知道, 名字,
   同事 where natural.
3. Per question: narrator question → gap → narrator short answer → narrator full answer.
4. Easy (yes/no) to hard (wh-); each question builds on the previous ones.

## Nói (5–8 min, Chinese only) — refs s…
1. Fixed short opener (e.g. 跟我说。).
2. Build-up for the 8 longest/most useful script lines: back-chain from the END of the
   sentence (进不去 → 没有卡，进不去 → 我没有卡，进不去), 3–4 steps per line, each step a
   line + a gap line.
3. Shadowing: every wang_fang / chen_dong line of the script, each + a gap line (no narrator).

## Self-check before finishing
Run a throwaway Python snippet (do not add it to the repo) that collects every Han
character in 09_modes.md and prints any not in: the 03_core.md script + 00_known.md known
characters + your `## Coach words`. Also print any
coach_vi line outside Hiểu. Fix until all three are empty; paste the output in the report.
