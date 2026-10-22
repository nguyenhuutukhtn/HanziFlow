import { useEffect, useState } from "react";

import { json, shared } from "./facade";

import { Shell, type Screen } from "./app/Shell";

import { Library, type Lesson } from "./screens/Library";

import { Player } from "./screens/Player";

import { Settings } from "./screens/Settings";

import { readSettings, type SettingsState, writeSettings } from "./settings";

const base = () => import.meta.env.VITE_CONTENT_BASE_URL as string | undefined;

const url = (path: string) => `${base()?.replace(/\/$/, "")}/${path}`;

export default function App() {
  const [catalog, setCatalog] = useState<string>();

  const [lessons, setLessons] = useState<Lesson[]>([]);

  const [screen, setScreen] = useState<Screen>("library");

  const [lessonId, setLessonId] = useState<string>();

  const [lastLessonId, setLastLessonId] = useState<string>();

  const [error, setError] = useState<string>();

  const [settings, setSettings] = useState<SettingsState>(readSettings);

  useEffect(() => {
    document.documentElement.classList.remove("theme-paper", "theme-dark");

    if (settings.theme !== "light") document.documentElement.classList.add(`theme-${settings.theme}`);

    document.documentElement.style.setProperty("--hanzi-size", `${settings.hanziSize}px`);

    writeSettings(settings);
  }, [settings]);

  useEffect(() => {
    if (!base()) {
      setError("Thiếu VITE_CONTENT_BASE_URL.");
      return;
    }
    fetch(`${url("catalog.json")}?v=${Date.now()}`)
      .then((response) => response.text())
      .then((text) => {
        setCatalog(text);

        setLessons(json<Lesson[]>(shared()?.com.hanziflow.shared.web.CatalogJs.lessonsJson(text) ?? null) ?? []);
      })
      .catch(() => setError("Không tải được thư viện bài học."));
  }, []);

  const openLesson = (id: string) => {
    setLessonId(id);
    setLastLessonId(id);
    setScreen("player");
  };

  if (error)
    return (
      <main className="loading">
        <p role="alert">{error}</p>
      </main>
    );

  if (!catalog)
    return (
      <main className="loading">
        <p>Đang tải thư viện…</p>
      </main>
    );

  return (
    <Shell
      screen={screen}
      setScreen={setScreen}
      lastLessonId={lastLessonId}
      onResume={() => lastLessonId && openLesson(lastLessonId)}
    >
      {screen === "library" && <Library lessons={lessons} onOpen={openLesson} />}
      {screen === "settings" && <Settings settings={settings} setSettings={setSettings} />}
      {screen === "player" && lessonId && (
        <Player
          catalog={catalog}
          lessonId={lessonId}
          initialView={settings.viewMode}
          onBack={() => setScreen("library")}
        />
      )}
    </Shell>
  );
}
