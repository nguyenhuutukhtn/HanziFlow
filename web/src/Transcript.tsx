import { useEffect, useRef } from "react";
import type { Cue, RenderLine, Session } from "./facade";
import { json } from "./facade";

type TargetWindow = Pick<Window, "requestAnimationFrame">;
type Props = {
  session: Session;
  track: string;
  positionMs: number;
  view: string;
  showVietnamese: boolean;
  onSeek: (targetMs: number) => void;
  targetWindow?: TargetWindow;
  onCharacter?: (char: string) => void;
  onCopy?: (text: string) => void;
};

export function Transcript({
  session,
  track,
  positionMs,
  view,
  showVietnamese,
  onSeek,
  targetWindow,
  onCharacter,
  onCopy,
}: Props) {
  const active = session.activeCueIndex(positionMs);
  const activeRef = useRef<HTMLButtonElement | null>(null);
  const ids = json<string[]>(session.linesJson(track)) ?? [];
  const cues = Array.from({ length: session.cueCount() }, (_, i) => json<Cue>(session.cueJson(i)));
  const activeId = active >= 0 ? cues[active]?.line_id : undefined;
  useEffect(() => {
    if (activeRef.current && targetWindow)
      targetWindow.requestAnimationFrame(() => {
        const activeLine = activeRef.current;
        if (typeof activeLine?.scrollIntoView === "function")
          activeLine.scrollIntoView({ block: "center", behavior: "smooth" });
      });
  }, [activeId, targetWindow]);
  return (
    <ol className="transcript" aria-label="Bản chép bài học">
      {ids.map((id) => {
        const line = json<RenderLine>(session.lineJson(id, view, showVietnamese, positionMs));
        if (!line?.lineVisible) return null;
        const isActive = id === activeId;
        return (
          <li key={id}>
            <button
              ref={isActive ? activeRef : undefined}
              className={isActive ? "line active" : "line"}
              onContextMenu={(event) => {
                event.preventDefault();
                onCopy?.(line.text);
              }}
              onClick={() => {
                const target = session.seekTargetMs(id, positionMs);
                if (target !== null) onSeek(target);
              }}
            >
              {line.placeholder ? (
                <span className="placeholder">…</span>
              ) : (
                <>
                  {line.showHanzi && (
                    <span className="hanzi">
                      {[...line.text].map((char, index) =>
                        /[\u3400-\u9fff]/.test(char) ? (
                          <span
                            className="character"
                            role="button"
                            tabIndex={0}
                            key={index}
                            onClick={(event) => {
                              event.stopPropagation();
                              onCharacter?.(char);
                            }}
                            onKeyDown={(event) => {
                              if (event.key === "Enter") onCharacter?.(char);
                            }}
                          >
                            {char}
                          </span>
                        ) : (
                          char
                        ),
                      )}
                    </span>
                  )}
                  {line.showPinyinLine && line.pinyin && <span className="pinyin">{line.pinyin}</span>}
                  {line.showTranslation && line.translation && <span className="meaning">{line.translation}</span>}
                </>
              )}
            </button>
          </li>
        );
      })}
    </ol>
  );
}
