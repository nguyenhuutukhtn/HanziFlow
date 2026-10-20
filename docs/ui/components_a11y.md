# UI — Component inventory and accessibility

> Part of the UI spec. Start at [README.md](README.md) for principles, navigation and the section map.

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
