import * as PopoverPrimitive from "@radix-ui/react-popover";
import { usePortalContainer } from "./portal-container";

export const Popover = PopoverPrimitive.Root;
export const PopoverTrigger = PopoverPrimitive.Trigger;
export function PopoverContent({ children, ...props }: React.ComponentProps<typeof PopoverPrimitive.Content>) {
  const container = usePortalContainer();
  return (
    <PopoverPrimitive.Portal container={container}>
      <PopoverPrimitive.Content className="ui-popover" {...props}>
        {children}
      </PopoverPrimitive.Content>
    </PopoverPrimitive.Portal>
  );
}
