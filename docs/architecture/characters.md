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

## Before designing in detail

Research data sources and their licenses (commercial use): Hán-Việt readings, component/IDS decomposition with semantic/phonetic roles, stroke-order paths (e.g. Make Me a Hanzi / Hanzi Writer data), frequency + HSK 3.0 lists, Vietnamese meanings. Check samples (妈, 学, 谢) for simplified-character accuracy. A source with an unclear license is not used.
