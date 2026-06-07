type Kpi = {
  label: string;
  value: string | number;
};

export function KpiCards({ items }: { items: Kpi[] }) {
  return (
    <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {items.map((item) => (
        <article key={item.label} className="rounded border p-4">
          <p className="text-sm text-gray-500">{item.label}</p>
          <p className="mt-2 text-2xl font-semibold">{item.value}</p>
        </article>
      ))}
    </section>
  );
}
