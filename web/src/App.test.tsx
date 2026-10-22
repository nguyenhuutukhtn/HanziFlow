import "@testing-library/jest-dom/vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "./App";

describe("Player", () => {
  afterEach(cleanup);
  it("sets audio playbackRate from the speed selector", async () => {
    vi.stubEnv("VITE_CONTENT_BASE_URL", "https://example.test/course/");
    const mediaHandlers: Record<string, () => void> = {};
    const nextCue = vi.fn(() => 0);
    Object.defineProperty(navigator, "mediaSession", {
      configurable: true,
      value: {
        setActionHandler: (name: string, handler: (() => void) | null) => {
          if (handler) mediaHandlers[name] = handler;
        },
        metadata: null,
      },
    });
    vi.stubGlobal(
      "MediaMetadata",
      class {
        constructor(_data: unknown) {}
      },
    );
    const audioPlay = vi.fn();
    Object.defineProperty(HTMLMediaElement.prototype, "play", { configurable: true, value: audioPlay });
    class FakeSession {
      lessonTitle() {
        return "Một";
      }
      audioPath() {
        return "a.mp3";
      }
      timelinePath() {
        return "t.json";
      }
      tracksJson() {
        return '[{"name":"guided","display_name":"Học có hướng dẫn"}]';
      }
      sectionsJson() {
        return '[{"id":"one","title":"Phần một","start_ms":500,"end_ms":1000}]';
      }
      activeCueIndex() {
        return 0;
      }
      cueCount() {
        return 1;
      }
      cueJson() {
        return '{"line_id":"a","start_ms":0,"end_ms":1}';
      }
      linesJson() {
        return '["a"]';
      }
      lineJson() {
        return '{"id":"a","text":"字","pinyin":"zì","tokens":[{"hanzi":"字","pinyin":"zì","showPinyin":true}],"translation":null,"showHanzi":true,"showPinyinLine":true,"showTranslation":false,"lineVisible":true,"placeholder":false}';
      }
      seekTargetMs() {
        return 0;
      }
      nextCueStartMs() {
        return nextCue();
      }
      previousCueStartMs() {
        return 0;
      }
      characterJson() {
        return '{"char":"字","pinyin":"zì","han_viet":"tự","vi_meaning":"chữ","components":[]}';
      }
    }
    (globalThis as any)["HanziFlow:shared"] = {
      com: {
        hanziflow: {
          shared: {
            web: {
              CatalogJs: { lessonsJson: () => '[{"id":"lesson_001","title":"Một","order":1,"version":1}]' },
              TrackSwitchJs: { positionFor: () => 0 },
              LessonSessionJs: FakeSession,
            },
          },
        },
      },
    };
    vi.stubGlobal(
      "fetch",
      vi.fn((value: string) =>
        Promise.resolve({
          text: () =>
            Promise.resolve(
              value.includes("catalog")
                ? '{"lessons":[{"id":"lesson_001","base_url":"lessons/lesson_001/v1/","tracks":{"guided":{"timeline":{"file":"t.json"}}},"files":{"reference":{"file":"r.json"}}}]}'
                : "{}",
            ),
          json: () => Promise.resolve({}),
        }),
      ),
    );
    render(<App />);
    fireEvent.click(await screen.findByRole("button", { name: /Một/ }));
    await screen.findByLabelText("Tốc độ");
    fireEvent.click(screen.getByRole("radio", { name: "0.7×" }));
    await waitFor(() => expect((document.querySelector("audio") as HTMLAudioElement).playbackRate).toBe(0.7));
    fireEvent.click(screen.getByRole("button", { name: "字" }));
    expect(screen.getByLabelText("Chữ 字")).toHaveTextContent("Hán-Việt: tự");
    expect(screen.getByLabelText("Chữ 字")).toHaveTextContent("Nghĩa: chữ");
    expect(screen.getByRole("button", { name: "Cửa sổ nổi" })).toBeDisabled();
    fireEvent.click(screen.getByRole("button", { name: "Phần một" }));
    expect((document.querySelector("audio") as HTMLAudioElement).currentTime).toBe(0.5);
    await waitFor(() => expect(mediaHandlers.nexttrack).toBeTypeOf("function"));
    mediaHandlers.nexttrack();
    expect(nextCue).toHaveBeenCalled();
  });
});
