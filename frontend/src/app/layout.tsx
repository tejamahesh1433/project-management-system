import type { ReactNode } from "react";

export const metadata = {
  title: "Project Management SaaS",
  description: "Self-hosted project management workspace",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
