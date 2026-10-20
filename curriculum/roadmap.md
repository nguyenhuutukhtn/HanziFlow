# Mandarin Course Roadmap

Draft structure for review. Phrasing, vocabulary choices, and example sentences below are placeholders to communicate intent — a native/fluent reviewer should correct naturalness before any lesson JSON is written from this.

## Method (applies to every level)

The detailed authoring standard is [`docs/lesson_authoring_guidelines.md`](../docs/lesson_authoring_guidelines.md). Every lesson script and review must follow it.

**Pivot (superseding the original scripted-dialogue design):** real listener feedback on the Lesson 1 trial (`lessons/lesson_001_mau.json`) found the two-character greeting-dialogue format too thin to support varied question drilling, and the retrieval-style Q&A too demanding for an absolute beginner. The course now follows the Effortless English / TPRS **mini-story** method instead:

- **Story first, grammar tracked, not taught.** A mini-story is chosen or written because it is a good, concrete, realistic story — not because it contains a target pattern. Grammar is checked *after* the story is written, as a simplicity/safety constraint on the author, never the reason the story exists. The order is: natural story → pick the words/lines it actually needs → check the grammar it uses is simple enough for where the learner is → design questions and variants around it. Picking a grammar point first and constructing a story to contain it produces the same stilted, textbook-dialogue feel this pivot is meant to fix — see `docs/lesson_authoring_guidelines.md`.
- A **mini-story** is a short, invented narration — not an authentic article or a scripted two-character dialogue — built around a realistic, adult-life situation (a relationship, an emotion, an everyday event) with enough concrete, checkable detail (who, what, where, how many, what color, in what order) to support heavy question drilling. It is not about animals or objects chosen for simplicity's sake; the situation should feel like something that could happen to a real person.
- Narrator count (one narrator telling the story vs. a cast of two or three characters) is an open design question. Prototype it **before** writing Level 1, not after — see "Open design questions."
- **Circling Q&A** is the core drill: the coach asks the same facts repeatedly in varied forms — yes/no, either/or (`A还是B`), and short wh-questions — so the learner can recognize and guess before they can produce. This replaces most full-sentence recall prompts for early lessons; recall-style retrieval ("answer before the model") is introduced later, once a pattern has already been circled.
- **Point of view retelling**: the same story is told again with one variable changed. Before a tense/aspect marker's own lesson has introduced it (e.g. `了` in week 12), only change subject, location, color, quantity, or a single flipped fact — never the tense, since retelling in an untaught tense would teach that structure through the back door. Tense/aspect becomes a valid retelling variable only from the week it is introduced onward.
- A short, single-line usage note replaces any grammar explanation — never a lecture. This matches the existing "no long grammar lecture" rule in the authoring guidelines.
- Lesson shape: vocabulary (brief) → mini-story (natural speed) → meaning mapping → circling Q&A → point-of-view retelling → closing natural replay.
- Active recall before the model answer still applies wherever recall drills are used, just later in the lesson than before.
- Old patterns resurface in new, varied contexts (spaced repetition by design, not flashcards).
- Audio is the primary channel; Pinyin supports listening, it never substitutes for it.
- One lesson = one week of real-life practice, not one sitting. Guided lessons 1–4 may be 25–35 minutes because Vietnamese teaching support is still dense; later guided tracks can shorten to roughly 15–25 minutes. Practice tracks remain short and replayable.

## Vietnamese support and phase-out

Vietnamese is temporary audio scaffolding, not a permanent second language in the course. Remove it gradually rather than at one fixed cutoff. Vietnamese translations should remain available in the app as optional reference material even after they disappear from the main audio.

**The readiness criteria below the table are the actual rule; the week numbers are only the typical pace for an on-track learner.** If a learner has not met the readiness criteria by the week a row implies, keep the previous row's level of Vietnamese support instead of advancing — never remove Vietnamese just because a calendar week arrived. The table is a planning estimate for the author, not a hard schedule to enforce in the audio.

| Weeks (typical pace, not a forced cutoff) | Vietnamese in guided audio | Vietnamese in practice audio |
|---|---|---|
| 1–4 | Full support for new material: Mandarin → concise Vietnamese meaning → Mandarin again. Instructions and scene setup are Vietnamese. | One Vietnamese setup for each scene on the first pass only. Later passes are Mandarin-only. |
| 5–8 | Translate only genuinely new patterns. Previously learned material is reviewed without Vietnamese. | Mandarin-only. |
| 9–12 | Use Vietnamese only to establish an unclear situation or explain a meaning that cannot be inferred reliably. Begin introducing simple Mandarin lesson instructions. | Mandarin-only. |
| 13–16 | Use simple Mandarin for normal lesson instructions. Vietnamese is an occasional fallback for difficult new meanings. | Mandarin-only. |
| 17–24 | Main audio should normally be entirely Mandarin. If needed, put Vietnamese explanations in a separate optional meaning track rather than the default lesson. | Mandarin-only. |
| 25–48 | All core audio is Mandarin-only. Vietnamese remains available only in transcripts or on-demand lookup. | Mandarin-only. |

Start introducing one or two reusable Mandarin teacher phrases per lesson from week 1, with immediate Vietnamese meaning the first few times. Week 11, *Repairing communication*, completes the bridge into Mandarin-led lessons with phrases such as `请再说一遍`, `请说慢一点`, `什么意思？`, `先听一遍`, `跟我说`, `现在你来回答`, and `再听一次`. Reuse familiar instructions without translating them repeatedly.

Do not remove Vietnamese merely because a calendar week has arrived. A learner is ready for Mandarin-only core audio when they can usually:

- identify the situation after the first listen;
- understand roughly 70–80% of the core exchange after two or three listens;
- answer familiar prompts within about two or three seconds;
- encounter only a few genuinely new chunks in each scene; and
- summarize the scene without consulting the transcript.

If those conditions are not met around weeks 17–20, retain Vietnamese only for new or ambiguous chunks. Do not restore full translation for material the learner already understands.

## Daily use and parallel literacy tracks

The course is audio-first. It must work when the learner cannot look at a screen, and it must not require completing exercises before the next lesson can play.

- **Daily passive listening:** listen to a 20–40 minute playlist while walking, commuting, or doing chores. The goal is familiarity with meaning, voices, rhythm, and real-speed dialogue; pausing or answering is optional.
- **Guided speaking:** when alone, spend 5–10 minutes with the guided track: answer the circling Q&A aloud (yes/no, either/or), imitate difficult chunks, and follow the point-of-view retelling.
- **Pinyin and tones:** study in short 5–10 minute sessions alongside the early lessons. Train four tones, the neutral tone, and high-confusion contrasts such as `zh/z`, `sh/s`, and `ü/u`. This supports pronunciation; it must not block audio learning.
- **Hanzi recognition:** study only characters and words already encountered in audio. The goal is recognition, meaning, and later typing pinyin to select characters. Handwriting and stroke order are optional later tracks, not prerequisites.

Each lesson therefore has two listening tracks: **guided** (meaning, circling Q&A, point-of-view retelling, and imitation) and **practice** (continuous natural-speed mini-story for passive replay). Pinyin and Hanzi are separate, optional app modules rather than mandatory steps in the listening flow.

Lesson authorship follows the detailed rules in [`docs/lesson_authoring_guidelines.md`](../docs/lesson_authoring_guidelines.md): meaning returns through micro-review, backward buildup is selective, prosody carries conversational intent, and Vietnamese fades according to familiarity rather than a rigid calendar alone.

Voice diversity is phased separately from accent difficulty: Lessons 1–3 use stable character voices, Lessons 4–12 rotate clear curated voice pairs, and later levels add mixed-voice reviews followed by lighter regional or non-native accent exposure.

For the first lessons, familiarity is managed manually through each JSON file's `new_patterns` and `review_pattern_ids`. These fields are an authoring ledger, not an automatic learner model. A read-only cross-lesson audit is the next useful tooling step; adaptive familiarity tracking comes later, after the guided/practice workflow has been validated.

## Level structure

Four levels, 12 weeks each = 48 lessons ≈ 1 year at one lesson/week. Each level closes with a "mixed review" lesson that recombines everything in that level before moving on (so effectively ~44 new-content lessons + 4 consolidation lessons — can renumber if you want the review lessons folded into week 12 of each level instead of added on top).

Each week centers on **one natural mini-story**; the grammar column below is a coverage check for the author (what this story is safe to use, given what earlier weeks already covered), not a goal the learner is meant to feel is "being taught" and not something that should shape which story gets picked. The "Learner can roughly..." column describes the accumulated grammar as a side effect of the stories heard, not a guaranteed survival script; a learner who has only done mini-stories may still need explicit practice before, say, actually ordering food, since no week is a dedicated "ordering food" script anymore.

| Level | Weeks | Rough HSK alignment | Learner can roughly... |
|---|---|---|---|
| 1. Foundations | 1–12 | HSK1 core | Use 是/不是, 还是 either-or questions, basic measure words, 在 + location, 有/没有, 的, and simple 在+verb continuous forms |
| 2. Elementary | 13–24 | HSK2 | Add 了 (completed action), 比 comparison, modal verbs (可以/应该/得), 因为/所以, sequencing (先...然后...), frequency adverbs |
| 3. Pre-Intermediate | 25–36 | HSK3 (partial) | Add connectors (虽然...但是..., 如果...就...), hedged opinions, multi-event past narration, comparative/superlative detail |
| 4. Intermediate | 37–48 | HSK3–4 (partial) | Add reasoned opinions, pros/cons, hypotheticals, change-over-time narration, light negotiation/debate language |

Grammar/function points are introduced once, then reused across later weeks and later mini-stories (point-of-view retelling plus later lessons' circling) — no week should introduce a structure it doesn't also recycle at least twice more downstream.

**Status:** Level 1 below has been re-sequenced for the mini-story pivot. Levels 2–4 still show the pre-pivot situational draft (kept for the grammar inventory and dependency ideas) and need the same re-sequencing once the Level 1 mini-story format is validated with real listeners — do not author lessons from the Level 2–4 tables as-is.

---

## Level 1 — Foundations (weeks 1–12)

`lessons/lesson_001_mau.json` (the pre-pivot scripted greeting dialogue) and `curriculum/beginner.json` are both superseded by this table and are kept only as a pronunciation/TTS reference, not as the template for new lessons. Week 1 has a draft at `lessons/lesson_001_story.json`: one narrator, plus two short character lines. It is not locked until a listener hears it. Do not author weeks 2–12, and do not replace `lesson_001.json`, until that listen.

Each week centers on one natural, realistic mini-story (placeholder below — needs a native/fluent pass and, per the prototype step, a chosen narrator format). The grammar column is what that story happens to need once written, checked against what earlier weeks covered — it is not why the story was picked.

| Wk | Mini-story situation (placeholder) | Grammar this story needs (author-side check) | Depends on |
|---|---|---|---|
| 1 | Anna and Lý Minh each think the takeout is the other's. It is a colleague's. | 是/不是, 还是, 的 (whose). No 了, no 在, no measure words. | — |
| 2 | Anna is looking for her jacket. She remembers it as blue, but Lý Minh saw a black one in the room. They check whose jacket is actually there. | A还是B (either-or), colors, 的 (whose) | wk1 |
| 3 | Someone looks for something and it isn't where they expected — retold with the real location | 在 + location (上/下/里/外) | wk1 |
| 4 | Someone realizes they don't have something they thought they had | 有/没有 (possession/existence) | wk1, wk3 |
| 5 | Figuring out whose thing was left behind, and what kind of thing it is | 的 (possessive/attributive) | wk4 |
| 6 | Someone reacts to how something turned out (too big, surprisingly good, too hot) | Basic adjectives + 很/太 | wk1 |
| 7 | Someone gets caught doing something when another event happens | 在 + verb (continuous action) | wk3 |
| 8 | The same situation, but corrected — what actually was *not* the case | 不 negation across the above | wk1–wk7 |
| 9 | Someone counts or miscounts something that matters to the story | Numbers & simple quantity | wk1 |
| 10 | The same kind of situation, happening at a different, specific time | Time words (今天/昨天/现在) | wk7 |
| 11 | A conversation that goes wrong because one person didn't catch what the other said | Ask to repeat/slow down, ask meaning (meta-skill) | recycles everything |
| 12 | The story from an earlier week, told as something that already happened | 了 (completed action) + mixed review | all of level 1 |

Point-of-view retelling for each week reuses the same facts with one variable changed (subject, location, color, quantity, or a single flipped fact — tense only from week 12 onward, once `了` has been introduced). This is also how `review_pattern_ids`/`coach_phrases_reduced` continuity should be read across these weeks: the same facts recur, not "the same two characters, a new script."

---

## Level 2 — Elementary (weeks 13–24)

**Pre-pivot draft — not re-sequenced yet.** This table still assumes the situational-script format. Treat the "New function" column as a grammar inventory to draw from when this level is redone as mini-stories; do not author a Level 2 lesson from this table's "Theme" column as a literal scene.

Introduces: 了 (completed action) reinforced, 在 (location/progressive), comparison 比, modal verbs (可以/应该/得 děi), simple clauses with 因为/所以, telephone/arrangement language, shopping beyond price (size/color/exchange), basic health complaints, weather/seasons, transport, simple past narration.

| Wk | Theme | New function | Depends on |
|---|---|---|---|
| 13 | Daily routine in detail | sequence actions (先...然后...), frequency (经常/有时候) | L1 wk8 |
| 14 | Weather & seasons | describe weather, talk about seasons, simple comparison 比 | — |
| 15 | Shopping beyond price | size, color, "too big/small", asking to exchange | L1 wk4 |
| 16 | Transport & getting around | take bus/taxi/subway, how long it takes | L1 wk10 |
| 17 | Making phone calls / arrangements | call someone, confirm a plan, leave a message | L1 wk9 |
| 18 | Health & feeling unwell | say what hurts, simple advice (应该/得) | — |
| 19 | Describing people & things | adjectives + 很/太, simple comparisons | L1 wk6 |
| 20 | Likes, dislikes & reasons | 因为/所以, give a reason for a preference | L1 wk7 |
| 21 | Past experiences (short) | 了 for completed events, simple past narration | L1 wk12 |
| 22 | Near-future plans | 要/打算/会, discuss a weekend plan | L1 wk12 |
| 23 | Asking permission & simple requests | 可以吗？, polite requests, declining politely | wk18 |
| 24 | Mixed review: a full weekend story | recombine wk13–23 in one extended dialogue | all of level 2 |

---

## Level 3 — Pre-Intermediate (weeks 25–36)

**Pre-pivot draft — not re-sequenced yet.** Same caveat as Level 2.

Introduces: more connectors (虽然...但是..., 如果...就...), comparative/superlative detail, giving opinions with hedges (我觉得/我觉得不太...), describing problems and asking for help, directions with landmarks, simple storytelling with multiple past events in sequence, discussing plans with uncertainty (可能/应该会), workplace/school small talk, apologizing and explaining a delay, expressing surprise/agreement/disagreement.

| Wk | Theme | New function | Depends on |
|---|---|---|---|
| 25 | Opinions, politely | 我觉得..., 我觉得不太..., asking for someone's opinion | L2 wk20 |
| 26 | Agreeing & disagreeing | 我也是/我不觉得, soft disagreement | wk25 |
| 27 | Telling a short story | sequence 3+ past events, connectors 后来/然后 | L2 wk21 |
| 28 | Problems & complaints (mild) | something is broken/wrong, asking for a fix | L2 wk18 |
| 29 | Apologies & explanations | explain a delay/mistake, 对不起，因为... | L1 wk2 |
| 30 | Conditional, simple | 如果...就..., hypothetical plans | L2 wk22 |
| 31 | Comparisons in depth | 比...更/还, "the most" 最 | L2 wk14 |
| 32 | Directions with landmarks | describe a route using landmarks, confirm understanding | L2 wk16 |
| 33 | Small talk at work/school | ask about someone's day/project, light chit-chat | L1 wk5 |
| 34 | Making & changing plans | reschedule, propose an alternative, express mild reluctance | L2 wk17 |
| 35 | Expressing surprise & reacting | 真的吗/不会吧, natural reactive phrases | recycles all levels |
| 36 | Mixed review: a misunderstanding resolved | full scene combining opinion, apology, problem-solving | all of level 3 |

---

## Level 4 — Intermediate (weeks 37–48)

**Pre-pivot draft — not re-sequenced yet.** Same caveat as Level 2.

Introduces: giving reasoned opinions on everyday topics, narrating longer personal stories, discussing pros/cons, light debate/negotiation, describing hypothetical or unreal situations, understanding and giving advice, talking about change over time (以前.../现在...), expressing hopes and worries, closing the course with an extended free-flowing mixed conversation.

| Wk | Theme | New function | Depends on |
|---|---|---|---|
| 37 | Pros and cons | 好处/坏处, weighing two options | L3 wk31 |
| 38 | Giving advice | 我建议/你最好..., softened suggestions | L2 wk18, L2 wk23 |
| 39 | Change over time | 以前.../现在..., describing how something changed | L1 wk12, L2 wk21 |
| 40 | Hopes, worries & plans | 希望/担心, talk about a goal or worry | L3 wk30 |
| 41 | Negotiating gently | price/time negotiation beyond basics, polite pushback | L1 wk4, L3 wk26 |
| 42 | Describing a problem in detail | multi-sentence complaint + proposed solution | L3 wk28 |
| 43 | Telling a longer personal story | 5+ event narration with reactions woven in | L3 wk27 |
| 44 | Light debate / differing views | present a view, concede a point, hold a position | L3 wk26, wk35 |
| 45 | Hypotheticals & unreal situations | 要是...的话..., imagining alternatives | L3 wk30 |
| 46 | Culture & comparison talk | comparing customs/habits, "in my country..." | recycles L3 wk31 |
| 47 | Free conversation scaffolding | open-ended Q&A chains on familiar topics | recycles everything |
| 48 | Capstone review | one long, mixed-topic conversation touching every level | all of course |

---

## Open design questions for the reviewer

1. **Narrator count — prototype before writing Level 1, not after:** whether a mini-story is told by one narrator, acted by two characters, or narrated with short embedded dialogue is not decided. Write 2–3 text-only outlines of the **same** mini-story (so only the narration format varies) in each candidate format, get real-listener reaction, and only then write Level 1 week 1. It may turn out to vary by story rather than being fixed for the whole course — confirm that with the prototypes rather than assuming it up front.
2. **Real vs. invented story content:** web research during this redesign found no confirmed evidence that Effortless English mini-stories at beginner level are sourced from real articles or confession-style posts — the examples found (e.g. a story about a boy named Bill buying water) are invented, consistent with the method's credited TPRS/Blaine Ray lineage. Hoge's separate "Learn Real English" product does use real recorded conversations, but it targets Upper-Intermediate/Advanced learners, not beginners. Treat Level 1–2 mini-stories as invented-but-realistic; revisit sourcing real material only once a level is reached where raw native content stops being overwhelming.
3. **Pacing**: 1 lesson/week assumes ~15-20 min/day of real practice outside the audio itself. If the target learner wants faster progress, levels could compress to 8 weeks each (32 weeks total) by merging some weeks.
4. **Review-lesson placement**: right now each level ends with one big "mixed review" lesson. An alternative is to drop a lighter review lesson every 4 weeks instead (more frequent, smaller reviews) rather than one large one every 12.
5. **Character literacy pacing**: recognition and pinyin typing are parallel optional tracks using only material already heard. Handwriting remains deferred unless a practical or exam goal requires it.
6. **Dialect/register**: all examples assume Mainland Standard Mandarin, casual-polite register. Flag if any week needs a more formal register.
