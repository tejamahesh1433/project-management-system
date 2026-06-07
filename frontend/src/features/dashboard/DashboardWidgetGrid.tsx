type Widget = {
  id: string;
  title: string;
  type: string;
};

export function DashboardWidgetGrid({ widgets }: { widgets: Widget[] }) {
  return (
    <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      {widgets.map((widget) => (
        <article key={widget.id} className="rounded border p-4">
          <h2 className="text-base font-semibold">{widget.title}</h2>
          <p className="mt-2 text-sm text-gray-500">{widget.type}</p>
        </article>
      ))}
    </section>
  );
}
