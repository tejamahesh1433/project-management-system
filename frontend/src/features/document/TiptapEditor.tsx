"use client";

import { useEditor, EditorContent } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";

type TiptapEditorProps = {
  content?: string;
};

export function TiptapEditor({ content = "" }: TiptapEditorProps) {
  const editor = useEditor({
    extensions: [StarterKit],
    content,
  });

  return <EditorContent editor={editor} />;
}
