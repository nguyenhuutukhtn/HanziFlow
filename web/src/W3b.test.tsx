import "@testing-library/jest-dom/vitest";
import { act, cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { Transcript } from "./Transcript";

const session = {
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
      showHanzi: true,
      showPinyinLine: false,
      showTranslation: false,
      lineVisible: true,
      placeholder: false,
    }),
  seekTargetMs: () => 1000,
} as any;

const transcript = (
  <Transcript session={session} track="guided" positionMs={1200} view="HANZI_ONLY" showVietnamese onSeek={vi.fn()} />
);

describe("W3b transcript follow", () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it("keeps follow on for programmatic scroll events and turns it off for a wheel", () => {
    vi.spyOn(window, "scrollTo").mockImplementation(() => undefined);
    render(transcript);
    act(() => window.dispatchEvent(new Event("scroll")));
    act(() => window.dispatchEvent(new Event("scrollend")));
    expect(document.querySelector(".transcript-wrap")).toHaveAttribute("data-follow", "on");
    fireEvent.wheel(window);
    expect(document.querySelector(".transcript-wrap")).toHaveAttribute("data-follow", "off");
    expect(screen.getByRole("button", { name: /Về câu đang phát/ })).toBeInTheDocument();
  });

  it("centers compact transcripts by scrolling the PiP scroller", () => {
    const scrollTo = vi.fn();
    Object.defineProperty(HTMLElement.prototype, "scrollTo", { configurable: true, value: scrollTo });
    vi.spyOn(Element.prototype, "getBoundingClientRect").mockImplementation(function (this: Element) {
      if (this.classList.contains("float-text")) {
        return { top: 100, bottom: 500, left: 0, right: 300, width: 300, height: 400, x: 0, y: 100, toJSON() {} };
      }
      if (this.classList.contains("active")) {
        return { top: 300, bottom: 340, left: 0, right: 280, width: 280, height: 40, x: 0, y: 300, toJSON() {} };
      }
      return { top: 0, bottom: 0, left: 0, right: 0, width: 0, height: 0, x: 0, y: 0, toJSON() {} };
    });
    render(<div className="float-text">{transcript}</div>);
    expect(scrollTo).toHaveBeenCalledWith({ top: 20, behavior: "smooth" });
    delete (HTMLElement.prototype as Partial<HTMLElement>).scrollTo;
  });
});
