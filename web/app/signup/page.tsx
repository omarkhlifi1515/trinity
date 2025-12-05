"use client"

import React, { useState } from 'react'
import supabase from '../../lib/supabaseClient'
import Link from 'next/link'

export default function Signup() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setMessage(null)
    setError(null)

    try {
      const res = await supabase.auth.signUp({ email, password })
      // supabase.v2 returns error on res.error
      if ((res as any).error) {
        setError((res as any).error.message || 'Sign up failed')
      } else {
        setMessage('Check your email for the confirmation link.')
      }
    } catch (err: any) {
      setError(err?.message || 'Sign up failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-slate-900 via-slate-950 to-black text-slate-100">
      <div className="w-full max-w-md p-8 rounded-xl bg-gradient-to-br from-[#071023] via-[#071428] to-[#0b0f1a] shadow-2xl ring-1 ring-slate-800">
        <h1 className="text-2xl font-bold mb-2 text-cyan-300">Trinity — Sign Up</h1>
        <p className="text-sm text-slate-400 mb-6">Create your company account.</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs text-slate-400 mb-1">Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
          </div>

          <div>
            <label className="block text-xs text-slate-400 mb-1">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 rounded-md bg-slate-800 border border-slate-700 text-slate-100 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
          </div>

          {error && <div className="text-sm text-rose-400">{error}</div>}
          {message && <div className="text-sm text-emerald-400">{message}</div>}

          <div className="flex items-center justify-between">
            <button
              type="submit"
              className="px-4 py-2 rounded-md bg-gradient-to-r from-cyan-500 to-violet-600 text-black font-semibold hover:opacity-90 disabled:opacity-50"
              disabled={loading}
            >
              {loading ? 'Creating…' : 'Sign up'}
            </button>
          </div>
        </form>

        <div className="mt-4 text-sm text-slate-400">
          Already have an account? <Link href="/login" className="text-cyan-300 hover:underline">Sign In</Link>
        </div>
      </div>
    </div>
  )
}
