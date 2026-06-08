"use client";
import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { AlertTriangle } from "lucide-react";

export default function ErrorPage({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-[var(--color-background)]">
      <div className="flex flex-col items-center gap-4 text-center max-w-sm">
        <div className="rounded-full bg-[var(--color-destructive)]/10 p-4">
          <AlertTriangle className="h-8 w-8 text-[var(--color-destructive)]" />
        </div>
        <div>
          <h1 className="text-lg font-semibold">Something went wrong</h1>
          <p className="text-sm text-[var(--color-muted-foreground)] mt-1">
            {error.message || "An unexpected error occurred."}
          </p>
        </div>
        <Button onClick={reset}>Try again</Button>
      </div>
    </div>
  );
}
