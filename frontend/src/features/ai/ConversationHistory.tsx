type Conversation = {
  id: string;
  title: string;
};

export function ConversationHistory({ conversations }: { conversations: Conversation[] }) {
  return (
    <aside className="rounded border p-4">
      <h2 className="text-base font-semibold">History</h2>
      <div className="mt-4 grid gap-2">
        {conversations.map((conversation) => (
          <button key={conversation.id} className="rounded border px-3 py-2 text-left" type="button">
            {conversation.title}
          </button>
        ))}
      </div>
    </aside>
  );
}
