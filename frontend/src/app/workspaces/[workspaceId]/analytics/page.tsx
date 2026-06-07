import { KpiCards } from "../../../../features/analytics/KpiCards";
import { TaskStatusChart } from "../../../../features/analytics/TaskStatusChart";

export default function WorkspaceAnalyticsPage() {
  return (
    <main className="space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Analytics</h1>
      <KpiCards
        items={[
          { label: "Projects", value: 0 },
          { label: "Tasks", value: 0 },
          { label: "Members", value: 0 },
          { label: "Activities", value: 0 },
        ]}
      />
      <TaskStatusChart
        data={[
          { status: "TODO", tasks: 0 },
          { status: "IN_PROGRESS", tasks: 0 },
          { status: "DONE", tasks: 0 },
        ]}
      />
    </main>
  );
}
