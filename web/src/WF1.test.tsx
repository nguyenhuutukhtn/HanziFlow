import "@testing-library/jest-dom/vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { PlayerProvider, usePlayer } from "./player-context";

const catalogLesson1 = JSON.stringify({
  lessons: [
    {
      id: "lesson_001",
      base_url: "lessons/lesson_001/v8/",
      tracks: {
        guided: { timeline: { file: "timeline_guided.json" } },
        guided_zh: { timeline: { file: "timeline_guided_zh.json" } },
      },
      files: { reference: { file: "reference.json" } },
    },
  ],
});
const catalogModes = readFileSync(
  resolve(process.cwd(), "../kmp/shared/src/commonTest/resources/contract/catalog_modes.json"),
  "utf8",
);
const cues = [
  { line_id: "w_waimai", start_ms: 79_224, end_ms: 80_020 },
  { line_id: "w_waimai_vi", start_ms: 80_520, end_ms: 81_000 },
];

const Probe = () => {
  const player = usePlayer();
  return (
    <>
      <output data-testid="track">{player.track}</output>
      <output data-testid="position">{player.position}</output>
      <output data-testid="playing">{String(player.playing)}</output>
      <button onClick={() => player.setTrack("guided_zh")}>Chỉ tiếng Trung</button>
      <button onClick={() => player.setTrack("guided")}>Học có hướng dẫn</button>
      <button onClick={() => player.setTrack("noi")}>Nói</button>
      <button onClick={() => player.setSpeed(0.8)}>0.8</button>
      <button onClick={player.replay}>Nghe lại câu</button>
    </>
  );
};

const fakeSession = (lessonId: string) => ({
  audioPath: (track: string) => `${track}.mp3`,
  timelinePath: (track: string) => `timeline_${track}.json`,
  tracksJson: () => "[]",
  sectionsJson: () => "[]",
  activeCueIndex: (position: number) => (position >= 79_224 && position <= 80_020 ? 0 : -1),
  cuePlaceJson: (position: number) =>
    JSON.stringify(
      position >= 79_224 && position <= 80_020 ? { speaking: 0, last: 0, next: 1 } : { speaking: -1, last: 0, next: 1 },
    ),
  cueJson: (index: number) => JSON.stringify(cues[index]),
  cueCount: () => cues.length,
  lessonTitle: () => lessonId,
});

const timelineText = (url: string, lessonId: string) =>
  url.includes("guided_zh") ? "guided-zh-timeline" : `${lessonId}-timeline`;

const renderPlayer = (
  catalog: string,
  lessonId: string,
  options: { positions?: number[]; fetch?: (url: string) => Promise<Response> | Response } = {},
) => {
  const positions = [...(options.positions ?? [422_006])];
  vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
    const url = String(input);
    const response = {
      text: () => Promise.resolve(timelineText(url, lessonId)),
      json: () => Promise.resolve({}),
    } as Response;
    return Promise.resolve(options.fetch ? options.fetch(url) : response);
  });
  const positionFor = vi.fn(() => (positions.length > 1 ? positions.shift()! : positions[0]));
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
            TrackSwitchJs: { positionFor },
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
  return { positionFor };
};

describe("WF1 track switching and replay", () => {
  afterEach(() => {
    cleanup();
    localStorage.clear();
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it("applies the mapped lesson-1 position to the new source after metadata loads", async () => {
    const play = vi.spyOn(HTMLMediaElement.prototype, "play").mockResolvedValue();
    const { positionFor } = renderPlayer(catalogLesson1, "lesson_001");
    await waitFor(() => expect(document.querySelector("audio")).toBeInTheDocument());
    const audio = document.querySelector("audio") as HTMLAudioElement;
    audio.currentTime = 500;
    fireEvent.timeUpdate(audio);
    fireEvent.play(audio);
    fireEvent.click(screen.getByRole("button", { name: "0.8" }));
    await waitFor(() => expect(audio.playbackRate).toBe(0.8));

    fireEvent.click(screen.getByRole("button", { name: "Chỉ tiếng Trung" }));
    await waitFor(() => expect(audio.getAttribute("src")).toMatch(/guided_zh\.mp3$/));
    audio.currentTime = 0;
    fireEvent.loadedMetadata(audio);

    expect(positionFor).toHaveBeenCalledWith("lesson_001-timeline", "guided-zh-timeline", 500_000);
    expect(audio.currentTime).toBe(422.006);
    expect(audio.playbackRate).toBe(0.8);
    expect(play).toHaveBeenCalled();
    expect(screen.getByTestId("playing")).toHaveTextContent("true");
  });

  it("maps back through the pending position when the previous switch has not loaded metadata", async () => {
    const { positionFor } = renderPlayer(catalogLesson1, "lesson_001", { positions: [422_006, 500_658] });
    await waitFor(() => expect(document.querySelector("audio")).toBeInTheDocument());
    const audio = document.querySelector("audio") as HTMLAudioElement;
    audio.currentTime = 500;
    fireEvent.timeUpdate(audio);

    fireEvent.click(screen.getByRole("button", { name: "Chỉ tiếng Trung" }));
    await waitFor(() => expect(audio.getAttribute("src")).toMatch(/guided_zh\.mp3$/));
    audio.currentTime = 0;

    fireEvent.click(screen.getByRole("button", { name: "Học có hướng dẫn" }));
    await waitFor(() => expect(audio.getAttribute("src")).toMatch(/\/guided\.mp3$/));
    expect(positionFor).toHaveBeenNthCalledWith(1, "lesson_001-timeline", "guided-zh-timeline", 500_000);
    expect(positionFor).toHaveBeenNthCalledWith(2, "guided-zh-timeline", "lesson_001-timeline", 422_006);

    audio.currentTime = 0;
    fireEvent.loadedMetadata(audio);

    expect(audio.currentTime).toBe(500.658);
    expect(screen.getByTestId("position")).toHaveTextContent("500658");
  });

  it("cancels a pending switch when the loaded track is selected again", async () => {
    let release = () => {};
    const gate = new Promise<void>((resolve) => {
      release = resolve;
    });
    const { positionFor } = renderPlayer(catalogLesson1, "lesson_001", {
      fetch: (url) =>
        url.includes("guided_zh")
          ? gate.then(
              () =>
                ({
                  text: () => Promise.resolve("guided-zh-timeline"),
                  json: () => Promise.resolve({}),
                }) as Response,
            )
          : ({
              text: () => Promise.resolve(timelineText(url, "lesson_001")),
              json: () => Promise.resolve({}),
            } as Response),
    });
    await waitFor(() => expect(document.querySelector("audio")).toBeInTheDocument());
    const audio = document.querySelector("audio") as HTMLAudioElement;
    audio.currentTime = 500;
    fireEvent.timeUpdate(audio);
    const source = audio.getAttribute("src");

    fireEvent.click(screen.getByRole("button", { name: "Chỉ tiếng Trung" }));
    expect(screen.getByTestId("track")).toHaveTextContent("guided_zh");
    fireEvent.click(screen.getByRole("button", { name: "Học có hướng dẫn" }));
    release();
    await new Promise((resolve) => setTimeout(resolve, 0));

    expect(positionFor).not.toHaveBeenCalled();
    expect(screen.getByTestId("track")).toHaveTextContent("guided");
    expect(audio.getAttribute("src")).toBe(source);
    expect(audio.currentTime).toBe(500);
    expect(screen.getByTestId("position")).toHaveTextContent("500000");
    expect(localStorage.getItem("hanziflow-track:lesson_001")).toBe("guided");
  });

  it("catches a play() rejection interrupted by a new load", async () => {
    const play = vi
      .spyOn(HTMLMediaElement.prototype, "play")
      .mockRejectedValue(new DOMException("The play() request was interrupted by a new load request.", "AbortError"));
    const warn = vi.spyOn(console, "warn").mockImplementation(() => {});
    renderPlayer(catalogLesson1, "lesson_001");
    await waitFor(() => expect(document.querySelector("audio")).toBeInTheDocument());
    const audio = document.querySelector("audio") as HTMLAudioElement;
    fireEvent.play(audio);
    audio.currentTime = 500;
    fireEvent.timeUpdate(audio);

    fireEvent.click(screen.getByRole("button", { name: "Chỉ tiếng Trung" }));
    await waitFor(() => expect(audio.getAttribute("src")).toMatch(/guided_zh\.mp3$/));
    fireEvent.loadedMetadata(audio);
    await new Promise((resolve) => setTimeout(resolve, 0));
    expect(play).toHaveBeenCalled();
    expect(warn).not.toHaveBeenCalled();

    play.mockRejectedValue(new Error("boom"));
    fireEvent.click(screen.getByRole("button", { name: "Học có hướng dẫn" }));
    await waitFor(() => expect(audio.getAttribute("src")).toMatch(/\/guided\.mp3$/));
    fireEvent.loadedMetadata(audio);
    await waitFor(() => expect(warn).toHaveBeenCalledWith("play() failed", expect.any(Error)));
  });

  it("keeps a modes_v1 track switch at position zero", async () => {
    const { positionFor } = renderPlayer(catalogModes, "lesson_002");
    await waitFor(() => expect(document.querySelector("audio")).toBeInTheDocument());
    const audio = document.querySelector("audio") as HTMLAudioElement;
    audio.currentTime = 42;
    fireEvent.timeUpdate(audio);

    fireEvent.click(screen.getByRole("button", { name: "Nói" }));
    await waitFor(() => expect(audio.getAttribute("src")).toMatch(/noi\.mp3$/));
    audio.currentTime = 7;
    fireEvent.loadedMetadata(audio);

    expect(audio.currentTime).toBe(0);
    expect(screen.getByTestId("position")).toHaveTextContent("0");
    expect(positionFor).not.toHaveBeenCalled();
  });

  it("replays the last-started cue while the playhead is in a gap", async () => {
    renderPlayer(catalogLesson1, "lesson_001");
    await waitFor(() => expect(document.querySelector("audio")).toBeInTheDocument());
    const audio = document.querySelector("audio") as HTMLAudioElement;
    audio.currentTime = 80.3;
    fireEvent.timeUpdate(audio);

    fireEvent.click(screen.getByRole("button", { name: "Nghe lại câu" }));

    expect(audio.currentTime).toBe(79.224);

    audio.currentTime = 80;
    fireEvent.timeUpdate(audio);
    fireEvent.click(screen.getByRole("button", { name: "Nghe lại câu" }));
    expect(audio.currentTime).toBe(79.224);
  });
});
