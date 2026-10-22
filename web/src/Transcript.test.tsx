import "@testing-library/jest-dom/vitest";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { Transcript } from "./Transcript";
const session = (active = 0) =>
  ({
    activeCueIndex: () => active,
    cuePlaceJson: () => JSON.stringify({ speaking: active, last: active, next: -1 }),
    cueCount: () => 2,
    cueJson: () => '{"line_id":"a","start_ms":0}',
    linesJson: () => '["a"]',
    lineJson: () =>
      '{"id":"a","text":"字 b","pinyin":"zì b","translation":null,"showHanzi":true,"showPinyinLine":true,"showTranslation":false,"lineVisible":true,"placeholder":false}',
    seekTargetMs: () => 456,
  }) as any;
describe("Transcript", () => {
  afterEach(cleanup);
  it("highlights the line selected by mocked currentTime", () => {
    render(
      <Transcript
        session={session(1)}
        track="guided"
        positionMs={1500}
        view="HANZI_WITH_PINYIN"
        showVietnamese
        onSeek={vi.fn()}
      />,
    );
    expect(screen.getByRole("button", { name: /字 b/ })).toHaveClass("active");
  });
  it("seeks to the facade target when a line is tapped", () => {
    const seek = vi.fn();
    render(<Transcript session={session()} track="guided" positionMs={0} view="HANZI" showVietnamese onSeek={seek} />);
    fireEvent.click(screen.getByRole("button", { name: /字 b/ }));
    expect(seek).toHaveBeenCalledWith(456);
  });
});
