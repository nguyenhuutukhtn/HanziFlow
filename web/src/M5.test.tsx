import "@testing-library/jest-dom/vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { PlayerProvider, usePlayer } from "./player-context";

const modes = [
  ["hieu", "Hiểu"],
  ["nghe", "Nghe"],
  ["phan_xa", "Phản xạ"],
  ["noi", "Nói"],
] as const;
const catalogModes = readFileSync(
  resolve(process.cwd(), "../kmp/shared/src/commonTest/resources/contract/catalog_modes.json"),
  "utf8",
);
const catalogLesson1 = JSON.stringify({
  lessons: [
    {
      id: "lesson_001",
      base_url: "lessons/lesson_001/v1/",
      tracks: { guided: { timeline: { file: "timeline.json" } }, practice: { timeline: { file: "practice.json" } } },
      files: { reference: { file: "reference.json" } },
    },
  ],
});

const Probe = () => {
  const player = usePlayer();
  return (
    <>
      <output data-testid="track">{player.track}</output>
      <output data-testid="position">{player.position}</output>
      <output data-testid="labels">{player.tracks.map(({ display_name }) => display_name).join(", ")}</output>
      <button onClick={() => player.setTrack("noi")}>Nói</button>
    </>
  );
};
const fakeSession = (lessonId: string) => ({
  lessonTitle: () => (lessonId === "lesson_002" ? "你在哪儿？" : "Bài một"),
  audioPath: (track: string) => `${track}.mp3`,
  timelinePath: (track: string) => `timeline_${track}.json`,
  tracksJson: () =>
    JSON.stringify(
      lessonId === "lesson_002"
        ? modes.map(([name, display_name]) => ({ name, display_name }))
        : [
            { name: "guided", display_name: "Học có hướng dẫn" },
            { name: "practice", display_name: "Luyện tập" },
          ],
    ),
  sectionsJson: () => "[]",
});
const renderPlayer = async (catalog: string, lessonId: string) => {
  vi.spyOn(globalThis, "fetch").mockImplementation(() =>
    Promise.resolve({ text: () => Promise.resolve("{}"), json: () => Promise.resolve({}) } as Response),
  );
  vi.stubGlobal("HanziFlow:shared", {
    com: {
      hanziflow: {
        shared: {
          web: {
            LessonSessionJs: class {
              constructor() {
                return fakeSession(lessonId);
              }
            },
          },
        },
      },
    },
  });
  render(
    <PlayerProvider catalog={catalog} lessonId={lessonId} initialView="HANZI_ONLY">
      <Probe />
    </PlayerProvider>,
  );
  await waitFor(() => expect(screen.getByTestId("track")).not.toHaveTextContent(""));
};

describe("M5 mode tracks", () => {
  afterEach(() => {
    cleanup();
    localStorage.clear();
    vi.restoreAllMocks();
  });
  it("starts lesson 2 on Hiểu, shows labels, and selects Nói audio/timeline at position zero", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockImplementation(() =>
        Promise.resolve({ text: () => Promise.resolve("{}"), json: () => Promise.resolve({}) } as Response),
      );
    vi.stubGlobal("HanziFlow:shared", {
      com: {
        hanziflow: {
          shared: {
            web: {
              LessonSessionJs: class {
                constructor() {
                  return fakeSession("lesson_002");
                }
              },
            },
          },
        },
      },
    });
    render(
      <PlayerProvider catalog={catalogModes} lessonId="lesson_002" initialView="HANZI_ONLY">
        <Probe />
      </PlayerProvider>,
    );
    await waitFor(() => expect(screen.getByTestId("track")).toHaveTextContent("hieu"));
    expect(screen.getByTestId("labels")).toHaveTextContent("Hiểu, Nghe, Phản xạ, Nói");
    fireEvent.click(screen.getByRole("button", { name: "Nói" }));
    await waitFor(() => expect(screen.getByTestId("track")).toHaveTextContent("noi"));
    expect(screen.getByTestId("position")).toHaveTextContent("0");
    expect(fetchMock.mock.calls.map(([url]) => String(url))).toEqual(
      expect.arrayContaining([expect.stringMatching(/timeline_noi\.json$/)]),
    );
    expect(document.querySelector("audio")?.getAttribute("src")?.endsWith("noi.mp3")).toBe(true);
  });
  it("starts lesson 1 on guided", async () => {
    await renderPlayer(catalogLesson1, "lesson_001");
    expect(screen.getByTestId("track")).toHaveTextContent("guided");
  });
  it("restores per-lesson noi without affecting lesson 1", async () => {
    localStorage.setItem("hanziflow-track:lesson_002", "noi");
    await renderPlayer(catalogModes, "lesson_002");
    expect(screen.getByTestId("track")).toHaveTextContent("noi");
    cleanup();
    await renderPlayer(catalogLesson1, "lesson_001");
    expect(screen.getByTestId("track")).toHaveTextContent("guided");
  });
  it("falls back to hieu for an invalid saved lesson-2 track", async () => {
    localStorage.setItem("hanziflow-track:lesson_002", "practice");
    await renderPlayer(catalogModes, "lesson_002");
    expect(screen.getByTestId("track")).toHaveTextContent("hieu");
  });
  it("writes only the selected lesson's track key", async () => {
    localStorage.setItem("hanziflow-track:lesson_001", "guided");
    await renderPlayer(catalogModes, "lesson_002");
    fireEvent.click(screen.getByRole("button", { name: "Nói" }));
    await waitFor(() => expect(localStorage.getItem("hanziflow-track:lesson_002")).toBe("noi"));
    expect(localStorage.getItem("hanziflow-track:lesson_001")).toBe("guided");
  });
});
