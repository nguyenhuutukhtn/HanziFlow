import "@testing-library/jest-dom/vitest";
import { cleanup, render } from "@testing-library/react";
import { createPortal } from "react-dom";
import { afterAll, afterEach, describe, expect, it, vi } from "vitest";
import { PipPlayer } from "./pip/PipPlayer";

const navigatorDescriptors = new Map<PropertyKey, PropertyDescriptor | undefined>();
const setNavigatorProperty = (property: PropertyKey, value: unknown) => {
  if (!navigatorDescriptors.has(property)) {
    navigatorDescriptors.set(property, Object.getOwnPropertyDescriptor(Navigator.prototype, property));
  }
  Object.defineProperty(Navigator.prototype, property, { configurable: true, value });
};
const restoreNavigator = () => {
  navigatorDescriptors.forEach((descriptor, property) => {
    if (descriptor) Object.defineProperty(Navigator.prototype, property, descriptor);
    else delete (Navigator.prototype as unknown as Record<PropertyKey, unknown>)[property];
  });
  navigatorDescriptors.clear();
};

const player = {
  session: {
    lessonTitle: () => "Bài thử",
    cuePlaceJson: () => JSON.stringify({ speaking: -1, last: 0, next: 1 }),
    cueCount: () => 2,
    cueJson: (index: number) => JSON.stringify({ line_id: index ? "b" : "a", start_ms: index * 1000 }),
    linesJson: () => '["a","b"]',
    lineJson: (id: string) =>
      JSON.stringify({
        id,
        text: id === "a" ? "你好" : "再见",
        language: "zh-CN",
        tokens: [{ hanzi: id === "a" ? "你" : "再", pinyin: "nǐ", showPinyin: true }],
        showHanzi: true,
        showPinyinLine: false,
        showTranslation: false,
        lineVisible: true,
        placeholder: false,
      }),
    seekTargetMs: () => 0,
    characterJson: () => null,
  },
  track: "guided",
  position: 500,
  playing: false,
  view: "HANZI_ONLY",
  vietnamese: true,
  toggle: vi.fn(),
  previous: vi.fn(),
  next: vi.fn(),
  replay: vi.fn(),
  seek: vi.fn(),
  setView: vi.fn(),
};

vi.mock("./player-context", () => ({ usePlayer: () => player }));

const renderPip = () => {
  const pipDocument = document.implementation.createHTMLDocument("PiP");
  const win = { document: pipDocument } as Window;
  render(createPortal(<PipPlayer win={win} />, pipDocument.body));
  return pipDocument;
};

describe("W2d PiP acceptance", () => {
  setNavigatorProperty("userAgentData", undefined);
  setNavigatorProperty("platform", "MacIntel");
  setNavigatorProperty("userAgent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");

  afterEach(() => {
    cleanup();
    document.documentElement.className = "";
    vi.clearAllMocks();
  });

  afterAll(restoreNavigator);

  it("keeps all controls outside the scrolling transcript and shows the paused gap state", () => {
    const pipDocument = renderPip();
    const controls = Array.from(pipDocument.querySelectorAll(".float-controls button"));
    expect(controls).toHaveLength(5);
    expect(controls.every((control) => !control.closest(".float-text"))).toBe(true);
    expect(pipDocument.body.textContent).toContain("vừa đọc");
    expect(pipDocument.body.textContent).toContain("Tiếp theo");
  });

  it("mirrors theme changes and dispatches controls in the PiP document", async () => {
    const pipDocument = renderPip();
    document.documentElement.classList.add("theme-dark");
    await vi.waitFor(() => expect(pipDocument.documentElement.classList.contains("theme-dark")).toBe(true));
    pipDocument.querySelector(".float-play")!.dispatchEvent(new Event("click", { bubbles: true }));
    pipDocument.querySelectorAll(".float-controls button")[0].dispatchEvent(new Event("click", { bubbles: true }));
    pipDocument.querySelectorAll(".float-controls button")[2].dispatchEvent(new Event("click", { bubbles: true }));
    pipDocument.querySelectorAll(".float-controls button")[3].dispatchEvent(new Event("click", { bubbles: true }));
    expect(player.toggle).toHaveBeenCalledOnce();
    expect(player.previous).toHaveBeenCalledOnce();
    expect(player.next).toHaveBeenCalledOnce();
    expect(player.replay).toHaveBeenCalledOnce();
    expect(document.querySelector(".float-controls")).toBeNull();
  });
});
