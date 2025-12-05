"use client"
import React, { useEffect, useRef, useState } from 'react'
import supabase from '../../lib/supabaseClient'
import Sidebar from '../../components/Sidebar'

type Message = {
  id?: string
  user_id?: number | string | null
  role: 'user' | 'assistant'
  message: string
}

export default function ChatPage() {
  const [sessionChecked, setSessionChecked] = useState(false)
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const listRef = useRef<HTMLDivElement | null>(null)

  // session guard
  useEffect(() => {
    let mounted = true
    ;(async () => {
      const { data } = await supabase.auth.getSession()
      if (!mounted) return
      if (!data.session) {
        // Redirect client-side to login
        window.location.href = '/login'
        return
      }
      setSessionChecked(true)
    })()
    return () => {
      mounted = false
    }
  }, [])

  // Scroll to bottom when messages change
  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight
    }
  }, [messages])

  async function sendMessage(e?: React.FormEvent) {
    e?.preventDefault()
    if (!input.trim()) return
    setSending(true)
    setError(null)

    // Optimistic update: show user message immediately
    const userMsg: Message = { role: 'user', message: input }
    setMessages((m) => [...m, userMsg])
    setInput('')

    try {
      const backend = process.env.NEXT_PUBLIC_API_URL || ''
      const res = await fetch(`${backend}/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ user_id: null, message: userMsg.message }),
      })

      if (!res.ok) throw new Error('Failed to send message')
      const json = await res.json()

      const aiMsg: Message = { role: 'assistant', message: json.reply }
      setMessages((m) => [...m, aiMsg])
    } catch (err: any) {
      console.error(err)
      setError(err?.message || 'Send failed')
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="flex">
      <Sidebar />

      <main className="flex-1 min-h-screen p-6 bg-gradient-to-b from-[#02020a] via-[#021025] to-[#04112a] text-slate-100 flex flex-col">
        <h2 className="text-2xl font-bold mb-4 text-cyan-300">Chat — Trinity AI</h2>

        <div className="flex-1 flex flex-col bg-gradient-to-br from-slate-900 to-slate-800 rounded-xl p-4 ring-1 ring-slate-700">
          <div ref={listRef} className="flex-1 overflow-auto space-y-4 pr-2">
            {messages.length === 0 && <div className="text-slate-500">Say hello to Trinity AI.</div>}

            {messages.map((m, i) => (
              <div key={i} className={`p-3 rounded-md max-w-md ${m.role === 'user' ? 'ml-auto bg-cyan-900 text-cyan-100' : 'bg-slate-800 text-slate-100'}`}>
                <div className="text-sm leading-relaxed whitespace-pre-wrap">{m.message}</div>
              </div>
            ))}
          </div>

          <form onSubmit={(e) => sendMessage(e)} className="mt-4">
            <div className="flex gap-3">
              <input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Ask Trinity something..."
                className="flex-1 px-4 py-3 rounded-md bg-slate-800 border border-slate-700 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              />
              <button
                type="submit"
                disabled={sending}
                className="px-4 py-3 rounded-md bg-gradient-to-r from-cyan-500 to-violet-600 text-black font-semibold disabled:opacity-50"
              >
                Send
              </button>
            </div>
            {error && <div className="text-rose-400 mt-2">{error}</div>}
          </form>
        </div>
      </main>
    </div>
  )
}
