import { DashboardWidgetGrid } from "../../../../../features/dashboard/DashboardWidgetGrid";

export default function DashboardDetailsPage() {
  return (
    <main className="space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Dashboard</h1>
      <DashboardWidgetGrid widgets={[]} />
    </main>
  );
}
