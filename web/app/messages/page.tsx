"use client"
import React, { useEffect, useState } from 'react'
import Sidebar from '../../components/Sidebar'

type Conv = { user_id: string; last_message?: string; last_ts?: string }
type Msg = { id?: string; sender_id?: string; receiver_id?: string; content: string; created_at?: string }

export default function MessagesPage() {
  const [convs, setConvs] = useState<Conv[]>([])
  const [selected, setSelected] = useState<string | null>(null)
  const [messages, setMessages] = useState<Msg[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)

  async function loadConvs() {
    try {
      const backend = process.env.NEXT_PUBLIC_API_URL || ''
      const res = await fetch(`${backend}/messages/conversations`, { credentials: 'include' })
      if (!res.ok) return
      const json = await res.json()
      setConvs(json.data || [])
    } catch (e) {
      // ignore
    }
  }

  async function loadMessages(withId: string) {
    setLoading(true)
    try {
      const backend = process.env.NEXT_PUBLIC_API_URL || ''
      const res = await fetch(`${backend}/messages/${withId}`, { credentials: 'include' })
      if (!res.ok) return
      const json = await res.json()
      setMessages(json.data || [])
    } catch (e) {
      // ignore
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadConvs()
    const t = setInterval(() => {
      loadConvs()
      if (selected) loadMessages(selected)
    }, 5000)
    return () => clearInterval(t)
  }, [selected])

  async function sendMessage() {
    if (!selected || !input.trim()) return
    try {
      const backend = process.env.NEXT_PUBLIC_API_URL || ''
      const res = await fetch(`${backend}/messages`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ receiver_id: selected, content: input }),
      })
      if (!res.ok) throw new Error('send failed')
      setInput('')
      await loadMessages(selected)
      await loadConvs()
    } catch (e) {
      // ignore
    }
  }

  return (
    <div className="flex">
      <Sidebar />
      <main className="flex-1 min-h-screen p-6 bg-[#050505] text-slate-100 flex">
        <div className="w-80 bg-[#0b0e14] border-r border-slate-800 p-4 overflow-auto">
          <h3 className="text-sm text-slate-400 mb-3">Conversations</h3>
          <div className="space-y-2">
            {convs.map((c) => (
              <button key={c.user_id} onClick={() => { setSelected(c.user_id); loadMessages(c.user_id) }} className={`w-full text-left p-2 rounded-md ${selected === c.user_id ? 'bg-slate-800' : 'hover:bg-slate-900'}`}>
                <div className="font-medium">User {c.user_id}</div>
                <div className="text-xs text-slate-500">{c.last_message}</div>
              </button>
            ))}
          </div>
        </div>

        <div className="flex-1 p-4 flex flex-col">
          {!selected && <div className="text-slate-500">Select a conversation</div>}
          {selected && (
            <>
              <div className="flex-1 overflow-auto mb-4 space-y-3">
                {loading && <div className="text-slate-400">Loading...</div>}
                {messages.map((m) => (
                  <div key={m.id} className={`p-3 rounded-md max-w-2xl ${m.sender_id === selected ? 'ml-0 bg-slate-800' : 'ml-auto bg-cyan-900 text-cyan-100'}`}>
                    <div className="text-sm whitespace-pre-wrap">{m.content}</div>
                    <div className="text-xs text-slate-500 mt-1">{m.created_at}</div>
                  </div>
                ))}
              </div>

              <div className="flex gap-3">
                <input value={input} onChange={(e) => setInput(e.target.value)} className="flex-1 px-4 py-2 rounded-md bg-slate-900 border border-slate-800" />
                <button onClick={sendMessage} className="px-4 py-2 rounded-md bg-cyan-600">Send</button>
              </div>
            </>
          )}
        </div>
      </main>
    </div>
  )
}
