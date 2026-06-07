import { ChatPanel } from "../../../../features/ai/ChatPanel";
import { ConversationHistory } from "../../../../features/ai/ConversationHistory";
import { SuggestedPrompts } from "../../../../features/ai/SuggestedPrompts";

export default function AiPage() {
  return (
    <main className="grid gap-6 p-6 lg:grid-cols-[280px_1fr]">
      <ConversationHistory conversations={[]} />
      <div className="space-y-4">
        <h1 className="text-2xl font-semibold">Local AI Assistant</h1>
        <SuggestedPrompts />
        <ChatPanel />
      </div>
    </main>
  );
}
