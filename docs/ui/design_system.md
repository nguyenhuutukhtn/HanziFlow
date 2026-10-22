# HanziFlow web design system

The source of truth is `web/src/theme/tokens.css`. Use semantic variables, not literal colours.

## Tokens

| Token | Sáng | Giấy | Tối | Use |
|---|---|---|---|---|
| `--color-canvas` | `#fbfaf7` | `#f3eadb` | `#1d2325` | page background |
| `--color-surface` | `#fff` | `#fbf4e9` | `#273033` | cards and navigation |
| `--color-surface-subtle` | `#f6f2ed` | `#ede1ce` | `#313b3d` | quiet controls |
| `--color-surface-active` | `#fff4ed` | `#f7ead9` | `#392f2b` | active transcript line |
| `--color-content` | `#282725` | `#40382e` | `#f4f0e8` | primary text |
| `--color-content-muted` | `#74716b` | `#746958` | `#bcc5c3` | secondary text; ≥4.5:1 on surface |
| `--color-content-on-accent` | `#fff` | `#fff` | `#1d2325` | accent button text |
| `--color-brand` | `#ee7047` | `#bd6e45` | `#f28b5e` | focus, active state, calls to action |
| `--color-brand-subtle` | `#fff0e9` | `#f5e4d3` | `#3e302b` | selected background |
| `--color-success` | `#6caf83` | `#6caf83` | `#79c294` | positive status |
| `--color-border` | `#ebe6df` | `#e2d4c0` | `#3c484a` | hairline border |
| `--radius-card` | `18px` | `18px` | `18px` | cards and sheets |
| `--radius-control` | `12px` | `12px` | `12px` | controls |
| `--space-page-x` | `54px` | `54px` | `54px` | desktop page inset |
| `--space-1` | `8px` | same | same | compact layout gap |
| `--space-2` | `12px` | same | same | control gap |
| `--space-3` | `18px` | same | same | card gap |
| `--space-4` | `24px` | same | same | section gap |
| `--font-display` | Noto Serif SC | same | same | headings |
| `--font-body` | Be Vietnam Pro | same | same | UI copy |
| `--font-hanzi` | Noto Serif SC | same | same | Chinese text |
| `--hanzi-size` | `22/28/34px` | same | same | user choice |

`--accent`, `--surface`, `--surface-soft`, `--text`, `--muted`, and `--border` are semantic aliases.

## Typography and layout

- UI text is at least 12px. Body text is 14px; page title is 38px desktop / 30px phone.
- Hanzi uses `--font-hanzi`; never substitute a Latin serif font.
- Space with 8, 12, 18, and 24px. Desktop pages use 54px side padding.
- Cards use 18px radius, controls use 12px. A soft, low-opacity shadow is reserved for floating sheets.
- At 720px and below, interactive targets are at least 44 × 44px.

## Components

- Card: surface background, one border, 18px radius. Hover may use brand border; do not imply unavailable progress.
- Segmented control: subtle container; selected item has a surface fill; keep labels at 12px or larger.
- Switch: accent track only when on; thumb stays surface-coloured.
- Pill button: control radius, 44px minimum touch height on phone; accent fill only for the primary action.
- Chip: subtle surface/border, compact metadata only—not made-up learner data.
- Sheet/popover: surface card, border, 18px radius, modest shadow; preserve focus-visible accent ring.
- Transcript: active line uses `--color-surface-active` plus brand border; past line may soften but remains readable; coach line is muted with a brand leading rule.
