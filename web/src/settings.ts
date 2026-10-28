export type Theme = "light" | "paper" | "dark";

export type ViewMode = "HIDDEN" | "PINYIN_ONLY" | "HANZI_WITH_PINYIN" | "HANZI_ONLY";

export type HanziSize = 22 | 28 | 34;

export type SettingsState = { theme: Theme; viewMode: ViewMode; hanziSize: HanziSize };

const key = "hanziflow.settings";
const pipHintKey = "hanziflow.pipAlwaysOnTopHintDismissed";

const defaults: SettingsState = { theme: "light", viewMode: "HANZI_WITH_PINYIN", hanziSize: 28 };

export function readSettings(): SettingsState {
  try {
    const saved = JSON.parse(localStorage.getItem(key) ?? "{}") as Partial<SettingsState>;
    return {
      theme: saved.theme === "paper" || saved.theme === "dark" ? saved.theme : "light",
      viewMode: ["HIDDEN", "PINYIN_ONLY", "HANZI_WITH_PINYIN", "HANZI_ONLY"].includes(saved.viewMode ?? "")
        ? (saved.viewMode as ViewMode)
        : defaults.viewMode,
      hanziSize: [22, 28, 34].includes(saved.hanziSize ?? 0) ? (saved.hanziSize as HanziSize) : defaults.hanziSize,
    };
  } catch {
    return defaults;
  }
}
export function writeSettings(settings: SettingsState) {
  try {
    localStorage.setItem(key, JSON.stringify(settings));
  } catch {
    /* storage is optional */
  }
}

export function readPipAlwaysOnTopHintDismissed() {
  try {
    return localStorage.getItem(pipHintKey) === "true";
  } catch {
    return false;
  }
}

export function writePipAlwaysOnTopHintDismissed(dismissed: boolean) {
  try {
    localStorage.setItem(pipHintKey, String(dismissed));
  } catch {
    /* storage is optional */
  }
}
