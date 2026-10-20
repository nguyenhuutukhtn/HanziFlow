# UI — Review tab and tests

> Part of the UI spec. Start at [README.md](README.md) for principles, navigation and the section map.

## 3. Screens and states

All wireframes are phone width (~360–412dp), portrait. `[ ]` = tap target,
`▓▓` = filled/active, `░░` = track/inactive, `··` = muted text.

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
