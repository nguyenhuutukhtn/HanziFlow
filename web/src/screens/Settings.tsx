import type { SettingsState, Theme, ViewMode } from "../settings";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

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
          <ToggleGroup
            className="theme-options"
            type="single"
            value={settings.theme}
            onValueChange={(theme) => theme && setSettings({ ...settings, theme: theme as Theme })}
          >
            {themes.map((theme) => (
              <ToggleGroupItem
                key={theme.id}
                className={`theme-option ${settings.theme === theme.id ? "selected" : ""}`}
                value={theme.id}
                aria-label={theme.label}
              >
                <span className={`theme-preview ${theme.id}`}>Aa</span>
                <span>{theme.label}</span>
              </ToggleGroupItem>
            ))}
          </ToggleGroup>
        </section>
        <label className="setting-card row-setting">
          <span>
            <strong>Chế độ hiển thị mặc định</strong>
            <small>Áp dụng cho transcript khi mở bài mới.</small>
          </span>
          <Select
            value={settings.viewMode}
            onValueChange={(viewMode) => setSettings({ ...settings, viewMode: viewMode as ViewMode })}
          >
            <SelectTrigger aria-label="Chế độ hiển thị mặc định">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {views.map((view) => (
                <SelectItem key={view.id} value={view.id}>
                  {view.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </label>
        <section className="setting-card row-setting">
          <span>
            <strong>Cỡ chữ Hán</strong>
            <small>Tùy chỉnh độ lớn của chữ Hán trong bài nghe.</small>
          </span>
          <ToggleGroup
            className="segmented"
            type="single"
            value={String(settings.hanziSize)}
            onValueChange={(value) =>
              value && setSettings({ ...settings, hanziSize: Number(value) as SettingsState["hanziSize"] })
            }
          >
            {(
              [
                { label: "Nhỏ", size: 22 },
                { label: "Vừa", size: 28 },
                { label: "Lớn", size: 34 },
              ] as const
            ).map((item) => (
              <ToggleGroupItem
                key={item.size}
                className={settings.hanziSize === item.size ? "active" : ""}
                value={String(item.size)}
              >
                {item.label}
              </ToggleGroupItem>
            ))}
          </ToggleGroup>
        </section>
      </div>
    </section>
  );
}
