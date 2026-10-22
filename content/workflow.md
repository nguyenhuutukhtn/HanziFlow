# Content workflow (chat-based, any AI)

How a lesson's **content** is made, before anything goes into the app. Every step
is one chat (any AI: ChatGPT, Claude, DeepSeek, Gemini…) that reads files and
writes one file. Chats never rely on history; the files are the memory.

Folder: `content/lesson_<id>/`. Converting finished content into
`lessons/src/*.story.md` + audio is a separate, later (code) step.

## Rules for every step

- **Top priority: natural, common spoken Chinese** (user, 2026-10-22). It outranks every
  count, band and recycle target below: a forced line to hit a number is a defect, an extra
  word in a natural line is fine. Reviews flag unnatural/rare phrasing first.

- One step = one output file, named `NN_<name>.md`, ending with a section
  `## Open doubts` (things the next step must check; "none" if none).
- **Writer and reviewer are different AIs.** A model does not catch its own mistakes.
- Reviewers report findings by line `ref`; they don't rewrite.
- Max 2 rounds writer↔reviewer per step; still failing → PM.
- The user decides what they can judge (topic, usefulness, learner feeling);
  Chinese and Hán-Việt correctness come from cross-AI checks + an outside reader.
- ★ = PM checkpoint: paste the files' paths to the PM session.

### Line format for Chinese content (all files from step 5 on)

```
ref | speaker | 中文 | pinyin | Tiếng Việt
```
`ref` is short and unique (`s3`, `q12`, `c4`). One sentence per line. Every
Chinese line counts — story, narration, coach talk, questions, answers.

### Vocabulary balance (lesson 2 on; replaces "8–12 new words")

Lesson 1 problem: 73 new words, but 的 516× / 名字 139× / 袋 110× while 21 words
were heard only 1–2 times (all coach words: 讲, 回答, 懂, 关系…). Every Chinese word
must be in exactly one tier below with its exposure band:

| Tier | What | Count / lesson | Heard per word |
|---|---|---|---|
| Core phrases | reusable chunks of the situation (你想吃什么, 要不要…) | 10–15 | 10–30 |
| Core words | new words inside those phrases / the story | 20–30 | 10–40 |
| Coach | new words for narration, instructions, questions | ≤ 6 | ≥ 8 (and reused in later lessons) |
| Recycle | lesson 1 words, **weak ones first** (heard < 10 in `00_known.md`) | 15–20 | ≥ 5 |
| Function | 的 是 这 不 吗 有 你 我 他 … | free | no cap |
| Names | 安娜 李明 … | free | no cap |

- **Bands are guidance, not gates** (user, 2026-10-22): a few words a bit over/under,
  or a few extra new words, are fine. Fix only clear outliers (a core word heard 1–2
  times, a content word in the hundreds without reason). No fix round just for counts.
- **Cap:** no content word (noun/verb/adjective) above ~60 hearings; past that, vary the sentences.
- **No stray words:** a Chinese word that is in no tier is either replaced by a known word or added to a tier and brought up to its band. Nothing heard only 1–2 times.
- **Characters are the bottleneck, not meaning** (Vietnamese ↔ Chinese share Hán-Việt):
  new characters ≤ 50 per lesson (lesson 1: 83, mostly coach words). Prefer new words whose characters are
  already known or whose Hán-Việt is transparent to a Vietnamese speaker.
- **AIs miscount characters** (lesson 2 step 3: called 在/哪/儿 known and 上/说 new). Writers
  only *list* items; character and length counts come from a script (PM runs it until a
  `just` command exists). Prompts tell the writer which characters are known.

## Steps

| # | Step | Who | Reads | Writes |
|---|---|---|---|---|
| 0 | Known-knowledge pack | PM (script) | — | `00_known.md` |
| 1 | Content brief | PM | user decisions | `01_brief.md` |
| 2 | Topic: the user writes it in `01_brief.md` "Chosen topic"; brainstorm prompt below only if the user wants ideas | user (AI A optional) | 00, 01 | `01_brief.md` / `02_topics.md` |
| 3 | Core sentences + word tiers | AI A | 00–02 | `03_core.md` |
| 4 | Vocab review vs lesson 1 → ★ | AI B | 00, 01, 03 | `04_core_review.md` |
| 5 | Story script | AI A | 00, 01, 03 | `05_story.md` |
| 6 | Language review | AI B | 05 | `06_lang_review.md` |
| 7 | Learner role-play review (+ the user reads too) | AI C | 00, 05 | `07_learner_review.md` |
| 8 | Fix flagged lines → ★ | AI A | 05–07 | `05_story.md` (v2) |
| 9 | Four modes | AI A (one chat per mode is fine) | 01, 03, 05 | `09_modes.md` |
| 10 | Characters: Hán-Việt + meaning, two AIs independently | AI A + AI B | 03, 09 | `10_hanzi_A.md`, `10_hanzi_B.md` |
| 11 | Final review + exposure check → ★ | AI B or D, outside reader for flagged lines | all | `11_final_review.md` |

## Prompts (copy into a new chat, attach the files named)

**Step 2 — topics**
```
You help write a Mandarin listening course for a Vietnamese learner. Read 01_brief.md
(goals, constraints) and 00_known.md (what they already know). Propose 6 everyday
situations they will really meet again (office/daily life). For each: 1 line why it is
frequent, 3 sample sentences (中文 | pinyin | Tiếng Việt) using mostly known words, and
the 3–5 new words it needs. Write the file 02_topics.md, end with "## Open doubts".
```

**Step 3 — core sentences + tiers**
```
Read 01_brief.md (topic under "Chosen topic"), 00_known.md and workflow.md "Vocabulary
balance". Write 03_core.md:
1) 10–15 core phrases people really say in this situation, each with 1–2 example
   sentences, line format: ref | speaker | 中文 | pinyin | Tiếng Việt.
2) Tiers: Core phrases, Core words, Coach, Recycle (from 00_known.md, weak words first),
   each with pinyin, Vietnamese, Hán-Việt; new characters marked.
3) New-character count (target ≤ 50). End with "## Open doubts".
```

**Step 4 — vocab review (different AI)**
```
You review, you don't rewrite. Read 00_known.md, 01_brief.md, 03_core.md. Report by ref/word:
- words listed as new that are already known (00_known.md), and known words missing from Recycle;
- words too rare or too formal for daily talk; better everyday alternatives;
- tier counts and new-character count vs the brief's limits;
- sentences that are not natural spoken Mandarin; wrong pinyin/tones/translation.
Severity per finding: block | fix | note. Write 04_core_review.md.
```

**Step 5 — story**
```
Read 01_brief.md, 00_known.md, 03_core.md (approved). Write 05_story.md: the story in
lesson 1's shape (one narrator, short lines from the characters, present time), using
every core sentence at least once and only words in 03_core.md tiers or 00_known.md.
Line format: ref | speaker | 中文 | pinyin | Tiếng Việt. 20–35 lines. Then a word-count
table: each Core/Recycle word × times it appears. End with "## Open doubts".
```

**Step 6 — language review (different AI)**
```
Review, don't rewrite. Read 05_story.md. By ref: unnatural Chinese, wrong measure words or
word order, pinyin/tone errors, Vietnamese translation errors or awkward Vietnamese,
words outside the allowed list (03_core.md + 00_known.md). Severity block | fix | note.
Write 06_lang_review.md.
```

**Step 7 — learner role-play (different AI)**
```
Role-play: you are a Vietnamese learner who knows ONLY the words in 00_known.md and
finished lesson 1 understanding ~70%. Read 05_story.md line by line, Chinese only (cover
the Vietnamese). By ref: what you would not understand, sentences too long to repeat,
moments that are boring or confusing, and words you'd forget because they appear too
rarely. Then 3 lines: overall difficulty (too easy / right / too hard) and why.
Write 07_learner_review.md.
```

**Step 8 — fix**
```
Read 05_story.md and the findings in 06_lang_review.md and 07_learner_review.md. Change
ONLY the lines named in block/fix findings (keep refs), list each change as
"ref: old → new (finding)". Update the word-count table. Save as 05_story.md v2.
```

**Step 9 — four modes**
```
Read 01_brief.md ("Modes"), 03_core.md, 05_story.md v2. Write 09_modes.md with four
sections, line format as before, every Chinese line counted:
- Hiểu: story once in Chinese with Vietnamese meaning, gloss of each Core word.
  The only mode with Vietnamese.
- Nghe: the story + 3–4 retellings, each changing one detail (person, place, thing,
  number, a flipped fact). Chinese only, narration included.
- Phản xạ: circling Q&A over story facts (是不是 / 吗, 还是, short wh-questions):
  question, gap, answer. Chinese only.
- Nói: chunk build-up for 6–8 core sentences (short chunk → longer → full), then
  shadowing of the story. Chinese only.
Each Chinese-only mode starts with one fixed short line naming the mode, so it works
when started cold. End with an exposure table: every Core/Coach/Recycle word × count per
mode × total, flag anything outside its band.
```

**Step 10 — characters (two AIs, separately)**
```
Read 03_core.md and 09_modes.md. List every character not in 00_known.md "Known
characters". For each: character | pinyin | Hán-Việt | short Vietnamese meaning | words in
this lesson that use it. Mark any Hán-Việt you are not sure of with "?". Don't look at
another AI's list. Write 10_hanzi_<A|B>.md.
```

**Step 11 — final review (different AI)**
```
Review, don't rewrite. Read all files. Check: 1) every block/fix finding from steps 4, 6, 7
is resolved; 2) the exposure table in 09_modes.md against the brief's bands (spot-check 5
words by counting yourself); 3) 10_hanzi_A vs 10_hanzi_B — list every disagreement;
4) mode rules (Vietnamese only in Hiểu, each mode starts cold). List lines an outside
Chinese reader should check. Write 11_final_review.md.
```
