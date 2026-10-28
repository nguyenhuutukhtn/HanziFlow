import { execFileSync, spawn } from "node:child_process";
import { createServer } from "node:net";
import { setTimeout as delay } from "node:timers/promises";
import { chromium } from "playwright-core";

const root = new URL("../..", import.meta.url);
const tolerance = 0.3;
const run = (command, args) => execFileSync(command, args, { cwd: root, stdio: "inherit" });
const freePort = () =>
  new Promise((resolve, reject) => {
    const server = createServer();
    server.once("error", reject);
    server.listen(0, "127.0.0.1", () => {
      const address = server.address();
      server.close(() => resolve(address.port));
    });
  });

let preview;
let browser;
// `npm run preview` is only a wrapper: kill the whole detached process group so Vite goes with it.
const stopPreview = async () => {
  const pid = preview?.pid;
  if (!pid) return;
  try {
    process.kill(-pid, "SIGTERM");
  } catch {
    return; // the group is already gone
  }
  for (let attempt = 0; attempt < 20; attempt += 1) {
    await delay(50);
    try {
      process.kill(-pid, 0);
    } catch {
      return;
    }
  }
  try {
    process.kill(-pid, "SIGKILL");
  } catch {}
};
for (const signal of ["SIGINT", "SIGTERM"]) process.on(signal, () => process.exit(130));
process.on("exit", () => {
  try {
    if (preview?.pid) process.kill(-preview.pid, "SIGKILL");
  } catch {}
});
const deadline = setTimeout(() => {
  console.log("FAIL track: exceeded 60 seconds");
  void stopPreview()
    .finally(() => browser?.close())
    .finally(() => process.exit(1));
}, 60_000);

try {
  run("npm", ["--prefix", "web", "run", "build"]);
  browser = await chromium.launch({ channel: "chrome", headless: true });
  const port = await freePort();
  preview = spawn(
    "npm",
    ["--prefix", "web", "run", "preview", "--", "--host", "127.0.0.1", "--port", String(port), "--strictPort"],
    { cwd: root, stdio: "ignore", detached: true },
  );
  const url = `http://127.0.0.1:${port}`;
  for (let attempt = 0; attempt < 40; attempt += 1) {
    try {
      if ((await fetch(url)).ok) break;
    } catch {}
    await delay(100);
  }

  const page = await browser.newPage({ viewport: { width: 1280, height: 800 } });
  page.setDefaultTimeout(10_000);
  await page.route("**/favicon.ico", (route) => route.fulfill({ status: 204 }));
  // Hold the guided_zh audio/timeline only for the rapid-switch checks, to widen the window where a
  // switch has been requested but its source or mapped position has not settled yet.
  let guidedZhAudioDelay = 0;
  await page.route("**/guided_zh.mp3*", async (route) => {
    if (guidedZhAudioDelay) await delay(guidedZhAudioDelay);
    await route.continue();
  });
  let guidedZhTimelineDelay = 0;
  await page.route("**/timeline_guided_zh.json*", async (route) => {
    if (guidedZhTimelineDelay) await delay(guidedZhTimelineDelay);
    await route.continue();
  });
  await page.addInitScript(() => localStorage.removeItem("hanziflow-track:lesson_001"));
  await page.goto(url, { waitUntil: "domcontentloaded" });
  await page.getByText("Đồ ăn mang về của đồng nghiệp", { exact: false }).click();
  const audio = page.locator("audio");
  await audio.waitFor({ state: "attached" });
  await page.waitForFunction(() => document.querySelector("audio")?.readyState >= 1);

  const mappedPositionMs = (from, to, positionMs) =>
    page.evaluate(
      async ({ fromTrack, toTrack, position }) => {
        const catalogEntry = performance
          .getEntriesByType("resource")
          .find((entry) => entry.name.includes("catalog.json"));
        if (!catalogEntry) throw new Error("catalog request was not found");
        const catalogUrl = new URL(catalogEntry.name);
        catalogUrl.search = "";
        const catalog = await fetch(catalogUrl).then((response) => response.json());
        const lesson = catalog.lessons.find((item) => item.id === "lesson_001");
        if (!lesson) throw new Error("lesson_001 missing from catalog");
        const timeline = async (track) => {
          const path = `${lesson.base_url}${lesson.tracks[track].timeline.file}`;
          return fetch(new URL(path, catalogUrl)).then((response) => response.text());
        };
        const [fromTimeline, toTimeline] = await Promise.all([timeline(fromTrack), timeline(toTrack)]);
        const library = globalThis["HanziFlow:shared"];
        return library.com.hanziflow.shared.web.TrackSwitchJs.positionFor(fromTimeline, toTimeline, position);
      },
      { fromTrack: from, toTrack: to, position: positionMs },
    );
  const mappedPosition = (from, to, seconds) => mappedPositionMs(from, to, seconds * 1000);

  const checkSwitch = async (from, to, start, buttonName) => {
    const expectedMs = await mappedPosition(from, to, start);
    const oldSource = await audio.getAttribute("src");
    await page.getByRole("radio", { name: buttonName, exact: true }).click();
    await page.waitForFunction((source) => {
      const element = document.querySelector("audio");
      return element && element.getAttribute("src") !== source && element.readyState >= 1;
    }, oldSource);
    const actual = await audio.evaluate((element) => element.currentTime);
    const expected = expectedMs / 1000;
    console.log(
      `track ${from}→${to} start=${start.toFixed(3)} expected=${expected.toFixed(3)} actual=${actual.toFixed(3)}`,
    );
    if (Math.abs(actual - expected) > tolerance) throw new Error(`${from}→${to} missed mapped position`);
    return expected;
  };

  for (const start of [500, 233.485]) {
    await audio.evaluate((element, seconds) => {
      element.currentTime = seconds;
    }, start);
    const reverseStart = await checkSwitch("guided", "guided_zh", start, "Chỉ tiếng Trung");
    await checkSwitch("guided_zh", "guided", reverseStart, "Học có hướng dẫn");
  }

  // Finding 1: switch back while the previous target's metadata is still loading. The position must
  // be mapped from the pending position on the pending timeline, not from the reset currentTime (0).
  {
    const start = 500;
    await audio.evaluate((element, seconds) => {
      element.currentTime = seconds;
      element.dispatchEvent(new Event("timeupdate"));
    }, start);
    const pendingMs = await mappedPosition("guided", "guided_zh", start);
    const expectedMs = await mappedPositionMs("guided_zh", "guided", pendingMs);
    guidedZhAudioDelay = 1_000;
    const oldSource = await audio.getAttribute("src");
    await page.getByRole("radio", { name: "Chỉ tiếng Trung", exact: true }).click();
    // The source has committed to guided_zh; its metadata is still held back by the route delay.
    await page.waitForFunction((source) => {
      const element = document.querySelector("audio");
      return element && element.getAttribute("src") !== source;
    }, oldSource);
    const pendingSource = await audio.getAttribute("src");
    await page.getByRole("radio", { name: "Học có hướng dẫn", exact: true }).click();
    await page.waitForFunction((source) => {
      const element = document.querySelector("audio");
      return element && element.getAttribute("src") !== source && element.readyState >= 1;
    }, pendingSource);
    const actual = await audio.evaluate((element) => element.currentTime);
    const expected = expectedMs / 1000;
    console.log(
      `track rapid-back start=${start.toFixed(3)} expected=${expected.toFixed(3)} actual=${actual.toFixed(3)}`,
    );
    if (Math.abs(actual - expected) > tolerance) throw new Error("rapid-back missed the pending mapped position");
    guidedZhAudioDelay = 0;
  }

  // Finding 2: two clicks inside the pending window — the last click (the loaded track) cancels the switch.
  {
    const start = 500;
    await audio.evaluate((element, seconds) => {
      element.currentTime = seconds;
      element.dispatchEvent(new Event("timeupdate"));
    }, start);
    guidedZhTimelineDelay = 500;
    const oldSource = await audio.getAttribute("src");
    await page.getByRole("radio", { name: "Chỉ tiếng Trung", exact: true }).click();
    await delay(50);
    await page.getByRole("radio", { name: "Học có hướng dẫn", exact: true }).click();
    await delay(700);
    const actual = await audio.evaluate((element) => element.currentTime);
    const source = await audio.getAttribute("src");
    console.log(
      `track cancel start=${start.toFixed(3)} expected=${start.toFixed(3)} actual=${actual.toFixed(3)}`,
    );
    if (source !== oldSource || Math.abs(actual - start) > tolerance)
      throw new Error("cancel lost the original position");
    guidedZhTimelineDelay = 0;
  }

  for (const start of [80.3, 79.2]) {
    await audio.evaluate((element, seconds) => {
      element.currentTime = seconds;
      element.dispatchEvent(new Event("timeupdate"));
    }, start);
    await page.keyboard.press("r");
    const actual = await audio.evaluate((element) => element.currentTime);
    const expected = { 80.3: 79.224, 79.2: 76.368 }[start]; // last-started cue: w_waimai, then word_lead_vi
    console.log(`replay start=${start.toFixed(3)} expected=${expected.toFixed(3)} actual=${actual.toFixed(3)}`);
    if (Math.abs(actual - expected) > tolerance) throw new Error(`replay from ${start} missed cue start`);
  }
} catch (error) {
  console.log(`FAIL track: ${error instanceof Error ? error.message : String(error)}`);
  process.exitCode = 1;
} finally {
  clearTimeout(deadline);
  await browser?.close();
  await stopPreview();
}
