export function EmptyState({ title, message }: { title: string; message: string }) {
  return (
    <section className="rounded border p-6">
      <h2 className="text-lg font-semibold">{title}</h2>
      <p className="mt-2 text-sm text-gray-500">{message}</p>
    </section>
  );
}
