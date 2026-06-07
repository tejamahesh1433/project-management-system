"use client";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { taskApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Modal, ModalBody, ModalFooter } from "@/components/ui/modal";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select } from "@/components/ui/select";
import { useToast } from "@/components/ui/toast";
import type { TaskStatus, TaskPriority, TaskType } from "@/types";

interface CreateTaskModalProps {
  open: boolean;
  onClose: () => void;
  projectId: string;
  sprintId?: string;
}

export function CreateTaskModal({ open, onClose, projectId, sprintId }: CreateTaskModalProps) {
  const qc = useQueryClient();
  const { toast } = useToast();

  const [form, setForm] = useState({
    title: "",
    description: "",
    status: "TODO" as TaskStatus,
    priority: "MEDIUM" as TaskPriority,
    type: "TASK" as TaskType,
    dueDate: "",
    estimatedHours: "",
  });

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const mutation = useMutation({
    mutationFn: () =>
      taskApi.create({
        projectId,
        sprintId,
        title: form.title,
        description: form.description || undefined,
        status: form.status,
        priority: form.priority,
        type: form.type,
        dueDate: form.dueDate || undefined,
        estimatedHours: form.estimatedHours ? Number(form.estimatedHours) : undefined,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.tasks.all(projectId) });
      toast("success", "Task created");
      onClose();
      setForm({ title: "", description: "", status: "TODO", priority: "MEDIUM", type: "TASK", dueDate: "", estimatedHours: "" });
    },
    onError: () => toast("error", "Failed to create task"),
  });

  return (
    <Modal open={open} onClose={onClose} title="New Task" size="lg">
      <ModalBody className="space-y-4">
        <Input label="Title" placeholder="What needs to be done?" value={form.title} onChange={set("title")} autoFocus />
        <Textarea label="Description" placeholder="Add more details..." value={form.description} onChange={set("description")} className="h-24" />
        <div className="grid grid-cols-3 gap-3">
          <Select
            label="Status"
            value={form.status}
            onChange={set("status")}
            options={[
              { value: "TODO", label: "Todo" },
              { value: "IN_PROGRESS", label: "In Progress" },
              { value: "IN_REVIEW", label: "In Review" },
              { value: "DONE", label: "Done" },
            ]}
          />
          <Select
            label="Priority"
            value={form.priority}
            onChange={set("priority")}
            options={[
              { value: "URGENT", label: "Urgent" },
              { value: "HIGH", label: "High" },
              { value: "MEDIUM", label: "Medium" },
              { value: "LOW", label: "Low" },
              { value: "NO_PRIORITY", label: "No Priority" },
            ]}
          />
          <Select
            label="Type"
            value={form.type}
            onChange={set("type")}
            options={[
              { value: "TASK", label: "Task" },
              { value: "BUG", label: "Bug" },
              { value: "FEATURE", label: "Feature" },
              { value: "IMPROVEMENT", label: "Improvement" },
              { value: "EPIC", label: "Epic" },
              { value: "STORY", label: "Story" },
            ]}
          />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <Input label="Due date" type="date" value={form.dueDate} onChange={set("dueDate")} />
          <Input label="Estimated hours" type="number" placeholder="0" value={form.estimatedHours} onChange={set("estimatedHours")} />
        </div>
      </ModalBody>
      <ModalFooter>
        <Button variant="outline" onClick={onClose}>Cancel</Button>
        <Button onClick={() => mutation.mutate()} loading={mutation.isPending} disabled={!form.title.trim()}>
          Create Task
        </Button>
      </ModalFooter>
    </Modal>
  );
}
