"use client";
import { useState } from "react";
import { Menu, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import type { ReactNode } from "react";

interface MobileNavProps {
  sidebar: ReactNode;
}

export function MobileNav({ sidebar }: MobileNavProps) {
  const [open, setOpen] = useState(false);

  return (
    <>
      {/* Hamburger — visible on mobile only */}
      <Button
        variant="ghost"
        size="icon-sm"
        onClick={() => setOpen(true)}
        className="md:hidden shrink-0"
        aria-label="Open navigation menu"
        aria-expanded={open}
        aria-controls="mobile-sidebar"
      >
        <Menu className="h-5 w-5" aria-hidden="true" />
      </Button>

      {/* Overlay */}
      {open && (
        <div className="fixed inset-0 z-40 md:hidden" aria-hidden="true">
          <div className="absolute inset-0 bg-black/50" onClick={() => setOpen(false)} />
          <div
            id="mobile-sidebar"
            className="absolute left-0 top-0 bottom-0 animate-slide-in"
            role="dialog"
            aria-modal="true"
            aria-label="Navigation menu"
          >
            {sidebar}
            <Button
              variant="ghost"
              size="icon-sm"
              onClick={() => setOpen(false)}
              className="absolute top-3 right-3"
              aria-label="Close navigation menu"
            >
              <X className="h-4 w-4" aria-hidden="true" />
            </Button>
          </div>
        </div>
      )}
    </>
  );
}
