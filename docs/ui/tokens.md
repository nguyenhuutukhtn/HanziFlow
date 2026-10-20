# UI — Design tokens (color, type, spacing, motion, Compose theme)

> Part of the UI spec. Start at [README.md](README.md) for principles, navigation and the section map.

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
