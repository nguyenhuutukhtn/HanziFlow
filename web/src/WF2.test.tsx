import "@testing-library/jest-dom/vitest";
import { act, cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { useEffect, useState, type ReactNode } from "react";
import { createPortal } from "react-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { PipPlayer } from "./pip/PipPlayer";
import { PlayerScreen } from "./screens/Player";
import { Settings } from "./screens/Settings";
import { readPipAlwaysOnTopHintDismissed, readSettings, writeSettings, type SettingsState } from "./settings";

const player = {
  session: {
    lessonTitle: () => "Bài thử",
    cuePlaceJson: () => JSON.stringify({ speaking: 0, last: 0, next: -1 }),
    cueCount: () => 1,
    cueJson: () => JSON.stringify({ line_id: "a", start_ms: 0, end_ms: 200000 }),
    linesJson: () => '["a"]',
    lineJson: () =>
      JSON.stringify({
        id: "a",
        text: "你好",
        language: "zh-CN",
        tokens: [{ hanzi: "你", pinyin: "nǐ", showPinyin: true }],
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
  tracks: [{ name: "guided", display_name: "Có hướng dẫn" }],
  position: 100000,
  duration: 2634181,
  playing: false,
  speed: 1,
  view: "HANZI_ONLY",
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
  PlayerProvider: ({ children }: { children: ReactNode }) => children,
  usePlayer: () => player,
}));

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
const setUserAgent = (platform: string, userAgent: string) => {
  setNavigatorProperty("userAgentData", undefined);
  setNavigatorProperty("platform", platform);
  setNavigatorProperty("userAgent", userAgent);
};

const renderPip = () => {
  const pipDocument = document.implementation.createHTMLDocument("PiP");
  const win = { document: pipDocument } as Window;
  const result = render(createPortal(<PipPlayer win={win} />, pipDocument.body));
  return { pipDocument, unmount: result.unmount };
};
const clickIn = (targetDocument: Document, selector: string) => {
  act(() => targetDocument.querySelector(selector)!.dispatchEvent(new MouseEvent("click", { bubbles: true })));
};

function AppSettingsPath() {
  const [settings, setSettings] = useState<SettingsState>(readSettings);
  useEffect(() => writeSettings(settings), [settings]);
  return <Settings settings={settings} setSettings={setSettings} />;
}

describe("WF2 PiP and seeking acceptance", () => {
  afterEach(() => {
    cleanup();
    localStorage.clear();
    player.position = 100000;
    vi.clearAllMocks();
    restoreNavigator();
  });

  it("seeks from the real Player slider and exposes readable time", () => {
    setUserAgent("MacIntel", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");
    const result = render(<PlayerScreen onBack={vi.fn()} />);
    let slider = screen.getByRole("slider", { name: "Thanh tiến trình" });
    expect(slider).toHaveAttribute("aria-valuetext", "1:40 / 43:54");

    fireEvent.keyDown(slider, { key: "ArrowRight" });
    expect(player.seek).toHaveBeenLastCalledWith(105000);
    fireEvent.keyDown(slider, { key: "PageUp" });
    expect(player.seek).toHaveBeenLastCalledWith(130000);

    player.position = 10000;
    result.rerender(<PlayerScreen onBack={vi.fn()} />);
    slider = screen.getByRole("slider", { name: "Thanh tiến trình" });
    fireEvent.keyDown(slider, { key: "PageDown" });
    expect(player.seek).toHaveBeenLastCalledWith(0);
  });

  it("copies readable CSS rules and an absolute fallback link through openPip", async () => {
    setUserAgent("MacIntel", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");
    const pipDocument = document.implementation.createHTMLDocument("PiP");
    const pipWindow = { document: pipDocument, addEventListener: vi.fn() } as unknown as Window;
    const requestWindow = vi.fn().mockResolvedValue(pipWindow);
    Object.defineProperty(window, "documentPictureInPicture", {
      configurable: true,
      value: { requestWindow },
    });
    const originalSheets = Object.getOwnPropertyDescriptor(document, "styleSheets");
    const readable = {
      ownerNode: { nodeName: "LINK" },
      cssRules: [{ cssText: ".readable { color: green; }" }],
      href: "https://example.test/app.css",
    };
    const unreadable = {
      ownerNode: { nodeName: "LINK" },
      get cssRules() {
        throw new DOMException("Blocked", "SecurityError");
      },
      href: "/fonts.css",
    };
    Object.defineProperty(document, "styleSheets", { configurable: true, value: [readable, unreadable] });

    try {
      render(<PlayerScreen onBack={vi.fn()} />);
      fireEvent.click(screen.getByRole("button", { name: "Cửa sổ nổi" }));
      await waitFor(() => expect(requestWindow).toHaveBeenCalledOnce());
      await waitFor(() => expect(pipDocument.head.textContent).toContain(".readable"));
      const fallback = pipDocument.head.querySelector('link[rel="stylesheet"][href$="/fonts.css"]');
      expect((fallback as HTMLLinkElement | null)?.href).toBe(new URL("/fonts.css", document.baseURI).href);
    } finally {
      if (originalSheets) Object.defineProperty(document, "styleSheets", originalSheets);
      else delete (document as unknown as Record<string, unknown>).styleSheets;
      delete (window as Window & { documentPictureInPicture?: unknown }).documentPictureInPicture;
    }
  });

  it("shows six Linux controls, persists dismissal, and lets the pin reopen the exact hint", () => {
    setUserAgent("Linux x86_64", "Mozilla/5.0 (X11; Linux x86_64) Chrome/141.0.0.0");
    const first = renderPip();
    expect(first.pipDocument.querySelectorAll(".float-controls button")).toHaveLength(6);
    expect(first.pipDocument.querySelector('[aria-label="Giữ cửa sổ trên cùng"]')).not.toBeNull();
    expect(first.pipDocument.querySelector(".pip-top-hint")?.textContent).toContain(
      "Chuột phải lên thanh tiêu đề cửa sổ này → Always on Top (Luôn ở trên cùng).",
    );
    expect(first.pipDocument.querySelector(".pip-top-hint")?.textContent).toContain(
      "Tự động mỗi lần: chạy Chrome với --ozone-platform=x11.",
    );
    clickIn(first.pipDocument, ".pip-top-hint button");
    expect(readPipAlwaysOnTopHintDismissed()).toBe(true);
    expect(first.pipDocument.querySelector(".pip-top-hint")).toBeNull();
    first.unmount();

    const second = renderPip();
    expect(second.pipDocument.querySelector(".pip-top-hint")).toBeNull();
    clickIn(second.pipDocument, '[aria-label="Giữ cửa sổ trên cùng"]');
    expect(second.pipDocument.querySelector(".pip-top-hint")).not.toBeNull();
  });

  it("keeps dismissal after a theme change through the app settings path", () => {
    setUserAgent("Linux x86_64", "Mozilla/5.0 (X11; Linux x86_64) Chrome/141.0.0.0");
    render(<AppSettingsPath />);
    const first = renderPip();
    clickIn(first.pipDocument, ".pip-top-hint button");
    first.unmount();
    fireEvent.click(screen.getByRole("radio", { name: "Tối" }));
    expect(readSettings().theme).toBe("dark");

    const second = renderPip();
    expect(second.pipDocument.querySelector(".pip-top-hint")).toBeNull();
  });

  it.each([
    ["macOS", "MacIntel", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"],
    ["Android", "Linux armv8l", "Mozilla/5.0 (Linux; Android 15; Pixel 9)"],
  ])("does not show the pin or hint on %s", (_name, platform, userAgent) => {
    setUserAgent(platform, userAgent);
    const { pipDocument } = renderPip();
    expect(pipDocument.querySelectorAll(".float-controls button")).toHaveLength(5);
    expect(pipDocument.querySelector('[aria-label="Giữ cửa sổ trên cùng"]')).toBeNull();
    expect(pipDocument.querySelector(".pip-top-hint")).toBeNull();
  });
});
