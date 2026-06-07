"use client";
import { useEffect, type ReactNode } from "react";
import { X } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "./button";

interface DrawerProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  side?: "right" | "left";
  className?: string;
  size?: "sm" | "md" | "lg";
}

const sizeMap = {
  sm: "w-80",
  md: "w-[480px]",
  lg: "w-[640px]",
};

export function Drawer({ open, onClose, title, children, side = "right", className, size = "md" }: DrawerProps) {
  useEffect(() => {
    if (!open) return;
    const handle = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
    document.addEventListener("keydown", handle);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", handle);
      document.body.style.overflow = "";
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex">
      <div className="fixed inset-0 bg-black/40" onClick={onClose} />
      <div
        className={cn(
          "fixed top-0 bottom-0 flex flex-col border border-[var(--color-border)] bg-[var(--color-card)] shadow-2xl animate-slide-in overflow-hidden",
          side === "right" ? "right-0" : "left-0",
          sizeMap[size],
          "max-w-[calc(100vw-2rem)]",
          className
        )}
      >
        <div className="flex items-center justify-between px-6 py-4 border-b border-[var(--color-border)] shrink-0">
          {title && <h2 className="text-base font-semibold">{title}</h2>}
          <Button variant="ghost" size="icon-sm" onClick={onClose} className="ml-auto">
            <X className="h-4 w-4" />
          </Button>
        </div>
        <div className="flex-1 overflow-y-auto">{children}</div>
      </div>
    </div>
  );
}
