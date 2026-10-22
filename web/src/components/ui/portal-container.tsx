import { createContext, useContext, type ReactNode } from "react";

export const PortalContainerContext = createContext<HTMLElement | null>(null);

export function PortalContainerProvider({ container, children }: { container: HTMLElement; children: ReactNode }) {
  return <PortalContainerContext.Provider value={container}>{children}</PortalContainerContext.Provider>;
}

export function usePortalContainer() {
  return useContext(PortalContainerContext) ?? document.body;
}
