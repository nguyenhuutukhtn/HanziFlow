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

## 3. Screens and states

All wireframes are phone width (~360–412dp), portrait. `[ ]` = tap target,
`▓▓` = filled/active, `░░` = track/inactive, `··` = muted text.

### 3.1 Learn (tab 1) — library states

**Empty (no catalog synced yet, first run)**

```
┌───────────────────────────────────┐
│  Học                               │
│                                     │
│         (illustration/icon)        │
│                                     │
│   Chưa có bài học nào.             │
│   Đồng bộ để tải danh sách bài     │
│   học mới nhất.                    │
│                                     │
│        [  Đồng bộ ngay  ]          │
│                                     │
└───────────────────────────────────┘
   Học · Ôn tập · Cài đặt
```

**Populated — mixed states (the real common case)**

```
┌───────────────────────────────────┐
│  Học                      [↻ sync] │
│ ┌─────────────────────────────────┐│
│ │ ▶ Đang nghe: Bài 1 · Guided     ││   ← continue-listening card,
│ │ ▓▓▓▓▓▓▓▓░░░░░░  12:04 / 24:10   ││     only if a lesson was opened
│ └─────────────────────────────────┘│     before; tap → Now Playing
│                                     │
│ ┌─────────────────────────────────┐│
│ │ Bài 1 · Sơ cấp          ✓ Đã qua ││  ← passed: check badge,
│ │ Người mới bắt đầu               ││     muted card tone
│ │                      [▶ Nghe]   ││
│ └─────────────────────────────────┘│
│ ┌─────────────────────────────────┐│
│ │ Bài 2 · Sơ cấp                  ││  ← downloaded, not yet passed
│ │ Ở siêu thị                      ││
│ │                      [▶ Nghe]   ││
│ └─────────────────────────────────┘│
│ ┌─────────────────────────────────┐│
│ │ Bài 3 · Sơ cấp        12 MB     ││  ← not downloaded
│ │ Hỏi đường                       ││
│ │                   [⬇ Tải xuống] ││
│ └─────────────────────────────────┘│
│ ┌─────────────────────────────────┐│
│ │ Bài 4 · Sơ cấp                  ││  ← downloading
│ │ Gọi điện thoại                  ││
│ │ ▓▓▓▓▓▓░░░░░░  45%    [✕ Hủy]   ││
│ └─────────────────────────────────┘│
│ ┌─────────────────────────────────┐│
│ │ Bài 1 · Sơ cấp     🆕 Có bản mới ││  ← update available: old version
│ │ Người mới bắt đầu               ││     still playable
│ │        [▶ Nghe bản cũ] [⬆ Cập nhật] │
│ └─────────────────────────────────┘│
└───────────────────────────────────┘
   Học · Ôn tập · Cài đặt
```

- Card states map 1:1 to `LessonEntity.downloadState`: `NOT_DOWNLOADED`,
  `DOWNLOADING`, `DOWNLOADED`, `UPDATE_AVAILABLE`, `FAILED`/`CANCELLED` (shown as
  "Thử lại" / Retry, same slot as Download).
- The "Đã qua" (passed) badge comes from `lesson_progress.status == 'passed'`.
  It never blocks re-listening or retaking the test.
- Swipe-to-delete on a downloaded card (confirm dialog: "Xóa bài học này? Bạn có
  thể tải lại sau." / "Delete this lesson? You can download it again later.") —
  covers the architecture doc's delete requirement without a dedicated icon
  competing for thumb space on every card.
- Sync icon in the top-right is a manual re-check; pull-to-refresh on the list
  does the same thing (standard, discoverable, doesn't need its own label).

**Error / offline (sync tapped with no network)**

```
┌───────────────────────────────────┐
│  Học                      [↻ sync] │
│ ┌─────────────────────────────────┐│
│ │ ⚠ Không có kết nối mạng.        ││
│ │ Các bài đã tải vẫn nghe được.   ││
│ └─────────────────────────────────┘│
│   (existing cards render as before,│
│    this banner is additive)        │
└───────────────────────────────────┘
```

### 3.2 Mini-player (persistent, all tabs)

```
┌───────────────────────────────────┐
│ ▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░ │ ← thin progress line, full width
│  Bài 1 · Guided        [‖ ] [⏭]   │ ← title/track · pause-or-play · next-line? no:
│  Người mới bắt đầu                │   next = next lesson only in Now Playing,
└───────────────────────────────────┘   mini-player keeps 2 controls max
   Học · Ôn tập · Cài đặt
```

- Exactly two controls in the mini-player: play/pause (56×48dp hit area) and one
  secondary action. Secondary action is **loop-current-line toggle** (↻), not
  skip — skip-to-next-lesson is rare and belongs in Now Playing; loop-current-line
  is the one action a learner repeats constantly while walking without looking.
  Tapping the title/track area (not the controls) opens Now Playing.
- Progress line is tappable-free (no seek here — seeking needs the full
  transcript/seek bar in Now Playing; a thin strip is too easy to mis-tap while
  walking). This resolves part of the gesture conflict in §4.
- Swipe up on the mini-player opens Now Playing (shared-element-style expand);
  swipe left/right is reserved (not used in MVP) to avoid accidental track
  switches.

### 3.3 Now Playing (full screen)

This is the one screen with five transcript-view sub-states plus loop and
Vietnamese toggle — same screen, same data, different render.

**Overall layout (chrome, any view mode)**

```
┌───────────────────────────────────┐
│  ⌄          Bài 1            ⋮    │ ← collapse chevron · lesson title · overflow (Kiểm tra / Báo lỗi)
│                                     │
│  ┌──────┬───────────┬──────┐      │ ← view-mode segmented control
│  │ Ẩn   │Pinyin│Hán+pinyin│Hán │  │   (horizontally scrollable chip row,
│  └──────┴───────────┴──────┘      │    see 5.4 — shown compact as 4 chips)
│          Tiếng Việt  [●  ⚪]       │ ← Vietnamese on/off switch, own row
│  ───────────────────────────────  │
│                                     │
│          (transcript area,         │
│           ~55–65% of screen         │
│           height, scrollable,       │
│           auto-scroll + highlight)  │
│                                     │
│  ───────────────────────────────  │
│  Guided  │  Luyện tập              │ ← track switch (only if practice exists)
│  00:42 ▓▓▓▓▓▓▓░░░░░░░░░░░░ 03:10  │ ← seek bar with time labels
│  0.8× 0.9× 1.0× 1.1×               │ ← speed chips
│     [⏮10s]    [ ▶ ]    [10s⏭]     │ ← primary transport, center button largest
└───────────────────────────────────┘
```

- Overflow menu (⋮): "Kiểm tra bài" (Take the test), "Tải lại bài" (if a file is
  corrupt), "Báo cáo vấn đề" (optional, later). Keeping test entry in overflow —
  not a persistent button — because it's an occasional action, not a listening
  action; it must not compete with transport for thumb space.
- The transcript area is intentionally the largest region, per Principle 1.

**Transcript view — Ẩn (Hidden)**

```
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │ ← active line: a plain highlighted bar,
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │   no text at all — tap anywhere on a bar
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ↻   │   to seek to it; the ↻ loop icon appears
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │   only on the currently active bar
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │
```
Pure listen mode. Bars give rhythm/structure feedback (how long each line is,
where you are in the lesson) without giving away content — useful for seeking
by "shape" without reading ahead.

**Transcript view — Pinyin only**

```
│  lǐ mǐng, nǐ hǎo.                 │
│  nǐ jiào shénme?                 │
│▓ wǒ jiào ānnà. nǐ ne?        ↻  │ ← active line, pinyin in body-size text
│  (Việt: Tôi tên là Anna. Bạn thì?)│ ← Vietnamese meaning, shown only if
│  hěn gāoxìng rènshi nǐ.           │   the VN switch is on; smaller, muted
```

**Transcript view — Hán tự + pinyin (ruby)**

```
│   nǐ    jiào   shénme             │ ← pinyin ruby row, small, muted
│   你     叫     什么？             │ ← Hanzi, large, tappable per token
│▓  wǒ    jiào   ānnà.  nǐ  ne  ↻  │
│   我     叫     安娜。  你  呢     │
│  (Việt: Tôi tên là Anna. Bạn thì?)│
```
Default view (see Principle 4 / §5.2 for ruby sizing). Each Hanzi+pinyin pair is
one token unit per `shared/transcript` `TokenRenderDecision`.

**Transcript view — Hán tự only**

```
│   你叫什么？                        │
│▓  我叫安娜。你呢？              ↻  │
│  (Việt: Tôi tên là Anna. Bạn thì?)│
```

**Vietnamese off (applies on top of any of the above)**

Same as above but the `(Việt: …)` line is omitted entirely (not collapsed to an
empty row — removing it reclaims vertical space, per the fade-out principle in
`lesson_design_principles.md` §8).

**`model_answer` line, not yet revealed**

```
│▓  ⟪ Đến lượt bạn — nói trước khi nghe ⟫   │  ← placeholder, same height as a
│                                             │    normal line, tap disabled except
│                                             │    for seek; no lookup, no selection
```
Rendered via `decision.placeholder` (already modeled in `TranscriptCues`/
`TranscriptRendering`). Once revealed (audio has passed the reveal point), it
renders exactly like any other line in the current view mode — never shown
early regardless of view mode, loop, or scrub-ahead via the seek bar. If the
learner drags the seek bar past the reveal point, the reveal rule still applies
(reveal is a function of "has playback position crossed X," not "is currently
at X"), so dragging past it is equivalent to having listened that far.

**Active loop state**

```
│  ┌───────────────────────────────┐│
│  │ ↻ Đang lặp: 你叫什么？      ✕ ││ ← persistent chip, sits just above the
│  └───────────────────────────────┘│   transcript or just below the view
│▓▓ 你叫什么？                 ↻1  ││   switcher; active line shows a filled
│                                     │   loop icon with a small "1" badge
```
Only one line can be looped at a time (matches `PlaybackService`'s single
loop-segment command). Loop persists across Now Playing → mini-player →
Now Playing (service-owned state, per Principle 7). Starting a new loop or
leaving the lesson clears it.

### 3.4 Lesson detail (only for not-yet-downloaded / update-available)

```
┌───────────────────────────────────┐
│  ←  Bài 3 · Hỏi đường              │
│                                     │
│  Trình độ: Sơ cấp                  │
│  Kích thước: 12 MB                 │
│  Thời lượng: ~22 phút              │
│                                     │
│  ☐ Chỉ tải qua Wi-Fi               │
│                                     │
│        [  ⬇ Tải xuống bài học  ]  │
└───────────────────────────────────┘
```
During download this becomes an inline progress bar + Cancel, same screen
(no separate "downloading" screen).

### 3.5 Lookup sheet

```
┌───────────────────────────────────┐
│              ──                   │  ← drag handle
│                                     │
│          叫                        │  ← Hanzi, headline size
│          jiào                      │  ← pinyin, title size
│          gọi là, tên là            │  ← Vietnamese meaning, body size
│                                     │
│   [ ▶ Phát âm ]                    │  ← play clip, only if clip exists
│                                     │
│  ──────────────────────────────── │
│  [ 📋 Sao chép ]  [ 📖 Mở Pleco ]  │
│  [ 📤 Chia sẻ đến... ]             │
└───────────────────────────────────┘
```

- Triggered by **long-press** on a token (see §4 for why long-press, not tap).
- "Mở Pleco" (Open in Pleco) only renders if Pleco is installed (resolved via
  `plecoapi://x-callback-url/df?hw=<hanzi>`, the community-documented Pleco
  definition deep link — [Pleco URL scheme thread](https://www.plecoforums.com/threads/urls-scheme-in-pleco.5875)).
  If Pleco is not installed, this button is omitted, not disabled/greyed —
  disabled buttons the learner can't act on add visual noise for no benefit.
- "Chia sẻ đến..." (Send to…) fires `ACTION_PROCESS_TEXT`/standard share with the
  Hanzi word, so any installed dictionary/translator can receive it
  ([ACTION_PROCESS_TEXT mechanics](https://medium.com/google-developers/custom-text-selection-actions-with-action-process-text-191f792d2999)).
- "Sao chép" (Copy) copies Hanzi only (not pinyin/meaning) — the most common
  reason to copy is to paste into another dictionary or a chat.
- Sheet dismiss: tap outside, drag down, or system back. Does not pause playback.

### 3.6 Text selection (free selection, any token or line)

Standard Android `SelectionContainer`/`TextToolbar` behavior: long-press-drag to
extend selection across a line (works because Hanzi tokens are rendered as
plain selectable text runs, not canvas-drawn glyphs). The system selection
toolbar appears above the selection with **Copy** plus whatever
`ACTION_PROCESS_TEXT` targets are installed (Pleco, Google Translate, etc.) —
no custom toolbar needed, which keeps this consistent with every other Android
text field the learner already knows. Selection is available on Hanzi and
pinyin rows, not on the Vietnamese meaning row (meaning is reference text, not
lesson content to look up further) and not on `model_answer` placeholders.

### 3.7 Review (tab 2)

**List**

```
┌───────────────────────────────────┐
│  Ôn tập                            │
│ ┌─────────────────────────────────┐│
│ │ Bài 1 · Người mới bắt đầu       ││
│ │ ✓ Đã qua · 3 lượt làm           ││
│ │                   [Làm lại]    ││
│ └─────────────────────────────────┘│
│ ┌─────────────────────────────────┐│
│ │ Bài 2 · Ở siêu thị              ││
│ │ Chưa qua · 1 lượt làm           ││
│ │                   [Làm bài]    ││
│ └─────────────────────────────────┘│
│ ┌─────────────────────────────────┐│
│ │ Bài 3 · Hỏi đường               ││
│ │ Chưa có lượt làm nào            ││  ← not downloaded or never attempted
│ │                      (mờ, disabled) │
│ └─────────────────────────────────┘│
└───────────────────────────────────┘
   Học · Ôn tập · Cài đặt
```
Tapping a card with attempts opens attempt history (date, mistakes, pass/fail
per attempt) before starting a new one; "Làm bài"/"Làm lại" always starts a
fresh generated attempt (never resumes a partial one — a wrong answer requeues
within the *same* attempt per the pass rule, so there is no partial-attempt
resume state to design for).

**Test question types** (per architecture doc §"Test generation"):

```
listen → choose (nghe → chọn)
┌───────────────────────────────────┐
│  Câu 3 / 10                [✕ Thoát]│
│                                     │
│         [ ▶ Nghe lại ]             │
│                                     │
│  Đây là từ nào?                    │
│  ┌─────────┐ ┌─────────┐          │
│  │  买      │ │  卖      │          │
│  └─────────┘ └─────────┘          │
│  ┌─────────┐ ┌─────────┐          │
│  │  schema │ │  schema │          │
│  └─────────┘ └─────────┘          │
└───────────────────────────────────┘

Hán tự → pinyin, tone-identification, cloze, and QA types share this frame:
fixed replay button top, prompt, 2×2 or 1-column answer grid sized to content
(short Hanzi options → 2×2 grid; longer sentence options → 1 column stacked).
```

- Replay button is always present and always in the same place (top, under the
  question counter) — the learner should never hunt for "play it again."
- Exit (✕) asks to confirm only if at least one question has been answered
  ("Thoát bài kiểm tra? Lượt làm sẽ không được lưu." / "Exit the test? This
  attempt won't be saved.").

**Correct / incorrect feedback**

```
┌───────────────────────────────────┐
│  Câu 3 / 10                [✕ Thoát]│
│  ┌─────────┐ ┌─────────┐          │
│  │  ✓ 买    │ │   卖     │          │  ← correct choice gets a green check
│  └─────────┘ └─────────┘          │     outline overlay, not a full repaint
│  ┌─────────┐ ┌─────────┐          │     (keeps layout stable)
│  │  schema │ │  schema │          │
│  └─────────┘ └─────────┘          │
│                                     │
│       Đúng rồi! 👍                 │  ← inline banner, auto-advances after
└───────────────────────────────────┘     ~900ms; no blocking dialog

Incorrect: chosen answer gets a red "✕" overlay, correct answer gets the green
"✓" overlay simultaneously (one glance teaches the right answer); banner reads
"Chưa đúng — câu này sẽ quay lại sau." (Not quite — this one will come back
later.) Per the architecture doc's requeue rule, no "reveal answer" link is
offered here (revealing would need to disqualify the attempt from passing,
which is extra complexity the MVP doesn't need — the requeue already teaches
the right answer through repetition).
```

**Result**

```
Pass:
┌───────────────────────────────────┐
│                                     │
│            ✓ (large)               │
│         Qua bài kiểm tra!          │
│      10/10 câu đúng · 2 lần sai    │
│        đã được làm lại             │
│                                     │
│   [  Về trang Ôn tập  ]            │
│   [  Nghe lại bài học  ]           │
└───────────────────────────────────┘

Fail: not reachable under the requeue pass rule (architecture doc: "passes only
when every question has been answered correctly... attempt passes when the
queue is empty"). An exited/abandoned attempt is simply not recorded — there is
no separate fail screen. If the pass rule config later changes to allow a hard
fail, add a parallel screen here: same layout, ✕ icon, mistake count, and a
single [Làm lại] (Retry) button — do not add it speculatively now.
```

### 3.8 Settings (tab 3)

```
┌───────────────────────────────────┐
│  Cài đặt                           │
│                                     │
│  HIỂN THỊ                          │
│  Hiển thị mặc định (Guided)        │
│    Hán tự + pinyin            >   │
│  Hiển thị mặc định (Luyện tập)     │
│    Ẩn                          >   │
│  Hiện tiếng Việt                [●]│
│  Giao diện tối                  [●]│
│                                     │
│  TẢI XUỐNG                         │
│  Chỉ tải qua Wi-Fi              [●]│
│  Dung lượng đã dùng    240 MB  >   │
│                                     │
│  TÀI KHOẢN                         │
│  nguyenhuutu.khtn@gmail.com        │
│  Đồng bộ lần cuối: 2 phút trước    │
│                              [Đăng xuất]│
└───────────────────────────────────┘
   Học · Ôn tập · Cài đặt
```
`Catalog URL` field exists only in debug builds, per architecture doc, placed
at the bottom of Settings behind a "Dành cho nhà phát triển" (Developer)
section that is simply absent from release builds (not hidden behind a toggle
— compiled out).

### 3.9 Error / offline states (cross-cutting)

- **Download failed** (checksum/size mismatch): card shows "Lỗi tải xuống —
  thử lại" and keeps the previous playable version untouched, per architecture
  doc.
- **Playback file missing/corrupt at play time**: snackbar "Không phát được bài
  này. Hãy tải lại." (Can't play this. Please re-download.) with a Retry
  action; does not crash to a blank player.
- **Sign-in required for sync** (Review/progress depends on account): Settings
  and Review show a banner "Đăng nhập để lưu tiến độ trên nhiều máy" (Sign in to
  save progress across devices) with a CTA — but Learn/playback/local test
  attempts work fully signed-out, since offline-first is the point.

## 4. Gesture map

The conflict to resolve: **tap a line = seek**, **tap/long-press a word =
lookup**, **drag = text selection**, **tap the loop icon = loop toggle**,
**drag the list = scroll**, **auto-scroll should not fight manual scroll**.

Resolution, by target and gesture:

| Target | Tap | Long-press | Drag | Notes |
|---|---|---|---|---|
| Blank area of a line (bar in Hidden view, margin in other views) | Seek to this line | — | Scroll (if drag starts here, it's a list scroll, not selection) | Primary seek affordance; works in every view mode including Hidden |
| A Hanzi/pinyin token | Seek to this line (same as blank area — tap never opens lookup) | Open lookup sheet for this token | Begin text selection (system `SelectionContainer` drag-handle behavior, standard Android) | Matches §3.5/3.6: tap and long-press are different actions on the same target, which Android users already expect from any selectable text + link |
| Loop icon (↻, shown only on the active line) | Toggle loop for this line | — | — | Small target (36dp icon, 48dp hit box) but only visible on one line at a time, so no adjacency risk |
| Vietnamese meaning row | — | — | — | Not interactive: no seek, no selection, no lookup (it's a gloss, not lesson content) — this removes a whole row from gesture contention |
| Transcript list background (between rows, not on a row) | — | — | Scroll; auto-scroll resumes 5s after the user stops dragging (already implemented as `AUTO_SCROLL_PAUSE_MS` in `TranscriptPanel.kt` — keep this constant) | Matches existing code; document it so it isn't "fixed" accidentally later |
| Mini-player body (title/art area) | Open Now Playing | — | Swipe up → open Now Playing; swipe down → (no-op, already closed) | |
| Mini-player play/pause button | Toggle play/pause | — | — | Never opens Now Playing — must stay instantly actionable without navigating |
| Now Playing seek bar | Seek by tap position | — | Scrub | Standard `Slider` |

Design rules that make this resolvable rather than clever:

- **Tap always means "move/act on the thing," long-press always means "tell me
  more about the thing."** This single rule covers both line-tap-to-seek and
  token-long-press-to-lookup without the learner needing per-element memory.
- **No action is reachable only by a gesture that also triggers scrolling.**
  Seek-by-tap and lookup-by-long-press both require the pointer to go down and
  up (or down-and-hold) without first moving past the system's slop threshold —
  exactly the distinction Android's own `combinedClickable` already encodes
  (used today in `TokenCell`/`TranscriptLineRow`), so no custom gesture
  detection needs to be written.
- **One-handed reach**: primary transport controls and the mini-player sit in
  the bottom third of the screen (natural thumb arc). The view-mode switcher
  and overflow menu, which are occasional, sit at the top, deliberately in the
  harder-to-reach zone — this is intentional friction for low-frequency
  actions, not an oversight.
- **Accidental-touch avoidance while walking**: the loop icon only renders on
  the single active line (not on every row), so a mis-tap while scrolling
  can't accidentally start a loop on the wrong line. The seek bar's drag thumb
  has a 48dp touch target even though its visual track is thin (standard
  Material `Slider` behavior). Destructive actions (delete lesson, clear
  account) always require an explicit confirm dialog, never a single swipe commit without undo.

## 5. Design tokens

### 5.1 Color roles (Material 3 `ColorScheme`)

Dark is the default (Principle 5). Light is supported, not deprioritized — same
roles, inverted tonal direction.

| Role (Compose `ColorScheme` field) | Dark | Light | Used for |
|---|---|---|---|
| `primary` | warm amber `#F2B155` | deep amber `#8A5A1E` | primary CTA (play buttons, "Take test"), active nav item, loop-active icon |
| `onPrimary` | `#2A1800` | `#FFFFFF` | text/icon on primary-filled surfaces |
| `primaryContainer` | `#4A350F` | `#FCE3B8` | active transcript line background (already `primaryContainer` in `TranscriptLineRow` — keep) |
| `onPrimaryContainer` | `#FFE2B0` | `#2A1800` | text on the active line |
| `secondary` | teal `#7FD6C4` | `#1C6E5D` | secondary actions (download, sync icon), Review "passed" badge |
| `background` / `surface` | `#15161A` near-black, not pure black (pure black crushes Hanzi stroke contrast on OLED) | `#FFFBF6` warm off-white | app background |
| `surfaceVariant` | `#23252B` | `#EDE6DA` | lesson cards, mini-player background |
| `onSurface` | `#ECECEE` | `#1B1C1F` | Hanzi text (primary reading text — see §5.2 role separation) |
| `onSurfaceVariant` | `#A7A9B3` | `#5C5E66` | pinyin text, secondary metadata (file size, duration) |
| `tertiary` / meaning-text role | muted blue-grey `#9FB4D6` | `#3E5A85` | Vietnamese meaning lines — a distinct hue from pinyin's neutral grey, not just a lighter grey, so "this is translation" is readable at a glance even for a learner skimming fast |
| `error` | `#F2A6A6` | `#B3261E` | download failures, wrong-answer overlay |
| `errorContainer` | `#4A2020` | `#FFDAD6` | error banners |
| custom `successGreen` (non-M3-standard token, additive) | `#8FD17A` | `#2E7D32` | correct-answer overlay, "Đã qua" (passed) badge — M3 has no built-in "success" role, so this is a project-defined `ColorScheme` extension, documented once and reused, not invented per screen |

Dynamic color (Android 12+ wallpaper-derived `dynamicDarkColorScheme`/
`dynamicLightColorScheme`) is **off by default**. This app's three text roles
(Hanzi/pinyin/Vietnamese) depend on *fixed* contrast relationships to stay
legible across lessons; a wallpaper-derived palette could collapse pinyin and
meaning into similar tones on some devices. Offer it in Settings as "Dùng màu
theo hình nền máy" (Use wallpaper color) — opt-in, not default
([dynamicDarkColorScheme reference](https://developer.android.com/jetpack/compose/themes/material3)).

Contrast targets: body text ≥ 4.5:1, large Hanzi (≥ 18sp) and icons ≥ 3:1
(WCAG AA, consistent with Material accessibility guidance — verify with
Android's Accessibility Scanner before shipping, since no single authoritative
M3 contrast-token page was found during research; the codelab linked in §1
point 2 is the closest official source).

### 5.2 Typography scale

Three text roles never share a type style, by design (Principle 4):

| Role | Compose style base | Size | Line height | Font | Notes |
|---|---|---|---|---|---|
| Hanzi (transcript, lesson text) | `bodyLarge` → custom | 22sp | 34sp (1.55×) | Noto Sans SC (bundled) | Generous line height because CJK glyphs are visually denser than Latin at the same size; tight leading makes Hanzi feel cramped and hurts tone-mark-adjacent pinyin legibility above it |
| Pinyin (ruby, standalone pinyin view) | `labelMedium` → custom | 13sp | 18sp | Noto Sans (bundled, same family as SC for glyph harmony) | Must render tone diacritics (ā á ǎ à, ü) correctly — Noto Sans's Latin subset covers full Vietnamese + pinyin diacritics, which is why one font family is used for *both* pinyin and Vietnamese UI text (one less font to bundle, and visual consistency between the two "support" text roles) |
| Vietnamese meaning | `bodySmall` → custom | 14sp | 20sp | Noto Sans (bundled) | Slightly larger than pinyin despite being "secondary," because Vietnamese is the learner's native-language safety net and must stay comfortably readable even for older learners glancing briefly |
| UI chrome (Vietnamese labels, buttons, nav) | Standard M3 type scale (`titleMedium`, `bodyMedium`, `labelLarge`, etc.) | per M3 defaults | per M3 defaults | Noto Sans | Vietnamese diacritics (ệ, ồ, ữ, …) must not be verified only against Roboto — Roboto's Vietnamese coverage is inconsistent across OEM skins, which is the concrete reason to bundle Noto Sans rather than rely on the system default |

Ruby (pinyin-above-Hanzi) layout: Compose has no native `<ruby>` primitive, so
this is built as a `Column` per token (pinyin `Text` above, Hanzi `Text` below,
center-aligned) — exactly the structure already in `TokenCell` in
`LookupSheet`/`TranscriptPanel.kt`. Keep that approach; it sidesteps the
immature cross-platform ruby-rendering landscape noted in research (WebKit's
own ruby implementation only handled basic placement as late as 2014,
[W3C www-style thread](https://lists.w3.org/Archives/Public/www-style/2014Sep/0240.html)) by not depending on OS ruby support at all.

Font scaling: all sizes above are `sp`, so they respect the system font-size
setting up to 200% (Accessibility §7). At 200%, the ruby `Column` layout must
not clip — verify `TokenCell` wraps via `FlowRow` (already true in
`TranscriptPanel.kt`) rather than a fixed-width row.

### 5.3 Spacing, shape, elevation, motion

- **Spacing scale**: 4 / 8 / 12 / 16 / 24 / 32dp, matching the 8dp grid already
  used in the existing Compose code (`padding(16.dp)`, `spacedBy(8.dp)`, etc.) —
  keep using this scale, don't introduce a second one.
- **Shape**: M3 default `Shapes()` — cards and sheets at `medium` (12dp corner),
  chips/buttons at `small` (8dp corner), bottom sheet top corners at `large`
  (28dp) per M3 defaults. No custom shape system.
- **Elevation**: mini-player and bottom nav at M3 level 2 (tonal elevation,
  not a drop shadow, since dark theme relies on surface-tint elevation per M3
  guidance); lesson cards at level 1; modal sheets (lookup) at level 3.
- **Motion**: 
  - View-mode switch: no transition (instant swap — Principle 3 says "never a
    reload," and a fade/cross-fade would read as a reload).
  - Mini-player → Now Playing expand: 300ms, `FastOutSlowInEasing`, container
    transform (shared element from mini-player art/title to Now Playing header).
  - Auto-scroll to active line: 250ms smooth scroll (already `animateScrollToItem`
    in `TranscriptPanel.kt` — keep).
  - Correct/incorrect overlay: 150ms fade-in, auto-dismiss/advance after ~900ms
    on correct, ~1400ms on incorrect (longer, since the learner needs to read
    which answer was right).
  - Respect **reduced motion** (`Settings.Global.ANIMATOR_DURATION_SCALE` /
    system "Remove animations"): when enabled, replace the container-transform
    expand with an instant cut and skip the auto-scroll's smooth interpolation
    (jump directly), per Accessibility §7.

### 5.4 Compose theme mapping

```kotlin
// ColorScheme: darkColorScheme(...) / lightColorScheme(...) built from §5.1 roles,
// chosen by a Settings-backed `useDarkTheme: Boolean = true` default (NOT
// isSystemInDarkTheme() — this app overrides the system default; see Principle 5).
// Extend with a small non-M3 `LocalAppColors` CompositionLocal for `successGreen`
// and the Vietnamese-meaning text color, since M3's ColorScheme has no "success"
// or "gloss text" role — do this once, centrally, not per screen.

// Typography: Typography() with bodyLarge/labelMedium/bodySmall overridden per
// §5.2, plus two custom TextStyle constants (HanziStyle, PinyinStyle) exposed via
// a CompositionLocal or extension on MaterialTheme, since M3's default type scale
// has no "Hanzi" or "pinyin" slot.

// Shapes: Shapes() defaults, unchanged.

// Font loading: bundle Noto Sans + Noto Sans SC subset (or full, weigh APK size
// vs. coverage) as FontFamily resources; do not rely on the system CJK font,
// since OEM skins vary in which CJK font ships and in tone-mark glyph quality.
```

Font files to bundle and their licences:

| Font | Purpose | Licence | Source |
|---|---|---|---|
| Noto Sans SC | Hanzi | OFL 1.1 | fonts.google.com/noto/specimen/Noto+Sans+SC |
| Noto Sans | pinyin, Vietnamese, Latin UI text | OFL 1.1 | fonts.google.com/noto/specimen/Noto+Sans |

Both are Google Fonts under the SIL Open Font License 1.1, which permits
bundling and redistribution inside an app with no attribution requirement
beyond keeping the licence file; include the `OFL.txt` in `assets/fonts/` next
to the font files. (General Noto family background: [pinyin.info on Noto](https://pinyin.info/news/category/writing-systems/page/10/) — confirm exact OFL text from the Google Fonts download itself before bundling, not from this secondary source.)

## 6. Component inventory

Reusable Composables, their props, and the M3 component they wrap. Build these
once in a `ui/components/` package; screens compose them, they don't duplicate
layout.

| Composable | Wraps (M3) | Key props |
|---|---|---|
| `LessonCard` | `Card` | `lesson: LessonEntity`, `downloadState`, `passed: Boolean`, `onPlay`, `onDownload`, `onCancel`, `onDelete` |
| `ContinueListeningCard` | `Card` (filled, `primaryContainer` tone) | `title`, `track`, `positionMs`, `durationMs`, `onResume` |
| `MiniPlayer` | custom row (not a direct M3 component; built from `Surface` + `IconButton` + a slim `LinearProgressIndicator`) | `title`, `track`, `playing: Boolean`, `looped: Boolean`, `progress: Float`, `onToggleLoop`, `onTogglePlay`, `onExpand` |
| `TranscriptPanel` | `LazyColumn` + `FlowRow` (existing — extend, don't replace) | unchanged signature from current `TranscriptPanel.kt`, plus a `modelAnswerRevealed` already covered by `decision.placeholder` |
| `ViewModeSwitcher` | `FilterChip` row (existing `ViewSwitcher`, rename for clarity) | `viewMode`, `showVietnamese`, callbacks — unchanged behavior, scroll-row already implemented |
| `TranscriptLineRow` | `Row` + `combinedClickable` (existing) | unchanged; add `onSelectText` passthrough so `SelectionContainer` wraps the Hanzi/pinyin region only (§3.6) |
| `TokenCell` | `Column` + `combinedClickable` (existing) | unchanged |
| `LookupSheet` | `ModalBottomSheet` (existing — extend) | add `onCopy`, `onOpenPleco: (() -> Unit)?` (null when Pleco absent), `onSendTo` |
| `TransportBar` | `Slider` + `IconButton`×3 + `FilterChip` row (speed) | `positionMs`, `durationMs`, `speed`, `playing`, `onSeek`, `onSeekBack`, `onSeekForward`, `onTogglePlay`, `onSpeedChange` |
| `TrackSwitch` | `TabRow` or two `FilterChip`s (existing pattern in `PlayerCard`) | `track`, `practiceAvailable`, `onSwitch` |
| `LoopChip` | `AssistChip` (existing in `PlayerCard`, extract to component) | `lineLabel`, `onCancel` |
| `TestQuestionCard` | `Card` + `OutlinedButton`/`FilledTonalButton` grid | `question`, `options`, `selected`, `correctId: String?` (non-null only after answer), `onSelect` |
| `AnswerOptionChip` | `FilledTonalButton` or `OutlinedButton` | `text`, `state: Idle|Correct|Incorrect`, `onClick` |
| `ResultSummary` | `Column` (icon + `Text` stack) | `passed`, `questionCount`, `mistakeCount`, `onDone`, `onRelisten` |
| `DownloadRow` | `LinearProgressIndicator` + `TextButton` | `progress: Float?`, `state`, `onCancel`, `onRetry` |
| `SettingsSection` / `SettingsRow` | `ListItem` | `title`, `subtitle?`, trailing `Switch`/`Text`/chevron |
| `OfflineBanner` / `ErrorBanner` | `Surface` (tonal, `errorContainer` or neutral) | `message`, `actionLabel?`, `onAction?` |

## 7. Accessibility

- **Touch targets**: every tappable element ≥ 48×48dp, including the loop icon
  and individual Hanzi tokens in dense lines (tokens get invisible padding to
  reach 48dp even when the glyph itself renders smaller) — per the Compose
  accessibility codelab's explicit guidance and worked example
  ([Jetpack Compose accessibility codelab](https://developer.android.com/codelabs/jetpack-compose-accessibility)).
- **Contrast**: ≥ 4.5:1 for pinyin/Vietnamese body text, ≥ 3:1 for large Hanzi
  and icons (§5.1); verify both theme variants with Accessibility Scanner before
  release, not just by eye.
- **Font scaling to 200%**: all type in `sp`; ruby layout wraps via `FlowRow`
  (§5.2); test the Now Playing screen specifically at 200%, since it has the
  densest text-to-space ratio.
- **TalkBack labels, in Vietnamese** (UI language is Vietnamese throughout, so
  labels are too):
  - Play/pause: "Phát" / "Tạm dừng" (state-dependent, not a static "Play button").
  - Loop icon: "Lặp lại câu này" (inactive) / "Đang lặp câu này, nhấn để dừng" (active).
  - Mini-player: "Trình phát nhỏ. [Tên bài], đang phát. Nhấn để mở." (describes
    state + affordance, not just "Mini player").
  - Hanzi token: content description includes pinyin + meaning so TalkBack
    reads "叫 (jiào): gọi là" rather than just the character — this makes the
    transcript usable by voice even though the app is not designed primarily
    for screen-reader use.
  - Lesson card download state: "Bài 1. Sơ cấp. Đã tải xuống, đã qua bài kiểm tra."
    — state is spoken, not just implied by icon color.
  - Follow the "describe the action, not the icon" rule surfaced in research
    (e.g. "Chia sẻ bài viết" over "Mũi tên chỉ sang phải" — [Android accessibility guide](https://www.miquido.com/blog/android-accessibility-developer-insights-implementation-guide/)).
- **Reduced motion**: see §5.3 motion rules — container-transform and
  smooth-scroll both get an instant fallback.
- **Reading order**: in Hanzi+pinyin ruby cells, TalkBack must read pinyin then
  Hanzi as one merged node per token (not two separate stops), so set
  `Modifier.semantics(mergeDescendants = true)` on the token `Column`, with the
  full "Hanzi (pinyin): meaning" string as the single content description —
  otherwise a learner swiping through with TalkBack hears fragmented, confusing
  output.

## 8. UI copy (Vietnamese)

Organized by screen, matching §3. These are the authoritative strings;
implementation should treat them as the string-resource source, not placeholder
text to be rewritten ad hoc.

**Bottom navigation**
- Học
- Ôn tập
- Cài đặt

**Learn**
- Title: Học
- Empty state: "Chưa có bài học nào." / "Đồng bộ để tải danh sách bài học mới nhất." / button: "Đồng bộ ngay"
- Sync in progress: "Đang đồng bộ..."
- Sync success: "Đã cập nhật danh sách bài học"
- Sync failure: "Không đồng bộ được. Kiểm tra kết nối mạng."
- Offline banner: "Không có kết nối mạng. Các bài đã tải vẫn nghe được."
- Card actions: "Nghe" · "Tải xuống" · "Hủy" · "Thử lại" · "Cập nhật" · "Nghe bản cũ" · "Xóa"
- Passed badge: "Đã qua"
- Update badge: "Có bản mới"
- Delete confirm: "Xóa bài học này?" / "Bạn có thể tải lại sau." / "Xóa" / "Hủy"
- Continue card prefix: "Đang nghe:"

**Lesson detail**
- "Trình độ:" · "Kích thước:" · "Thời lượng:"
- "Chỉ tải qua Wi-Fi"
- Button: "Tải xuống bài học"

**Now Playing**
- View-mode chips: "Ẩn" · "Pinyin" · "Hán + pinyin" · "Hán tự"
- Vietnamese switch label: "Tiếng Việt"
- Track switch: "Guided" · "Luyện tập" (kept as-is — these are already established English loan terms for Vietnamese learners of this course; do not translate to avoid inconsistency with the lesson JSON's own `track` naming)
- Overflow menu: "Kiểm tra bài" · "Tải lại bài" · "Báo cáo vấn đề"
- Loop chip: "Đang lặp: {dòng}" (dismiss icon has TalkBack label "Dừng lặp")
- model_answer placeholder: "Đến lượt bạn — nói trước khi nghe"
- Reload-needed snackbar: "Không phát được bài này. Hãy tải lại." / action: "Tải lại"

**Lookup sheet**
- Button: "Phát âm"
- "Sao chép" · "Mở trong Pleco" · "Chia sẻ đến..."

**Review**
- Title: Ôn tập
- "Đã qua" · "Chưa qua" · "Chưa có lượt làm nào"
- "{n} lượt làm"
- Buttons: "Làm bài" · "Làm lại"
- Exit confirm: "Thoát bài kiểm tra?" / "Lượt làm sẽ không được lưu." / "Thoát" / "Tiếp tục làm"
- Correct feedback: "Đúng rồi!"
- Incorrect feedback: "Chưa đúng — câu này sẽ quay lại sau."
- Result pass: "Qua bài kiểm tra!" / "{correct}/{total} câu đúng · {mistakes} lần sai đã được làm lại" / "Về trang Ôn tập" / "Nghe lại bài học"
- Question counter: "Câu {n} / {total}"
- Replay button: "Nghe lại"

**Settings**
- Section headers: "HIỂN THỊ" · "TẢI XUỐNG" · "TÀI KHOẢN" · "DÀNH CHO NHÀ PHÁT TRIỂN"
- "Hiển thị mặc định (Guided)" · "Hiển thị mặc định (Luyện tập)"
- "Hiện tiếng Việt" · "Giao diện tối" · "Dùng màu theo hình nền máy"
- "Chỉ tải qua Wi-Fi" · "Dung lượng đã dùng"
- "Đăng xuất" · "Đăng nhập để lưu tiến độ trên nhiều máy"
- "Đồng bộ lần cuối: {time}"

**Generic errors**
- No network, generic action: "Không có kết nối mạng."
- Unexpected error: "Đã có lỗi xảy ra. Vui lòng thử lại."

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
