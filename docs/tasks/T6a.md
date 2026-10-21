# T6a — Lesson ledger: what earlier lessons taught, in every authoring bundle
Status: todo · Suggested agent: fresh Codex · Size: M · Device check: user (none)

## Goal
When an AI writes lesson N, the `export-lesson` bundle must tell it exactly what lessons 1..N-1 taught: words, characters, patterns, story premise, cast. Today "Already taught" holds only pattern ids and coach phrases. Same "taught before" logic also fixes `characters.json` for lesson ≥ 2 (C1 left `seen_chars` empty, so every lesson would re-list all its chars).

## Read only
- `src/mandarin_course/story/bundle.py` (`_already_taught`, `export_lesson`), `src/mandarin_course/characters/resolve.py`, the `characters.json` call in `src/mandarin_course/publish_core.py` (grep `resolve_lesson_characters`).
- `lessons/src/lesson_001.story.md` lines 1–12 (header: `> Story:` / grammar notes, title, goals, patterns), `lessons/lesson_001_vocab.json` keys only (`words[]`: hanzi, pinyin, meaning_vi).
- `docs/CODEMAP.md` via grep for `story/` and `authoring`.

## Do
1. `src/mandarin_course/story/ledger.py`: `build_ledger(lessons_dir, before_lesson_id) -> str` (Markdown) from committed sources only (story headers, lesson JSON, `*_vocab.json`), deterministic:
   - **Per earlier lesson**, the last 2 in full: id, title, story premise (the `> Story:` note), grammar note, goals, patterns, vocab table `hanzi | pinyin | vi`, new characters (chars first seen in that lesson).
   - Older lessons compact: id, title, one premise line, vocab as a hanzi list.
   - **Cumulative:** known word count + list, known character list, recycle candidates (words that appear in only one earlier lesson's zh segments — suggest reusing them).
   - **Story bible:** `lessons/src/bible.md` inlined verbatim if present.
   - Cap: ≤ 200 lines for up to 10 lessons (compress older lessons further if needed).
2. `export-lesson` puts the ledger in its "Already taught" section (keep the existing pattern/coach-phrase lines). Add `mandarin-course ledger <id>` (+ `just ledger <id>`) printing it to `build/ledger_<id>.md`.
3. Create `lessons/src/bible.md` (≤ 30 lines) from lesson 1's story source: cast (Chinese name, pinyin, Vietnamese rendering, role, traits), places, objects/running threads, tense/style constraints. It is authored data; later lessons update it.
4. Cross-lesson characters: one function `chars_taught_before(lessons_dir, lesson_id) -> set[str]` used by the ledger **and** by publish (pass it as `seen_chars` to `resolve_lesson_characters`). Lesson 001 still gets an empty set.
5. Tests: two synthetic lessons in a temp dir → lesson 2's ledger lists lesson 1's words/chars/premise/bible; lesson 2's `characters.json` excludes lesson 1 chars and its `components_prereq` excludes chars lesson 1 taught.

## Acceptance
- `just check` passes; no new ⚠ file.
- `just lesson 001` dry-run → publish reports `unchanged v7` (lesson 1 output must not change). No upload.
- Verify below exits 0.

## Verify
```
just check
mandarin-course export-lesson 002 >/dev/null
python3 - <<'EOF'
import sys, re
b=open('build/bundles/lesson_002.bundle.md',encoding='utf-8').read()
need=['安娜','李明','还是','外卖','我们','wǒmen','chúng ta','## Story bible']
miss=[n for n in need if n not in b]
sec=b.split('## Already taught',1)[1].split('\n## Story source',1)[0]
print('missing',miss,'ledger lines',sec.count('\n'))
sys.exit(bool(miss) or sec.count('\n')>200)
EOF
```

## Out of scope
Metrics against the model lesson and row-level edits (T6b), skills (PM), lesson 002 content, Kotlin, upload.
