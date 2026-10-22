# W1b — web app: library, player, karaoke transcript, build + deploy scripts
Status: todo · Suggested agent: codex/gpt-5.6-terra (medium), ask first · Session: new · Size: M · Device check: user
Wave: 1 · Owns: `web/` (new), `justfile`, `scripts/web_build.sh`, `scripts/web_deploy.sh`, `.gitignore`, `scripts/check.sh` (add one step) · Depends on: W1a · Checkpoint after: no

## Goal
A static React app in Chrome (Mac + Linux) that streams lesson audio from the public Supabase bucket and shows the karaoke transcript, Hanzi with pinyin by default, so the learner can listen and read at work. No sign-in, no downloads, no progress.

## Read only
- [docs/architecture/overview.md](../architecture/overview.md) "Web app" (sign-in does **not** apply to W1)
- [docs/architecture/content_package.md](../architecture/content_package.md): catalog + timeline sections only
- The W1a facade: `kmp/shared/src/commonMain/kotlin/com/hanziflow/shared/web/` + its generated `.d.ts`
- [docs/ui/README.md](../ui/README.md): only the palette/typography tokens (amber accent, dark/light), to match the Android look roughly

## Do
1. `web/`: Vite + React + TypeScript, pinned versions, `npm` lockfile committed. Consume the shared library from the Gradle output (a `file:` dependency or a copy step in `scripts/web_build.sh`; never commit the built JS).
2. Content base URL from env `VITE_CONTENT_BASE_URL`. `scripts/web_build.sh` derives it from `supabase.url` in `kmp/local.properties` as `<url>/storage/v1/object/public/course/`. That URL is public, but don't print the whole thing in logs. Fetch `catalog.json` with a cache-buster query (same reason as U1), and other files as plain URLs.
3. Screens:
   - **Library:** the lesson list.
   - **Player** (one lesson):
     - track switch `guided` / `guided_zh` ("Chỉ tiếng Trung"), keeping position via the shared track-switch logic if W1a exposed it, else restarting at the matching line;
     - `<audio>` streaming the MP3;
     - play/pause, ±10s, previous/next cue, "Nghe lại câu" (seek to the active line's start);
     - speed selector 1× / 0.8× / 0.7× / 0.6× / 0.5× (`playbackRate`, `preservesPitch` true);
     - view mode (Hán+pinyin default / Hán / pinyin) and a Vietnamese toggle;
     - transcript list with the active line highlighted and auto-scrolled; tap a line to seek.

   All rendering decisions come from the W1a facade.
4. Keep the transcript component **portable into another document**: no `document.*`/`window.*` globals inside it; take the target window from props/context. W1c renders it inside a Document Picture-in-Picture window.
5. `scripts/web_deploy.sh`: build, then `npx wrangler pages deploy web/dist --project-name hanziflow`. The **user** runs this (one-time `wrangler login`). Add `just web` (dev server), `just web-build` and `just web-deploy` recipes.
6. Add `web` typecheck + unit tests (vitest) as one step in `scripts/check.sh`. Test at least:
   - the active-line highlight follows a mocked `currentTime`;
   - tapping a line calls seek with the facade's target;
   - the speed selector sets `playbackRate`.

## Acceptance
- `bash scripts/web_build.sh` produces `web/dist/index.html`. `npm --prefix web test` passes with ≥ 3 tests.
- Real content check (scripted; the agent runs it): a node/vitest script fetches the live `catalog.json` from `VITE_CONTENT_BASE_URL`, builds a `LessonSessionJs` for `lesson_001`, and asserts `cueCount()` equals the live timeline's `cues.length`, plus HTTP 206/200 with `Access-Control-Allow-Origin` on a `Range` request to `guided.mp3`. This proves CORS and streaming work before the user looks.
- `scripts/check.sh` all ok.

## Verify
```bash
bash scripts/web_build.sh && ls web/dist/index.html
npm --prefix web test -- --run 2>&1 | tail -5
npm --prefix web run test:live 2>&1 | tail -5     # the real-content check above
bash scripts/check.sh
```

## Out of scope
Document PiP, Media Session, character card (W1c); sign-in/Supabase JS client; service worker; the Android app.
