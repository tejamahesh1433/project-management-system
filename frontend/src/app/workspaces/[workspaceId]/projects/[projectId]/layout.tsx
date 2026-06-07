import type { ReactNode } from "react";
import { ProjectSidebar } from "@/components/layout/project-sidebar";

interface Props {
  children: ReactNode;
  params: Promise<{ workspaceId: string; projectId: string }>;
}

export default async function ProjectLayout({ children, params }: Props) {
  const { workspaceId, projectId } = await params;
  return (
    <div className="flex flex-1 overflow-hidden">
      <ProjectSidebar workspaceId={workspaceId} projectId={projectId} />
      <div className="flex flex-1 flex-col overflow-hidden">{children}</div>
    </div>
  );
}
