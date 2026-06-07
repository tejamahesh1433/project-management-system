"use client";

import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

type StatusDatum = {
  status: string;
  tasks: number;
};

export function TaskStatusChart({ data }: { data: StatusDatum[] }) {
  return (
    <div className="h-72 w-full rounded border p-4">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="status" />
          <YAxis allowDecimals={false} />
          <Tooltip />
          <Bar dataKey="tasks" fill="#2563eb" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
