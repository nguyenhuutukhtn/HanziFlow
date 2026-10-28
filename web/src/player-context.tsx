import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from "react";
import { catalogLesson, contentUrl, fetchJson, fetchText } from "./content";
import { json, shared, type Cue, type CuePlace, type Section, type Session, type Track } from "./facade";
import type { ViewMode } from "./settings";

const savedTrack = (lessonId: string) => {
  try {
    return localStorage.getItem(`hanziflow-track:${lessonId}`);
  } catch {
    return null;
  }
};

const firstTrack = (catalog: string, lessonId: string) => {
  const lesson = catalogLesson(catalog, lessonId);
  const names = lesson ? Object.keys(lesson.tracks) : [];
  const saved = savedTrack(lessonId);
  return saved && names.includes(saved) ? saved : (names[0] ?? "guided");
};

type PlayerState = {
  session?: Session;
  track: string;
  tracks: Track[];
  position: number;
  duration: number;
  playing: boolean;
  speed: number;
  view: ViewMode;
  vietnamese: boolean;
  sections: Section[];
  audio: React.RefObject<HTMLAudioElement>;
  seek: (ms: number) => void;
  toggle: () => void;
  previous: () => void;
  next: () => void;
  replay: () => void;
  setTrack: (track: string) => void;
  setSpeed: (speed: number) => void;
  setView: (view: ViewMode) => void;
  setVietnamese: (value: boolean) => void;
};
const Context = createContext<PlayerState | null>(null);
export const usePlayer = () => {
  const player = useContext(Context);
  if (!player) throw new Error("usePlayer requires PlayerProvider");
  return player;
};

export function PlayerProvider({
  catalog,
  lessonId,
  initialView,
  children,
}: {
  catalog: string;
  lessonId: string;
  initialView: ViewMode;
  children: ReactNode;
}) {
  const audio = useRef<HTMLAudioElement>(null);
  const [track, setTrack] = useState(() => firstTrack(catalog, lessonId));
  const [session, setSession] = useState<Session>();
  const [timeline, setTimeline] = useState("");
  const [position, setPosition] = useState(0);
  const [duration, setDuration] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [speed, setSpeed] = useState(1);
  const [view, setView] = useState<ViewMode>(initialView);
  const [vietnamese, setVietnamese] = useState(true);
  const pendingSeek = useRef<{
    lessonId: string;
    track: string;
    position: number;
    playing: boolean;
    speed: number;
  }>();
  const trackChange = useRef(0);
  const live = useRef({ playing, speed });
  live.current = { playing, speed };
  useEffect(() => {
    pendingSeek.current = undefined;
    trackChange.current += 1;
  }, [lessonId]);
  useEffect(() => {
    const lesson = catalogLesson(catalog, lessonId);
    if (!lesson || !lesson.tracks[track]) return;
    let stale = false;
    void Promise.all([
      fetchText(`${lesson.base_url}${lesson.tracks[track].timeline.file}`),
      fetchJson<string | object>(`${lesson.base_url}${lesson.files.reference.file}`),
      lesson.files.characters
        ? fetchJson<string | object>(`${lesson.base_url}${lesson.files.characters.file}`).catch(() => null)
        : null,
    ])
      .then(([tl, reference, characters]) => {
        if (stale) return;
        setTimeline(tl);
        setSession(
          new (shared()!.com.hanziflow.shared.web.LessonSessionJs)(
            catalog,
            lessonId,
            tl,
            typeof reference === "string" ? reference : JSON.stringify(reference),
            characters ? (typeof characters === "string" ? characters : JSON.stringify(characters)) : null,
            track,
          ),
        );
      })
      .catch(() => !stale && setSession(undefined));
    return () => {
      stale = true;
    };
  }, [catalog, lessonId, track]);
  useEffect(() => {
    if (audio.current) {
      audio.current.playbackRate = speed;
      audio.current.preservesPitch = true;
    }
  }, [speed, session]);
  const seek = (ms: number) => {
    if (audio.current) audio.current.currentTime = ms / 1000;
    setPosition(ms);
  };
  const toggle = () => {
    const element = audio.current;
    if (element) void element[playing ? "pause" : "play"]();
  };
  const previous = () => {
    const value = session?.previousCueStartMs(position);
    if (value != null) seek(value);
  };
  const next = () => {
    const value = session?.nextCueStartMs(position);
    if (value != null) seek(value);
  };
  const replay = () => {
    const place = session && json<CuePlace>(session.cuePlaceJson(position));
    const cueIndex = place ? (place.speaking >= 0 ? place.speaking : place.last) : -1;
    const cue = session && cueIndex >= 0 ? json<Cue>(session.cueJson(cueIndex)) : null;
    if (cue) seek(cue.start_ms);
  };
  const tracks = session ? (json<Track[]>(session.tracksJson()) ?? []) : [];
  const sections = session ? (json<Section[]>(session.sectionsJson?.() ?? null) ?? []) : [];
  const changeTrack = (name: string) => {
    if (name === track || !session) return;
    pendingSeek.current = undefined;
    const request = ++trackChange.current;
    const old = timeline;
    const time = audio.current?.currentTime ?? 0;
    fetchText(session.timelinePath(name)!).then((text) => {
      if (request !== trackChange.current) return;
      const lesson = catalogLesson(catalog, lessonId);
      const modeTracks = lesson && !lesson.tracks.guided;
      const nextPosition = modeTracks
        ? 0
        : shared()!.com.hanziflow.shared.web.TrackSwitchJs.positionFor(old, text, time * 1000);
      // Read play state and speed now, not at click time: the user may change them while the timeline loads.
      pendingSeek.current = { lessonId, track: name, position: nextPosition, ...live.current };
      setPosition(nextPosition);
      setTrack(name);
      try {
        localStorage.setItem(`hanziflow-track:${lessonId}`, name);
      } catch {
        // Storage is optional (private browsing and embedded webviews can reject it).
      }
    });
  };
  const mediaReady = (element: HTMLAudioElement) => {
    setDuration(element.duration * 1000);
    const pending = pendingSeek.current;
    if (!pending || pending.lessonId !== lessonId || pending.track !== track) return;
    pendingSeek.current = undefined;
    element.currentTime = pending.position / 1000;
    element.playbackRate = pending.speed;
    element.preservesPitch = true;
    setPosition(pending.position);
    if (pending.playing) void element.play();
  };
  const value: PlayerState = {
    session,
    track,
    tracks,
    position,
    duration,
    playing,
    speed,
    view,
    vietnamese,
    sections,
    audio,
    seek,
    toggle,
    previous,
    next,
    replay,
    setTrack: changeTrack,
    setSpeed,
    setView,
    setVietnamese,
  };
  useEffect(() => {
    if (!session || !("mediaSession" in navigator)) return;
    const media = navigator.mediaSession;
    const handlers: [MediaSessionAction, MediaSessionActionHandler][] = [
      ["play", toggle],
      ["pause", toggle],
      ["seekbackward", () => seek(Math.max(0, position - 10000))],
      ["seekforward", () => seek(position + 10000)],
      ["previoustrack", previous],
      ["nexttrack", next],
    ];
    handlers.forEach(([action, handler]) => media.setActionHandler(action, handler));
    return () => handlers.forEach(([action]) => media.setActionHandler(action, null));
  }, [session, position, playing, track]);
  return (
    <Context.Provider value={value}>
      {session && (
        <audio
          ref={audio}
          src={contentUrl(session.audioPath(track)!)}
          onTimeUpdate={(event) => setPosition(event.currentTarget.currentTime * 1000)}
          onLoadedMetadata={(event) => mediaReady(event.currentTarget)}
          onCanPlay={(event) => mediaReady(event.currentTarget)}
          onPlay={() => setPlaying(true)}
          onPause={() => setPlaying(false)}
        />
      )}
      {children}
    </Context.Provider>
  );
}
