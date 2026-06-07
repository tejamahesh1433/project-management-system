"use client";
import { useState, type ReactNode } from "react";
import { cn } from "@/lib/utils";

interface Tab {
  id: string;
  label: string;
  icon?: ReactNode;
  badge?: number;
}

interface TabsProps {
  tabs: Tab[];
  activeTab?: string;
  onTabChange?: (id: string) => void;
  className?: string;
}

export function Tabs({ tabs, activeTab, onTabChange, className }: TabsProps) {
  const [internal, setInternal] = useState(tabs[0]?.id ?? "");
  const active = activeTab ?? internal;

  const handleChange = (id: string) => {
    setInternal(id);
    onTabChange?.(id);
  };

  return (
    <div className={cn("flex items-center gap-1 border-b border-[var(--color-border)]", className)}>
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => handleChange(tab.id)}
          className={cn(
            "flex items-center gap-1.5 px-3 py-2 text-sm font-medium border-b-2 -mb-px transition-colors",
            active === tab.id
              ? "border-[var(--color-primary)] text-[var(--color-primary)]"
              : "border-transparent text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]"
          )}
        >
          {tab.icon}
          {tab.label}
          {tab.badge !== undefined && (
            <span className={cn(
              "ml-1 rounded-full px-1.5 py-0.5 text-xs",
              active === tab.id
                ? "bg-[var(--color-primary)]/10 text-[var(--color-primary)]"
                : "bg-[var(--color-muted)] text-[var(--color-muted-foreground)]"
            )}>
              {tab.badge}
            </span>
          )}
        </button>
      ))}
    </div>
  );
}
