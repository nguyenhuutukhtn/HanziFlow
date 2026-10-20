# UI Design Spec — Android App (MVP)

Written for AI agents implementing this in Jetpack Compose / Material 3. This is the
source of truth for visual and interaction design; `docs/app_architecture.md` §
"Navigation and MVP screens" points here and must not be redesigned ad hoc inside
feature tasks. If this file and the architecture doc conflict on data/contract
details (timelines, items, pass rule), the architecture doc wins; this file owns
layout, components, gestures, tokens, and copy.

Content and teacher behavior (what the lesson *says*) follow
[`lesson_design_principles.md`](lesson_design_principles.md). This doc only decides
how that content is displayed and touched.

## Review decisions (2026-10-20) — these override the screen files where they conflict

- **Long-press a token = lookup** (not text selection). The lookup sheet offers **Copy word**, **Copy line**, **Select text…** (opens the whole line in a sheet with freely selectable text, where the system menu shows installed dictionaries/translators) and **Send to…**.
- **No "Open in Pleco" button.** Pleco appears under Send to… / the selection menu when installed.
- **Mini-player secondary action = "Nghe lại câu"** (one-shot jump to the start of the current line). Loop is only toggled in Now Playing, on the active line.
- **Tests:** pass only if every question is right on the first try (see the pass rule in app_architecture.md). Wrong answers show the correct one and are requeued as a different question; the attempt continues but won't pass. There is a **"Xem đáp án"** action (marks the attempt not passed). The result screen has both states: **pass** and **not passed** (first-try correct / total, [Làm lại], [Nghe lại bài học]).
- **Accent color: amber** as in tokens.md.
- **Track display names: "Học có hướng dẫn" / "Luyện nghe"** (ids stay guided/practice).

## Section map

Other files in this folder refer to sections by their original numbers:

| Section | File |
|---|---|
| §0–§2 context, principles, navigation; §9 implementation order | this file |
| §3.1 Learn, §3.4 Lesson detail | [learn.md](learn.md) |
| §3.2 Mini-player, §3.3 Now Playing, §3.5 Lookup sheet, §3.6 Text selection, §4 Gesture map | [player.md](player.md) |
| §3.7 Review and tests | [review.md](review.md) |
| §3.8 Settings, §3.9 Error/offline | [settings_errors.md](settings_errors.md) |
| §5 Design tokens | [tokens.md](tokens.md) |
| §6 Components, §7 Accessibility | [components_a11y.md](components_a11y.md) |
| §8 UI copy (Vietnamese) | [copy_vi.md](copy_vi.md) |

A UI task reads this file plus only the files for the screens it touches (and tokens.md / components_a11y.md when it changes theme or shared components).

## 0. Context that shapes every decision

- Audio is the primary channel; the screen is secondary. The learner is often
  walking, on a bus, or at a desk with the phone face-down. [`lesson_design_principles.md`](lesson_design_principles.md)
  §1 is explicit: screen is for lookup or extra pinyin/Hanzi study, not the main
  channel.
- One-handed, thumb-only reach is the default posture, not an edge case.
- The UI language is Vietnamese throughout (labels, errors, empty states).
  Chinese content (Hanzi/pinyin) and Vietnamese meanings are two different text
  roles on screen and must be visually distinguishable at a glance.
- `model_answer` lines must never be visible before the audio reaches them
  (architecture doc, non-configurable). This is a display rule, not just a data
  rule — see §3.4.
- There is no existing design system to preserve; the current `kmp/android/` code
  (`MainActivity.kt`, `TranscriptPanel.kt`, `LookupSheet.kt`) is a single-screen
  developer harness with no bottom nav, no mini-player, no Settings/Review
  screens, and tap-for-seek / long-press-for-lookup on tokens. This spec replaces
  that screen with the real IA below; `TranscriptRendering`/`TranscriptCues`
  (in `shared/`) stay as the one source for what a line looks like — only the
  Compose layer around them changes.

## 1. Design principles

1. **The transcript is the hero, everything else is chrome.** On Now Playing, the
   transcript panel gets the majority of vertical space; transport controls
   collapse to a thin, thumb-reachable strip. Borrowed from Apple Music / Spotify's
   synced-lyrics screen, where lyrics dominate and controls shrink to a bottom bar
   ([Spotify lyrics on Now Playing](https://www.androidauthority.com/spotify-now-playing-interface-lyrics-update-3636643/), [Spotify lyrics feature](https://routenote.com/blog/spotify-lyrics-feature-launches-everywhere/)).
2. **Never require a look to keep listening.** Play/pause, ±10s, and loop-current-line
   must work from the mini-player and from lock-screen/notification controls
   (Media3-provided) without opening Now Playing. Large hit targets (56dp for the
   primary play/pause, never below 48dp per Material/WCAG — [Compose accessibility codelab](https://developer.android.com/codelabs/jetpack-compose-accessibility)).
3. **One dataset, switchable views, never a reload.** The hidden / pinyin / Hanzi+pinyin
   / Hanzi × Vietnamese-on/off matrix is a pure rendering decision over one transcript
   (already true in `shared/transcript`), not six screens. View switch is instant,
   same scroll position, same audio position.
4. **Three clearly separated text roles, not a color accent.** Hanzi (largest,
   highest contrast), pinyin (smaller, muted, diacritic-safe font), Vietnamese
   meaning (secondary color, smaller still). This is a typography hierarchy
   problem, not a hue problem — see §5.2. Precedent: Du Chinese and Pleco's reader
   both separate "head word" size/weight from gloss size/weight rather than using
   color alone to mean "this is a translation" ([Du Chinese Android launch](https://duchinese.net/blog/?p=48)).
5. **Dark by default.** This is a listening app used outdoors, on transit, and at
   night; dark reduces glare and battery draw on OLED, and is the expected mode
   for music/podcast apps at rest. Light theme exists and is selectable, but dark
   is the first-run default regardless of system theme (deliberate deviation from
   `isSystemInDarkTheme()` default — see §5.1).
6. **Gestures are disambiguated by target and duration, not stacked on one tap.**
   Word-level actions (lookup, select-to-copy) live on the token; line-level
   actions (seek, loop) live on the row chrome outside the token; see §4. This
   avoids Du Chinese/Pleco's common conflict of "tap word = everything."
7. **Every view must be resumable, not just available.** Transcript view mode,
   Vietnamese toggle, and loop state are state the service/app owns and restores,
   never transient UI state that resets on rotation or process death — consistent
   with the existing `TranscriptState`/session-extras architecture in
   `MainActivity.kt`.
8. **No navigation drawer, no tabs beyond the three bottom-nav destinations.**
   Material 3 Expressive's flexible nav bar is phone-first already
   ([9to5Google: Material 3 Expressive navigation](https://9to5google.com/2025/05/14/material-3-expressive-navigation/)); adding a drawer or secondary tab bar would fight the "don't need to look" principle by adding things to search for.

## 2. Information architecture

```
┌─────────────────────────────────────────────┐
│  (no top app bar on Learn/Review — content   │
│   starts with the screen title as a header)  │
├─────────────────────────────────────────────┤
│                                               │
│              [ active tab content ]          │
│                                               │
├─────────────────────────────────────────────┤
│  Mini-player (only when something is loaded) │
├─────────────────────────────────────────────┤
│  Bottom nav: Học (Learn) · Ôn tập (Review) · │
│              Cài đặt (Settings)              │
└─────────────────────────────────────────────┘
```

- **Bottom nav, 3 destinations, always visible** (standard Material 3
  `NavigationBar`, 3 items — under the 3–5 item sweet spot, no overflow needed):
  1. **Học** (Learn) — lesson library.
  2. **Ôn tập** (Review) — tests + attempt history.
  3. **Cài đặt** (Settings).
- **Mini-player** is a persistent region docked directly above the bottom nav,
  present on all three tabs whenever a lesson is loaded (even if paused). It
  never disappears on tab switch — switching tabs must not stop or hide playback,
  matching the architecture doc's requirement.
- **Now Playing** is not a nav destination; it is a modal full-screen route
  pushed by tapping/swiping-up the mini-player, with a system/gesture back (or a
  down-chevron) returning to whichever tab was active. This mirrors Spotify/Pocket
  Casts: the full player is always one gesture from anywhere, never a tab of its
  own ([Pocket Casts Android redesign](https://9to5google.com/2024/05/14/pocket-casts-redesign-android/)).
- **Lesson detail** is a lightweight intermediate screen reached from a Learn
  card only when the lesson needs attention (not yet downloaded, update
  available): it shows size, version delta, and the download action. A lesson
  that is already downloaded and tapped goes straight to Now Playing — no
  detail screen in the common path.

### Primary flow

```
Learn (library)
  → tap a downloaded lesson card
      → Now Playing (guided track, resumes last position)
          → swipe down / back → mini-player (keeps playing)
          → tap "Kiểm tra" (Take test) in Now Playing overflow
              → Test flow (question → feedback → next)
                  → Result (pass/fail)
                      → back → Review (if reached from there) or Learn
```

### Secondary flows

```
Learn (not downloaded) → tap card → Lesson detail → Download → (progress) → Downloaded → tap → Now Playing
Review (tab) → lesson row → Result/history → "Làm lại" (Retry) → Test flow
Now Playing → transcript line long-press → Lookup sheet → Copy / Mở trong Pleco / Chia sẻ
Settings → change default transcript view / Vietnamese default / Wi-Fi only / account / sync
```

## 9. Implementation order (biggest perceived-quality jump first)

1. **Bottom nav + persistent mini-player shell** (§2, §3.2). Right now there is
   one screen; nav + a mini-player that survives tab switches is the single
   change that makes this feel like a real music-style app rather than a
   debug harness, and every later screen slots into it.
2. **Now Playing as its own full-screen route** with the chrome layout in §3.3
   (transport bar, view-mode switcher, track switch) wrapping the *existing*
   `TranscriptPanel` — this is mostly re-plumbing navigation around code that
   already works, not new logic.
3. **Transcript visual polish**: the three-text-role typography (§5.2), the
   model-answer placeholder treatment, and the loop chip — these are what the
   learner stares at for the entire session, so correctness and legibility
   here outweighs every other screen combined.
4. **Lookup sheet actions**: Copy / Open in Pleco / Send to (§3.5) — currently
   only "Play clip" exists; this is small, self-contained, and high value for
   a learner trying to actually look words up mid-lesson.
5. **Learn screen card states + Lesson detail** (§3.1, §3.4) — turns the
   current flat lesson list into the real states (downloading/update/passed).
6. **Review tab + test flow + result** (§3.7) — the biggest *new* surface
   area; sequence after the above since it depends on `shared/`'s test
   generator being wired up per the architecture doc, not just UI.
7. **Settings tab** (§3.8) — mostly wiring existing preferences (view mode,
   Vietnamese toggle, Wi-Fi-only) that already exist as `repository` calls into
   one screen; low risk, do last among "real" screens.
8. **Dark/light theme tokens + Noto font bundling** (§5.1, §5.2, §5.3) — can
   technically run in parallel with steps 1–3, but sequenced after them here
   because getting the three-text-role hierarchy *right in default Material
   colors* first, then re-skinning, avoids re-deriving contrast decisions twice.
9. **Accessibility pass** (§7) — TalkBack labels and reduced-motion fallbacks
   last, once the real components (not placeholders) exist to label.

## Open questions

- Empty-state illustration style and app icon: not decided; use text-only empty states until then.
