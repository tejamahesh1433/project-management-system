import { KpiCards } from "../../../../../../features/analytics/KpiCards";
import { TaskStatusChart } from "../../../../../../features/analytics/TaskStatusChart";

export default function ProjectAnalyticsPage() {
  return (
    <main className="space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Project Analytics</h1>
      <KpiCards
        items={[
          { label: "Total Tasks", value: 0 },
          { label: "Sprint Progress", value: "0%" },
          { label: "Completed", value: 0 },
          { label: "Open", value: 0 },
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
