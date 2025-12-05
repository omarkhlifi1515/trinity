"use client";

import React, { useEffect, useRef, useState } from "react";
import AppSidebar from "../../components/AppSidebar";
import { apiPost, apiGet } from "../../lib/api";
import { supabase } from "../../lib/supabase";

type Message = { id?: string; user_id?: string; content: string; from_bot?: boolean };

export default function ChatPage() {
  const [user, setUser] = useState<any>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [text, setText] = useState("");
  const ref = useRef<HTMLDivElement | null>(null);

  // 1. Load User
  useEffect(() => {
    async function getUser() {
      const { data } = await supabase.auth.getSession();
      if (data.session) {
        setUser({
          id: data.session.user.id,
          email: data.session.user.email,
          role: "employee" // You would fetch the real role here
        });
      }
    }
    getUser();
  }, []);

  // 2. Poll for Messages (The "Mailbox Check")
  useEffect(() => {
    if (!user) return;

    const fetchHistory = async () => {
      try {
        const data = await apiGet("/chat/history");
        if (data.messages) {
          setMessages(data.messages);
        }
      } catch (e) {
        console.error("Polling error", e);
      }
    };

    fetchHistory(); // Initial load
    const interval = setInterval(fetchHistory, 3000); // Check every 3 seconds

    return () => clearInterval(interval);
  }, [user]);

  // Auto-scroll
  useEffect(() => {
    if (ref.current) ref.current.scrollTop = ref.current.scrollHeight;
  }, [messages]);

  async function send() {
    if (!text.trim() || !user) return;
    const content = text.trim();
    const is_bot = /trinity/i.test(content) || true; // Always trigger bot for now
    
    // Optimistic UI update
    const tempMsg = { content, user_id: user.id, from_bot: false };
    setMessages((m) => [...m, tempMsg]);
    setText("");

    try {
      await apiPost("/chat/send", { content, is_bot_command: is_bot });
    } catch (e) {
      console.error("Send failed", e);
      alert("Failed to send message");
    }
  }

  return (
    <div className="flex h-screen bg-[#020617] text-white">
      <AppSidebar user={user} />
      <main className="flex-1 p-6 flex flex-col">
        <h1 className="text-2xl font-bold mb-4 text-cyan-400">Trinity Chat</h1>
        
        <div ref={ref} className="flex-1 overflow-auto space-y-4 p-4 border border-slate-800 rounded-xl mb-4 bg-[#0f172a]">
          {messages.length === 0 && <div className="text-slate-500 text-center mt-10">Say "Hello Trinity" to start...</div>}
          
          {messages.map((m, i) => (
            <div key={i} className={`flex ${m.from_bot ? "justify-start" : "justify-end"}`}>
              <div className={`max-w-[70%] p-3 rounded-lg text-sm ${
                m.from_bot 
                  ? "bg-slate-800 text-slate-200 rounded-tl-none" 
                  : "bg-cyan-600 text-white rounded-tr-none"
              }`}>
                {m.content}
              </div>
            </div>
          ))}
        </div>

        <div className="flex items-center gap-2">
          <input 
            value={text} 
            onChange={(e) => setText(e.target.value)} 
            onKeyDown={(e) => e.key === 'Enter' && send()}
            className="flex-1 px-4 py-3 bg-slate-900 border border-slate-700 rounded-lg focus:ring-2 focus:ring-cyan-500 outline-none" 
            placeholder="Message Trinity..." 
          />
          <button onClick={send} className="px-6 py-3 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-lg transition-colors">
            Send
          </button>
        </div>
      </main>
    </div>
  );
}
