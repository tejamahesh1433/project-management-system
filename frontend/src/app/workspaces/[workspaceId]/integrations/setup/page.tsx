"use client";
import { useParams, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";

export default function IntegrationSetupPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const router = useRouter();
  return (
    <div className="flex flex-col h-full overflow-hidden">
      <div className="flex h-12 items-center gap-3 border-b border-[var(--color-border)] px-4">
        <Button variant="ghost" size="sm" onClick={() => router.push(`/workspaces/${workspaceId}/integrations`)}>
          <ArrowLeft className="h-4 w-4" />
          Back to Integrations
        </Button>
      </div>
      <div className="flex-1 flex items-center justify-center p-6">
        <p className="text-sm text-[var(--color-muted-foreground)]">
          Use the Integrations page to add a new integration.
        </p>
      </div>
    </div>
  );
}
