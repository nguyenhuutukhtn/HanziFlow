import type { SettingsState, Theme, ViewMode } from "../settings";

const themes: { id: Theme; label: string }[] = [
  { id: "light", label: "Sáng" },
  { id: "paper", label: "Giấy" },
  { id: "dark", label: "Tối" },
];

const views: { id: ViewMode; label: string }[] = [
  { id: "HIDDEN", label: "Ẩn" },
  { id: "PINYIN_ONLY", label: "Pinyin" },
  { id: "HANZI_WITH_PINYIN", label: "Hán tự + pinyin" },
  { id: "HANZI_ONLY", label: "Hán tự" },
];

export function Settings({
  settings,
  setSettings,
}: {
  settings: SettingsState;
  setSettings: (settings: SettingsState) => void;
}) {
  return (
    <section className="page-section settings-page">
      <p className="eyebrow">CÁ NHÂN HÓA</p>
      <h1>Cài đặt</h1>
      <p className="subheading">Chọn cách học khiến bạn thấy dễ chịu nhất.</p>
      <div className="settings-grid">
        <section className="setting-card">
          <h2>Giao diện</h2>
          <p>Chọn màu sắc phù hợp với thời điểm học.</p>
          <div className="theme-options">
            {themes.map((theme) => (
              <button
                key={theme.id}
                className={`theme-option ${settings.theme === theme.id ? "selected" : ""}`}
                onClick={() => setSettings({ ...settings, theme: theme.id })}
              >
                <span className={`theme-preview ${theme.id}`}>Aa</span>
                <span>{theme.label}</span>
              </button>
            ))}
          </div>
        </section>
        <label className="setting-card row-setting">
          <span>
            <strong>Chế độ hiển thị mặc định</strong>
            <small>Áp dụng cho transcript khi mở bài mới.</small>
          </span>
          <select
            aria-label="Chế độ hiển thị mặc định"
            value={settings.viewMode}
            onChange={(event) => setSettings({ ...settings, viewMode: event.target.value as ViewMode })}
          >
            {views.map((view) => (
              <option key={view.id} value={view.id}>
                {view.label}
              </option>
            ))}
          </select>
        </label>
        <section className="setting-card row-setting">
          <span>
            <strong>Cỡ chữ Hán</strong>
            <small>Tùy chỉnh độ lớn của chữ Hán trong bài nghe.</small>
          </span>
          <div className="segmented">
            {(
              [
                { label: "Nhỏ", size: 22 },
                { label: "Vừa", size: 28 },
                { label: "Lớn", size: 34 },
              ] as const
            ).map((item) => (
              <button
                key={item.size}
                className={settings.hanziSize === item.size ? "active" : ""}
                onClick={() => setSettings({ ...settings, hanziSize: item.size })}
              >
                {item.label}
              </button>
            ))}
          </div>
        </section>
      </div>
    </section>
  );
}
