"use client";

export default function ErrorPage({ reset }: { reset: () => void }) {
  return (
    <main className="space-y-4 p-6">
      <h1 className="text-2xl font-semibold">Something went wrong</h1>
      <button className="rounded border px-4 py-2" onClick={reset} type="button">Try again</button>
    </main>
  );
}
