"use client";
import { useState, useRef, useEffect, type ReactNode } from "react";
import { cn } from "@/lib/utils";

interface DropdownItem {
  label: string;
  icon?: ReactNode;
  onClick?: () => void;
  destructive?: boolean;
  separator?: boolean;
  disabled?: boolean;
}

interface DropdownProps {
  trigger: ReactNode;
  items: DropdownItem[];
  align?: "left" | "right";
  className?: string;
}

export function Dropdown({ trigger, items, align = "left", className }: DropdownProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const handleClick = (e: MouseEvent) => {
      if (!ref.current?.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [open]);

  return (
    <div ref={ref} className="relative inline-block">
      <div onClick={() => setOpen((p) => !p)}>{trigger}</div>
      {open && (
        <div
          className={cn(
            "absolute z-50 mt-1 min-w-[160px] rounded-lg border border-[var(--color-border)] bg-[var(--color-popover)] shadow-lg p-1 animate-fade-in",
            align === "right" ? "right-0" : "left-0",
            className
          )}
        >
          {items.map((item, i) => (
            item.separator ? (
              <div key={i} className="my-1 h-px bg-[var(--color-border)]" />
            ) : (
              <button
                key={i}
                disabled={item.disabled}
                onClick={() => {
                  item.onClick?.();
                  setOpen(false);
                }}
                className={cn(
                  "flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors",
                  item.destructive
                    ? "text-[var(--color-destructive)] hover:bg-[var(--color-destructive)]/10"
                    : "hover:bg-[var(--color-accent)] text-[var(--color-foreground)]",
                  item.disabled && "opacity-50 cursor-not-allowed"
                )}
              >
                {item.icon && <span className="h-4 w-4 shrink-0">{item.icon}</span>}
                {item.label}
              </button>
            )
          ))}
        </div>
      )}
    </div>
  );
}
