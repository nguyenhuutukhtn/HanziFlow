import * as SelectPrimitive from "@radix-ui/react-select";
import { ChevronDown } from "lucide-react";
import { usePortalContainer } from "./portal-container";

export const Select = SelectPrimitive.Root;
export function SelectTrigger({ children, ...props }: React.ComponentProps<typeof SelectPrimitive.Trigger>) {
  return (
    <SelectPrimitive.Trigger className="ui-button" {...props}>
      {children}
      <ChevronDown size={16} />
    </SelectPrimitive.Trigger>
  );
}
export const SelectValue = SelectPrimitive.Value;
export function SelectContent({ children }: { children: React.ReactNode }) {
  const container = usePortalContainer();
  return (
    <SelectPrimitive.Portal container={container}>
      <SelectPrimitive.Content className="ui-popover">
        <SelectPrimitive.Viewport>{children}</SelectPrimitive.Viewport>
      </SelectPrimitive.Content>
    </SelectPrimitive.Portal>
  );
}
export function SelectItem({ children, ...props }: React.ComponentProps<typeof SelectPrimitive.Item>) {
  return (
    <SelectPrimitive.Item className="ui-toggle-item" {...props}>
      <SelectPrimitive.ItemText>{children}</SelectPrimitive.ItemText>
    </SelectPrimitive.Item>
  );
}
