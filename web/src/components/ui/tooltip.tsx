import * as TooltipPrimitive from "@radix-ui/react-tooltip";
import { usePortalContainer } from "./portal-container";
// Self-contained provider (shadcn pattern): a Tooltip outside a TooltipProvider throws.
export function Tooltip(props: React.ComponentProps<typeof TooltipPrimitive.Root>) {
  return (
    <TooltipPrimitive.Provider delayDuration={200}>
      <TooltipPrimitive.Root {...props} />
    </TooltipPrimitive.Provider>
  );
}
export const TooltipTrigger = TooltipPrimitive.Trigger;
export function TooltipContent({ children, ...props }: React.ComponentProps<typeof TooltipPrimitive.Content>) {
  const container = usePortalContainer();
  return (
    <TooltipPrimitive.Portal container={container}>
      <TooltipPrimitive.Content className="ui-tooltip" sideOffset={6} {...props}>
        {children}
      </TooltipPrimitive.Content>
    </TooltipPrimitive.Portal>
  );
}
