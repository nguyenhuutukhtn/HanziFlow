import type { ReactNode } from "react";

export type Screen = "library" | "player" | "settings";

type NavId = Exclude<Screen, "player"> | "resume";

const nav: { id: NavId; label: string; icon: string }[] = [
  { id: "library", label: "Thư viện", icon: "▤" },
  { id: "resume", label: "Đang học", icon: "▶" },
  { id: "settings", label: "Cài đặt", icon: "⚙" },
];

export function Shell({
  screen,
  setScreen,
  lastLessonId,
  onResume,
  children,
}: {
  screen: Screen;
  setScreen: (screen: Screen) => void;
  lastLessonId?: string;
  onResume: () => void;
  children: ReactNode;
}) {
  const navigate = (id: NavId) => (id === "resume" ? onResume() : setScreen(id));
  return (
    <main className="app-shell">
      <aside className="sidebar" aria-label="Điều hướng chính">
        <Brand />
        <Nav screen={screen} navigate={navigate} resumeDisabled={!lastLessonId} />
      </aside>
      <header className="mobile-header">
        <Brand />
        <button className="header-settings" onClick={() => setScreen("settings")} aria-label="Cài đặt">
          ⚙
        </button>
      </header>
      <div className="main-content">{children}</div>
      <nav className="mobile-nav" aria-label="Điều hướng điện thoại">
        <Nav screen={screen} navigate={navigate} resumeDisabled={!lastLessonId} />
      </nav>
    </main>
  );
}
function Brand() {
  return (
    <div className="brand">
      <span className="brand-mark">汉</span>
      <strong>
        Hanzi<span>Flow</span>
      </strong>
    </div>
  );
}
function Nav({
  screen,
  navigate,
  resumeDisabled,
}: {
  screen: Screen;
  navigate: (id: NavId) => void;
  resumeDisabled: boolean;
}) {
  return (
    <>
      {nav.map((item) => (
        <button
          key={item.id}
          className={screen === item.id ? "active" : ""}
          disabled={item.id === "resume" && resumeDisabled}
          onClick={() => navigate(item.id)}
        >
          <span aria-hidden="true">{item.icon}</span>
          {item.label}
        </button>
      ))}
    </>
  );
}
