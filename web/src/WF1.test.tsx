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

const renderPlayer = (catalog: string, lessonId: string, mappedPosition = 422_006) => {
  vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
    const url = String(input);
    const timeline = url.includes("guided_zh") ? "guided-zh-timeline" : `${lessonId}-timeline`;
    return Promise.resolve({ text: () => Promise.resolve(timeline), json: () => Promise.resolve({}) } as Response);
  });
  const positionFor = vi.fn(() => mappedPosition);
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
