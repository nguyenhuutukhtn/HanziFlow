import { useEffect, useState } from "react";
import { ChevronLeft, ChevronRight, Pause, Play, RotateCcw } from "lucide-react";
import { CharacterCard } from "../components/CharacterCard";
import { Button } from "../components/ui/button";
import { PortalContainerProvider } from "../components/ui/portal-container";
import { usePlayer } from "../player-context";
import { isPlayerShortcutTarget } from "../screens/Player";
import type { ViewMode } from "../settings";
import { Transcript } from "../Transcript";

export function PipPlayer({ win }: { win: Window }) {
  const player = usePlayer();
  const [char, setChar] = useState<string>();
  const pinyinOn = player.view === "HANZI_WITH_PINYIN";
  useEffect(() => {
    const key = (event: KeyboardEvent) => {
      if (isPlayerShortcutTarget(event)) return;
      if (event.code === "Space") {
        event.preventDefault();
        player.toggle();
      } else if (!event.shiftKey && event.key === "ArrowLeft") player.previous();
      else if (!event.shiftKey && event.key === "ArrowRight") player.next();
      else if (event.shiftKey && event.key === "ArrowLeft") player.seek(Math.max(0, player.position - 10000));
      else if (event.shiftKey && event.key === "ArrowRight") player.seek(player.position + 10000);
      else if (event.key.toLowerCase() === "r") player.replay();
    };
    win.document.addEventListener("keydown", key);
    return () => win.document.removeEventListener("keydown", key);
  }, [player, win]);
  useEffect(() => {
    const syncTheme = () => {
      win.document.documentElement.className = document.documentElement.className;
      win.document.documentElement.style.cssText = document.documentElement.style.cssText;
    };
    syncTheme();
    const observer = new MutationObserver(syncTheme);
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ["class", "style"] });
    return () => observer.disconnect();
  }, [win]);
  return (
    <PortalContainerProvider container={win.document.body}>
      <section className="floating-player">
        <div className="float-top">
          <span>{player.playing ? "ĐANG PHÁT" : "TẠM DỪNG"}</span>
          <strong>{player.session?.lessonTitle()}</strong>
        </div>
        <div className="float-text" aria-label="Bản chép trong cửa sổ nổi">
          {player.session && (
            <Transcript
              session={player.session}
              track={player.track}
              positionMs={player.position}
              view={player.view}
              showVietnamese={false}
              onSeek={player.seek}
              onCharacter={setChar}
              compact
            />
          )}
        </div>
        <div className="float-controls">
          <Button onClick={player.previous} aria-label="Câu trước">
            <ChevronLeft aria-hidden />
          </Button>
          <Button className="float-play" onClick={player.toggle} aria-label={player.playing ? "Tạm dừng" : "Nghe tiếp"}>
            {player.playing ? <Pause aria-hidden /> : <Play aria-hidden />}
          </Button>
          <Button onClick={player.next} aria-label="Câu sau">
            <ChevronRight aria-hidden />
          </Button>
          <Button onClick={player.replay} aria-label="Nghe lại câu">
            <RotateCcw aria-hidden />
          </Button>
          <Button
            aria-label="Hiển thị pinyin"
            aria-pressed={pinyinOn}
            onClick={() => player.setView((pinyinOn ? "HANZI_ONLY" : "HANZI_WITH_PINYIN") as ViewMode)}
          >
            拼
          </Button>
        </div>
        {char && player.session && (
          <CharacterCard
            session={player.session}
            char={char}
            close={() => setChar(undefined)}
            select={setChar}
            copy={(text) => void navigator.clipboard?.writeText(text)}
          />
        )}
      </section>
    </PortalContainerProvider>
  );
}
