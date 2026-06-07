import { cn } from "@/lib/utils";
import { type InputHTMLAttributes, forwardRef } from "react";

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string;
  label?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, error, label, id, ...props }, ref) => {
    return (
      <div className="flex flex-col gap-1.5 w-full">
        {label && (
          <label htmlFor={id} className="text-sm font-medium text-[var(--color-foreground)]">
            {label}
          </label>
        )}
        <input
          id={id}
          type={type}
          ref={ref}
          className={cn(
            "flex h-9 w-full rounded-md border border-[var(--color-input)] bg-transparent px-3 py-1 text-sm",
            "placeholder:text-[var(--color-muted-foreground)]",
            "focus:outline-none focus:ring-2 focus:ring-[var(--color-ring)] focus:ring-offset-0",
            "disabled:cursor-not-allowed disabled:opacity-50",
            error && "border-[var(--color-destructive)] focus:ring-[var(--color-destructive)]",
            className
          )}
          {...props}
        />
        {error && <p className="text-xs text-[var(--color-destructive)]">{error}</p>}
      </div>
    );
  }
);
Input.displayName = "Input";
