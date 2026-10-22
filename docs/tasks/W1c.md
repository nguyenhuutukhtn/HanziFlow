# W1c — floating window (Document PiP), media keys, character card, copy
Status: todo · Suggested agent: codex/gpt-5.6-terra (medium), ask first · Session: continue W1b window if its context is < ~150k, else new · Size: M · Device check: user
Wave: 1 · Owns: `web/src/` · Depends on: W1b · Checkpoint after: yes (end of wave)

## Goal
"Cửa sổ nổi": a small always-on-top window that keeps playing and stays interactive while the learner works in other apps. It runs in Chrome 116+ on Mac and Linux.

## Read only
- `web/src/` from W1b (the transcript component is already portable)
- The W1a facade `characterJson`
- MDN: Document Picture-in-Picture API, Media Session API (only if unsure)

## Do
1. "Cửa sổ nổi" button (hidden when `!('documentPictureInPicture' in window)`, with a one-line hint "Cần Chrome/Edge 116+"):
   - call `documentPictureInPicture.requestWindow({width: 420, height: 260})` from the click;
   - copy the stylesheets into the new window;
   - render a compact player into it through a React portal: active line large (Hán + pinyin per the current view), the previous and next lines small, transport (play/pause, ±10s, Nghe lại câu, speed).

   The `<audio>` element stays in the main document, so audio keeps playing. On `pagehide` of the PiP window, unmount the portal and show the full player again.
2. Inside the PiP window and the main view:
   - tap a Hanzi character → compact card from `characterJson`: char, pinyin, Hán-Việt, vi meaning, components (tap a component → its card, back);
   - long-press or right-click a line → copy the line's Hanzi;
   - a card button copies the char.

   Use the PiP window's own `navigator.clipboard`.
3. Media Session:
   - `play`, `pause`, `seekbackward`/`seekforward` (10s);
   - `previoustrack`/`nexttrack` = previous/next cue (as headset next/prev on Android);
   - metadata = lesson title + track display name.
4. Keyboard shortcuts in both windows: Space play/pause, ←/→ ±10s, R replay line.
5. Tests (vitest + testing-library):
   - the card renders the fixture values for one char;
   - a media-session handler calls the facade's next-cue target;
   - the PiP button is hidden when the API is missing.

## Acceptance
- `npm --prefix web test -- --run` passes, including the 3 tests above; `scripts/check.sh` ok.
- The user's device check (Chrome on Mac, then Linux):
  1. `just web`, open the URL, pick lesson 1, press play.
  2. Click "Cửa sổ nổi". The window floats above other apps, the active line follows the audio, and the speed change is audible.
  3. Switch to another app. Audio continues and the floating window stays on top.
  4. Tap a character → the card shows pinyin + Hán-Việt + nghĩa. Tap a component → its card. Back works.
  5. Use the keyboard media keys (or headset) for play/pause and next/previous line.
  6. Close the floating window. The full player is back and playback continues.
  7. After the user runs `just web-deploy`, the same works from the Cloudflare URL on the Linux machine.

## Verify
```bash
npm --prefix web test -- --run 2>&1 | tail -5
bash scripts/check.sh
```

## Out of scope
Firefox / video-PiP fallback; native Linux app; sign-in/progress; stroke animation on web.
