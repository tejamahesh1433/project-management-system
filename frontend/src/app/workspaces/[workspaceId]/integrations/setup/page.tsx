export default function IntegrationSetupPage() {
  return (
    <main className="space-y-6 p-6">
      <h1 className="text-2xl font-semibold">Integration Setup</h1>
      <form className="grid max-w-xl gap-4">
        <input className="rounded border p-2" name="name" placeholder="Name" />
        <input className="rounded border p-2" name="endpointUrl" placeholder="Endpoint URL" />
        <input className="rounded border p-2" name="repositoryUrl" placeholder="Repository URL" />
        <button className="rounded border px-4 py-2" type="submit">Create</button>
      </form>
    </main>
  );
}
