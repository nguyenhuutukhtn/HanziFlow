# Web player review — Chrome/Linux (WR1)

Date: 2026-10-28 · HEAD `6f9511e` · Chrome 156.0.8078.73 · Vite production preview (`npm --prefix web run build` → `npm --prefix web run preview`) · Playwright driven, headless at 1280×800 and 390×844; the PiP step used headed Chrome. Content came from the public Supabase catalog: lesson 1 v8 (`guided`, `guided_zh`) and lesson 2 v4 (5 modes). No source changes were made.

## P1

- **web/src/player-context.tsx:135 — Switching track discards the mapped position and restarts at 0.**
  1. Open lesson 1 and wait for `audio.duration ≈ 2634.18`.
  2. Set `audio.currentTime = 500` (500.0 s) and wait for the transcript to settle.
  3. Click the track radio `Chỉ tiếng Trung`; wait until the new source is ready (`duration ≈ 2354.88`).
  4. Read `audio.currentTime`.
  - Expected: `TrackSwitchJs.positionFor(guided, guided_zh, 500000)` = **422.006 s**.
  - Actual: **0.000 s**. Switching back expected 582.701 s, actual 0.000 s again. A second run from 233.485 s also landed at 0.000 s.
  - Cause: `changeTrack` calls `seek(nextPosition)` while the old `<audio>` is still loaded, then `setTrack` rebuilds the session and replaces `src`; the new media element starts at 0 and nothing re-applies the mapped position.
  - Confidence: **reproduced** (3 runs).

## P2

- **web/src/Transcript.tsx:223 — “Hán tự + pinyin” renders no pinyin in the main transcript.**
  1. Open lesson 1 with the default settings (`viewMode = HANZI_WITH_PINYIN`; `localStorage` confirms it).
  2. Count `.transcript rt` and `.transcript .pinyin`: both are **0**; the active line HTML is `<span class="character">我们</span>...` with no `<rt>` and no pinyin row.
  3. Click `Hán tự + pinyin` explicitly: still **0**. Click `Pinyin`: **473** `.pinyin` rows appear.
  4. Repeat at 390×844: same result (`rt = 0`, `.pinyin = 0`).
  - Expected: pinyin is visible for each Chinese line in the default mode.
  - Actual: “Hán tự + pinyin” is visually identical to “Hán tự”; only “Pinyin” (Hanzi hidden) shows pinyin.
  - Cause: `showPinyin` requires `compact && pinyinEnabled`; `PlayerScreen` never passes `compact` to `<Transcript>`, and the real facade sets `showPinyinLine = false` for this mode while its tokens carry `showPinyin = true`.
  - Confidence: **reproduced** (desktop + mobile).

- **web/src/Transcript.tsx:92 — Auto-follow silently turns off after a long programmatic scroll.**
  1. Open lesson 1; optionally throttle the page with CDP `Emulation.setCPUThrottlingRate(4)` to remove the timing race.
  2. Click the section marker `Nghe lại` (95.4%) or `Kể lại` (86.5%).
  3. Sample `.transcript-wrap[data-follow]` at 1 s/2 s/3 s: `on` at 1 s, then `off` from ~2 s with no user scroll.
  4. Press play and wait 20 s: `scrollY` stays at 58488 while the active line moves from top 319 px to top 934 px (below the 800 px viewport), so follow is no longer keeping it visible.
  - Expected: follow stays on and scrolls to each new cue after a seek.
  - Actual: follow turns off and the transcript stops tracking playback.
  - Cause: the 1.5 s fallback (line 92) clears `programmed.current` before Chrome finishes a long smooth scroll; the remaining scroll events hit `stopFollowing()` at line 134.
  - Confidence: **reproduced** (3/3 with 4× CPU throttling; 3/4 unthrottled far-marker runs; the race depends on scroll distance/speed).

- **web/src/player-context.tsx:119 — “Nghe lại câu” / `R` does nothing while the playhead is in a cue gap.**
  1. Open lesson 1 and set `audio.currentTime = 80.3` (gap between `w_waimai` ending at 80.020 s and `w_waimai_vi` starting at 80.520 s).
  2. Press `R` (or click `Nghe lại câu`).
  3. Current time stays at **80.300**; repeat at 79.2 s (gap before `w_waimai` at 79.224 s) and it stays at **79.200**.
  4. Inside a cue (`currentTime = 80.0`), `R` correctly seeks to **79.224**.
  - Expected: in a gap, replay the last-started cue (the line already marked “vừa đọc”), i.e. 79.224.
  - Actual: no-op because `replay` uses `activeCueIndex`, which returns `-1` in a gap, while the transcript uses the gap-aware `cuePlaceJson`.
  - Confidence: **reproduced** (2 gaps, repeated).

- **web/src/screens/Player.tsx:49 — Document PiP renders unstyled because the cloned stylesheet links never load.**
  1. Build and preview the production bundle; open lesson 1 in headed Chrome; real-click `Cửa sổ nổi`.
  2. The PiP window opens (614 `.line`, 5 controls), but in its document both stylesheet links (`/assets/index-*.css` and the Google Fonts link) have `link.sheet === null` after 2.5 s, and `performance.getEntriesByType("resource")` is empty.
  3. Computed layout: `.float-text` is 15782 px tall with `overflow-y: visible`, body font is Times New Roman, and `.float-controls` sits at top 15840 px.
  4. Clicking line 10 in PiP seeks the main audio 0 → 52.293 s, but the active line is 6029 px away from the `.float-text` centre and the container does not scroll.
  5. Injecting the same `/assets/index-*.css` text as a `<style>` node in the PiP head fixes it: `.float-text` becomes 709 px / `overflow-y: auto`, and the same line click scrolls to `scrollTop 484` with the active line centred (offset 0).
  - Expected: PiP inherits the app styling and its transcript follows the active cue.
  - Actual: the PiP document is a giant unstyled page; scroll-follow is impossible. PiP play/next/pinyin controls still drive the main player, and close/reopen works.
  - Cause: `openPip` clones `style` and `link[rel="stylesheet"]` nodes into the `about:blank` PiP head; the `<link>` loads never complete there, while cloned `<style>` does apply.
  - Confidence: **reproduced** (4 runs).

## P3

- **web/src/screens/Player.tsx:172 — Progress slider keyboard step is 1 ms and its ARIA value is raw milliseconds.**
  1. Open lesson 1 and set `audio.currentTime = 100`.
  2. Focus `[role="slider"]` and press `ArrowRight` 20 times: current time becomes **100.02** (+0.02 s). One `PageUp` moves it back only **0.01 s**.
  3. Attributes are `aria-valuemin="0"`, `aria-valuemax="2634181.655"`, `aria-valuenow="100000"`, with no `aria-valuetext`.
  - Expected: a usable step (for example 5 s/10 s, matching the ±10 s shortcuts) and a human-readable value such as “1:40”.
  - Actual: keyboard seek is effectively unusable and screen readers announce raw milliseconds.
  - Confidence: **reproduced** (2 runs).

## Checked, no bug

- Library: both lessons render title + `Bài N` order; no page/console errors; theme switch to Tối and back persists across reload (`theme-dark`, stored `hanziflow.settings`).
- Lesson 1: audio source and duration load (2634.181655 s), 614 Chinese rows + 183 Vietnamese coach rows, 8 section markers, track selector shows both labels.
- Lesson 2: five mode radios (`Hiểu`, `Từ vựng`, `Nghe`, `Phản xạ`, `Nói`); each switch changed `src`, reset `currentTime` to 0, and changed transcript size to the matching reference track (122/157/69/121/43 total lines). Per-lesson track choice survived navigation and reload (lesson 1 `guided_zh`, lesson 2 `Nghe`).
- Play/pause: `currentTime` advanced; Space paused; speeds 1/0.8/0.7 set `playbackRate` and 0.8 survived a seek (100 s) and a track switch.
- Seeking: section marker 2 set exactly 70.059 s; `next` 70.159 → 72.215; `previous` 72.215 → 70.059; Shift+→ 100 → 110 s; Shift+← → 100 s; keyboard → from 70.1 → 72.215; ← → 70.059; `R` inside a cue 80.0 → 79.224. Slider click/drag landed within ~2 px of the requested fraction (50%: 1317.091 vs 1317.091 expected; 25%: 650.835; 75%: 1972.012; drag 60%: 1584.529), i.e. normal pointer rounding on a 44-minute track.
- Transcript states: sampled 14 cues and 8 gaps across the timeline; `.line.active` matched the zh cue text, gaps showed `.line.just-read` and/or `.line.next` with the expected ids, and the active line was null only during vi-VN cues (no `.line` is rendered for the coach row).
- Follow: clicking line 0/20 centred it (offset ≤ 0.06%); wheel input set `data-follow="off"` and showed the return pill; the pill restored follow and re-centred the active line. At 390×844 there was no horizontal overflow and the player bar (bottom 744) did not overlap the mobile nav (top 782).
- PiP: opened with a real click, contained 614 lines and 5 controls; PiP next changed the main `currentTime` 233.485 → 238.859; the pinyin toggle changed the shared view (`rt` 2749 → 0); close removed the portal (`pip false`, `.floating-player` absent) and reopen restored 5 controls.
- No `pageerror` or console-error entries in any run (library, player, settings, mobile, PiP).

## Could not check

- PiP visual screenshot: Playwright cannot capture the separate Document PiP window; layout was assessed through DOM geometry and computed styles instead. The PiP window was opened with a real Playwright click, but its internal controls were dispatched via DOM clicks because Playwright cannot address that window as a Page.
- PiP window geometry: under this Wayland/Playwright session the PiP window reported 1280×800 even though `requestWindow({width: 360, height: 200})` was called (outer size 868×596), so real desktop sizing could not be verified.
- Real touch input: follow pause was exercised with a mouse wheel, not a phone/tablet `touchmove`.
- Audio quality, pronunciation, and human listening judgement: only transport/timing was verified against the remote MP3s.
- Dark/paper visual contrast beyond computed class/background values.
