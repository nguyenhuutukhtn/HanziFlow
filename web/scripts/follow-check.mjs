import { execFileSync, spawn } from "node:child_process";
import { createServer } from "node:net";
import { setTimeout as delay } from "node:timers/promises";
import { chromium } from "playwright-core";

const root = new URL("../..", import.meta.url);
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
try {
  run("npm", ["--prefix", "web", "run", "build"]);
  browser = await chromium.launch({ channel: "chrome" });
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

  for (const size of [
    { name: "desktop", width: 1280, height: 800 },
    { name: "mobile", width: 390, height: 844 },
  ]) {
    const page = await browser.newPage({ viewport: size });
    page.setDefaultTimeout(10000);
    await page.route("**/favicon.ico", (route) => route.fulfill({ status: 204 }));
    await page.goto(url, { waitUntil: "domcontentloaded" });
    await page.getByText("Đồ ăn mang về của đồng nghiệp", { exact: false }).click();
    const lines = page.locator(".line");
    for (let cue = 0; cue < 5; cue += 1) {
      await lines.nth(cue).evaluate((line) => line.click());
      await page.waitForFunction(
        (index) => document.querySelectorAll(".line")[index]?.classList.contains("active"),
        cue,
      );
      await page.waitForTimeout(700);
      const result = await page.evaluate(() => {
        const active = document.querySelector(".line.active");
        const playerBar = document.querySelector(".player-bar");
        if (!active || !playerBar) throw new Error("active line or player bar missing");
        const activeRect = active.getBoundingClientRect();
        const barRect = playerBar.getBoundingClientRect();
        const visibleBottom = Math.min(innerHeight, Math.max(0, barRect.top));
        const offset = (((activeRect.top + activeRect.bottom) / 2 - visibleBottom / 2) / visibleBottom) * 100;
        return { offset, follow: document.querySelector(".transcript-wrap")?.getAttribute("data-follow") };
      });
      console.log(
        `${size.name} cue ${cue + 1} offset ${result.offset >= 0 ? "+" : ""}${result.offset.toFixed(1)}% follow=${result.follow}`,
      );
      if (Math.abs(result.offset) > 10) throw new Error(`${size.name} cue ${cue + 1} is not centered`);
      if (result.follow !== "on") throw new Error(`${size.name} cue ${cue + 1} turned follow off`);
    }
    await page.close();
  }
  console.log("PiP document unavailable in headless Chrome; PiP scroller offset is covered by W3b.test.tsx");
} catch (error) {
  console.log(`FAIL follow: ${error instanceof Error ? error.message : String(error)}`);
  process.exitCode = 1;
} finally {
  await browser?.close();
  await stopPreview();
}
