"use client";
import { useEffect, useRef, useId, type ReactNode } from "react";
import { X } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "./button";

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  description?: string;
  children: ReactNode;
  className?: string;
  size?: "sm" | "md" | "lg" | "xl";
}

const sizeMap = {
  sm: "max-w-sm",
  md: "max-w-md",
  lg: "max-w-lg",
  xl: "max-w-2xl",
};

export function Modal({ open, onClose, title, description, children, className, size = "md" }: ModalProps) {
  const overlayRef = useRef<HTMLDivElement>(null);
  const dialogRef = useRef<HTMLDivElement>(null);
  const titleId = useId();
  const descId = useId();

  useEffect(() => {
    if (!open) return;
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handleKey);
    document.body.style.overflow = "hidden";

    // Move focus into dialog
    const firstFocusable = dialogRef.current?.querySelector<HTMLElement>(
      "button, [href], input, select, textarea, [tabindex]:not([tabindex='-1'])"
    );
    firstFocusable?.focus();

    return () => {
      document.removeEventListener("keydown", handleKey);
      document.body.style.overflow = "";
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      ref={overlayRef}
      className="fixed inset-0 z-50 flex items-center justify-center"
      onClick={(e) => { if (e.target === overlayRef.current) onClose(); }}
    >
      <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" aria-hidden="true" />
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-describedby={description ? descId : undefined}
        className={cn(
          "relative z-50 w-full mx-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] shadow-2xl animate-fade-in",
          sizeMap[size],
          className
        )}
      >
        {(title || description) && (
          <div className="flex items-start justify-between p-6 pb-4">
            <div>
              {title && <h2 id={titleId} className="text-lg font-semibold">{title}</h2>}
              {description && (
                <p id={descId} className="text-sm text-[var(--color-muted-foreground)] mt-1">{description}</p>
              )}
            </div>
            <Button variant="ghost" size="icon-sm" onClick={onClose} className="ml-4 shrink-0" aria-label="Close dialog">
              <X className="h-4 w-4" aria-hidden="true" />
            </Button>
          </div>
        )}
        <div className={cn(!title && !description && "pt-6")}>{children}</div>
      </div>
    </div>
  );
}

export function ModalFooter({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <div className={cn("flex items-center justify-end gap-2 p-6 pt-4 border-t border-[var(--color-border)]", className)}>
      {children}
    </div>
  );
}

export function ModalBody({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn("px-6 pb-4", className)}>{children}</div>;
}
