# Architecture — Character learning (planned, not scheduled)

> Part of the architecture docs; index: [../app_architecture.md](../app_architecture.md). Discussed 2026-10-21; build after G2c (Review tab + SRS base). Nothing here is implemented.

## Learner and goal

Vietnamese-speaking adult who types Chinese (pinyin IME) and rarely hand-writes. Goal: recognize the characters of words they already understand by ear, so they can read and type them. Hand-writing from memory is not a goal.

## Decisions (user, 2026-10-21 — don't reopen)

- **Stroke order is view-only:** a "Xem nét" animation in the character card. No tracing exercise, no stroke scoring, nothing gated on writing.
- **Character order follows the listening lessons:** a character is taught only after the learner has heard a word that contains it. No separate simple-to-complex character curriculum.
- **Components just-in-time:** before a new character, teach the components it is built from that the learner doesn't know yet (e.g. 妈 → 女 + 马). Already-known components are skipped.

## Proposed design (PM draft, refine before building)

- **Character card:** character, components (each tappable, with semantic/phonetic role), Hán-Việt reading, pinyin, Vietnamese meaning, the lesson words that contain it, optional stroke animation, and characters sharing the phonetic component (e.g. 妈 吗 码 骂 — view only).
- **Optional primer:** the ~30–50 most productive components (氵讠扌口木心…), for learners who want a base first. Not required.
- **Exercises (recognition only, all in the Review tab's SRS):** character → meaning / Hán-Việt / pinyin; audio → pick the character (distractors share components or look alike — extends the test generator's phonetic distractors); type the pinyin of a word. No meaning → write-the-character cards.
- **Where it shows up:** long-press lookup opens the character card; a "Chữ của bài" step after a lesson's listening is passed; daily review in Ôn tập.

## Architecture (follows "Shared core, thin platform shells" in android_code.md)

- **Pipeline (Python):** `publish` adds a per-lesson `characters.json` to the package: new characters of the lesson + components needed first, with readings, meanings and decomposition. Built offline, deterministic, no AI at runtime.
- **`kmp/shared`:** unlock rule (heard words → characters → missing components), SRS scheduler (FSRS-style), exercise generation. Shared by Android, iOS and web.
- **Platform shells:** character card, review screens, stroke animation renderer.

## Data sources (decided 2026-10-21 from R1 research)

| Need | Source | License |
|------|--------|---------|
| Hán-Việt readings | Unihan kVietnamese + kTraditionalVariant fallback | Unicode License |
| Decomposition + semantic/phonetic roles | Make Me a Hanzi `dictionary.txt` | LGPL 3.0 (data only) |
| Stroke-order paths | Hanzi Writer data | Arphic Public License |
| HSK level | ivankra/hsk30 | MIT |
| English meanings | CC-CEDICT | CC BY-SA 4.0 |
| Vietnamese meanings | Author (AI gen + human review) — `vi_meaning: null` in C1 | — |
| Component names in Vietnamese (bộ thủ) | Author ~250 entries — `data/radicals_vi.json` | — |

See `docs/research/characters_data.md` for full license analysis + sample records.

## Packaging decisions (decided 2026-10-21)

- **Stroke data ships separately** as a one-time-download base package (not per-lesson). Lesson `characters.json` carries a `stroke_available: bool` flag only; the renderer fetches from the base pack.
- **Vietnamese meanings start null** in `characters.json`. Authored later via story source or a review pass.
- **Bộ thủ names** authored before C1 (task C0): ~250 radicals + simplified-only components, AI-generated, human-reviewed, saved to `data/radicals_vi.json`.

## characters.json schema (per lesson package)

```json
{
  "lesson_id": "lesson_001",
  "characters": [
    {
      "char": "妈",
      "pinyin": "mā",
      "han_viet": "mã",
      "hsk_level": 1,
      "decomp_ids": "⿰女马",
      "semantic": "女",
      "phonetic": "马",
      "en_meaning": "mother, mama",
      "vi_meaning": null,
      "phonetic_family": ["妈", "吗", "码", "骂"],
      "stroke_available": true,
      "lesson_words": ["妈妈"]
    }
  ],
  "components_prereq": [
    {
      "char": "女",
      "role": "semantic",
      "name_en": "woman",
      "name_vi": "nữ"
    }
  ]
}
```

## Before designing in detail

~~Research data sources~~ — Done (R1). Next: C0 (bộ thủ data), then C1 (pipeline characters.json), C2 (character card UI).
