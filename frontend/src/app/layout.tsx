import type { ReactNode } from "react";
import { Providers } from "@/components/providers";
import "@/styles/globals.css";

export const metadata = {
  title: "ProjectFlow – Project Management",
  description: "Self-hosted project management workspace",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className="min-h-screen bg-[var(--color-background)] text-[var(--color-foreground)] antialiased">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
