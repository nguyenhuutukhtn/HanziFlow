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
try {
  run("bash", ["scripts/web_facade.sh"]);
  run("npm", ["--prefix", "web", "run", "build"]);
  try {
    browser = await chromium.launch({ channel: "chrome" });
  } catch {
    console.log("skip smoke: no chrome");
    process.exit(0);
  }
  const port = await freePort();
  preview = spawn("npm", ["--prefix", "web", "run", "preview", "--", "--host", "127.0.0.1", "--port", String(port), "--strictPort"], {
    cwd: root,
    stdio: "ignore",
  });
  const url = `http://127.0.0.1:${port}`;
  for (let attempt = 0; attempt < 40; attempt += 1) {
    try {
      const response = await fetch(url);
      if (response.ok) break;
    } catch {}
    await delay(100);
  }
  const page = await browser.newPage();
  page.setDefaultTimeout(5000);
  await page.route("**/favicon.ico", (route) => route.fulfill({ status: 204 }));
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  page.on("console", (message) => {
    if (message.type() === "error") errors.push(`${message.text()} (${message.location().url})`);
  });
  await page.goto(url, { waitUntil: "networkidle", timeout: 20000 });
  await page.getByRole("button", { name: /Bài/ }).first().click();
  try {
    await page.locator(".section-marker").first().click();
  } catch (error) {
    if (errors.length) throw new Error(errors[0]);
    throw new Error(`no section marker on the Player route (${error instanceof Error ? error.message.split("\n")[0] : error})`);
  }
  if (errors.length) throw new Error(errors[0]);
  console.log("ok smoke");
} catch (error) {
  console.log(`FAIL smoke: ${error instanceof Error ? error.message : String(error)}`);
  process.exitCode = 1;
} finally {
  await browser?.close();
  preview?.kill();
}
