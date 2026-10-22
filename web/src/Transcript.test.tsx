import "@testing-library/jest-dom/vitest";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { Transcript } from "./Transcript";
import type { Session } from "./facade";

const line = (id: string, position: number) => JSON.stringify({ id, text: `字 ${id}`, pinyin: "zì", translation: "chữ", showHanzi: true, showPinyinLine: true, showTranslation: true, lineVisible: true, placeholder: false });
const session = (active = 0) => ({ activeCueIndex: () => active, cueCount: () => 2, cueJson: (i: number) => JSON.stringify({ line_id: i ? "b" : "a", start_ms: i * 1000, end_ms: i * 1000 + 500 }), linesJson: () => JSON.stringify(["a", "b"]), lineJson: (id: string, _v: string, _vi: boolean, position: number) => line(id, position), seekTargetMs: (id: string) => id === "a" ? 123 : 456 }) as unknown as Session;
describe("Transcript", () => {
  afterEach(cleanup);
  it("highlights the line selected by mocked currentTime", () => { render(<Transcript session={session(1)} track="guided" positionMs={1500} view="HANZI_WITH_PINYIN" showVietnamese onSeek={vi.fn()} />); expect(screen.getByRole("button", { name: /字 b/ })).toHaveClass("active"); });
  it("seeks to the facade target when a line is tapped", () => { const seek = vi.fn(); render(<Transcript session={session()} track="guided" positionMs={0} view="HANZI" showVietnamese onSeek={seek} />); fireEvent.click(screen.getByRole("button", { name: /字 b/ })); expect(seek).toHaveBeenCalledWith(456); });
});
