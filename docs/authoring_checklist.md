# Authoring checklist (condensed)

For writing or revising a `lessons/src/lesson_XXX.story.md`. Self-contained: no other file needs to be read to follow it. Long-form source of these rules: `docs/content_rules.md` and `docs/lesson_authoring_guidelines.md` — keep this file in sync with them when either changes.

## Format

- Metadata (`lesson_id:`, `title:`, `level:`, `version:`, `goals:`, `patterns:`) comes before the first `#` section, one `key: value` per line; list values are separated by `|`.
- `# guided` and `# practice` are variant sections; `# data` holds named row sets (`## lines <name>`, `## questions <name>`, `## tags`) reused via `from=<name>` or `from=<name>[start:stop]`.
- `## activity <name>` must precede the first block in a variant; it labels the blocks that follow.
- Pipe rows (`a | b | c`) belong to the block above them, one per line; `> text` is an ignored note.

## Block types and row shapes

- `## say` / `## zh`: `key | 中文 | pinyin | Vietnamese [| pause_ms]` — bilingual (`say`) or Mandarin-only (`zh`) coach line.
- `## story`, `## gloss`, `## shadow`: `key | speaker | 中文 | pinyin | Vietnamese [| pause_ms]`; speaker is `coach_zh`, `anna`, or `li_ming`.
- `## teach prefix=`: exactly three rows keyed `rule`, `ex`, `ans` — explain a signal word, give an example, give the model answer.
- `## buildup prefix=`: a `full` row then chunk rows; each chunk's text must contain the previous chunk's text, and the last chunk's text must sit inside `full`'s text.
- `## ask prefix=`: `key | 问题 | pinyin | cue | answer [| pause_ms] [| praise]`; `answer` is `是`/`不是`/`有`/`没有` or three cells `答 | pinyin | Vietnamese`. Flags: `vi` (add Vietnamese cue + answer meaning), `praise` (allow the row's praise, needs `vi`), `reverse` (flip row order).
- A block takes inline rows or `from=<set>`, never both.

## Hard rules

- Coach Chinese and Vietnamese are always separate TTS segments, never mixed in one line; a bilingual coach line goes Mandarin → Vietnamese, never the reverse, and both must say the same thing.
- Do not add praise after every retrieval row — one or two varied cues per activity, not a cycling list fired on every answer.
- A buildup chunk that doesn't contain the previous chunk is not buildup; don't label it as such.
- Give a real speaking gap after every model line: ≥1.5s after a buildup chunk, ≥3s after the slow full line, ≥4s after the natural full line (absolute beginners; lengthen for longer lines).
- `tts_tags` (via `## tags` + `key | tag[,tag]`) are delivery direction only, sent to TTS, never written into any transcript text; use sparingly (scene opens, reassurance, a real emotional beat), never on every line.
- Never write an accent into the story text — voice/accent selection is a pipeline concern.
- Keep `patterns:`/`review_patterns:` (dialogue) separate from `coach_phrases:`/`coach_phrases_reduced:` (coach narration) — they fade at different paces.

## Teaching loop (keep this order)

1. Natural story at natural speed, brief Vietnamese scene-setting first.
2. Short vocabulary gloss of the few new words — not a list to memorize.
3. Meaning mapping on key lines: Mandarin → Vietnamese → Mandarin again.
4. Circling Q&A (yes/no, `A还是B`, short wh-questions) — the main drill.
5. Pronunciation buildup on 3–5 genuinely hard lines only, by meaningful chunk, never by character.
6. Point-of-view retelling: change subject/location/color/quantity/a flipped fact; never tense/aspect before its own lesson has introduced it.
7. Natural replay, Mandarin-only, no explanation.

## Vietnamese fade (within one lesson)

- First story listen: Mandarin-only.
- Meaning mapping: Mandarin → Vietnamese → Mandarin.
- Q&A round 1: full Vietnamese support.
- Q&A round 2: Mandarin with sparse Vietnamese anchors, not a translation after every prompt.
- Q&A rounds 3+, shadowing, closing replay: Mandarin-only.
- Write the story because it's good and concrete first; check its grammar against earlier lessons after — never start from a grammar point.

## Beginner-overload guardrails

- 3–5 active vocabulary targets per lesson, taught deliberately and reused soon after; other story words are incidental, never drilled.
- Introduce active targets in groups of one or two, each reused immediately in a line or question.
- Keep first sentences short, one new idea per segment; grow sentence length later only by recombining familiar material, not by adding many new items at once.
- Don't summarize the whole plot before the first listen — keep a little curiosity.
- Vary question order/form in circling, but not so much the learner loses the story thread.
