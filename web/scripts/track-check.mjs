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
const deadline = setTimeout(() => {
  console.log("FAIL track: exceeded 60 seconds");
  preview?.kill();
  void browser?.close().finally(() => process.exit(1));
}, 60_000);

try {
  run("npm", ["--prefix", "web", "run", "build"]);
  browser = await chromium.launch({ channel: "chrome", headless: true });
  const port = await freePort();
  preview = spawn(
    "npm",
    ["--prefix", "web", "run", "preview", "--", "--host", "127.0.0.1", "--port", String(port), "--strictPort"],
    { cwd: root, stdio: "ignore" },
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
  await page.addInitScript(() => localStorage.removeItem("hanziflow-track:lesson_001"));
  await page.goto(url, { waitUntil: "domcontentloaded" });
  await page.getByText("Đồ ăn mang về của đồng nghiệp", { exact: false }).click();
  const audio = page.locator("audio");
  await audio.waitFor({ state: "attached" });
  await page.waitForFunction(() => document.querySelector("audio")?.readyState >= 1);

  const mappedPosition = (from, to, seconds) =>
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
        return library.com.hanziflow.shared.web.TrackSwitchJs.positionFor(fromTimeline, toTimeline, position * 1000);
      },
      { fromTrack: from, toTrack: to, position: seconds },
    );

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
  preview?.kill();
}
