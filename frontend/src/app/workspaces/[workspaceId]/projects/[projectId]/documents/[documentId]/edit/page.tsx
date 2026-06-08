"use client";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { documentApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { useToast } from "@/components/ui/toast";
import { useEditor, EditorContent } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import Placeholder from "@tiptap/extension-placeholder";
import Link from "@tiptap/extension-link";
import { useEffect, useState, useCallback } from "react";
import { cn } from "@/lib/utils";
import {
  Bold, Italic, Strikethrough, Code, List, ListOrdered,
  Quote, Heading1, Heading2, Heading3, Undo, Redo, Save, ArrowLeft
} from "lucide-react";

export default function EditDocumentPage() {
  const { workspaceId, projectId, documentId } = useParams<{ workspaceId: string; projectId: string; documentId: string }>();
  const qc = useQueryClient();
  const router = useRouter();
  const { toast } = useToast();
  const [saved, setSaved] = useState(true);
  const [title, setTitle] = useState("");

  const { data: doc, isLoading } = useQuery({
    queryKey: queryKeys.documents.detail(documentId),
    queryFn: () => documentApi.getDocument(documentId),
  });

  const saveMutation = useMutation({
    mutationFn: (content: string) => documentApi.updateDocument(documentId, { title, content }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.documents.detail(documentId) });
      qc.invalidateQueries({ queryKey: queryKeys.documents.all(projectId) });
      setSaved(true);
      toast("success", "Document saved");
    },
    onError: () => toast("error", "Failed to save"),
  });

  const editor = useEditor({
    extensions: [
      StarterKit,
      Placeholder.configure({ placeholder: "Start writing..." }),
      Link.configure({ openOnClick: false }),
    ],
    onUpdate: () => setSaved(false),
  });

  useEffect(() => {
    if (doc && editor) {
      setTitle(doc.title);
      editor.commands.setContent(doc.content ?? "");
    }
  }, [doc, editor]);

  const handleSave = useCallback(() => {
    if (!editor) return;
    saveMutation.mutate(JSON.stringify(editor.getJSON()));
  }, [editor, saveMutation]);

  useEffect(() => {
    const handle = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "s") {
        e.preventDefault();
        handleSave();
      }
    };
    document.addEventListener("keydown", handle);
    return () => document.removeEventListener("keydown", handle);
  }, [handleSave]);

  const ToolbarBtn = ({ onClick, active, children }: { onClick: () => void; active?: boolean; children: React.ReactNode }) => (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "p-1.5 rounded transition-colors",
        active ? "bg-[var(--color-accent)] text-[var(--color-foreground)]" : "text-[var(--color-muted-foreground)] hover:bg-[var(--color-accent)] hover:text-[var(--color-foreground)]"
      )}
    >
      {children}
    </button>
  );

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header
        workspaceId={workspaceId}
        actions={
          <div className="flex items-center gap-2">
            <span className="text-xs text-[var(--color-muted-foreground)]">{saved ? "Saved" : "Unsaved changes"}</span>
            <Button size="sm" onClick={handleSave} loading={saveMutation.isPending}>
              <Save className="h-4 w-4" />
              Save
            </Button>
          </div>
        }
      />

      {/* Toolbar */}
      {editor && (
        <div className="flex items-center gap-0.5 px-4 py-2 border-b border-[var(--color-border)] overflow-x-auto shrink-0">
          <ToolbarBtn onClick={() => editor.chain().focus().undo().run()}><Undo className="h-4 w-4" /></ToolbarBtn>
          <ToolbarBtn onClick={() => editor.chain().focus().redo().run()}><Redo className="h-4 w-4" /></ToolbarBtn>
          <div className="w-px h-5 bg-[var(--color-border)] mx-1 shrink-0" />
          <ToolbarBtn onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()} active={editor.isActive("heading", { level: 1 })}><Heading1 className="h-4 w-4" /></ToolbarBtn>
          <ToolbarBtn onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()} active={editor.isActive("heading", { level: 2 })}><Heading2 className="h-4 w-4" /></ToolbarBtn>
          <ToolbarBtn onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()} active={editor.isActive("heading", { level: 3 })}><Heading3 className="h-4 w-4" /></ToolbarBtn>
          <div className="w-px h-5 bg-[var(--color-border)] mx-1 shrink-0" />
          <ToolbarBtn onClick={() => editor.chain().focus().toggleBold().run()} active={editor.isActive("bold")}><Bold className="h-4 w-4" /></ToolbarBtn>
          <ToolbarBtn onClick={() => editor.chain().focus().toggleItalic().run()} active={editor.isActive("italic")}><Italic className="h-4 w-4" /></ToolbarBtn>
          <ToolbarBtn onClick={() => editor.chain().focus().toggleStrike().run()} active={editor.isActive("strike")}><Strikethrough className="h-4 w-4" /></ToolbarBtn>
          <ToolbarBtn onClick={() => editor.chain().focus().toggleCode().run()} active={editor.isActive("code")}><Code className="h-4 w-4" /></ToolbarBtn>
          <div className="w-px h-5 bg-[var(--color-border)] mx-1 shrink-0" />
          <ToolbarBtn onClick={() => editor.chain().focus().toggleBulletList().run()} active={editor.isActive("bulletList")}><List className="h-4 w-4" /></ToolbarBtn>
          <ToolbarBtn onClick={() => editor.chain().focus().toggleOrderedList().run()} active={editor.isActive("orderedList")}><ListOrdered className="h-4 w-4" /></ToolbarBtn>
          <ToolbarBtn onClick={() => editor.chain().focus().toggleBlockquote().run()} active={editor.isActive("blockquote")}><Quote className="h-4 w-4" /></ToolbarBtn>
        </div>
      )}

      <div className="flex-1 overflow-y-auto">
        <div className="max-w-3xl mx-auto px-8 py-6">
          <input
            className="w-full text-3xl font-bold mb-6 bg-transparent outline-none placeholder:text-[var(--color-muted-foreground)]"
            placeholder="Untitled"
            value={title}
            onChange={(e) => { setTitle(e.target.value); setSaved(false); }}
          />
          {isLoading ? (
            <div className="space-y-3">
              {[1,2,3,4].map(i => <div key={i} className="h-5 animate-pulse bg-[var(--color-muted)] rounded" />)}
            </div>
          ) : (
            <EditorContent editor={editor} className="prose prose-sm max-w-none" />
          )}
        </div>
      </div>
    </div>
  );
}
