"use client";
import { useState, useRef, useEffect, useCallback, type ReactNode, useId } from "react";
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
  const [activeIdx, setActiveIdx] = useState(-1);
  const ref = useRef<HTMLDivElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const menuId = useId();

  const actionItems = items.filter((i) => !i.separator);

  const close = useCallback(() => {
    setOpen(false);
    setActiveIdx(-1);
  }, []);

  useEffect(() => {
    if (!open) return;
    const handleClick = (e: MouseEvent) => {
      if (!ref.current?.contains(e.target as Node)) close();
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [open, close]);

  // Focus first item when menu opens
  useEffect(() => {
    if (open) {
      setActiveIdx(0);
      // Focus the menu container so keyboard events fire
      menuRef.current?.focus();
    }
  }, [open]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!open) {
      if (e.key === "Enter" || e.key === " " || e.key === "ArrowDown") {
        e.preventDefault();
        setOpen(true);
      }
      return;
    }

    const nonSeparator = items.filter((i) => !i.separator && !i.disabled);
    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setActiveIdx((i) => Math.min(i + 1, nonSeparator.length - 1));
        break;
      case "ArrowUp":
        e.preventDefault();
        setActiveIdx((i) => Math.max(i - 1, 0));
        break;
      case "Enter":
      case " ": {
        e.preventDefault();
        const item = nonSeparator[activeIdx];
        if (item && !item.disabled) { item.onClick?.(); close(); }
        break;
      }
      case "Escape":
        e.preventDefault();
        close();
        break;
      case "Tab":
        close();
        break;
    }
  };

  return (
    <div ref={ref} className="relative inline-block">
      <div
        onClick={() => setOpen((p) => !p)}
        onKeyDown={handleKeyDown}
        role="button"
        tabIndex={0}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={open ? menuId : undefined}
      >
        {trigger}
      </div>
      {open && (
        <div
          ref={menuRef}
          id={menuId}
          role="menu"
          tabIndex={-1}
          onKeyDown={handleKeyDown}
          className={cn(
            "absolute z-50 mt-1 min-w-[160px] rounded-lg border border-[var(--color-border)] bg-[var(--color-popover)] shadow-lg p-1 animate-fade-in focus:outline-none",
            align === "right" ? "right-0" : "left-0",
            className
          )}
        >
          {items.map((item, i) => {
            if (item.separator) return <div key={i} role="separator" className="my-1 h-px bg-[var(--color-border)]" />;
            const isActive = actionItems.indexOf(item) === activeIdx;
            return (
              <button
                key={i}
                role="menuitem"
                disabled={item.disabled}
                onClick={() => { item.onClick?.(); close(); }}
                className={cn(
                  "flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors",
                  item.destructive
                    ? "text-[var(--color-destructive)] hover:bg-[var(--color-destructive)]/10"
                    : "hover:bg-[var(--color-accent)] text-[var(--color-foreground)]",
                  item.disabled && "opacity-50 cursor-not-allowed",
                  isActive && !item.destructive && "bg-[var(--color-accent)]",
                  isActive && item.destructive && "bg-[var(--color-destructive)]/10",
                )}
              >
                {item.icon && <span className="h-4 w-4 shrink-0" aria-hidden="true">{item.icon}</span>}
                {item.label}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
