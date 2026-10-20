---
name: android-ui-testing
description: How to verify Android UI changes in kmp/android cheaply — ViewModel tests, Compose UI tests and Roborazzi screenshot tests on the JVM instead of repeated device screenshots. Use for any task that adds or changes Compose screens, theme, typography, or UI copy.
---

# Android UI testing (token-lean)

Goal: UI changes are verified by tests that cost nothing to re-run. Images are only looked at when a screenshot test fails (the diff) or when the user approves a new screen once.

## Layers — use the cheapest one that proves the behavior

| Layer | Proves | Where |
|---|---|---|
| ViewModel / logic unit tests | state: active line, model_answer hidden, loop state, test flow | `kmp/android/src/test/`, `kmp/shared/src/commonTest/` |
| Compose UI tests on JVM (Robolectric) | behavior: element appears, tap navigates, playback survives tab switch | `kmp/android/src/test/` |
| Roborazzi screenshot tests (Robolectric) | looks: layout, colors, dark/light, font scale 2.0, long Vietnamese text | `kmp/android/src/test/`, goldens in `kmp/android/src/test/screenshots/` |
| Device check | only what a real phone can prove: background playback, lock screen, Bluetooth buttons, MIUI killing the app | attached phone, text output only |

## Rules

- Screens are stateless Composables taking a `UiState` + event lambdas, so tests render them with fake state. No repositories, Room, network or `MediaController` in UI tests.
- For every screen or component state in `docs/ui/`, add a Roborazzi test with fake state. Cover dark and light, and font scale 2.0 for text-heavy screens (Now Playing transcript, lookup sheet, test questions).
- Goldens are recorded only after the user has approved the screen visually once. Until then, record into the build dir and don't commit goldens.
- An intentional visual change re-records the affected goldens and the report says which goldens changed and why. Never re-record to make an unexplained failure pass.
- On a screenshot failure, open only the diff image(s) in `kmp/android/build/outputs/roborazzi/`, not every screenshot.
- Device checks: prefer `adb shell dumpsys media_session`, `uiautomator dump` piped through grep, and filtered logcat. At most 3 device screenshots per task, saved to `kmp/build/screenshots/`, and only when the user needs to approve a new look.
- No on-device UI automation (Espresso/Maestro) for routine changes.

## Commands (from `kmp/`)

```bash
./gradlew -q :android:testDebugUnitTest          # unit + Compose UI tests
./gradlew -q :android:verifyRoborazziDebug       # compare against goldens
./gradlew -q :android:recordRoborazziDebug       # (re)record goldens — only per the rules above
./gradlew -q :shared:allTests :android:testDebugUnitTest :android:verifyRoborazziDebug :android:lintDebug :android:assembleDebug   # full verify before reporting
```

Configure Roborazzi's output so goldens live in `kmp/android/src/test/screenshots/` (committed) and diffs in the build dir (ignored).
