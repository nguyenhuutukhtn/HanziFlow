---
name: lesson-review
description: Review a drafted lesson (lessons/src/lesson_<id>.story.md) against a fixed rubric and report findings by row ref — no rewriting. Includes an independent Hán-Việt cross-check of the lesson's new characters. Run with a different model than the one that wrote the lesson. Use when asked to "review lesson <id>".
---

# Lesson review

You find problems; the author fixes them (`lesson-author` step 5). Do not edit any file. The learner can't judge Chinese, so be concrete: every finding names a row and says what is wrong.

## Inputs (read only these)

1. `just ledger <id>` → `build/ledger_<id>.md` (what earlier lessons taught, story bible).
2. `lessons/src/lesson_<id>.story.md` (the source; row ref = first cell, `ref#n` when it repeats).
3. `just lesson-metrics <id>` output.
4. `just export-characters <id>` → the new characters; their current Vietnamese data is in `data/characters_vi.json` (look up only those characters).

## Rubric

1. **Chinese:** natural, grammatical, what a native speaker would say in that scene.
2. **Pinyin:** matches the hanzi; tones as lesson 001 writes them.
3. **Vietnamese:** faithful to the Chinese, natural Vietnamese, names as the bible gives them.
4. **Level:** no word or grammar beyond the ledger + this lesson's goals/patterns; bible's tense/style constraints kept.
5. **Story:** consistent with the bible (cast, traits, places, threads); scenes follow from each other.
6. **Teaching loop:** each new word heard ≥ 3 times; buildups grow chunk by chunk; asks answerable from what was just heard; Vietnamese support fades as in lesson 001.
7. **Metrics:** every `FLAG` line either fixed or worth a finding.
8. **Hán-Việt:** for each new character, write your own Hán-Việt reading and Vietnamese meaning **before** looking at `data/characters_vi.json`, then compare. Report only disagreements.

## Output (≤ 40 lines, nothing else)

```text
Verdict: ready | fix (n findings) | rewrite (why, 1 line)
Findings:
<ref> | block|fix|nit | <rubric #> | <problem> | <suggested direction, not a full rewrite>
...
Hán-Việt: <char> data=<reading/meaning> mine=<reading/meaning> | ... (or "agree on all N")
Outside reviewer: <ref>: <zh line> — <why a human who knows zh↔vi should check it>
```

- `block` = wrong Chinese/translation or a level violation; `fix` = teaching/story problem; `nit` = optional.
- "Outside reviewer" lists only lines you are not confident about (≤ 10). Hán-Việt disagreements go to a second model, not the outside reviewer.
