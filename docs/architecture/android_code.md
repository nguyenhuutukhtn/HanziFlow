# Architecture — Android code structure

> Part of the architecture docs; index: [../app_architecture.md](../app_architecture.md).

## Code architecture (Android)

Goal: the UI can be redesigned or rewritten without touching business logic, and the logic is testable without a device. Follows Google's recommended app architecture (UI → optional domain → data), with unidirectional data flow. No extra ceremony beyond that.

```text
ui/<feature>/        Composables (stateless: state in, events out) + one ViewModel per screen
                     ViewModel exposes StateFlow<UiState>, takes events (onEvent / functions)
domain/              use cases only where logic spans several repositories or the player
                     (StartTestAttempt, FinishTestAttempt, SyncProgress, ResolveContinueListening)
data/                repositories (interfaces + implementations): Course, Progress, Settings, Auth
                     data sources: Room, OkHttp downloads, file store, Supabase
playback/            PlaybackService (owns ExoPlayer, loop, resumption) +
                     PlayerConnection: wraps MediaController, exposes StateFlow<PlaybackState>
                     and commands; the only place UI-side code talks to Media3
kmp/shared/          pure platform-independent rules (contract models, test generator,
                     pass rule, transcript decisions); also used by the web app
```

Rules:

- Dependency direction is one-way: `ui → domain → data`, `ui → playback (PlayerConnection)`, everything may use `shared`. `data` and `playback` never import `ui`. Composables never touch repositories, Room, OkHttp, or `MediaController` directly.
- Composables are stateless and previewable; screen state is one immutable `UiState` per screen. Navigation and one-off effects (snackbar, open URL) go through the ViewModel as events.
- Logic that the web app will also need goes into `kmp/shared`, not into Android ViewModels.
- The service is the source of truth for playback state (position, track, loop); ViewModels only mirror it via `PlayerConnection`.
- Dependency injection: Hilt.
- Single `:android` module with these packages for now; split into Gradle modules only when build time or ownership requires it. Package boundaries are enforced by architecture tests (Konsist) so AI-written code can't quietly break the layering.

## Shared core, thin platform shells (Android, iOS, web)

Decided 2026-10-21: the app will ship on Android, web and iOS. The layering above stays (it is Google's app architecture, not full clean architecture; don't add layers). What changes is **where decisions live**: every decision about lessons, timelines, playback rules, progress and sync is written once in `kmp/shared` and each platform is a shell that renders state and runs side effects.

```text
kmp/shared/<feature>/   pure core: models, rules, engines/state machines (+ tests with contract fixtures)
                        talks to the outside only through small ports (interfaces) it declares
platform shell          adapters for the ports (ExoPlayer/AVPlayer/HTMLAudio, Room/IndexedDB, HTTP)
                        + UI that renders the core's state and forwards user events
```

Rules:

1. **No business `if` in a shell.** If a branch depends on lesson/timeline/progress data (loop end, skip range, pass count, section, pass rule, merge), it belongs in `shared` with a unit test. Shells may branch only on platform concerns (permissions, lifecycle, audio focus).
2. **Playback rules are one engine.** `shared` owns a `ListeningEngine` (loop line/section, Chinese-only skips, cue/section navigation, correcting user seeks). Platform players call it on each position tick and on each user command and execute the returned action (`Seek(ms)`, `None`); they hold no loop or skip state of their own.
3. **UI state is mapped in shared.** Formatting and derivation that every platform repeats (section `n/N`, `≈ mm:ss` remaining, loop chip text inputs) is a pure function in `shared`; ViewModels just collect and call it.
4. **Ports, not libraries, in the core's public API.** Public `shared` API = data classes, sealed classes, `Long` milliseconds and plain functions, so it exports to JS (`@JsExport`) and Swift cleanly. No `Flow`, coroutines, or platform types in public signatures of the core.
5. **Mirror feature names.** `shared/<feature>/` and `android/ui/<feature>/` (later `ios/…`, `web/src/<feature>/`) use the same feature names, so a task reads one feature folder per side.
6. **Debug by event log, not screenshots.** The engine logs one line per event and resulting action under one tag (`HF/engine`). A bug report is a logcat excerpt; the fix starts with a `shared` unit test that replays those events.
7. **Sizes:** core files ≤ 200 lines; Service/ViewModel ≤ 200; any file ≤ 300 (`just check` audits).
8. **Data layer:** stays per platform for now (Room/OkHttp on Android). Sync/merge *rules* go to `shared`. Which KMP libraries to use for shared data (Ktor, supabase-kt, Room KMP/SQLDelight) is decided when D2 or iOS starts, not before.
