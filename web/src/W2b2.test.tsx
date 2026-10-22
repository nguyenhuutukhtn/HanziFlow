import "@testing-library/jest-dom/vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { PortalContainerProvider } from "./components/ui/portal-container";
import { Popover, PopoverContent, PopoverTrigger } from "./components/ui/popover";
import { Settings } from "./screens/Settings";

describe("W2b2 component layer", () => {
  it("sets the dark HTML class from the Tối toggle and supports arrow-key selection", () => {
    const settings = { theme: "light" as const, viewMode: "HANZI_WITH_PINYIN" as const, hanziSize: 28 as const };
    render(
      <Settings
        settings={settings}
        setSettings={(next) => document.documentElement.classList.toggle("theme-dark", next.theme === "dark")}
      />,
    );
    const dark = screen.getByRole("radio", { name: "Tối" });
    fireEvent.keyDown(dark, { key: "ArrowRight" });
    fireEvent.click(dark);
    expect(document.documentElement).toHaveClass("theme-dark");
  });

  it("mounts Popover content into its PortalContainerContext", () => {
    const container = document.createElement("div");
    document.body.append(container);
    render(
      <PortalContainerProvider container={container}>
        <Popover>
          <PopoverTrigger>Mở</PopoverTrigger>
          <PopoverContent>Nội dung</PopoverContent>
        </Popover>
      </PortalContainerProvider>,
    );
    fireEvent.click(screen.getByRole("button", { name: "Mở" }));
    expect(container).toHaveTextContent("Nội dung");
  });
});
