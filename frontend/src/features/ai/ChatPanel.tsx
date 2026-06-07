"use client";

export function ChatPanel() {
  return (
    <section className="grid gap-4 rounded border p-4">
      <div className="min-h-72 rounded border p-3">
        <p className="text-sm text-gray-500">Start a local Ollama-backed conversation.</p>
      </div>
      <form className="flex gap-2">
        <input className="min-w-0 flex-1 rounded border p-2" placeholder="Ask about this workspace" />
        <button className="rounded border px-4 py-2" type="submit">Send</button>
      </form>
    </section>
  );
}
