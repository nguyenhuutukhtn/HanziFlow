# Architecture — lesson package, timeline, versioning

> Part of the architecture docs; index: [../app_architecture.md](../app_architecture.md).

## Lesson package contract

The Python project remains the authoring and audio-generation system. `publish` writes:

```text
publish/
├── catalog.json
└── lessons/
    └── lesson_001/
        └── v2/
            ├── guided.mp3
            ├── practice.mp3        # optional while practice audio is not generated
            ├── timeline_guided.json
            ├── timeline_practice.json  # present only with practice.mp3
            ├── lesson.json
            ├── reference.json
            ├── items.json
            └── clips/<sha256>.mp3
```

Use versioned paths. Never overwrite a published version in place; CDN and devices may have cached it. Upload versioned files with a one-year `cacheControl`; upload `catalog.json` with no-cache. For this personal deployment, a replacement release ends with cleanup: only after the new package and catalog have been verified from the public endpoint, delete the superseded version from both local `publish/` and Supabase Storage. Delete only the exact old version prefix; never remove the fallback before verification succeeds.

Versioning rule (decided 2026-10-20, replaces manual version bumps):

- Only the latest version of a lesson is kept, on the server and locally. Older versions have no value once superseded; they exist only as the fallback until the new one is verified.
- Versioned paths stay (`lessons/<id>/v<N>/`, never overwritten) purely for cache and download safety, not for history.
- `publish` assigns the package version itself: it compares the new package's artifact digests with the current `catalog.json` entry; if anything differs, version = previous + 1; if identical, it keeps the current version and writes nothing new. The lesson JSON `version` field is not used for packaging, so publishing never edits lesson files.
- `upload` uploads the new version, then `catalog.json`, verifies both from the public endpoint, then deletes every version of that lesson no longer referenced by the catalog (Storage and local `publish/`). A phone that was mid-download of the old version fails that download, re-syncs the catalog and fetches the new one.

`mandarin-course publish lessons/<lesson_id>.json` reads only local lesson files,
generated tracks, the voice registry/cast, and `cache/<cache_key>.mp3`. It never calls
ElevenLabs or any other network API. The guided track is required. The authored
`lessons/<lesson_id>_practice.json` may contribute a reference transcript, but its
track is omitted from the catalog with a warning until the practice MP3 exists.
Vocabulary is authored separately in `lessons/<lesson_id>_vocab.json` with
`schema_version`, matching `lesson_id`, `status` (`draft` or `reviewed`), and a
`words` array containing `hanzi`, `pinyin`, and `meaning_vi`; publish validates this
sidecar before packaging.

### catalog.json

The only file a client checks to discover new or updated content. Every downloadable artifact carries byte size and SHA-256 so Android can detect incomplete or corrupt downloads.

```json
{
  "schema_version": 1,
  "catalog_version": 1,
  "lessons": [
    {
      "id": "lesson_001",
      "version": 2,
      "order": 1,
      "title": "...",
      "level": "absolute_beginner",
      "base_url": "lessons/lesson_001/v2/",
      "tracks": {
        "guided":   { "file": "guided.mp3",   "bytes": 1639675, "duration_ms": 204889, "sha256": "...",
          "timeline": { "file": "timeline_guided.json", "bytes": 4200, "sha256": "..." } },
        "practice": { "file": "practice.mp3", "bytes": 921125, "duration_ms": 112500, "sha256": "...",
          "timeline": { "file": "timeline_practice.json", "bytes": 2300, "sha256": "..." } }
      },
      "files": {
        "manifest":  { "file": "lesson.json",    "bytes": 12400, "sha256": "..." },
        "reference": { "file": "reference.json", "bytes": 8900,  "sha256": "..." },
        "items":     { "file": "items.json",     "bytes": 6100,  "sha256": "..." }
      }
    }
  ]
}
```

`base_url` is relative to the bucket's public root, which each client gets from config. Clip files are listed with their size and SHA-256 inside `items.json`.
Catalog output is deterministic: entries are sorted by the numeric part of the
lesson ID, and publishing identical inputs does not change `catalog.json`.
Optional track `guided_zh` (Chinese-only variant of `guided`, same catalog shape with its own `timeline`): built from cached clips, `vi-VN` segments and their pauses removed, ~400 ms gap before the next `zh-CN` segment; its cues keep the guided `line_id`s so players can switch tracks at the same line. Clients must ignore unknown track keys.

Published tracks are mono 44.1 kHz MP3 at 64 kbps. Every published track must have
a timeline whose cue IDs all exist in that track's reference lines.

### reference.json

Full transcript for the reference view: per line speaker, pinyin, Hanzi, Vietnamese meaning, and a **token list** (`hanzi`, `pinyin`, `item_id` if the token is a lesson item). Clients render each token as a tappable word; the lookup popup shows pinyin and meaning and plays the item clip. Segmentation is done once in the pipeline, never in the client.
Dialogue segments with `turns` produce one reference line per turn; their pinyin
and translation are authored on each turn so publish never has to infer a split.

### items.json (test item bank)

Raw material for test generation, not a fixed test.

```json
{
  "schema_version": 1,
  "lesson_id": "lesson_001",
  "lesson_version": 2,
  "words": [
    { "id": "w_买_mai3", "hanzi": "买", "pinyin": "mǎi", "tones": [3], "meaning_vi": "mua",
      "clip": { "file": "clips/<sha>.mp3", "bytes": 9100, "sha256": "..." } }
  ],
  "sentences": [
    { "id": "s_<text-sha256-prefix>", "activity": "...", "speaker": "coach_zh",
      "hanzi": "...", "pinyin": "...", "meaning_vi": "...", "clip": { "...": "..." },
      "tokens": [ { "hanzi": "买", "pinyin": "mǎi", "item_id": "w_买_mai3" } ] }
  ],
  "qa": [
    {
      "id": "r1_food",
      "activity": "...",
      "speaker": "coach_zh",
      "question": { "hanzi": "...", "pinyin": "...", "meaning_vi": "...", "clip": { "...": "..." } },
      "answer": { "hanzi": "...", "pinyin": "...", "meaning_vi": "...", "clip": { "...": "..." } }
    }
  ]
}
```

`qa` pairs come from consecutive authored Mandarin `prompt` → `model_answer`
segments, not from new writing. Word clips are available when an existing
non-dialogue segment exactly speaks that word after punctuation is stripped;
sentence and Q&A clips likewise come only from their existing cached segment audio.
Publish normalizes available clips to mono 44.1 kHz MP3 at 64 kbps with loudness
normalization and content-addresses them by the SHA-256 of the output bytes. A
missing cache clip leaves the item without `clip` and produces a report warning; it
never triggers TTS. Any item needing new TTS must be produced separately by
`generate` (paid, with the usual dry run), never by `publish`.
Word IDs are derived from Hanzi plus canonical tone-number pinyin, so the same word
has the same ID across lessons and vocabulary ordering. Sentence IDs use a stable
short SHA-256 prefix of the complete Hanzi text.

## Timeline contract (karaoke transcript)

Each published audio track has a timeline, `timeline_<track>.json`, listed in `catalog.json` under that track (`"timeline": {"file", "bytes", "sha256"}`).

- **Sentence level (MVP):** one cue per spoken line and per repetition, with
  `start_ms`/`end_ms` and the `reference.json` line ID it displays. A `turns`
  segment is one composite cached clip, so its cue uses the first turn as `line_id`,
  lists every turn in `line_ids`, and sets `granularity` to `"segment"`. Ordinary
  cues omit both optional fields and default to line granularity. Times are recorded
  by `build_audio` while it assembles the track (measured clip durations after
  `atempo`, plus inserted silences), never estimated from text length. Rebuilding
  audio from cache to get a timeline costs no TTS request.
- The timeline's `duration_ms` is the measured-parts end (including all silence).
  Generation compares it with the encoded MP3 duration from ffprobe and fails when
  absolute drift exceeds 150 ms.
- **Word level (later):** optional per-token `start_ms`/`end_ms` inside a cue, which
  also provides the timestamps needed to split grouped composite cues into per-turn
  timing. Planned once Lesson 1 has finished its content audit and is regenerated:
  switch TTS to ElevenLabs' with-timestamps endpoint (store the alignment next to
  each cached clip), and use local forced alignment for clips already in the cache.
  Character times must be divided by the segment's `playback_speed`.
- **Cue metadata:** each cue also carries `language` (`zh-CN` / `vi-VN`), `type` (segment type) and `section_id`, so clients never re-derive them from `reference.json` for playback decisions (Chinese-only mode, section replay).
- **Sections:** a top-level `sections` list, one entry per run of consecutive segments with the same `activity`: `{ "id", "title" (the activity label, e.g. "Nghe chuyện"), "start_ms", "end_ms" }`, contiguous and covering the whole track. Section boundaries include the silences inside the run.

```json
{
  "schema_version": 1,
  "lesson_id": "lesson_001",
  "lesson_version": 2,
  "track": "guided",
  "duration_ms": 204889,
  "sections": [
    { "id": "sec_03", "title": "Nghe chuyện", "start_ms": 30602, "end_ms": 70059 }
  ],
  "cues": [
    { "line_id": "r1_food", "start_ms": 12400, "end_ms": 14800, "language": "zh-CN", "type": "prompt", "section_id": "sec_05" }
  ]
}
```
