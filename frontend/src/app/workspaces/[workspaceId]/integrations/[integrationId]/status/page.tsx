import { IntegrationStatusCard } from "../../../../../../features/integration/IntegrationStatusCard";

export default function IntegrationConnectionStatusPage() {
  return (
    <main className="space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Connection Status</h1>
      <IntegrationStatusCard name="Integration" type="Connection" status="Not tested" />
    </main>
  );
}
