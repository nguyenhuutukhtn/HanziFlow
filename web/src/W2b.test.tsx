import "@testing-library/jest-dom/vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "./App";

const nativeStorage = window.localStorage;

const catalog = JSON.stringify({
  lessons: [
    {
      id: "three",
      base_url: "lessons/three/",
      tracks: { guided: { timeline: { file: "t.json" } } },
      files: { reference: { file: "r.json" } },
    },
    {
      id: "one",
      base_url: "lessons/one/",
      tracks: { guided: { timeline: { file: "t.json" } } },
      files: { reference: { file: "r.json" } },
    },
    {
      id: "two",
      base_url: "lessons/two/",
      tracks: { guided: { timeline: { file: "t.json" } } },
      files: { reference: { file: "r.json" } },
    },
  ],
});

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
  referencePath() {
    return "r.json";
  }
  charactersPath() {
    return null;
  }
  tracksJson() {
    return '[{"name":"guided","display_name":"Học có hướng dẫn"}]';
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
    return '{"id":"a","text":"字","pinyin":"zì","translation":null,"showHanzi":true,"showPinyinLine":true,"showTranslation":false,"lineVisible":true,"placeholder":false}';
  }
  seekTargetMs() {
    return 0;
  }
  nextCueStartMs() {
    return 0;
  }
  previousCueStartMs() {
    return 0;
  }
  characterJson() {
    return null;
  }
}

function installFixture() {
  vi.stubEnv("VITE_CONTENT_BASE_URL", "https://example.test/course/");
  (globalThis as Record<string, unknown>)["HanziFlow:shared"] = {
    com: {
      hanziflow: {
        shared: {
          web: {
            CatalogJs: {
              lessonsJson: () =>
                '[{"id":"three","title":"Ba","order":3,"version":1},{"id":"one","title":"Một","order":1,"version":1},{"id":"two","title":"Hai","order":2,"version":1}]',
            },
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
        text: () => Promise.resolve(value.includes("catalog") ? catalog : "{}"),
        json: () => Promise.resolve({}),
      }),
    ),
  );
}

async function renderLibrary() {
  installFixture();
  render(<App />);
  await screen.findByRole("button", { name: /Một/ });
}

describe("W2b shell and settings", () => {
  afterEach(() => {
    cleanup();
    Object.defineProperty(window, "localStorage", { configurable: true, value: nativeStorage });
    nativeStorage.clear();
    vi.unstubAllEnvs();
    vi.unstubAllGlobals();
    document.documentElement.className = "";
  });

  it("sets the HTML theme class and persists it across a remount", async () => {
    await renderLibrary();
    fireEvent.click(document.querySelector<HTMLButtonElement>(".sidebar button:last-child")!);
    fireEvent.click(screen.getByRole("radio", { name: /Giấy/ }));
    expect(document.documentElement).toHaveClass("theme-paper");
    cleanup();
    await renderLibrary();
    expect(document.documentElement).toHaveClass("theme-paper");
  });

  it("uses light theme when localStorage throws", async () => {
    const storage = {
      getItem: () => {
        throw new Error("blocked");
      },
      setItem: () => {
        throw new Error("blocked");
      },
    };
    Object.defineProperty(window, "localStorage", { configurable: true, value: storage });
    await renderLibrary();
    expect(document.documentElement).not.toHaveClass("theme-paper");
    expect(document.documentElement).not.toHaveClass("theme-dark");
  });

  it("renders catalog lessons in order", async () => {
    await renderLibrary();
    expect(screen.getAllByRole("button", { name: /Bài [123]/ }).map((item) => item.textContent)).toEqual([
      "01MộtBài 1›",
      "02HaiBài 2›",
      "03BaBài 3›",
    ]);
  });

  it("uses the stored default view mode for a newly opened Player", async () => {
    localStorage.setItem(
      "hanziflow.settings",
      JSON.stringify({ theme: "light", viewMode: "HANZI_ONLY", hanziSize: 28 }),
    );
    await renderLibrary();
    fireEvent.click(screen.getByRole("button", { name: /Một/ }));
    await waitFor(() => expect(screen.getByLabelText("Hiển thị")).toHaveValue("HANZI_ONLY"));
  });

  it("has the mobile bottom navigation and hides the sidebar at 390px", async () => {
    Object.defineProperty(window, "innerWidth", { configurable: true, value: 390 });
    window.dispatchEvent(new Event("resize"));
    await renderLibrary();
    expect(document.querySelector(".mobile-nav")).toBeInTheDocument();
    expect(document.querySelector(".sidebar")).toBeInTheDocument();
    expect(document.querySelector(".mobile-nav")?.className).toBe("mobile-nav");
  });
});
