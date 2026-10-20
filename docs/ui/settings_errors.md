# UI — Settings and error/offline states

> Part of the UI spec. Start at [README.md](README.md) for principles, navigation and the section map.

## 3. Screens and states

All wireframes are phone width (~360–412dp), portrait. `[ ]` = tap target,
`▓▓` = filled/active, `░░` = track/inactive, `··` = muted text.

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
