import { useEffect } from "react";
import { PortalContainerProvider } from "../components/ui/portal-container";
import { json, type Cue, type CuePlace } from "../facade";
import { usePlayer } from "../player-context";
import { isPlayerShortcutTarget } from "../screens/Player";

export function PipPlayer({ win }: { win: Window }) {
  const player = usePlayer();
  const place = player.session ? json<CuePlace>(player.session.cuePlaceJson(player.position)) : null;
  const index = place && (place.speaking >= 0 ? place.speaking : place.last);
  const cue = index != null && index >= 0 && player.session ? json<Cue>(player.session.cueJson(index)) : null;
  const next = place && place.next >= 0 && player.session ? json<Cue>(player.session.cueJson(place.next)) : null;
  const line =
    cue && player.session
      ? json<{ text: string; pinyin: string | null; translation: string | null }>(
          player.session.lineJson(cue.line_id, player.view, player.vietnamese, player.position),
        )
      : null;
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
    win.document.documentElement.className = document.documentElement.className;
    win.document.documentElement.style.cssText = document.documentElement.style.cssText;
  }, [win, player.view]);
  return (
    <PortalContainerProvider container={win.document.body}>
      <section className="floating-player">
        <div className="float-top">
          <span>{player.playing ? "ĐANG PHÁT" : "TẠM DỪNG"}</span>
        </div>
        <div className="float-text">
          {place?.speaking === -1 && <small>vừa đọc</small>}
          <strong>{line?.text ?? "…"}</strong>
          {line?.pinyin && <span>{line.pinyin}</span>}
          {player.vietnamese && line?.translation && <small>{line.translation}</small>}
          {next && place?.speaking === -1 && <small>Tiếp theo</small>}
        </div>
        <div className="float-controls">
          <button onClick={player.previous}>‹</button>
          <button className="float-play" onClick={player.toggle}>
            {player.playing ? "Tạm dừng" : "Nghe tiếp"}
          </button>
          <button onClick={player.next}>›</button>
          <button onClick={player.replay}>Nghe lại câu</button>
        </div>
      </section>
    </PortalContainerProvider>
  );
}
