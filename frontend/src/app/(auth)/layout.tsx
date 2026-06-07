import type { ReactNode } from "react";

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--color-background)] p-4">
      <div className="w-full max-w-sm">
        <div className="flex items-center justify-center gap-2 mb-8">
          <div className="h-8 w-8 rounded-lg bg-[var(--color-primary)] flex items-center justify-center">
            <span className="text-white font-bold text-sm">PF</span>
          </div>
          <span className="text-xl font-bold tracking-tight">ProjectFlow</span>
        </div>
        {children}
      </div>
    </div>
  );
}
