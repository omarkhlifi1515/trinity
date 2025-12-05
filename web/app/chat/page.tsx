"use client";

import React, { useEffect, useRef, useState } from "react";
import AppSidebar from "../../components/AppSidebar";
import { apiPost } from "../../lib/api";

type Message = { id?: string; user_id?: string; content: string; from_bot?: boolean };

export default function ChatPage() {
  const [user, setUser] = useState<{ id: string; role: string; department?: string } | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [text, setText] = useState("");
  const ref = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const stored = (typeof window !== "undefined" && localStorage.getItem("trinity_user")) || null;
    if (stored) setUser(JSON.parse(stored));
    // TODO: load history from backend
  }, []);

  useEffect(() => {
    if (ref.current) ref.current.scrollTop = ref.current.scrollHeight;
  }, [messages]);

  async function send() {
    if (!text.trim() || !user) return;
    const content = text.trim();
    const is_bot = /trinity/i.test(content);
    const payload = { user_id: user.id, content, is_bot_command: is_bot };
    // optimistic UI
    setMessages((m) => [...m, { content, user_id: user.id }]);
    setText("");
    try {
      await apiPost("/chat/send", payload);
      // in a real app, you'd re-sync from server or listen via websocket
    } catch (e) {
      console.error(e);
    }
  }

  return (
    <div className="flex">
      <AppSidebar user={user} />
      <main className="flex-1 p-6 flex flex-col h-screen">
        <h1 className="text-2xl font-bold mb-4">Chat</h1>
        <div ref={ref} className="flex-1 overflow-auto space-y-3 p-4 border rounded mb-4 bg-white">
          {messages.map((m, i) => (
            <div key={i} className={`p-3 rounded ${m.from_bot ? "bg-gray-100 self-start" : "bg-blue-50 self-end"}`}>
              <div className="text-sm">{m.content}</div>
            </div>
          ))}
        </div>

        <div className="flex items-center gap-2">
          <input value={text} onChange={(e) => setText(e.target.value)} className="flex-1 px-3 py-2 border rounded" placeholder="Message Trinity..." />
          <button onClick={send} className="px-4 py-2 bg-blue-600 text-white rounded">Send</button>
        </div>
      </main>
    </div>
  );
}
