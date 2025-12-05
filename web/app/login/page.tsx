"use client"
import React, { useState } from 'react'
import supabase from '../../lib/supabaseClient'
import { useRouter } from 'next/navigation'

export default function LoginPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      const res = await supabase.auth.signInWithPassword({ email, password })
      if (res.error) {
        setError(res.error.message)
      } else {
        // On success, navigate to dashboard
        router.push('/dashboard')
      }
    } catch (err: any) {
      setError(err?.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-slate-900 via-slate-950 to-black text-slate-100">
      <div className="w-full max-w-md p-8 rounded-xl bg-gradient-to-br from-[#071023] via-[#071428] to-[#0b0f1a] shadow-2xl ring-1 ring-slate-800">
        <h1 className="text-2xl font-bold mb-2 text-cyan-300">Trinity — Sign In</h1>
        <p className="text-sm text-slate-400 mb-6">Enter your company email and password.</p>

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

          <div className="flex items-center justify-between">
            <button
              type="submit"
              className="px-4 py-2 rounded-md bg-gradient-to-r from-cyan-500 to-violet-600 text-black font-semibold hover:opacity-90 disabled:opacity-50"
              disabled={loading}
            >
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
