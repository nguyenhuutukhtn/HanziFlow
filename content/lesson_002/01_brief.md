# 01 — Lesson 2 content brief

Process and prompts: `content/workflow.md`. What the learner knows: `00_known.md`.

## Learner (user, 2026-10-22)

- Vietnamese speaker. Finished lesson 1, understood and imitated ~70%.
- Listens everywhere: at work with the screen on (reading characters), outside with
  headphones (pauses on lines they don't get), free time for speaking/pronunciation.
- Goals, any of: listening, speaking, new words/phrases, characters.
- **This is a communication course, not a grammar course** (user, 2026-10-22): no grammar
  limits; content is judged by how natural and common the spoken Chinese is.
- Meaning comes fast (Hán-Việt); **characters are the slow part**.
- Lesson 1 was an on-ramp to the method. The learner is now used to learning words,
  **phrases (cụm từ)** and longer sentences: lesson 2 steps up, not repeats lesson 1's level.

## Topic

**Chosen by the user** (step 2 is skipped unless the user asks for ideas): a familiar
everyday situation with **high-frequency spoken sentences** the learner will meet again
elsewhere (office or daily life). Not textbook greetings. Cast: see "Chosen topic"
(new characters for new voices). The user writes the topic, in 1–3 lines, under
"## Chosen topic" at the end of this file.

## Constraints

- Story shape like lesson 1: one narrator, short lines from the characters.
- No tense/grammar restrictions (user, 2026-10-22): Vietnamese and Chinese grammar are close enough that the learner understands structures without lessons. Write whatever natural, common spoken Chinese needs (了, 过, 就, 能…); limits are on new words/characters only.
- **Learning unit = phrase**, not single word: core items are reusable chunks
  (e.g. 你想吃什么, 要不要一起…), each made of known or new words.
- Sentence length steps up: average ~8–12 characters, longest ~20 (lesson 1: 6 avg).
  Long sentences are built from phrases the learner already heard on their own.
- Vocabulary balance and character budget: `content/workflow.md` → "Vocabulary balance".

## Chosen topic

**Phone call: can't find each other at the partner's building** (user + PM, 2026-10-22).
New cast (user: new voices; 安娜/李明 keep their lesson 1 voices and stay out of this
lesson): 王芳 Wáng Fāng (Vương Phương, female, speaker id `wang_fang`) and 陈东 Chén Dōng
(Trần Đông, male, `chen_dong`), colleagues at the same company. At conversion time the
pipeline's role→gender table (`voice_registry.py`, today only anna/li_ming) needs these two.
Narrator (`coach_zh`): **female voice** this lesson (user, 2026-10-22: lesson 1's was male),
and audibly different from 王芳's voice. Gender for coach_zh is per lesson, so conversion
needs a per-lesson role→gender override, not a global entry.

王芳 arrives at a partner company's building for a meeting, has no access card and can't
get in, and calls 陈东 (already upstairs, 8th floor). 陈东 comes down to fetch her but can't
find her in the lobby. Twist: she is standing at building B across the street; the meeting
is in building A. Mostly a phone call (voice only), plus short narrator lines.
- Key pattern the story repeats: 在 + place / 在哪儿 (where someone is).
- Facts for circling Q&A: who is where, which building (A还是B), which floor, card or no card,
  how many minutes to wait, who comes down.

## Modes (replaces one long linear track)

The learner picks a mode by situation; any order after the first Hiểu.

| Mode | Content | Vietnamese | Length |
|---|---|---|---|
| Hiểu | story + meaning + core word gloss | yes (only here) | 6–8 min |
| Nghe | story + 3–4 one-detail retellings | no, narration included | 6–10 min |
| Phản xạ | circling Q&A with answer gaps | no | 6–10 min |
| Nói | chunk build-up + shadowing with imitation gaps | no | 5–8 min |

Each Chinese-only mode opens with one fixed short line naming the mode, so it works
when started cold.

## Comparison with lesson 1 (user tracks, informal)

Minutes listened voluntarily in 7 days · day the Chinese-only story is fully understood ·
% of Phản xạ questions answered inside the gap.
