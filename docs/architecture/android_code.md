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
