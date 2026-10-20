# UI — Mini-player, Now Playing, lookup, selection, gestures

> Part of the UI spec. Start at [README.md](README.md) for principles, navigation and the section map.

## 3. Screens and states

All wireframes are phone width (~360–412dp), portrait. `[ ]` = tap target,
`▓▓` = filled/active, `░░` = track/inactive, `··` = muted text.

### 3.2 Mini-player (persistent, all tabs)

```
┌───────────────────────────────────┐
│ ▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░ │ ← thin progress line, full width
│  Bài 1 · Học có hướng dẫn        [‖ ] [⏭]   │ ← title/track · pause-or-play · next-line? no:
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
│  Học có hướng dẫn │ Luyện nghe      │ ← track switch (only if practice exists)
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
