type IntegrationStatusCardProps = {
  name: string;
  type: string;
  status: string;
};

export function IntegrationStatusCard({ name, type, status }: IntegrationStatusCardProps) {
  return (
    <article className="rounded border p-4">
      <h2 className="text-base font-semibold">{name}</h2>
      <p className="mt-2 text-sm text-gray-500">{type}</p>
      <p className="mt-4 text-sm font-medium">{status}</p>
    </article>
  );
}
