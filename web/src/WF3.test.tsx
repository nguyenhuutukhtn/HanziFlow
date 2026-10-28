import "@testing-library/jest-dom/vitest";
import { act, cleanup, fireEvent, render, screen } from "@testing-library/react";
import { createRequire } from "node:module";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { createPortal } from "react-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { PipPlayer } from "./pip/PipPlayer";
import { PlayerScreen } from "./screens/Player";

const require = createRequire(import.meta.url);
const shared = require("../public/shared/HanziFlow-shared.js") as {
  com: {
    hanziflow: {
      shared: {
        web: {
          LessonSessionJs: new (
            catalog: string,
            lessonId: string,
            timeline: string,
            reference: string,
            characters: string,
            track: string,
          ) => typeof followSession;
        };
      };
    };
  };
};
const fixture = (name: string) =>
  readFileSync(resolve(process.cwd(), `../kmp/shared/src/commonTest/resources/contract/${name}`), "utf8");

const followSession = {
  lessonTitle: () => "Bài thử",
  cuePlaceJson: () => JSON.stringify({ speaking: 1, last: 1, next: 2 }),
  cueCount: () => 3,
  cueJson: (index: number) =>
    JSON.stringify({ line_id: ["a", "b", "c"][index], start_ms: index * 1000, end_ms: index * 1000 + 800 }),
  linesJson: () => '["a","b","c"]',
  lineJson: (id: string) =>
    JSON.stringify({
      id,
      text: id,
      language: "zh-CN",
      tokens: [{ hanzi: id, pinyin: `${id}1`, showPinyin: true }],
      showHanzi: true,
      showPinyinLine: false,
      showTranslation: false,
      lineVisible: true,
      placeholder: false,
    }),
  seekTargetMs: () => 1000,
  characterJson: () => null,
};
const lessonSession = new shared.com.hanziflow.shared.web.LessonSessionJs(
  fixture("catalog.json"),
  "lesson_001",
  fixture("timeline_guided.json"),
  fixture("reference.json"),
  fixture("characters.json"),
  "guided",
);
const player = {
  session: lessonSession,
  track: "guided",
  tracks: [{ name: "guided", display_name: "Có hướng dẫn" }],
  position: 0,
  duration: 2_634_181,
  playing: false,
  speed: 1,
  view: "HANZI_WITH_PINYIN",
  vietnamese: true,
  sections: [],
  toggle: vi.fn(),
  previous: vi.fn(),
  next: vi.fn(),
  replay: vi.fn(),
  seek: vi.fn(),
  setTrack: vi.fn(),
  setSpeed: vi.fn(),
  setView: vi.fn(),
  setVietnamese: vi.fn(),
};

vi.mock("./player-context", () => ({
  PlayerProvider: ({ children }: { children: React.ReactNode }) => children,
  usePlayer: () => player,
}));

const installScrollableLayout = () => {
  vi.spyOn(window, "scrollTo").mockImplementation(() => undefined);
  vi.spyOn(Element.prototype, "getBoundingClientRect").mockImplementation(function (this: Element) {
    if (this.classList.contains("player-bar")) return rect(0, 700, 1280, 100);
    if (this.classList.contains("active")) return rect(0, 500, 600, 40);
    if (this.classList.contains("float-text")) return rect(0, 100, 360, 400);
    return rect(0, 0, 0, 0);
  });
  Object.defineProperty(document.documentElement, "scrollHeight", { configurable: true, value: 2000 });
  Object.defineProperty(document.documentElement, "clientHeight", { configurable: true, value: 800 });
};
const rect = (left: number, top: number, width: number, height: number) => ({
  left,
  top,
  right: left + width,
  bottom: top + height,
  width,
  height,
  x: left,
  y: top,
  toJSON() {},
});
const keepScrollingForTwoSeconds = (target: EventTarget) => {
  for (let elapsed = 0; elapsed < 2000; elapsed += 200) {
    act(() => {
      vi.advanceTimersByTime(200);
      target.dispatchEvent(new Event("scroll", { bubbles: true }));
    });
  }
};

describe("WF3 real player transcript", () => {
  afterEach(() => {
    cleanup();
    vi.useRealTimers();
    vi.restoreAllMocks();
    player.session = lessonSession;
    player.position = 0;
    player.view = "HANZI_WITH_PINYIN";
    const prototype = HTMLElement.prototype as unknown as Record<string, unknown>;
    delete prototype.scrollHeight;
    delete prototype.clientHeight;
    delete prototype.scrollTo;
  });

  it.each([1280, 390])("renders lesson-1 token pinyin through PlayerScreen at %ipx", (width) => {
    Object.defineProperty(window, "innerWidth", { configurable: true, value: width });
    vi.spyOn(window, "scrollTo").mockImplementation(() => undefined);
    const { container } = render(<PlayerScreen onBack={vi.fn()} />);
    expect(container.querySelectorAll(".transcript rt").length).toBeGreaterThan(0);
    const women = Array.from(container.querySelectorAll(".transcript ruby")).find((ruby) =>
      ruby.textContent?.startsWith("我们"),
    );
    expect(women).toHaveTextContent("我们wǒmen");
  });

  it("keeps main-player follow on while slow scroll events continue, then ends after 250ms idle", () => {
    vi.useFakeTimers();
    player.session = followSession as typeof lessonSession;
    player.position = 1200;
    installScrollableLayout();
    render(<PlayerScreen onBack={vi.fn()} />);

    keepScrollingForTwoSeconds(window);
    expect(document.querySelector(".transcript-wrap")).toHaveAttribute("data-follow", "on");
    act(() => vi.advanceTimersByTime(249));
    expect(document.querySelector(".transcript-wrap")).toHaveAttribute("data-follow", "on");
    act(() => vi.advanceTimersByTime(1));
    fireEvent.scroll(window);
    expect(document.querySelector(".transcript-wrap")).toHaveAttribute("data-follow", "off");
    expect(screen.getByRole("button", { name: /Về câu đang phát/ })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /Về câu đang phát/ }));
    expect(document.querySelector(".transcript-wrap")).toHaveAttribute("data-follow", "on");
  });

  it("applies the same rolling 250ms protection to the PiP transcript scroller", () => {
    vi.useFakeTimers();
    player.session = followSession as typeof lessonSession;
    player.position = 1200;
    installScrollableLayout();
    Object.defineProperties(HTMLElement.prototype, {
      scrollHeight: { configurable: true, value: 1200 },
      clientHeight: { configurable: true, value: 400 },
      scrollTo: { configurable: true, value: vi.fn() },
    });
    const pipDocument = document.implementation.createHTMLDocument("PiP");
    const win = { document: pipDocument } as Window;
    render(createPortal(<PipPlayer win={win} />, pipDocument.body));
    const scroller = pipDocument.querySelector<HTMLElement>(".float-text")!;
    const transcript = pipDocument.querySelector<HTMLElement>(".transcript-wrap")!;
    Object.defineProperties(scroller, {
      scrollHeight: { configurable: true, value: 1200 },
      clientHeight: { configurable: true, value: 400 },
    });

    keepScrollingForTwoSeconds(scroller);
    expect(transcript.getAttribute("data-follow")).toBe("on");
    act(() => vi.advanceTimersByTime(250));
    act(() => scroller.dispatchEvent(new Event("scroll", { bubbles: true })));
    expect(transcript.getAttribute("data-follow")).toBe("off");
  });
});
