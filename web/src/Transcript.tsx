import { useEffect, useRef, useState, type KeyboardEvent, type MouseEvent } from "react";
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
  compact?: boolean;
};
let reportedMissingCuePlaceJson = false;

type ProgrammaticTarget = { current: () => number; target: number };

const scrollToVisibleCenter = (target: HTMLElement): ProgrammaticTarget | undefined => {
  const doc = target.ownerDocument;
  const targetRect = target.getBoundingClientRect();
  if (targetRect.height === 0) {
    target.scrollIntoView?.({ block: "center", behavior: "smooth" });
    return;
  }
  const scroller = target.closest<HTMLElement>(".float-text");
  if (scroller) {
    const scrollerRect = scroller.getBoundingClientRect();
    const top = scroller.scrollTop + targetRect.top - scrollerRect.top - (scrollerRect.height - targetRect.height) / 2;
    scroller.scrollTo({ top, behavior: "smooth" });
    return { current: () => scroller.scrollTop, target: top };
  }

  const win = doc.defaultView ?? window;
  const playerBar = doc.querySelector<HTMLElement>(".player-bar");
  const playerBarRect = playerBar?.getBoundingClientRect();
  const visibleBottom =
    playerBarRect && playerBarRect.top < win.innerHeight && playerBarRect.bottom > 0
      ? Math.max(0, playerBarRect.top)
      : win.innerHeight;
  const targetCenter = win.scrollY + targetRect.top + targetRect.height / 2;
  const top = targetCenter - visibleBottom / 2;
  win.scrollTo({ top, behavior: "smooth" });
  return { current: () => win.scrollY, target: top };
};

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
  compact = false,
}: Props) {
  const [follow, setFollow] = useState(true);
  const [outOfView, setOutOfView] = useState(false);
  const [peekedToken, setPeekedToken] = useState<string>();
  const programmed = useRef(false);
  const programmedTarget = useRef<ProgrammaticTarget>();
  const programmedTimer = useRef<number>();
  useEffect(() => () => window.clearTimeout(programmedTimer.current), []);
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
  const pinyinEnabled = view === "HANZI_WITH_PINYIN";
  const selected = () =>
    Boolean(activeRef.current?.ownerDocument.getSelection?.()?.toString() ?? window.getSelection?.()?.toString());
  const returnToActive = () => {
    programmed.current = true;
    setFollow(true);
    setOutOfView(false);
    if (activeRef.current) programmedTarget.current = scrollToVisibleCenter(activeRef.current);
    // Fallback when scrollend never fires (older Safari) or the target is out of scroll range.
    window.clearTimeout(programmedTimer.current);
    programmedTimer.current = window.setTimeout(() => {
      programmed.current = false;
      programmedTarget.current = undefined;
    }, 1500);
  };
  useEffect(() => {
    if (follow && activeRef.current) returnToActive();
  }, [anchorId]);
  useEffect(() => setPeekedToken(undefined), [anchorId]);
  useEffect(() => {
    if (pinyinEnabled) setPeekedToken(undefined);
  }, [pinyinEnabled]);
  useEffect(() => {
    const target = activeRef.current;
    if (!target || follow || typeof IntersectionObserver === "undefined") return;
    const observer = new IntersectionObserver(([entry]) => setOutOfView(!entry.isIntersecting));
    observer.observe(target);
    return () => observer.disconnect();
  }, [anchorId, follow]);
  useEffect(() => {
    const doc = activeRef.current?.ownerDocument ?? document;
    const targets = [doc, doc.defaultView ?? window];
    targets.forEach((target) => target.addEventListener("scroll", stopFollowingOnScroll, true));
    targets.forEach((target) => target.addEventListener("wheel", stopFollowing, true));
    targets.forEach((target) => target.addEventListener("touchmove", stopFollowing, true));
    targets.forEach((target) => target.addEventListener("scrollend", finishProgrammaticScroll, true));
    doc.addEventListener("keydown", stopFollowingOnScrollKey);
    doc.addEventListener("pointerdown", stopFollowingOnScrollbar, true);
    return () => {
      targets.forEach((target) => target.removeEventListener("scroll", stopFollowingOnScroll, true));
      targets.forEach((target) => target.removeEventListener("wheel", stopFollowing, true));
      targets.forEach((target) => target.removeEventListener("touchmove", stopFollowing, true));
      targets.forEach((target) => target.removeEventListener("scrollend", finishProgrammaticScroll, true));
      doc.removeEventListener("keydown", stopFollowingOnScrollKey);
      doc.removeEventListener("pointerdown", stopFollowingOnScrollbar, true);
    };
  });
  const stopFollowing = () => {
    setFollow(false);
    setOutOfView(true);
  };
  const stopFollowingOnScroll = () => {
    if (!programmed.current) stopFollowing();
  };
  const finishProgrammaticScroll = () => {
    const destination = programmedTarget.current;
    if (!destination || Math.abs(destination.current() - destination.target) <= 2) {
      programmed.current = false;
      programmedTarget.current = undefined;
    }
  };
  const stopFollowingOnScrollKey = (event: globalThis.KeyboardEvent) => {
    const target = event.target as HTMLElement | null;
    if (target?.closest("input, textarea, select, [contenteditable='true'], [role='slider']")) return;
    if (["ArrowUp", "ArrowDown", "PageUp", "PageDown", "Home", "End"].includes(event.key)) stopFollowing();
  };
  const stopFollowingOnScrollbar = (event: PointerEvent) => {
    const doc = activeRef.current?.ownerDocument ?? document;
    const scroller = activeRef.current?.closest<HTMLElement>(".float-text");
    if (scroller) {
      const rect = scroller.getBoundingClientRect();
      if (scroller.scrollHeight > scroller.clientHeight && event.clientX >= rect.right - 16) stopFollowing();
    } else if (event.clientX >= doc.documentElement.clientWidth) {
      stopFollowing();
    }
  };
  return (
    <div className={`transcript-wrap${compact ? " compact-transcript" : ""}`} data-follow={follow ? "on" : "off"}>
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
          if (line.language === "vi-VN") {
            if (!showVietnamese) return null;
            return (
              <li key={id}>
                <div className="coach-line" lang="vi-VN">
                  {line.text}
                </div>
              </li>
            );
          }
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
                          ? line.tokens.map((token, index) => {
                              const tokenId = `${id}-${index}`;
                              const showPinyin =
                                Boolean(token.pinyin) && ((compact && pinyinEnabled) || peekedToken === tokenId);
                              const openCharacter = (event: MouseEvent | KeyboardEvent) => {
                                event.stopPropagation();
                                if (event.currentTarget.ownerDocument.getSelection?.()?.toString()) return;
                                if (!pinyinEnabled && token.pinyin && peekedToken !== tokenId) {
                                  setPeekedToken(tokenId);
                                  return;
                                }
                                onCharacter?.(token.hanzi);
                              };
                              return (
                                <span
                                  className="character"
                                  role="button"
                                  tabIndex={0}
                                  key={tokenId}
                                  onClick={openCharacter}
                                  onKeyDown={(event) => event.key === "Enter" && openCharacter(event)}
                                >
                                  {compact || showPinyin ? (
                                    <ruby>
                                      {token.hanzi}
                                      {showPinyin && <rt>{token.pinyin}</rt>}
                                    </ruby>
                                  ) : (
                                    token.hanzi
                                  )}
                                </span>
                              );
                            })
                          : line.text}
                      </span>
                    )}
                    {line.showPinyinLine && line.pinyin && <span className="pinyin">{line.pinyin}</span>}
                    {showVietnamese && line.showTranslation && line.translation && (
                      <span className="meaning">{line.translation}</span>
                    )}
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
