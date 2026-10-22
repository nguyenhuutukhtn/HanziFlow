import { useEffect, useRef, useState } from "react";

import { createPortal } from "react-dom";

import { json, shared, type Cue, type Session, type Track } from "../facade";

import { Transcript } from "../Transcript";

import { CharacterCard } from "../components/CharacterCard";

import type { ViewMode } from "../settings";

const base = () => import.meta.env.VITE_CONTENT_BASE_URL as string | undefined;

const url = (path: string) => `${base()?.replace(/\/$/, "")}/${path}`;

const getJson = async <T,>(path: string) => (await fetch(url(path))).json() as Promise<T>;

type Pip = { requestWindow(x: { width: number; height: number }): Promise<Window> };

const pipApi = () => (window as Window & { documentPictureInPicture?: Pip }).documentPictureInPicture;

const copy = (text: string, win: Window) => void win.navigator.clipboard?.writeText(text).catch(() => undefined);

type Props = { catalog: string; lessonId: string; initialView: ViewMode; onBack: () => void };

export function Player({ catalog, lessonId, initialView, onBack }: Props) {
  const audio = useRef<HTMLAudioElement>(null);

  const [track, setTrack] = useState("guided");

  const [session, setSession] = useState<Session>();

  const [timeline, setTimeline] = useState<string>();

  const [position, setPosition] = useState(0);

  const [playing, setPlaying] = useState(false);

  const [view, setView] = useState(initialView);

  const [vi, setVi] = useState(true);

  const [speed, setSpeed] = useState(1);

  const [pip, setPip] = useState<{ win: Window; root: HTMLElement }>();

  const [char, setChar] = useState<string>();

  useEffect(() => {
    const load = async () => {
      const lesson = JSON.parse(catalog).lessons.find((item: { id: string }) => item.id === lessonId);

      const [tl, ref, chars] = await Promise.all([
        fetch(url(`${lesson.base_url}${lesson.tracks[track].timeline.file}`)).then((response) => response.text()),

        getJson<string | object>(`${lesson.base_url}${lesson.files.reference.file}`),

        lesson.files.characters
          ? getJson<string | object>(`${lesson.base_url}${lesson.files.characters.file}`).catch(() => null)
          : null,
      ]);

      setTimeline(tl);

      setSession(
        new (shared()!.com.hanziflow.shared.web.LessonSessionJs)(
          catalog,
          lessonId,
          tl,

          typeof ref === "string" ? ref : JSON.stringify(ref),
          chars ? (typeof chars === "string" ? chars : JSON.stringify(chars)) : null,
          track,
        ),
      );
    };

    void load().catch(() => setSession(undefined));
  }, [catalog, lessonId, track]);

  useEffect(() => {
    if (audio.current) {
      audio.current.playbackRate = speed;
      audio.current.preservesPitch = true;
    }
  }, [speed, session]);

  const seek = (time: number) => {
    if (audio.current) audio.current.currentTime = time / 1000;
    setPosition(time);
  };

  const toggle = () => {
    const item = audio.current;
    if (item) void item[playing ? "pause" : "play"]();
  };

  const previous = () => {
    const time = session?.previousCueStartMs(position);
    if (time != null) seek(time);
  };

  const next = () => {
    const time = session?.nextCueStartMs(position);
    if (time != null) seek(time);
  };

  const replay = () => {
    const cue = session && json<Cue>(session.cueJson(session.activeCueIndex(position)));
    if (cue) seek(cue.start_ms);
  };

  useEffect(() => {
    if (!session || !("mediaSession" in navigator)) return;

    const handlers: [MediaSessionAction, MediaSessionActionHandler][] = [
      ["play", toggle],
      ["pause", toggle],
      ["seekbackward", () => seek(Math.max(0, position - 10000))],
      ["seekforward", () => seek(position + 10000)],
      ["previoustrack", previous],
      ["nexttrack", next],
    ];

    navigator.mediaSession.metadata = new MediaMetadata({
      title: session.lessonTitle(),
      artist: json<Track[]>(session.tracksJson())?.find((item) => item.name === track)?.display_name ?? track,
    });

    handlers.forEach(([action, handler]) => navigator.mediaSession.setActionHandler(action, handler));

    return () => handlers.forEach(([action]) => navigator.mediaSession.setActionHandler(action, null));
  }, [session, position, playing, track]);

  if (!session || !timeline)
    return (
      <section className="page-section">
        <p>Đang tải bài học…</p>
      </section>
    );

  const tracks = json<Track[]>(session.tracksJson()) ?? [];

  const changeTrack = (name: string) => {
    const old = timeline;
    const current = audio.current?.currentTime ?? 0;
    setTrack(name);
    fetch(url(session.timelinePath(name)!))
      .then((response) => response.text())
      .then((text) => seek(shared()!.com.hanziflow.shared.web.TrackSwitchJs.positionFor(old, text, current * 1000)));
  };

  const openPip = async () => {
    const api = pipApi();
    if (!api) return;
    const win = await api.requestWindow({ width: 420, height: 260 });
    const root = win.document.body.appendChild(win.document.createElement("div"));
    win.addEventListener("pagehide", () => setPip(undefined), { once: true });
    setPip({ win, root });
  };

  const body = (win: Window) => (
    <PlayerBody
      session={session}
      track={track}
      position={position}
      playing={playing}
      view={view}
      vi={vi}
      speed={speed}
      win={win}
      seek={seek}
      toggle={toggle}
      previous={previous}
      next={next}
      replay={replay}
      setSpeed={setSpeed}
      char={char}
      setChar={setChar}
    />
  );

  return (
    <section className="player-page">
      <header className="player-top">
        <button onClick={onBack}>← Thư viện</button>
        <h1>{session.lessonTitle()}</h1>
      </header>
      <audio
        ref={audio}
        src={url(session.audioPath(track)!)}
        onTimeUpdate={(event) => setPosition(event.currentTarget.currentTime * 1000)}
        onPlay={() => setPlaying(true)}
        onPause={() => setPlaying(false)}
      />
      <div className="controls">
        <label>
          Track{" "}
          <select value={track} onChange={(event) => changeTrack(event.target.value)}>
            {tracks.map((item) => (
              <option key={item.name} value={item.name}>
                {item.display_name}
              </option>
            ))}
          </select>
        </label>
        {pipApi() ? <button onClick={() => void openPip()}>Cửa sổ nổi</button> : <span>Cần Chrome/Edge 116+</span>}
      </div>
      <div className="views">
        <label>
          Hiển thị{" "}
          <select value={view} onChange={(event) => setView(event.target.value as ViewMode)}>
            <option value="HIDDEN">Ẩn</option>
            <option value="HANZI_WITH_PINYIN">Hán + pinyin</option>
            <option value="HANZI_ONLY">Hán</option>
            <option value="PINYIN_ONLY">Pinyin</option>
          </select>
        </label>
        <label>
          <input type="checkbox" checked={vi} onChange={(event) => setVi(event.target.checked)} /> Tiếng Việt
        </label>
      </div>
      {body(window)}
      {pip && createPortal(<div className="pip-shell">{body(pip.win)}</div>, pip.root)}
    </section>
  );
}

type BodyProps = {
  session: Session;
  track: string;
  position: number;
  playing: boolean;
  view: ViewMode;
  vi: boolean;
  speed: number;
  win: Window;
  seek: (time: number) => void;
  toggle: () => void;
  previous: () => void;
  next: () => void;
  replay: () => void;
  setSpeed: (speed: number) => void;
  char?: string;
  setChar: (char?: string) => void;
};

function PlayerBody(props: BodyProps) {
  useEffect(() => {
    const handler = (event: KeyboardEvent) => {
      if ((event.target as HTMLElement)?.matches("input,select")) return;
      if (event.code === "Space") {
        event.preventDefault();
        props.toggle();
      }
      if (event.key === "ArrowLeft") props.seek(Math.max(0, props.position - 10000));
      if (event.key === "ArrowRight") props.seek(props.position + 10000);
      if (event.key.toLowerCase() === "r") props.replay();
    };
    props.win.addEventListener("keydown", handler);
    return () => props.win.removeEventListener("keydown", handler);
  }, [props]);
  return (
    <>
      <div className="controls">
        <button onClick={() => props.seek(Math.max(0, props.position - 10000))}>−10s</button>
        <button onClick={props.toggle}>{props.playing ? "Tạm dừng" : "Phát"}</button>
        <button onClick={() => props.seek(props.position + 10000)}>+10s</button>
        <button onClick={props.previous}>Câu trước</button>
        <button onClick={props.next}>Câu sau</button>
        <button onClick={props.replay}>Nghe lại câu</button>
        <label>
          Tốc độ{" "}
          <select
            aria-label="Tốc độ"
            value={props.speed}
            onChange={(event) => props.setSpeed(Number(event.target.value))}
          >
            {[1, 0.8, 0.7].map((item) => (
              <option key={item} value={item}>
                {item}×
              </option>
            ))}
          </select>
        </label>
      </div>
      <Transcript
        session={props.session}
        track={props.track}
        positionMs={props.position}
        view={props.view}
        showVietnamese={props.vi}
        onSeek={props.seek}
        targetWindow={props.win}
        onCharacter={props.setChar}
        onCopy={(text) => copy(text, props.win)}
      />
      {props.char && (
        <CharacterCard
          session={props.session}
          char={props.char}
          close={() => props.setChar(undefined)}
          select={props.setChar}
          copy={(text) => copy(text, props.win)}
        />
      )}
    </>
  );
}
