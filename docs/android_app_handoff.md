# Android App Handoff

## Product decision

Build a personal, Android-only, audio-first Mandarin course app.

Course content and teacher behavior follow [Lesson Audio Design Principles](lesson_design_principles.md). The app plays and tracks that content; it does not attempt to replace the teacher script with generic UI exercises.

- The app is installed once. Adding a lesson must not require rebuilding or reinstalling the APK.
- Course content is published separately from app code.
- Firebase Hosting is the remote content source; do not use Firebase Storage, Firestore, Auth, or Cloud Functions for the MVP.
- Every downloaded lesson is stored on the phone and remains playable offline.
- Audio must continue while the screen is locked or the UI is closed.
- The home screen prioritizes a passive daily playlist, resume playback, and background listening; it must not require looking at the screen or answering prompts.
- Each lesson has two tracks: **guided** for meaning, pronunciation, imitation, answer gaps, and quick response; **practice** for continuous natural-speed passive listening.
- Guided and practice tracks for one lesson must use the same character voice assignment.
- Voice diversity is intentional: Lessons 1–3 stay stable, later lessons rotate curated Mandarin voice pairs, and mixed-voice review can reuse familiar patterns with a new pair.
- The listening view defaults to pinyin. Vietnamese meaning and Hanzi belong in a separate full-reference view.
- Pinyin/tones and Hanzi recognition are optional modules. They never block listening progress.
- Voice lists and assignments are resolved during authoring/publishing; the Android app downloads finalized audio only.

## Recommended Android stack

- Kotlin
- Jetpack Compose
- Jetpack Media3 with ExoPlayer
- `MediaLibraryService` for background playback, lock-screen controls, headset controls, and the system media notification
- A `MediaController` in the Compose UI
- Room for lesson/download/progress records, or DataStore if the persisted model stays very small
- WorkManager plus an HTTP client for reliable content downloads

Do not make the Activity own the player. The player and media session must live in the media service so playback survives leaving the UI.

## Content architecture

The existing Python project remains the course-authoring and audio-generation system. It should eventually publish this structure:

```text
publish/
├── catalog.json
└── lessons/
    ├── lesson_001/
    │   └── v2/
    │       ├── guided.mp3
    │       ├── practice.mp3
    │       ├── lesson.json
    │       ├── timeline_guided.json
    │       ├── timeline_practice.json
    │       └── reference.json
    └── lesson_002/
        └── v1/
            └── ...
```

Use versioned paths. Do not overwrite an existing version in place, because the CDN and the Android device may have cached it.

The app downloads remote files into an app-private structure such as:

```text
filesDir/course/
├── catalog.json
└── lessons/
    └── lesson_001/v2/
        ├── guided.mp3
        ├── practice.mp3
        ├── lesson.json
        ├── timeline_guided.json
        ├── timeline_practice.json
        └── reference.json
```

## Remote catalog contract

`catalog.json` is the only file the app checks to discover new or updated content.

Example:

```json
{
  "schema_version": 1,
  "catalog_version": 3,
  "generated_at": "2026-10-19T12:00:00Z",
  "lessons": [
    {
      "id": "lesson_001",
      "version": 2,
      "title": "Greetings and Introductions",
      "level": "absolute_beginner",
      "duration_ms": 204889,
      "base_url": "/lessons/lesson_001/v2/",
      "tracks": {
        "guided": {
          "file": "guided.mp3",
          "bytes": 3279350,
          "duration_ms": 204889,
          "sha256": "replace-with-real-digest",
          "timeline": {
            "file": "timeline_guided.json",
            "bytes": 4200,
            "sha256": "replace-with-real-digest"
          }
        },
        "practice": {
          "file": "practice.mp3",
          "bytes": 1842250,
          "duration_ms": 112500,
          "sha256": "replace-with-real-digest",
          "timeline": {
            "file": "timeline_practice.json",
            "bytes": 1800,
            "sha256": "replace-with-real-digest"
          }
        }
      },
      "manifest": {
        "file": "lesson.json",
        "bytes": 12400,
        "sha256": "replace-with-real-digest"
      },
      "reference": {
        "file": "reference.json",
        "bytes": 8900,
        "sha256": "replace-with-real-digest"
      }
    }
  ]
}
```

Use SHA-256 and byte size for every downloadable artifact. This lets the app detect incomplete or corrupt downloads.

## Timeline contract

The current generator does not yet produce synchronized timelines. Each downloaded audio track needs its own timeline before implementing line highlighting and tap-to-seek.

Each `timeline_<track>.json` should describe its final assembled MP3, including generated speech and pauses:

```json
{
  "schema_version": 1,
  "lesson_id": "lesson_001",
  "lesson_version": 2,
  "duration_ms": 204889,
  "cues": [
    {
      "id": "listen_scene_turn_03",
      "segment_id": "listen_scene",
      "turn_index": 2,
      "start_ms": 12400,
      "end_ms": 14800,
      "speaker": "li_ming",
      "pinyin": "nǐ jiào shénme",
      "hanzi": "你叫什么？",
      "translation": "Bạn tên gì?"
    }
  ]
}
```

Times must be measured from the final assembled file, not estimated from text length. The audio builder should record the duration of every clip and inserted silence while composing the lesson.

## Transcript behavior

Listening mode:

- Show narration and pinyin.
- Highlight the active cue when timeline data exists.
- Do not show Vietnamese meaning or Hanzi by default.
- Tapping a cue seeks to `start_ms`.
- Allow repeating the current cue or a selected range.

Full-reference mode:

- Show pinyin, Hanzi, Vietnamese meaning, speaker, and optional context.
- This can initially use `reference.json`; Markdown rendering is not required in the Android MVP.

## Sync and download behavior

1. Fetch `catalog.json` when the user taps Sync and optionally once on app startup.
2. Compare remote lesson version and SHA-256 with the local database.
3. Show available downloads; do not automatically download every lesson.
4. Download each file to a temporary `.part` path.
5. Verify byte size and SHA-256.
6. Atomically rename the files only after every artifact for the lesson is valid.
7. Mark the lesson available offline.
8. Never delete an older playable version until the replacement has been completely verified.

Download failures must not damage an already installed lesson. Provide retry, cancel, and delete-download actions.

## Playback requirements

- Play local guided and practice tracks when a lesson has been downloaded.
- Default **Continue listening** to the last selected track and position. A daily-playlist mode queues practice tracks without requiring interaction.
- Optionally support remote preview later; it is not required for the MVP.
- Continue playback with the screen locked.
- Provide play, pause, seek backward, seek forward, and previous/next lesson through Media3.
- Restore the last lesson and playback position.
- Support playback speed such as 0.8x, 0.9x, 1.0x, and 1.1x locally without regenerating audio.
- Display lesson title, course name, and artwork in the system media notification and lock-screen controls.

## Local progress

Persist locally:

- Installed content version
- Download status and local file paths
- Last playback position per lesson
- Completion percentage
- Last played lesson
- Optional bookmarks
- Selected audio track and last position per track
- Pinyin/tones and Hanzi learning state when those modules are added
- Per-pattern exposure count, last exposure, and learner-supplied familiarity rating when adaptive review is added

No account or cloud sync is required for the personal MVP.

Adaptive familiarity is a later local feature. The app may record exposure and learner ratings (`again`, `hard`, `good`, `easy`) to drive future content generation and Vietnamese phase-out, but the MVP must not imply that passive listening alone proves mastery.

## Hanzi module

Keep Hanzi study separate from the listening screen. It should use only characters and words already introduced by downloaded lessons.

Suggested later features:

- Play audio, then choose the matching Hanzi
- Show Hanzi and ask the learner to pronounce it
- Reveal pinyin and meaning on demand
- Mark items as `new`, `learning`, or `mastered`
- Add spaced repetition after the basic player is stable
- Add stroke-order animation only after recognition practice exists

The first app release does not need handwriting or stroke-order support.

## Pinyin and tones module

Keep pronunciation study separate from the player and make it optional. It should support short 5–10 minute sessions for:

- four tones and the neutral tone;
- listening contrasts such as `zh/z`, `sh/s`, and `ü/u`;
- playing a model sound, choosing a matching sound, and repeating aloud; and
- reviewing only sounds and words introduced by downloaded lessons where possible.

Speech recognition, automatic scoring, AI chat, and gamification are explicitly out of scope for the first release.

## Firebase Hosting

Use Firebase Hosting for public static course artifacts. A typical public URL is:

```text
https://PROJECT_ID.web.app/catalog.json
```

Recommended caching policy:

- `catalog.json`: `Cache-Control: no-cache` or a very short max age
- Versioned lesson artifacts: `Cache-Control: public, max-age=31536000, immutable`

Never publish:

- `.env`
- ElevenLabs API keys
- TTS cache internals
- Source files containing secrets
- Temporary `.part` files

Only publish finalized lesson packages.

Firebase Hosting is preferred over Cloud Storage for this personal MVP. Hosting has a no-cost static-hosting allowance, while Cloud Storage for Firebase requires a Blaze billing plan. If Firebase Hosting is not used, GitHub Releases is an acceptable public-content fallback.

## Publishing workflow to implement later

Desired command:

```bash
mandarin-course publish lessons/lesson_001.json
```

Expected behavior:

1. Validate the lesson.
2. Require an existing generated MP3.
3. Generate `lesson.json`, a reference JSON, and one timeline per audio track for the app.
4. Compute duration, sizes, and SHA-256 digests.
5. Copy artifacts into a versioned `publish/` directory.
6. Update `catalog.json` deterministically.
7. Never call ElevenLabs during publishing.
8. Run `firebase deploy --only hosting` as a separate explicit step.

## MVP screens

1. Library: available, downloaded, and completed lessons
2. Downloads: progress, retry, cancel, and delete
3. Player: guided/practice switcher, artwork, progress, speed, skip controls, and pinyin transcript
4. Full reference: pinyin, Hanzi, and Vietnamese meaning
5. Settings: catalog URL, storage usage, and optional Wi-Fi-only downloads

Pinyin/tones practice can be the first optional learning module after these screens are stable; Hanzi recognition follows it.

## MVP acceptance criteria

- The APK is installed once and receives a new lesson through Sync.
- A downloaded lesson plays with airplane mode enabled.
- Audio continues after locking the screen.
- Lock-screen and headset play/pause controls work.
- Playback resumes near the saved position after restarting the app.
- A failed or cancelled update leaves the previous lesson playable.
- The practice view shows pinyin without Vietnamese meaning or Hanzi.
- The full-reference view shows pinyin, Vietnamese meaning, and Hanzi.
- No ElevenLabs or Firebase administrative credential is embedded in the APK.

## Current repository state

- Lessons 1–3 have guided and practice audio; published packages must preserve both tracks.
- Timeline and app-oriented JSON export are not implemented yet.
- Firebase deployment is not configured yet.

## References

- Android Media3 background playback: https://developer.android.com/media/media3/session/background-playback
- Firebase pricing: https://firebase.google.com/pricing
- Firebase Storage billing requirement: https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024
- GitHub Releases: https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases
