import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { CharacterCard } from "../components/CharacterCard";
import { Slider } from "../components/ui/slider";
import { Switch } from "../components/ui/switch";
import { ToggleGroup, ToggleGroupItem } from "../components/ui/toggle-group";
import { Tooltip, TooltipContent, TooltipTrigger } from "../components/ui/tooltip";
import { PlayerProvider, usePlayer } from "../player-context";
import { Transcript } from "../Transcript";
import { PipPlayer } from "../pip/PipPlayer";
import type { ViewMode } from "../settings";

type Props = { catalog: string; lessonId: string; initialView: ViewMode; onBack: () => void };
export function isPlayerShortcutTarget(event: Pick<KeyboardEvent, "target" | "metaKey" | "ctrlKey" | "altKey">) {
  return (
    event.metaKey ||
    event.ctrlKey ||
    event.altKey ||
    (event.target instanceof HTMLElement &&
      event.target.matches("input,textarea,select,[role=combobox],[role=slider],[contenteditable]"))
  );
}
export function Player(props: Props) {
  return (
    <PlayerProvider
      key={props.lessonId}
      catalog={props.catalog}
      lessonId={props.lessonId}
      initialView={props.initialView}
    >
      <PlayerScreen onBack={props.onBack} />
    </PlayerProvider>
  );
}
function clock(ms: number) {
  const seconds = Math.max(0, Math.floor(ms / 1000));
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, "0")}`;
}
export function PlayerScreen({ onBack }: { onBack: () => void }) {
  const player = usePlayer();
  const [char, setChar] = useState<string>();
  const [compact, setCompact] = useState(() => window.innerWidth <= 720);
  const [pip, setPip] = useState<Window>();
  const openPip = async () => {
    const api = (
      window as Window & {
        documentPictureInPicture?: { requestWindow: (size: { width: number; height: number }) => Promise<Window> };
      }
    ).documentPictureInPicture;
    if (!api) return;
    const win = await api.requestWindow({ width: 360, height: 200 });
    // The PiP document starts empty. CSS links resolve against about:blank there, so copy
    // readable CSSOM rules into styles and use an absolute URL only for unreadable sheets.
    document.querySelectorAll("style").forEach((node) => win.document.head.append(node.cloneNode(true)));
    Array.from(document.styleSheets).forEach((sheet: CSSStyleSheet) => {
      if (sheet.ownerNode?.nodeName === "STYLE") return;
      try {
        const css = Array.from(sheet.cssRules, (rule: CSSRule) => rule.cssText).join("\n");
        const style = win.document.createElement("style");
        style.textContent = css;
        win.document.head.append(style);
      } catch {
        if (!sheet.href) return;
        const link = win.document.createElement("link");
        link.rel = "stylesheet";
        link.href = new URL(sheet.href, document.baseURI).href;
        win.document.head.append(link);
      }
    });
    win.addEventListener("pagehide", () => setPip(undefined), { once: true });
    setPip(win);
  };
  useEffect(() => {
    const resize = () => setCompact(window.innerWidth <= 720);
    window.addEventListener("resize", resize);
    return () => window.removeEventListener("resize", resize);
  }, []);
  useEffect(() => {
    const key = (event: KeyboardEvent) => {
      if (isPlayerShortcutTarget(event)) return;
      if (event.code === "Space") {
        event.preventDefault();
        player.toggle();
      }
      if (!event.shiftKey && event.key === "ArrowLeft") {
        event.preventDefault();
        player.previous();
      }
      if (!event.shiftKey && event.key === "ArrowRight") {
        event.preventDefault();
        player.next();
      }
      if (event.shiftKey && event.key === "ArrowLeft") player.seek(Math.max(0, player.position - 10000));
      if (event.shiftKey && event.key === "ArrowRight") player.seek(player.position + 10000);
      if (event.key.toLowerCase() === "r") player.replay();
    };
    window.addEventListener("keydown", key);
    return () => window.removeEventListener("keydown", key);
  }, [player]);
  if (!player.session)
    return (
      <section className="page-section">
        <p>Đang tải bài học…</p>
      </section>
    );
  return (
    <section className="player-page">
      <header className="player-top">
        <button className="back-link" onClick={onBack}>
          ← Thư viện
        </button>
        <div>
          <p className="eyebrow">BÀI NGHE</p>
          <h1>{player.session.lessonTitle()}</h1>
        </div>
      </header>
      <ToggleGroup
        className="track-switch"
        type="single"
        value={player.track}
        onValueChange={(value) => player.setTrack(value)}
      >
        {player.tracks.map((track) => (
          <ToggleGroupItem key={track.name} value={track.name}>
            {track.display_name}
          </ToggleGroupItem>
        ))}
      </ToggleGroup>
      <div className="player-layout">
        <main className="lesson-transcript">
          <div className="transcript-toolbar">
            <ToggleGroup
              aria-label="Hiển thị"
              className="segmented"
              type="single"
              value={player.view}
              onValueChange={(value) => value && player.setView(value as ViewMode)}
            >
              {[
                ["HIDDEN", "Ẩn"],
                ["PINYIN_ONLY", "Pinyin"],
                ["HANZI_WITH_PINYIN", "Hán tự + pinyin"],
                ["HANZI_ONLY", "Hán tự"],
              ].map(([value, label]) => (
                <ToggleGroupItem key={value} value={value}>
                  {label}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>
            <label className="vi-toggle">
              Tiếng Việt <Switch checked={player.vietnamese} onCheckedChange={player.setVietnamese} />
            </label>
          </div>
          <Transcript
            session={player.session}
            track={player.track}
            positionMs={player.position}
            view={player.view}
            showVietnamese={player.vietnamese}
            onSeek={player.seek}
            onPrevious={player.previous}
            onNext={player.next}
            onCharacter={setChar}
            onCopy={(text) => void navigator.clipboard?.writeText(text)}
          />
          {char && (
            <CharacterCard
              session={player.session}
              char={char}
              close={() => setChar(undefined)}
              select={setChar}
              copy={(text) => void navigator.clipboard?.writeText(text)}
              compact={compact}
            />
          )}
        </main>
        <aside className="player-side">
          <section className="shortcut-card">
            <strong>Phím tắt</strong>
            <p>Space · Phát / tạm dừng</p>
            <p>← → · Câu trước / sau</p>
            <p>Shift + ← → · ±10 giây</p>
            <p>R · Nghe lại câu</p>
          </section>
        </aside>
      </div>
      <footer className="player-bar">
        <div className="seek-row">
          <span>{clock(player.position)}</span>
          <div className="seek">
            <Slider
              aria-label="Thanh tiến trình"
              min={0}
              max={Math.max(player.duration, 1)}
              step={5000}
              aria-valuetext={`${clock(player.position)} / ${clock(player.duration)}`}
              value={[Math.min(player.position, player.duration || player.position)]}
              onKeyDown={(event) => {
                if (event.key === "PageUp") {
                  event.preventDefault();
                  player.seek(Math.min(player.duration, player.position + 30000));
                } else if (event.key === "PageDown") {
                  event.preventDefault();
                  player.seek(Math.max(0, Math.min(player.duration, player.position) - 30000));
                }
              }}
              onValueChange={([value]) => player.seek(value)}
            />
            {player.sections.map((section) => (
              <Tooltip key={section.id}>
                <TooltipTrigger asChild>
                  <button
                    className="section-marker"
                    style={{ left: `${player.duration ? (section.start_ms / player.duration) * 100 : 0}%` }}
                    onClick={() => player.seek(section.start_ms)}
                    aria-label={section.title}
                  />
                </TooltipTrigger>
                <TooltipContent>{section.title}</TooltipContent>
              </Tooltip>
            ))}
          </div>
          <span>{clock(player.duration)}</span>
        </div>
        <div className="control-row">
          <button onClick={player.previous} aria-label="Câu trước">
            ‹
          </button>
          <button className="play-control" onClick={player.toggle}>
            {player.playing ? "Tạm dừng" : "Phát"}
          </button>
          <button onClick={player.next} aria-label="Câu sau">
            ›
          </button>
          <button onClick={player.replay}>Nghe lại câu</button>
          <ToggleGroup
            aria-label="Tốc độ"
            className="speed-control"
            type="single"
            value={String(player.speed)}
            onValueChange={(value) => value && player.setSpeed(Number(value))}
          >
            {[1, 0.8, 0.7].map((speed) => (
              <ToggleGroupItem key={speed} value={String(speed)}>
                {speed}×
              </ToggleGroupItem>
            ))}
          </ToggleGroup>
          <button onClick={() => void openPip()} disabled={!("documentPictureInPicture" in window)}>
            Cửa sổ nổi
          </button>
        </div>
      </footer>
      {pip && createPortal(<PipPlayer win={pip} />, pip.document.body)}
    </section>
  );
}
