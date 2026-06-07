import type { ReactNode } from "react";
import { Sidebar } from "@/components/layout/sidebar";

interface Props {
  children: ReactNode;
  params: Promise<{ workspaceId: string }>;
}

export default async function WorkspaceLayout({ children, params }: Props) {
  const { workspaceId } = await params;
  return (
    <div className="flex h-screen overflow-hidden">
      <Sidebar workspaceId={workspaceId} />
      <main className="flex flex-1 flex-col overflow-hidden">{children}</main>
    </div>
  );
}
