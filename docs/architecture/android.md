# Architecture — Android app behavior

> Part of the architecture docs; index: [../app_architecture.md](../app_architecture.md).

## Android app

Stack:

- Kotlin, Jetpack Compose
- Jetpack Media3 / ExoPlayer in a `MediaLibraryService`, with a `MediaController` in the UI. The Activity never owns the player, so playback survives leaving the UI and locking the screen.
- Room for content, download, and progress records, plus a sync outbox
- WorkManager + OkHttp for downloads and sync
- supabase-kt (Auth, Postgrest)

### Download behavior

1. Fetch `catalog.json` on startup and when the user taps Sync.
2. Compare remote lesson version and SHA-256 with Room.
3. Show available downloads; do not auto-download every lesson. Optional "Wi-Fi only".
4. Download each file to a `.part` path and verify byte size and SHA-256.
5. Rename atomically only after every artifact for the lesson is valid; then mark the lesson available offline.
6. Never delete an older playable version until the replacement is fully verified. A failed or cancelled update must leave the previous version playable. Provide retry, cancel, and delete.

### Playback

- Two tracks per lesson: **guided** and **practice**. "Continue listening" defaults to the last track and position. A daily-playlist mode queues practice tracks with no interaction.
- Play, pause, seek ±, previous/next lesson; lock-screen, notification, and headset controls via Media3.
- Speed 0.8×–1.1× locally.
- Restore last lesson and position (local only).

### Headset and Bluetooth controls

Handled in `PlaybackService`, so they work with the screen off and the UI closed:

- Single press: play/pause (Media3 default). Pressing play after the app was killed resumes the saved lesson, track and position (media-button resumption).
- Next (double press on most earbuds): seek to the start of the next transcript cue.
- Previous (triple press on most earbuds): seek to the start of the current cue ("Nghe lại câu"); if already within the first 1.5 s of the cue, go to the previous cue.
- Without a timeline, next/previous fall back to ±10 s.
- Disconnecting headphones pauses playback (audio-becoming-noisy).

Device note: on Xiaomi/MIUI (and similar aggressive battery managers) the app must be set to battery "No restrictions" and autostart allowed, otherwise the system kills it and headset buttons stop working. Settings should link to this explanation (G2b).

### Karaoke transcript

The player shows the transcript in sync with the audio: the current line is highlighted and auto-scrolls; tapping a line seeks to it; a single line can be looped for shadowing. Sentence level in the MVP, word level later (see Timeline contract).

Display is one dataset with several views, switched instantly, never separate audio or separate files:

- text hidden (listen only)
- pinyin only
- Hanzi with pinyin above (ruby)
- Hanzi only
- Vietnamese meaning on/off, independently of the above

The learner chooses the default view in Settings, and can change it per track (guided / practice) and on the fly in the player. Recommended default is text hidden or pinyin only, because audio is the main channel (see [lesson design principles](../lesson_design_principles.md)), but it is a preference, not a lock. Opening the text is normal and never penalized: if a line still can't be heard or said correctly after many listens, looking at it is the right move. The app does not count opening the text against progress.

One rule is not configurable: a `model_answer` line is never shown before the audio reaches it, so the answer gap for speaking aloud is preserved whatever view is selected.

### Chinese-only listening ("Chỉ nghe tiếng Trung")

For a learner who has heard a lesson many times and now understands the Chinese directly, so the Vietnamese restatement gets in the way. Decided 2026-10-20.

- A toggle in Now Playing (and in Settings as a per-track default), stored per lesson + track, always off for a lesson's first listen. Manual only: no automatic switch based on listen counts.
- When on, `PlaybackService` skips every `vi-VN` cue and the silence that follows it, jumping to the next `zh-CN` cue with a short natural gap (~400 ms) instead of a long pause. All Chinese stays, including model answers whose question was asked in Vietnamese, and the answer gaps between Chinese lines are kept as they are.
- **Implemented as a pre-rendered track** (decided 2026-10-21, replaces the L2 runtime skip): the pipeline builds `guided_zh.mp3` + `timeline_guided_zh.json` from cached clips (0 paid TTS) with every `vi-VN` cue and its trailing silence removed and a ~400 ms gap before the next `zh-CN` cue. Turning the toggle on/off switches the player to the other track at the same `line_id` (start of the current line, or of the next Chinese line if the current one is Vietnamese). Loop, sections, tap-to-seek and headset next/previous need no special casing: the track has its own full timeline.
- Why not a runtime skip: measured on device (L2-fix), "detect position, then seek" lands 0–270 ms late whatever the scheduling (150 ms poll, Media3 `PlayerMessage`, exact-gap delay), and 47 of 183 zh→vi transitions in lesson 1 have no silence to absorb the lead. Clip concatenation (clicks at splices, a window per clip) and a custom `AudioProcessor` (Android-only DSP, nothing iOS/web can share) were rejected. Web `timeupdate` (~250 ms) would be worse.
- Vietnamese text is hidden in the transcript while it is on. The player shows the effective duration of the remaining audio.
- Old packages without a `guided_zh` track: toggle disabled.

### Section replay

A lesson is divided into sections (timeline `sections`, from consecutive segments with the same `activity`, e.g. "Nghe chuyện", "Hỏi đáp", "Kể lại"). Decided 2026-10-20.

- Now Playing shows a section list (chapter style) and section markers on the seek bar; tapping a section jumps to its start.
- "Lặp phần này" repeats the current section **up to 5 times**, then playback continues into the next section automatically. The player shows "Lần n/5".
- The learner can end the repetition at any moment: **"Bỏ lặp"** (finish this pass, then continue) and **"Sang phần sau"** (jump to the next section now). Both are also reachable from the mini-player while a section loop is active.
- Section loop and line loop share the service's single loop state (only one is active at a time); state is published to controllers like the line loop.
- Combined with Chinese-only mode, repeating "Nghe chuyện" works as a self-made Chinese-only practice track.

### Progress sync

Room is the source of truth; the app is fully usable offline once signed in.

- `test_attempts` rows are immutable and keyed by a client-generated UUID. Push is an idempotent upsert.
- `lesson_progress` is one row per lesson; merge is last-write-wins on `updated_at`, except that `passed_at` is never cleared once set.
- Local writes go into an outbox. Sync runs on startup, on network regain (WorkManager constraint), and after each test: push the outbox, then pull rows with `updated_at` greater than the last pull cursor.
- Sign-in is email + password (or email OTP). No OAuth redirect flows in the MVP.

### Navigation and MVP screens

Music-app structure (Spotify / Zing MP3 style), because listening is the main activity and must never be interrupted by moving around the app:

- **Bottom navigation, three tabs:**
  1. **Learn**: lesson library (available, downloading, downloaded, update available, passed), continue-listening card on top, download/cancel/delete/retry.
  2. **Review**: lesson tests (generated questions, requeue on mistakes, result) and attempt history.
  3. **Settings**: default transcript view per track, Vietnamese default, Wi-Fi-only downloads, storage used, account, last sync time; catalog URL in debug builds only.
- **Mini-player** docked above the bottom navigation on every tab whenever something is loaded: title, track, play/pause, progress. Switching tabs never stops playback.
- **Now Playing**: tapping or swiping up the mini-player opens a full-screen player; the karaoke transcript fills most of it, like synced lyrics in a music app. Compact controls: play/pause, ±10 s, seek bar, speed, guided/practice switch, transcript view, Vietnamese toggle, loop state.

Visual design follows [`docs/ui/`](../ui/README.md) (start at its README, then read only the files for the screens a task touches); screens must not be designed ad hoc inside feature tasks.

### Word lookup and copy

- Tap a word (token with `item_id`) → in-app lookup sheet: Hanzi, pinyin, Vietnamese meaning, play the word clip when available.
- The lookup sheet also offers: **Copy**, **Open in Pleco** (Pleco's URL scheme, only if installed), and **Send to…** (Android `ACTION_PROCESS_TEXT` / share, so any installed dictionary or translator can receive it).
- Chinese text in the transcript is selectable (select a word or a whole line) so the learner can copy any span, not only lesson items; the system selection menu then shows installed dictionary/translator apps.
- Gestures must not conflict: line seek, word lookup, and text selection each need a distinct, discoverable interaction; the UI design decides which.

### MVP acceptance criteria

- A new lesson arrives through Sync without reinstalling the APK.
- A downloaded lesson plays, and its test can be taken, in airplane mode.
- Audio continues with the screen locked; lock-screen and headset play/pause work.
- Playback resumes near the saved position after restarting the app.
- A failed or cancelled update leaves the previous version playable.
- The highlighted transcript line matches the spoken line (including at 0.8× and 1.1× speed); tapping a line seeks to it.
- The default transcript view follows Settings; a `model_answer` line never appears before its audio.
- Two attempts at the same lesson test produce different questions.
- A test passed offline shows as passed on another device after both sync.
- No ElevenLabs key, Supabase service-role key, or other admin credential ships in the APK.
