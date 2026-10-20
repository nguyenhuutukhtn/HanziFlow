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

## Pending review changes

These parts of the spec are under review (see [docs/STATUS.md](../STATUS.md) "Open decisions"). Do not implement them as written until resolved:

- Long-press on a token is specified for both lookup and starting text selection; on Android a long-press inside selectable text always starts selection. Proposed: long-press = lookup; the lookup sheet offers "copy line" and "select text…" (the line in a freely selectable sheet).
- Mini-player secondary action = loop toggle. Proposed: "replay current line" (one-shot jump to the line start) instead.
- Review: "no reveal / no fail screen" conflicts with the pass rule in the architecture doc, which is itself awaiting a decision.
- "Open in Pleco" button: proposed to drop and rely on "Send to…" only.

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

## Open questions needing the user's taste

These are deliberately left as decisions, not defaults buried in the tokens above:

- **Accent color**: §5.1 proposes a warm amber primary (evokes a "teacher's
  highlighter," pairs well with the brown-black dark background) with teal as
  secondary. This is a placeholder choice, not a brand decision — confirm or
  replace before implementation locks in `Color.kt`.
- **Illustration/icon style** for empty states (Learn empty, Review "no
  attempts") — this spec describes them only as "(illustration/icon)" (§3.1)
  and intentionally does not prescribe a style (flat icon vs. a small
  character-based mascot vs. none at all/text-only). No research was done on
  illustration style since it's a brand-taste call, not a usability one.
- **App icon / launcher branding** — out of scope for this spec entirely.
- **"Guided" / "Luyện tập" track naming**: §8 keeps the English loan terms as-is
  to match the lesson JSON's own vocabulary, but if the user wants fully
  Vietnamese track names (e.g. "Bài học" / "Luyện nói") that's a one-line string
  change — flagging it here because it touches both UI copy and potentially
  the lesson-authoring side's mental model, so it's worth a deliberate choice
  rather than a silent pick.
- **Whether "Mở trong Pleco" is worth the Android 11+ package-visibility
  query** (`<queries>` manifest entry for Pleco's package) versus just always
  offering the generic system "Send to…" and letting Pleco show up there
  automatically if installed — research (§ Pleco) found the deep-link scheme
  is community-documented, not officially stable API, so there's a real
  reliability-vs-polish tradeoff the user should weigh before committing to a
  direct Pleco button.
