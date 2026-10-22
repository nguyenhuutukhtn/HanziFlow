import { Drawer as DrawerPrimitive } from "vaul";
import { usePortalContainer } from "./portal-container";
export const Drawer = DrawerPrimitive.Root;
export const DrawerTrigger = DrawerPrimitive.Trigger;
export function DrawerContent({ children, ...props }: React.ComponentProps<typeof DrawerPrimitive.Content>) {
  const container = usePortalContainer();
  return (
    <DrawerPrimitive.Portal container={container}>
      <DrawerPrimitive.Overlay className="ui-drawer-overlay" />
      <DrawerPrimitive.Content className="ui-drawer" {...props}>
        {children}
      </DrawerPrimitive.Content>
    </DrawerPrimitive.Portal>
  );
}
