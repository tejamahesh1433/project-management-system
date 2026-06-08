"use client";
import { useState, useRef, useEffect } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation } from "@tanstack/react-query";
import { aiApi } from "@/lib/api";
import { queryKeys } from "@/lib/queryKeys";
import { Header } from "@/components/layout/header";
import { Button } from "@/components/ui/button";
import { Avatar } from "@/components/ui/avatar";
import { useAuthStore } from "@/stores/auth";
import { useToast } from "@/components/ui/toast";
import { cn, formatRelativeTime } from "@/lib/utils";
import { Send, Bot, Sparkles, Clock, MessageSquare } from "lucide-react";
import type { AiConversation, AiMessage } from "@/types";

const SUGGESTED_PROMPTS = [
  "Summarize this workspace's recent activity",
  "What tasks are overdue?",
  "Which projects are most active?",
  "Give me a sprint status report",
  "What are the team's priorities this week?",
  "Find all high-priority bugs",
];

export default function AiPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const user = useAuthStore((s) => s.user);
  const { toast } = useToast();

  const [messages, setMessages] = useState<AiMessage[]>([]);
  const [input, setInput] = useState("");
  const [conversationId, setConversationId] = useState<string | undefined>();
  const [selectedConvId, setSelectedConvId] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  const { data: conversations = [] } = useQuery<AiConversation[]>({
    queryKey: queryKeys.ai.conversations(workspaceId),
    queryFn: () => aiApi.conversations(workspaceId),
  });

  const chatMutation = useMutation({
    mutationFn: (variables: { message: string; convId?: string }) =>
      aiApi.chat(workspaceId, variables.message, variables.convId),
    onSuccess: (data) => {
      setConversationId(data.conversationId);
      setMessages((prev) => [...prev, { role: "ASSISTANT", content: data.message }]);
    },
    onError: () => toast("error", "AI error", "Could not get a response"),
  });

  const sendMessage = () => {
    if (!input.trim()) return;
    const msg = input.trim();
    setMessages((prev) => [...prev, { role: "USER", content: msg }]);
    setInput("");
    chatMutation.mutate({ message: msg, convId: conversationId });
  };

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, chatMutation.isPending]);

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <Header title="AI Assistant" workspaceId={workspaceId} />
      <div className="flex flex-1 overflow-hidden">
        {/* Conversation sidebar */}
        <div className="w-56 shrink-0 border-r border-[var(--color-border)] flex flex-col overflow-hidden">
          <div className="p-3 border-b border-[var(--color-border)]">
            <Button
              size="sm"
              className="w-full"
              onClick={() => {
                setMessages([]);
                setConversationId(undefined);
                setSelectedConvId(null);
              }}
            >
              <MessageSquare className="h-4 w-4" />
              New Chat
            </Button>
          </div>
          <div className="flex-1 overflow-y-auto p-2 space-y-1">
            {conversations.map((c) => (
              <button
                key={c.id}
                onClick={() => {
                  setSelectedConvId(c.id);
                  setConversationId(c.id);
                  setMessages(c.messages ?? []);
                }}
                className={cn(
                  "w-full text-left rounded-md px-2.5 py-2 text-xs transition-colors",
                  selectedConvId === c.id
                    ? "bg-[var(--color-accent)] text-[var(--color-foreground)]"
                    : "text-[var(--color-muted-foreground)] hover:bg-[var(--color-accent)]"
                )}
              >
                <p className="font-medium truncate">{c.title || "Chat"}</p>
                <p className="text-[10px] mt-0.5 opacity-70">{formatRelativeTime(c.createdAt)}</p>
              </button>
            ))}
          </div>
        </div>

        {/* Chat area */}
        <div className="flex flex-1 flex-col overflow-hidden">
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.length === 0 && (
              <div className="flex flex-col items-center justify-center h-full gap-6 pb-8">
                <div className="rounded-full bg-[var(--color-primary)]/10 p-4">
                  <Bot className="h-10 w-10 text-[var(--color-primary)]" />
                </div>
                <div className="text-center">
                  <h2 className="text-lg font-semibold mb-1">AI Assistant</h2>
                  <p className="text-sm text-[var(--color-muted-foreground)] max-w-xs">
                    Ask questions about your workspace, projects, tasks, and team activity.
                  </p>
                </div>
                <div className="grid grid-cols-2 gap-2 max-w-lg w-full">
                  {SUGGESTED_PROMPTS.map((prompt) => (
                    <button
                      key={prompt}
                      onClick={() => { setInput(prompt); }}
                      className="text-left p-3 rounded-lg border border-[var(--color-border)] text-sm hover:bg-[var(--color-accent)] hover:border-[var(--color-primary)] transition-colors"
                    >
                      <Sparkles className="h-3.5 w-3.5 text-[var(--color-primary)] mb-1.5" />
                      {prompt}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {messages.map((msg, i) => (
              <div key={i} className={cn("flex items-start gap-3", msg.role === "USER" && "flex-row-reverse")}>
                {msg.role === "ASSISTANT" ? (
                  <div className="h-8 w-8 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center shrink-0">
                    <Bot className="h-4 w-4 text-[var(--color-primary)]" />
                  </div>
                ) : user ? (
                  <Avatar name={user.displayName ?? user.email} src={user.avatarUrl} size="sm" />
                ) : null}
                <div className={cn(
                  "max-w-[75%] rounded-xl px-4 py-3 text-sm",
                  msg.role === "USER"
                    ? "bg-[var(--color-primary)] text-white"
                    : "bg-[var(--color-muted)] text-[var(--color-foreground)]"
                )}>
                  <p className="whitespace-pre-wrap leading-relaxed">{msg.content}</p>
                </div>
              </div>
            ))}

            {chatMutation.isPending && (
              <div className="flex items-start gap-3">
                <div className="h-8 w-8 rounded-full bg-[var(--color-primary)]/10 flex items-center justify-center shrink-0">
                  <Bot className="h-4 w-4 text-[var(--color-primary)]" />
                </div>
                <div className="bg-[var(--color-muted)] rounded-xl px-4 py-3">
                  <div className="flex gap-1.5 items-center h-5">
                    {[1,2,3].map(i => (
                      <div key={i} className="h-2 w-2 rounded-full bg-[var(--color-muted-foreground)] animate-bounce" style={{ animationDelay: `${i * 150}ms` }} />
                    ))}
                  </div>
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {/* Input */}
          <div className="p-4 border-t border-[var(--color-border)]">
            <div className="flex items-end gap-2">
              <textarea
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    sendMessage();
                  }
                }}
                placeholder="Ask anything about your workspace... (Enter to send)"
                className="flex-1 min-h-[44px] max-h-32 rounded-xl border border-[var(--color-input)] bg-transparent px-4 py-2.5 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-[var(--color-ring)] placeholder:text-[var(--color-muted-foreground)]"
                rows={1}
              />
              <Button
                onClick={sendMessage}
                disabled={!input.trim() || chatMutation.isPending}
                loading={chatMutation.isPending}
                size="icon"
                className="shrink-0 h-11 w-11 rounded-xl"
              >
                <Send className="h-4 w-4" />
              </Button>
            </div>
            <p className="text-[10px] text-[var(--color-muted-foreground)] mt-2 text-center">
              AI is read-only · responses may not be perfectly accurate
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
