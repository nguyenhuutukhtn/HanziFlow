import "@testing-library/jest-dom/vitest";
import { act, cleanup, render } from "@testing-library/react";
import { createPortal } from "react-dom";
import { afterAll, afterEach, describe, expect, it, vi } from "vitest";
import { PipPlayer } from "./pip/PipPlayer";
import { Transcript } from "./Transcript";

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

const cueIds = ["a", "b", "c"];
const line = (id: string, view: string) =>
  JSON.stringify({
    id,
    text: id === "a" ? "你好" : id === "b" ? "再见" : "谢谢",
    language: "zh-CN",
    tokens: [
      { hanzi: id === "a" ? "你" : id === "b" ? "再" : "谢", pinyin: id === "a" ? "nǐ" : "zài", showPinyin: true },
    ],
    pinyin: "nǐ hǎo",
    translation: "bản dịch không được hiện trong PiP",
    showHanzi: view !== "PINYIN_ONLY",
    showPinyinLine: view === "HANZI_WITH_PINYIN",
    showTranslation: true,
    lineVisible: true,
    placeholder: false,
  });
const session = {
  lessonTitle: () => "Bài học giả",
  cuePlaceJson: (position: number) =>
    JSON.stringify(position >= 1000 ? { speaking: 1, last: 1, next: 2 } : { speaking: -1, last: 0, next: 1 }),
  cueCount: () => cueIds.length,
  cueJson: (index: number) =>
    JSON.stringify({ line_id: cueIds[index], start_ms: index * 1000, end_ms: index * 1000 + 500 }),
  linesJson: () => JSON.stringify([...cueIds, "coach"]),
  lineJson: (id: string, view: string) =>
    id === "coach"
      ? JSON.stringify({ id, text: "Hãy nghe kỹ.", language: "vi-VN", lineVisible: true })
      : line(id, view),
  seekTargetMs: (id: string) => cueIds.indexOf(id) * 1000,
  characterJson: (char: string) =>
    JSON.stringify({ char, pinyin: "nǐ", han_viet: "nhĩ", vi_meaning: "bạn", components: [] }),
};
const player = {
  session,
  track: "guided",
  position: 700,
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
  const result = render(createPortal(<PipPlayer win={win} />, pipDocument.body));
  return { pipDocument, rerender: () => result.rerender(createPortal(<PipPlayer win={win} />, pipDocument.body)) };
};
const click = (element: Element) => element.dispatchEvent(new Event("click", { bubbles: true }));

describe("W3a PiP learning transcript", () => {
  setNavigatorProperty("userAgentData", undefined);
  setNavigatorProperty("platform", "MacIntel");
  setNavigatorProperty("userAgent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");

  afterEach(() => {
    cleanup();
    player.position = 700;
    player.view = "HANZI_ONLY";
    player.vietnamese = true;
    vi.clearAllMocks();
  });

  afterAll(restoreNavigator);

  it("lists every cue in its scroll container and pins five controls outside it", () => {
    const { pipDocument } = renderPip();
    expect(pipDocument.querySelectorAll(".float-text .line")).toHaveLength(cueIds.length);
    expect(pipDocument.querySelectorAll(".float-controls button")).toHaveLength(5);
    expect(pipDocument.querySelector(".float-text")?.contains(pipDocument.querySelector(".float-controls"))).toBe(
      false,
    );
  });

  it("marks the just-read and next lines during a gap", () => {
    const { pipDocument } = renderPip();
    expect(pipDocument.querySelector(".line.just-read")?.textContent).toContain("你");
    expect(pipDocument.querySelector(".line.next")?.textContent).toContain("Tiếp theo");
  });

  it("never renders Vietnamese in PiP", () => {
    const { pipDocument } = renderPip();
    expect(pipDocument.body.textContent).not.toContain("bản dịch không được hiện trong PiP");
    expect(pipDocument.body.textContent).not.toContain("Hãy nghe kỹ.");
  });

  it("toggles shared pinyin mode and renders ruby pinyin for every token", () => {
    const { pipDocument, rerender } = renderPip();
    const toggle = pipDocument.querySelector('[aria-label="Hiển thị pinyin"]')!;
    expect(toggle?.getAttribute("aria-pressed")).toBe("false");
    act(() => click(toggle));
    expect(player.setView).toHaveBeenCalledWith("HANZI_WITH_PINYIN");
    player.view = "HANZI_WITH_PINYIN";
    rerender();
    expect(pipDocument.querySelector('[aria-label="Hiển thị pinyin"]')?.getAttribute("aria-pressed")).toBe("true");
    expect(pipDocument.querySelectorAll("rt")).toHaveLength(cueIds.length);
    act(() => click(pipDocument.querySelector('[aria-label="Hiển thị pinyin"]')!));
    expect(player.setView).toHaveBeenLastCalledWith("HANZI_ONLY");
  });

  it("peeks one pinyin token before opening its PiP character card", () => {
    const { pipDocument } = renderPip();
    const token = pipDocument.querySelector(".character")!;
    act(() => click(token));
    expect(pipDocument.querySelectorAll("rt")).toHaveLength(1);
    expect(pipDocument.querySelector("[aria-label^='Chữ']")).toBeNull();
    act(() => click(token));
    expect(pipDocument.querySelector("[aria-label='Chữ 你']")).not.toBeNull();
  });

  it("opens the card with one tap when pinyin is on", () => {
    player.view = "HANZI_WITH_PINYIN";
    const { pipDocument } = renderPip();
    act(() => click(pipDocument.querySelector(".character")!));
    expect(pipDocument.querySelector("[aria-label='Chữ 你']")).not.toBeNull();
  });

  it("clears a peek after the anchor line changes", () => {
    const { pipDocument, rerender } = renderPip();
    act(() => click(pipDocument.querySelector(".character")!));
    expect(pipDocument.querySelectorAll("rt")).toHaveLength(1);
    player.position = 1200;
    rerender();
    expect(pipDocument.querySelectorAll("rt")).toHaveLength(0);
  });

  it("keeps the main transcript's pinyin as its existing line below", () => {
    const { container } = render(
      <Transcript
        session={session as any}
        track="guided"
        positionMs={700}
        view="HANZI_WITH_PINYIN"
        showVietnamese
        onSeek={vi.fn()}
      />,
    );
    expect(container.querySelector(".pinyin")).toHaveTextContent("nǐ hǎo");
    expect(container.querySelector("ruby")).toBeNull();
  });
});
