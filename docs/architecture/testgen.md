# Architecture — test generation

> Part of the architecture docs; index: [../app_architecture.md](../app_architecture.md).

## Test generation (shared/)

- Input: item banks of the target lesson and every earlier lesson, a seed, and `generator_version`. Output is deterministic for the same input on JVM and JS. A checked-in set of seed → expected-test vectors runs against both targets.
- Each attempt uses a fresh random seed; the seed is stored with the attempt.
- Question types:
  - listen → choose Hanzi / pinyin / Vietnamese meaning;
  - see Hanzi → choose pinyin;
  - identify the tone;
  - fill the missing word in a story sentence;
  - true/false or either/or about a story fact.
- At least half the questions target the current lesson; the rest review earlier lessons.
- Distractors are chosen deliberately, never uniformly at random: same syllable with a different tone (`mǎi`/`mài`), confusable initials and finals (`zh/z`, `sh/s`, `ü/u`), then other taught words of the same kind. Never use a word the learner has not been taught.
- Answer position is shuffled per question.

**Pass rule (decided 2026-10-20):** an attempt passes only if every question is answered correctly on the first try and no answer was revealed. Mistakes don't end the attempt: a wrong answer shows the correct one, and a different question of the same type on the same item is requeued at the end so the learner still practices it, but that attempt is recorded as not passed. The result screen shows first-try correct / total. A lesson counts as passed when it has at least one passed attempt.
