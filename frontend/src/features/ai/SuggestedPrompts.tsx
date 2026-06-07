const prompts = [
  "Summarize this workspace",
  "Find blocked tasks",
  "Summarize sprint progress",
  "Search recent activity",
];

export function SuggestedPrompts() {
  return (
    <section className="flex flex-wrap gap-2">
      {prompts.map((prompt) => (
        <button key={prompt} className="rounded border px-3 py-2 text-sm" type="button">
          {prompt}
        </button>
      ))}
    </section>
  );
}
