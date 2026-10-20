# Lesson authoring guidelines

These rules define how a Mandarin lesson should teach through audio. They apply to guided and practice tracks; they are content rules, not UI requirements.

## Core teaching loop (mini-story pivot)

**Superseded:** the two-character scripted-dialogue loop that used to live in this section is replaced by the mini-story loop below. See `curriculum/roadmap.md`'s "Method" section for why. `lessons/lesson_001.json`, built by `scripts/build_lesson_001_story.py`, is the template for later lessons.

**Story first, grammar tracked, not taught.** Write or choose a mini-story because it is a good, concrete, realistic story — not because it contains a target pattern. Only after the story is written, check which grammar patterns appear in it against what the learner has already safely covered, and trim or adjust if the story accidentally uses something too advanced. The order is:

```text
Natural story → pick the words/lines it actually needs → check the grammar it uses is simple enough → design questions and variants around it
```

Never the reverse (pick a grammar point, then construct a story to contain it) — that produces the same stilted, textbook-dialogue feel this pivot is meant to fix. The grammar column in `curriculum/roadmap.md`'s Level 1 table is a coverage/sequencing check for the author, not something the learner should ever feel is "being taught."

Every mini-story should move through this order:

1. **Natural story:** the whole mini-story narrated at natural speed, with a brief Vietnamese scene-setting line first. The story must have enough concrete, checkable facts (who, what, where, how many, what color, in what order) to support many different questions about the same handful of facts.
2. **Vocabulary:** a short gloss of the few new words needed, before or woven into the first listen — brief, not a list to memorize.
3. **Meaning mapping:** key lines, Mandarin → concise Vietnamese meaning → the same line again at natural speed. Fewer lines need this than a full dialogue did, because the story is denser with reusable facts rather than one-off exchange lines.
4. **Circling Q&A:** ask the same facts repeatedly in varied forms — yes/no, either/or (`A还是B`), short wh-questions — so the learner recognizes and guesses before they can produce. This is the main drill, and it should dominate lesson time, not the old full-sentence retrieval pass.
5. **Pronunciation support:** select only 3–5 genuinely difficult lines. Use meaningful chunks, then build back to the complete sentence (see "Backward buildup" below). Do not split every character.
6. **Point of view retelling:** retell the same story with one variable changed. Before the grammar point for a tense/aspect marker (e.g. `了`) has been introduced in the roadmap sequence, only change subject, location, color, quantity, or a single flipped fact — never the tense, since that would teach an untaught structure through the back door. Tense/aspect becomes a valid retelling variable only once its own lesson has introduced it.
7. **Natural replay:** finish with the story at natural speed and without Vietnamese explanation.

Practice audio is separate: one short Vietnamese setup on the first pass, then the natural-speed mini-story. It has no answer gaps and does not replace the guided circling/retrieval pass.

## Meaning must return

A translation should not appear once and disappear. Reintroduce a new pattern after two or three segments in a short retrieval prompt, a new context, or a later scene. The learner should have to recall meaning before hearing the model again.

Do not turn every line into a vocabulary flashcard. Repetition should stay inside a real exchange, and review should prioritize patterns that are both useful and difficult.

## Backward buildup

Use backward buildup selectively for long or difficult lines, especially when the learner must produce the whole sentence. Each added step must contain the previous chunk, then the full line:

```text
什么 → 叫什么 → 你叫什么？
你呢 → 我叫安娜。你呢？
认识你 → 很高兴认识你。
```

`你呢` followed by `我叫安娜` is chunk practice, not backward buildup: the middle step does not contain `你呢`. Prefer meaningful words or prosodic chunks over isolated characters. A chunk that sounds unnatural by itself should be widened. Keep the full sentence in the drill so connected tones and rhythm remain the target.

## Bilingual coach narration

The coach's **whole narration** is bilingual at first, not only selected vocabulary. Every Vietnamese scene-setting, transition, instruction, or explanation should first be spoken in simple Mandarin, then immediately restated in Vietnamese. This lets the learner hear the meaning-bearing Chinese continuously, even before they can understand it independently.

For example:

```text
Jīntiān liǎng ge rén jiànmiàn.
Hôm nay hai người gặp nhau.
```

Speak to the learner with “bạn”, “mình”, “bạn có thể nói”, “Nghe lại nhé”, “Đến lượt bạn”, and “Thử nói xem”. Do not use “ta nói”, and do not narrate a character’s line as “Anna nói”. Character names belong in the scene setup.

On the first exposure, use the full pair: Mandarin narration → Vietnamese meaning. If a line is important or difficult, repeat the Mandarin narration after the Vietnamese. On later exposures, shorten the Vietnamese support while keeping the Mandarin narration. When the learner has heard the line repeatedly in meaningful contexts, remove the Vietnamese and retain only the Mandarin narration. The same fade applies to instructions and transitions, not just target phrases.

Chinese and Vietnamese never share one TTS request. A Mandarin coach voice speaks the Chinese narration segment; the stable coach voice speaks the Vietnamese restatement. Do not sprinkle isolated Chinese words into an otherwise Vietnamese sentence when a complete bilingual narration line is intended.

### Bilingual alignment and beginner length

Whenever a Vietnamese coach segment follows a Mandarin coach segment as its meaning support, the two must express the same content. Do not let the Vietnamese sentence silently add details that were not said in Chinese (for example, “một cái túi” when the Mandarin only says `外卖`), and do not let the Vietnamese turn become a different instruction or a closing announcement. If extra explanation is useful, give it its own bilingual pair.

Use a short semantic-audit pass over every adjacent `zh-CN`/`vi-VN` coach pair. These are valid only when the Vietnamese is a faithful restatement, not merely related commentary. Praise after a model answer and a prompt translation are different segment types; do not treat praise such as “Đúng rồi” as a translation of `不是`.

For absolute beginners, keep the first meaning-mapping units short. A natural story may contain several short clauses in one continuous narration, but the first bilingual explanation should split them:

```text
安娜猜是李明的。 → Anna đoán là của Lý Minh.
李明猜是安娜的。 → Lý Minh đoán là của Anna.
他们看袋子上的名字。 → Họ nhìn tên trên túi.
```

Avoid presenting a long multi-clause Mandarin sentence as one unexplained chunk. Build longer questions progressively from short ones. A long either/or question is acceptable later in circling after its component phrases have already appeared, but it should not be the learner's first exposure.

If a Vietnamese restatement contains a concrete detail that matters to the story, include that detail in Mandarin as well. Conversely, do not add a new Mandarin clause only to make the translation look symmetrical; incidental words may remain incidental, but their meaning must still be honest.

Sentence length should grow with familiarity. In the first lessons, prefer very short sentences and one idea per segment, even when a longer sentence would be grammatically possible. Do not make a beginner process several new words, a new pattern, and a long clause at the same time. In later lessons, combine words and chunks that have already appeared repeatedly into longer natural sentences. The length increase should come from recombining familiar material, not from introducing many new items inside one long sentence.

### Chunk buildup for absolute beginners

A five- or six-syllable Mandarin sentence can still be difficult when every part is new. Keep the complete sentence in the natural story, but select only about 3–5 difficult lines for a short chunk-buildup drill. Split by meaningful prosodic chunks, never by individual characters:

```text
李明的 → 这是李明的 → 这是李明的吗？
同事的名字 → 袋子上有同事的名字
不知道 → 不知道是谁的 → 安娜和李明不知道是谁的
```

Play the complete line naturally first. Then play each useful chunk once or twice, add a concise meaning when needed, and rebuild the full line at a slightly slower speed before returning to natural speed. Do not split familiar short lines such as `你好`, `不是`, or `再见`. Chunk buildup is pronunciation and processing support for selected difficult lines, not a character-by-character spelling exercise and not a reason to lengthen every lesson.

### Support fade within the first lesson

Repeated exposure inside one listening session counts as practice, but it must not be treated as proof that an absolute beginner has become familiar with a pattern. For the first lesson, use a gradual within-session fade:

```text
first story listen       Mandarin-only natural flow
meaning mapping          Mandarin → Vietnamese → Mandarin
Q&A round 1              full Vietnamese support
Q&A round 2              Mandarin with sparse Vietnamese anchors
Q&A rounds 3–4           Mandarin-only
shadowing and closing    Mandarin-only
```

An anchor is a short Vietnamese reminder for a group of related questions (or for a genuinely new pattern), not a translation after every prompt. Keep the closing replay and shadowing free of Vietnamese. Later lessons may move the Mandarin-only transition earlier when the relevant patterns have already appeared in previous lessons.

Do not mark a pattern as familiar across lessons merely because it survived three or four repetitions in the same session. Validate the transition with real listeners: check whether they can answer roughly 70–80% of the second-round prompts, still recognize the key patterns, and understand the situation on the next day's replay. If not, add sparse anchors to the next round rather than restoring full translation everywhere.

### Teacher-led question preparation

Before the first circling block, the coach should briefly teach the learner how to recognize the question and what kind of answer is expected. A beginner must not be dropped into a stream of unexplained Mandarin prompts. The explanation should be concrete and tied to the story, not a grammar lecture:

```text
“吗” ở cuối câu thường báo hiệu câu hỏi có hoặc không.
Nghe: 这是外卖吗？
Nếu đúng, bạn chỉ cần trả lời 是. Nếu không đúng, trả lời 不是.

“谁的” nghĩa là “của ai”. Câu này yêu cầu bạn trả lời tên người hoặc nhóm sở hữu:
这是谁的？
Bạn có thể trả lời: 安娜的 hoặc 同事的.

“还是” dùng khi câu hỏi đưa ra hai lựa chọn. Bạn chỉ cần chọn một:
这是李明的还是同事的？
```

The Mandarin cue and Vietnamese explanation must be separate, matching segments. After the explanation, ask the same question in Mandarin, leave the response pause, and give a short model answer. Revisit the explanation later in a shorter form when the learner is likely to need it (“Nghe chữ `吗` ở cuối nhé”, “Câu này đang hỏi của ai”). This is the teacher's guidance layer, not praise after every answer.

New words and patterns should receive many meaningful touches before Vietnamese support disappears. As a planning target for the first lessons, expose each important pattern roughly 8–12 times across the story, meaning mapping, question types, short answers, point-of-view retelling, and replay. Do not simply repeat one prompt 8–12 times. Vary the question form and keep the fact stable, then space the Vietnamese glosses: full explanation at first, short anchors in later rounds, and Mandarin-only only after the learner has heard the pattern in several contexts.

### Beginner overload guardrails

The first lesson must not make an absolute beginner actively learn every word that happens to occur in the story. Choose only 3–5 active targets for deliberate teaching. Other words may appear as incidental story support, but do not list them as required vocabulary, drill each one, or introduce their grammar at the same time. Introduce the active targets in small groups of one or two, immediately reuse them in a meaningful story line or question, and return to their meaning later.

The first natural story listen may remain Mandarin-first, but give a short Vietnamese situation setup and keep the story clauses short. Do not summarize the entire plot before playing the story; preserve a small amount of curiosity. After the story, bridge comprehension in layers: short word/phrase meaning, one or two story lines, then a question block.

For the first absolute-beginner Q&A, retain enough Vietnamese support to prevent sequence-guessing from becoming the only way to answer:

```text
round 1: Mandarin prompt → Vietnamese meaning → pause → short model answer
round 2: Mandarin prompts with sparse Vietnamese anchors for new patterns
round 3+: Mandarin-led variation, with Vietnamese only when a new pattern causes loss of the thread
```

Vary the question order and form, but do not randomize so aggressively that the learner loses the story. A long lesson may use several chapters (story/meaning, supported questions, varied questions, chunk practice, point-of-view, replay); duration must come from meaningful variation, not many near-identical Q&A loops.

## Lesson 1 template

`lessons/lesson_001.json` is the mini-story template. One narrator, short lines from the people in the story, the meaning mapped once, then three question passes: Vietnamese, guided Chinese, Chinese-only. Copy that shape. Do not bring back the greeting-dialogue trial.

## Audio tags for delivery

`tts_tags` (on a `Segment`, or per-`DialogueTurn` when the segment uses `turns`) is a list of short ElevenLabs inline audio-direction tags (e.g. `gentle`, `curious`, `reassuring`) rendered as `[tag1][tag2] text` only in the actual TTS request — never in `text`, the transcript, pinyin, or translation. It is inline audio direction, not a prompt describing how to act; keep each tag a single short word. Supported on `eleven_v4`, `eleven_v4_turbo`, and `eleven_v3`; silently ignored (plain text sent as-is) on any other model, reported via `generate --dry-run`'s "Audio tags supported" line.

Use tags sparingly, only where the delivery genuinely needs to shift:

- opening/scene-setting lines;
- reassurance after something the learner might find hard;
- genuine encouragement (paired with the existing "don't praise every line" rule — a tag doesn't make more praise acceptable);
- surprise or another clear emotional beat in the story or dialogue;
- a clearly emotional dialogue line.

Do not tag every repeated line, every question, or every vocabulary item — that defeats the "sparse, varied" intent the same way over-praising does. A segment with `turns` ignores any tag on the segment itself; tag the individual turns that actually need it, since characters in the same exchange often have different deliveries. Audio tags are unverified until heard — they do not behave identically across every voice, language, or very short line, so treat a new tag as something to confirm by listening, not something to assume works.

## Prosody and encouragement

Mandarin model audio should preserve conversational intent: questions, confirmations, surprise, and closing phrases should not all sound flat. Voice selection and text punctuation should be checked by ear.

Coach encouragement is useful after a retrieval attempt, but it must remain brief and varied. Do not insert praise after every line or interrupt passive listening. One or two natural cues per activity are enough, such as “Đúng rồi”, “Tốt”, or “Nghe lại một lần nữa”.

## Voice diversity for international listening

Voice variation is a learning goal, not random decoration. The learner should gradually become comfortable with customers and colleagues who have different voices while still hearing clear target Mandarin.

- Keep Lessons 1–3 on a stable Anna/Lý Minh pair so the beginner can establish speaker identity.
- From Lesson 4 onward, rotate curated voice packs. Each lesson chooses two distinct Mandarin voices, one per character, and keeps that assignment across every guided and practice track.
- Choose the pack deterministically from the lesson ID and course version, then save the selected voice IDs in the generated lesson manifest. Never re-randomize on regeneration.
- Introduce timbre, age, gender, and speaking-rate variation before introducing strong regional or non-native accents.
- Add mixed-voice review scenes after several lessons: familiar patterns spoken by a new pair of clear voices.
- Keep the Vietnamese coach voice stable for now so voice diversity does not compete with the meaning explanation.

Voice selection must use a curated, account-available pool. Fetching a voice list is an authoring-pipeline operation; the Android app receives finalized audio and never receives an ElevenLabs key.

### Voice registry (implemented)

Voice IDs are no longer stored in `.env`. The ElevenLabs API key has permission to list available voices (a free, non-synthesizing call), so `mandarin-course voices sync` fetches every voice already in the account's library and saves it to a local registry file (`src/mandarin_course/voice_registry.py`, default path `data/voices.json`). Run this **manually, once, and again only when the account's voice library changes** — never automatically on every `generate`.

At generation time, `voice_ids_for()` (`src/mandarin_course/tts.py`) picks a voice per role from the registry:

- Picking is **deterministic**, seeded by `lesson_id` + `version` + role, so regenerating the same lesson never changes its cast (same rule as the old fixed-ID model, now generalized instead of replaced).
- Picking is **priority-ranked, not hard-filtered**: `preferred_accent_for_level()` gives Beijing Mandarin the top priority tier for `absolute_beginner` lessons (today, Lessons 1–2) via `prefer_accent="beijing"`, but every other voice in the registry stays a usable fallback. **Agents authoring Lesson 1–2 content should assume a Beijing-accented Mandarin voice by default**, not pick an accent in the story text itself — the audio pipeline handles this, not the lesson JSON.
- When a segment needs more than one distinct speaker at once (dialogue `turns`), the picker assigns the single top-priority voice to one role and falls back through the next-ranked voices for the others, so two characters never collapse onto the same voice just because only one voice matches the priority accent. This matters today specifically because the account currently has only two Mandarin voices (one Beijing, one Taiwan-accented) — hard-filtering by accent would have made every role resolve to the same single Beijing voice.
- The new `coach_zh` speaker (a Mandarin-narrating coach, distinct from Mandarin characters and from the Vietnamese coach — see "Bilingual coach narration" above and "Gradual reduction of Vietnamese support" below) resolves through this same registry, not a dedicated env var.
- As more voices are added to the account, both the priority ranking and the pool of fallback voices grow automatically — no code change needed, just rerun `voices sync`.

## Vietnamese fade-out

Follow the phase-out schedule in `curriculum/roadmap.md`. New lessons may use Vietnamese for genuinely new or ambiguous meanings; already familiar patterns should be reviewed in Mandarin. Practice audio is Mandarin-only after its first scene setup from the earliest lessons.

### Gradual reduction of Vietnamese support

The coach should gradually move from Vietnamese-supported Mandarin to Mandarin-only narration. This is not random sprinkling of Chinese words into Vietnamese: each bilingual coach line must convey the same scene, instruction, or explanation in both languages.

Use this progression:

1. **First exposure:** Mandarin coach line → Vietnamese restatement.
   `Jīntiān liǎng ge rén jiànmiàn.` → `Hôm nay hai người gặp nhau.`
2. **Familiar review:** Mandarin coach line first, followed by a shorter Vietnamese cue or meaning.
   `Jīntiān liǎng ge rén jiànmiàn.` → `Hôm nay hai người gặp nhau.`
3. **Mandarin-only cue:** when the line and its vocabulary are familiar, retain only the Mandarin coach line.
   `Jīntiān liǎng ge rén jiànmiàn.`

Apply the same pattern to functional language, not only vocabulary:

```text
Nǐ jiào shénme? Khi muốn hỏi tên một người bạn, bạn nói: “Bạn tên gì?”
Nǐ ne? Khi muốn hỏi lại, bạn nói: “Còn bạn?”
Zuìjìn zěnmeyàng? Vài ngày sau, bạn có thể hỏi: “Dạo này thế nào?”
```

Explain a new phrase or question pattern once in plain Vietnamese, then let it recur in the conversation, chunk drill, and retrieval pass. Do not translate every occurrence forever. Keep the explanation about use and situation (“cách hỏi tên giữa bạn bè”, “hỏi thăm dạo này”), not a long grammar lecture. The bilingual coach rule still applies to the surrounding instructions and scene narration.

The Vietnamese and Mandarin portions of a bilingual coach line should be separate audio segments. Use a stable Mandarin coach/narrator voice for the Chinese line and the stable Vietnamese coach voice for its restatement. Do not send mixed Vietnamese–Mandarin text to a Vietnamese TTS voice: pronunciation and tones are likely to degrade.

The coach narration has its own ledger, separate from the dialogue ledger: `coach_phrases_introduced` (first bilingual exposure, step 1 above) and `coach_phrases_reduced` (Vietnamese dropped, steps 2–3 above). Keep it separate from `new_patterns`/`review_pattern_ids` because the two tracks fade at different paces — a phrase like `再见` can be a review item in the dialogue while the coach still gives it full Vietnamese support, or the reverse. A pattern should not lose its Vietnamese support merely because it appeared once; reduce support only after repeated exposure in meaningful contexts. Familiar phrases such as `你好`, `谢谢`, and `再见` may fade sooner, while a new sentence pattern or ambiguous phrase keeps its explanation longer.

The `mandarin-course audit` command reports both ledgers: cumulative patterns introduced, reductions/reviews declared with no earlier introduction, duplicate introductions, and a reuse map. Run it before finalizing a new lesson's Vietnamese fade decisions.

## Familiarity and adaptation

For the current lessons, `new_patterns`/`review_pattern_ids` and `coach_phrases_introduced`/`coach_phrases_reduced` are **manual authoring ledgers**. They record what the writer intends to introduce and reuse; they are not automatic familiarity tracking. The author still decides whether a familiar pattern needs Vietnamese because it appears inside a new or difficult sentence.

`ProgressStore` currently stores learner-rated review items from eligible pattern segments. It does not read either ledger, and it must not be used as evidence that a learner understood a passively played line.

Content generation may eventually use a per-pattern familiarity record, but passive listening alone is not proof of mastery. Track at least:

- exposure count and last exposure;
- guided retrieval attempts and a learner-supplied result (`again`, `hard`, `good`, or `easy`);
- the lesson and context where the pattern was last used; and
- whether the pattern is due for review.

The first app release does not need speech recognition or automatic pronunciation scoring. Learner ratings and completed retrieval activities are sufficient signals for deciding whether to reduce Vietnamese and increase Mandarin-only review.

Before adding an adaptive engine, `mandarin-course audit` (see `src/mandarin_course/audit.py`) is a small read-only authoring audit that reports, for both the dialogue and coach-phrase ledgers:

- all patterns introduced up to a selected lesson (`--up-to`);
- each lesson's declared review/reduced patterns;
- reuse or reduction declared with no earlier introduction;
- duplicate introductions; and
- where each pattern has already been reused.

This audit reduces authoring mistakes without changing the lesson schema's runtime behavior or TTS generation.

## Scope decisions

- Lesson 1 is the template. Do not author lesson 2 until this lesson has been heard and the Vietnamese fade feels right.
- Choose each mini-story because it is a good, concrete, realistic story first; check it against the grammar budget second. Do not pick a grammar point and construct a story to contain it.
- Apply buildup and circling only where they improve a specific pattern or fact; avoid making every lesson longer by default.
- Keep pinyin and Hanzi study optional and separate from the passive listening flow.
- Add adaptive authoring and familiarity tracking after the guided/practice player and local progress model are stable.
