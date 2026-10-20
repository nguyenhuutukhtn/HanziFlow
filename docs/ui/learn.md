# UI — Learn tab and lesson detail

> Part of the UI spec. Start at [README.md](README.md) for principles, navigation and the section map.

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
│ │ ▶ Đang nghe: Bài 1 · Học có hướng dẫn     ││   ← continue-listening card,
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
