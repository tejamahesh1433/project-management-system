import { cn } from "@/lib/utils";
import { type TextareaHTMLAttributes, forwardRef } from "react";

export interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  error?: string;
  label?: string;
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ className, error, label, id, ...props }, ref) => {
    return (
      <div className="flex flex-col gap-1.5 w-full">
        {label && (
          <label htmlFor={id} className="text-sm font-medium text-[var(--color-foreground)]">
            {label}
          </label>
        )}
        <textarea
          id={id}
          ref={ref}
          className={cn(
            "flex min-h-[80px] w-full rounded-md border border-[var(--color-input)] bg-transparent px-3 py-2 text-sm",
            "placeholder:text-[var(--color-muted-foreground)] resize-none",
            "focus:outline-none focus:ring-2 focus:ring-[var(--color-ring)] focus:ring-offset-0",
            "disabled:cursor-not-allowed disabled:opacity-50",
            error && "border-[var(--color-destructive)]",
            className
          )}
          {...props}
        />
        {error && <p className="text-xs text-[var(--color-destructive)]">{error}</p>}
      </div>
    );
  }
);
Textarea.displayName = "Textarea";
