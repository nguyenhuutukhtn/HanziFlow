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
const stopPreview = async () => {
  if (!preview?.pid) return;
  try {
    process.kill(-preview.pid, "SIGTERM");
  } catch {
    return;
  }
  await delay(250);
  try {
    process.kill(-preview.pid, "SIGKILL");
  } catch {}
};
process.on("exit", () => {
  try {
    if (preview?.pid) process.kill(-preview.pid, "SIGKILL");
  } catch {}
});

try {
  run("npm", ["--prefix", "web", "run", "build"]);
  browser = await chromium.launch({ channel: "chrome", headless: false });
  const port = await freePort();
  preview = spawn("npm", ["--prefix", "web", "run", "preview", "--", "--host", "127.0.0.1", "--port", String(port), "--strictPort"], {
    cwd: root,
    stdio: "ignore",
    detached: true,
  });
  const url = `http://127.0.0.1:${port}`;
  for (let attempt = 0; attempt < 40; attempt += 1) {
    try {
      if ((await fetch(url)).ok) break;
    } catch {}
    await delay(100);
  }
  const page = await browser.newPage();
  await page.goto(url, { waitUntil: "networkidle", timeout: 20000 });
  await page.getByRole("button", { name: /Bài 1/ }).click();
  await page.getByRole("button", { name: "Cửa sổ nổi" }).click();
  await page.waitForFunction(() => Boolean(window.documentPictureInPicture?.window));
  const result = await page.evaluate(async () => {
    const pip = window.documentPictureInPicture.window;
    const get = () => {
      const text = pip.document.querySelector(".float-text");
      const bodyFont = getComputedStyle(pip.document.body).fontFamily;
      return {
        overflow: text && getComputedStyle(text).overflowY,
        height: text?.clientHeight ?? 0,
        innerHeight: pip.innerHeight,
        font: bodyFont,
      };
    };
    const before = get();
    const lines = pip.document.querySelectorAll(".float-text .line");
    if (!lines[300]) throw new Error(`missing line 300 (${lines.length} lines)`);
    lines[300].dispatchEvent(new MouseEvent("click", { bubbles: true }));
    await new Promise((resolve) => setTimeout(resolve, 1500));
    const text = pip.document.querySelector(".float-text");
    const active = pip.document.querySelector(".float-text .line.active");
    if (!text || !active) throw new Error("active line missing after click");
    const textBox = text.getBoundingClientRect();
    const activeBox = active.getBoundingClientRect();
    return { ...before, scrollTop: text.scrollTop, offset: activeBox.top + activeBox.height / 2 - (textBox.top + textBox.height / 2) };
  });
  if (result.overflow !== "auto") throw new Error(`overflow-y ${result.overflow}, expected auto`);
  if (!(result.height < result.innerHeight)) throw new Error(`float-text height ${result.height} is not smaller than window ${result.innerHeight}`);
  if (/times/i.test(result.font)) throw new Error(`body font is Times: ${result.font}`);
  if (!(result.scrollTop > 0) || Math.abs(result.offset) > result.height * 0.25) throw new Error(`follow failed: scrollTop=${result.scrollTop}, offset=${result.offset}`);
  console.log(`overflow-y ${result.overflow}`);
  console.log(`float-text height ${result.height} < window ${result.innerHeight}`);
  console.log(`body font ${result.font}`);
  console.log("follow ok");
} catch (error) {
  console.error(`FAIL pip-check: ${error instanceof Error ? error.message : String(error)}`);
  process.exitCode = 1;
} finally {
  await browser?.close();
  await stopPreview();
}
