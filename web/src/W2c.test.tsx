import "@testing-library/jest-dom/vitest";
import { act, cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { CharacterCard } from "./components/CharacterCard";
import { isPlayerShortcutTarget } from "./screens/Player";
import { Transcript } from "./Transcript";

const line = (id: string, language = "zh-CN") =>
  JSON.stringify({
    id,
    type: "line",
    language,
    speaker: "",
    text: language === "vi-VN" ? "Hãy nghe thật kỹ." : id === "a" ? "你好" : "再见",
    pinyin: "nǐ hǎo",
    tokens: [{ hanzi: "你", pinyin: "nǐ", showPinyin: true }],
    translation: "xin chào",
    showHanzi: language === "zh-CN",
    showPinyinLine: language === "zh-CN",
    showTranslation: language === "zh-CN",
    lineVisible: true,
    placeholder: false,
  });

const fakeSession = (place = { speaking: -1, last: 0, next: 1 }) =>
  ({
    cuePlaceJson: () => JSON.stringify(place),
    activeCueIndex: () => place.speaking,
    cueCount: () => 2,
    cueJson: (index: number) =>
      JSON.stringify({ line_id: index ? "b" : "a", start_ms: index * 1000, end_ms: index * 1000 + 500 }),
    linesJson: () => '["a","b","coach"]',
    lineJson: (id: string, view: string) =>
      id === "coach"
        ? line(id, "vi-VN")
        : view === "HIDDEN"
          ? JSON.stringify({ ...JSON.parse(line(id)), showHanzi: false, showPinyinLine: false, placeholder: true })
          : line(id),
    seekTargetMs: (id: string) => (id === "a" ? 0 : 1000),
    nextCueStartMs: () => 1000,
    previousCueStartMs: () => 0,
    characterJson: () =>
      '{"char":"你","pinyin":"nǐ","han_viet":"nhĩ","hsk_level":1,"decomp_ids":"亻尔","vi_meaning":"bạn","lesson_words":["你好"],"components":[{"char":"亻"}]}',
  }) as any;

describe("W2c player acceptance", () => {
  afterEach(cleanup);

  it("marks the last and next lines in a gap, but only the speaking line while audio speaks", () => {
    const { rerender } = render(
      <Transcript
        session={fakeSession()}
        track="guided"
        positionMs={700}
        view="HANZI_ONLY"
        showVietnamese
        onSeek={vi.fn()}
      />,
    );
    expect(screen.getAllByRole("button", { name: /你/ }).find((item) => item.classList.contains("line"))).toHaveClass(
      "just-read",
    );
    expect(screen.getByText("vừa đọc")).toBeInTheDocument();
    expect(screen.getByText("Tiếp theo")).toBeInTheDocument();
    rerender(
      <Transcript
        session={fakeSession({ speaking: 1, last: 1, next: -1 })}
        track="guided"
        positionMs={1100}
        view="HANZI_ONLY"
        showVietnamese
        onSeek={vi.fn()}
      />,
    );
    expect(screen.getAllByRole("button", { name: /你/ }).find((item) => item.classList.contains("active"))).toHaveClass(
      "active",
    );
    expect(screen.queryByText("Tiếp theo")).not.toBeInTheDocument();
  });

  it("stops following on user scroll, returns to the current line, and preserves programmatic follow", () => {
    const scrollIntoView = vi.fn();
    Element.prototype.scrollIntoView = scrollIntoView;
    render(
      <Transcript
        session={fakeSession({ speaking: 1, last: 1, next: -1 })}
        track="guided"
        positionMs={1100}
        view="HANZI_ONLY"
        showVietnamese
        onSeek={vi.fn()}
      />,
    );
    act(() => window.dispatchEvent(new Event("scrollend")));
    fireEvent.wheel(window);
    expect(screen.getByRole("button", { name: /Về câu đang phát/ })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /Về câu đang phát/ }));
    expect(scrollIntoView).toHaveBeenCalled();
    expect(screen.queryByRole("button", { name: /Về câu đang phát/ })).not.toBeInTheDocument();
  });

  it("renders hidden bars and coach Vietnamese", () => {
    const seek = vi.fn();
    render(
      <Transcript session={fakeSession()} track="guided" positionMs={700} view="HIDDEN" showVietnamese onSeek={seek} />,
    );
    expect(document.querySelectorAll(".hidden-line")).toHaveLength(2);
    expect(screen.getByText("Hãy nghe thật kỹ.")).toHaveClass("coach-line");
  });

  it("uses a responsive sheet card and popover card", () => {
    const { rerender } = render(
      <CharacterCard session={fakeSession()} char="你" close={vi.fn()} select={vi.fn()} copy={vi.fn()} compact />,
    );
    expect(screen.getByLabelText("Chữ 你")).toHaveClass("character-sheet");
    rerender(
      <CharacterCard
        session={fakeSession()}
        char="你"
        close={vi.fn()}
        select={vi.fn()}
        copy={vi.fn()}
        compact={false}
      />,
    );
    expect(screen.getByLabelText("Chữ 你")).toHaveClass("character-popover");
  });

  it("uses window scroll events for follow mode and keeps programmatic scrolls followed until scrollend", () => {
    const scrollIntoView = vi.fn();
    Element.prototype.scrollIntoView = scrollIntoView;
    render(
      <Transcript
        session={fakeSession({ speaking: 1, last: 1, next: -1 })}
        track="guided"
        positionMs={1100}
        view="HANZI_ONLY"
        showVietnamese
        onSeek={vi.fn()}
      />,
    );
    act(() => window.dispatchEvent(new Event("scrollend")));
    act(() => window.dispatchEvent(new Event("scroll")));
    expect(screen.getByRole("button", { name: /Về câu đang phát/ })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /Về câu đang phát/ }));
    act(() => window.dispatchEvent(new Event("scroll")));
    act(() => window.dispatchEvent(new Event("scroll")));
    expect(screen.queryByRole("button", { name: /Về câu đang phát/ })).not.toBeInTheDocument();
    act(() => window.dispatchEvent(new Event("scrollend")));
    expect(screen.queryByRole("button", { name: /Về câu đang phát/ })).not.toBeInTheDocument();
  });

  it("anchors a gap return pill to the last line", () => {
    const scrollIntoView = vi.fn();
    Element.prototype.scrollIntoView = scrollIntoView;
    render(
      <Transcript
        session={fakeSession()}
        track="guided"
        positionMs={700}
        view="HANZI_ONLY"
        showVietnamese
        onSeek={vi.fn()}
      />,
    );
    act(() => window.dispatchEvent(new Event("scrollend")));
    act(() => window.dispatchEvent(new Event("scroll")));
    fireEvent.click(screen.getByRole("button", { name: /Về câu đang phát/ }));
    expect(scrollIntoView).toHaveBeenCalled();
  });

  it("does not treat modified replay or slider arrow keys as player shortcuts", () => {
    const slider = document.createElement("span");
    slider.setAttribute("role", "slider");
    expect(isPlayerShortcutTarget({ metaKey: true, ctrlKey: false, altKey: false, target: null })).toBe(true);
    expect(isPlayerShortcutTarget({ metaKey: false, ctrlKey: false, altKey: false, target: null })).toBe(false);
    expect(isPlayerShortcutTarget({ metaKey: false, ctrlKey: false, altKey: false, target: slider })).toBe(true);
  });
});
