"use client";
import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { workspaceApi, organizationApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useToast } from "@/components/ui/toast";

export default function NewWorkspacePage() {
  const router = useRouter();
  const qc = useQueryClient();
  const { toast } = useToast();
  const [form, setForm] = useState({ name: "", description: "" });

  const mutation = useMutation({
    mutationFn: async () => {
      // 1. Fetch organizations
      const orgs = await organizationApi.list();
      let orgId;
      if (orgs.length > 0) {
        orgId = orgs[0].id;
      } else {
        // 2. Create default organization if none exists
        const defaultOrgName = form.name + " Org";
        const defaultOrgSlug = defaultOrgName.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)+/g, "") || "default-org";
        const newOrg = await organizationApi.create({ name: defaultOrgName, slug: defaultOrgSlug });
        orgId = newOrg.id;
      }

      // 3. Generate slug for the workspace
      const slug = form.name.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)+/g, "") || "workspace";

      // 4. Create workspace
      return workspaceApi.create({
        organizationId: orgId,
        name: form.name,
        slug,
        description: form.description
      });
    },
    onSuccess: (workspace) => {
      qc.invalidateQueries({ queryKey: queryKeys.workspaces.all });
      toast("success", "Workspace created!");
      router.push(`/workspaces/${workspace.id}/overview`);
    },
    onError: () => toast("error", "Failed to create workspace"),
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!form.name.trim()) return;
    mutation.mutate();
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-[var(--color-background)]">
      <div className="w-full max-w-sm">
        <div className="flex items-center justify-center gap-2 mb-8">
          <div className="h-8 w-8 rounded-lg bg-[var(--color-primary)] flex items-center justify-center">
            <span className="text-white font-bold text-sm">PF</span>
          </div>
          <span className="text-xl font-bold tracking-tight">ProjectFlow</span>
        </div>
        <Card>
          <CardHeader>
            <CardTitle>Create your workspace</CardTitle>
            <CardDescription>A workspace is where your team collaborates on projects.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              <Input
                label="Workspace name"
                placeholder="Acme Corp"
                value={form.name}
                onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))}
                autoFocus
              />
              <Textarea
                label="Description (optional)"
                placeholder="What does your team work on?"
                value={form.description}
                onChange={(e) => setForm(f => ({ ...f, description: e.target.value }))}
                className="h-20"
              />
              <Button type="submit" loading={mutation.isPending} disabled={!form.name.trim()} className="w-full">
                Create Workspace
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
