import { IntegrationStatusCard } from "../../../../features/integration/IntegrationStatusCard";

export default function IntegrationsPage() {
  return (
    <main className="space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Integrations</h1>
      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <IntegrationStatusCard name="GitHub" type="GITHUB" status="Not connected" />
        <IntegrationStatusCard name="Jenkins" type="JENKINS" status="Not connected" />
        <IntegrationStatusCard name="SMTP" type="SMTP" status="Not connected" />
      </section>
    </main>
  );
}
