import { useEffect, useRef, useState } from "react";
import type { Cue, CuePlace, RenderLine, Session } from "./facade";
import { json } from "./facade";

type Props = {
  session: Session;
  track: string;
  positionMs: number;
  view: string;
  showVietnamese: boolean;
  onSeek: (targetMs: number) => void;
  onCharacter?: (char: string) => void;
  onCopy?: (text: string) => void;
  onPrevious?: () => void;
  onNext?: () => void;
};
const selected = () => Boolean(window.getSelection?.()?.toString());
let reportedMissingCuePlaceJson = false;

export function Transcript({
  session,
  track,
  positionMs,
  view,
  showVietnamese,
  onSeek,
  onCharacter,
  onCopy,
  onPrevious,
  onNext,
}: Props) {
  const [follow, setFollow] = useState(true);
  const [outOfView, setOutOfView] = useState(false);
  const programmed = useRef(false);
  const programmedTimer = useRef<number>();
  const activeRef = useRef<HTMLDivElement | null>(null);
  const cues = Array.from({ length: session.cueCount() }, (_, i) => json<Cue>(session.cueJson(i)));
  const cuePlaceJson = (session as Partial<Session>).cuePlaceJson;
  if (!cuePlaceJson && !reportedMissingCuePlaceJson) {
    reportedMissingCuePlaceJson = true;
    console.error("The shared facade is missing cuePlaceJson; rebuild web/public/shared.");
  }
  const place = json<CuePlace>(cuePlaceJson?.call(session, positionMs) ?? null);
  const ids = json<string[]>(session.linesJson(track)) ?? [];
  const anchorIndex = place ? (place.speaking >= 0 ? place.speaking : place.last) : -1;
  const activeId = place?.speaking != null && place.speaking >= 0 ? cues[place.speaking]?.line_id : undefined;
  const anchorId = anchorIndex >= 0 ? cues[anchorIndex]?.line_id : undefined;
  const finishProgrammaticScroll = () => {
    programmed.current = false;
    if (programmedTimer.current) window.clearTimeout(programmedTimer.current);
  };
  const returnToActive = () => {
    programmed.current = true;
    setFollow(true);
    setOutOfView(false);
    const target = activeRef.current as
      | (HTMLDivElement & { scrollIntoView?: (options: ScrollIntoViewOptions) => void })
      | null;
    target?.scrollIntoView?.({ block: "center", behavior: "smooth" });
    if (programmedTimer.current) window.clearTimeout(programmedTimer.current);
    programmedTimer.current = window.setTimeout(finishProgrammaticScroll, 1000);
  };
  useEffect(() => {
    if (follow && activeRef.current) returnToActive();
  }, [anchorId]);
  useEffect(() => {
    const target = activeRef.current;
    if (!target || follow || typeof IntersectionObserver === "undefined") return;
    const observer = new IntersectionObserver(([entry]) => setOutOfView(!entry.isIntersecting));
    observer.observe(target);
    return () => observer.disconnect();
  }, [anchorId, follow]);
  useEffect(() => {
    window.addEventListener("scroll", stopFollowing);
    window.addEventListener("wheel", stopFollowing);
    window.addEventListener("touchmove", stopFollowing);
    window.addEventListener("scrollend", finishProgrammaticScroll);
    return () => {
      window.removeEventListener("scroll", stopFollowing);
      window.removeEventListener("wheel", stopFollowing);
      window.removeEventListener("touchmove", stopFollowing);
      window.removeEventListener("scrollend", finishProgrammaticScroll);
      if (programmedTimer.current) window.clearTimeout(programmedTimer.current);
    };
  });
  const stopFollowing = () => {
    if (!programmed.current) {
      setFollow(false);
      setOutOfView(true);
    }
  };
  return (
    <div className="transcript-wrap">
      <ol className="transcript" aria-label="Bản chép bài học">
        {ids.map((id) => {
          const line = json<RenderLine>(session.lineJson(id, view, showVietnamese, positionMs));
          if (!line?.lineVisible) return null;
          const cueIndex = cues.findIndex((cue) => cue?.line_id === id);
          const state =
            place && place.speaking >= 0
              ? id === activeId
                ? "active"
                : cueIndex >= 0 && cueIndex < place.speaking
                  ? "past"
                  : ""
              : cueIndex === place?.last
                ? "just-read"
                : cueIndex === place?.next
                  ? "next"
                  : place && cueIndex >= 0 && cueIndex < place.last
                    ? "past"
                    : "";
          const seek = () => {
            if (selected()) return;
            const target = session.seekTargetMs(id, positionMs);
            if (target != null) {
              setFollow(true);
              onSeek(target);
            }
          };
          if (line.language === "vi-VN")
            return (
              <li key={id}>
                <div className="coach-line" lang="vi-VN">
                  {line.text}
                </div>
              </li>
            );
          return (
            <li key={id}>
              <div
                ref={id === anchorId ? activeRef : undefined}
                className={`line ${state}`}
                role="button"
                tabIndex={0}
                onKeyDown={(event) => event.key === "Enter" && seek()}
                onClick={seek}
                onContextMenu={(event) => {
                  event.preventDefault();
                  onCopy?.(line.text);
                }}
              >
                {state === "just-read" && <span className="sr-only">vừa đọc</span>}
                {state === "next" && <span className="next-hint">Tiếp theo</span>}
                {line.placeholder ? (
                  <span className="hidden-line" aria-label="Nội dung ẩn" />
                ) : (
                  <>
                    {line.showHanzi && (
                      <span className="hanzi">
                        {line.tokens?.length
                          ? line.tokens.map((token, index) => (
                              <span
                                className="character"
                                role="button"
                                tabIndex={0}
                                key={`${token.hanzi}-${index}`}
                                onClick={(event) => {
                                  event.stopPropagation();
                                  if (!selected()) onCharacter?.(token.hanzi);
                                }}
                                onKeyDown={(event) => event.key === "Enter" && onCharacter?.(token.hanzi)}
                              >
                                {token.hanzi}
                              </span>
                            ))
                          : line.text}
                      </span>
                    )}
                    {line.showPinyinLine && line.pinyin && <span className="pinyin">{line.pinyin}</span>}
                    {line.showTranslation && line.translation && <span className="meaning">{line.translation}</span>}
                  </>
                )}
              </div>
            </li>
          );
        })}
      </ol>
      {!follow && outOfView && anchorId && (
        <button className="return-current" onClick={returnToActive}>
          Về câu đang phát {activeRef.current && activeRef.current.getBoundingClientRect().top < 0 ? "↑" : "↓"}
        </button>
      )}
    </div>
  );
}
