"use client"
import React, { useState } from 'react'
import { supabase } from '../../lib/supabase'
import { useRouter } from 'next/navigation'
import Link from 'next/link'

export default function SignupPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      // 1. Create Auth User
      const { data, error: authError } = await supabase.auth.signUp({
        email,
        password,
        options: {
          data: { full_name: fullName, role: 'employee' }, // Metadata
        },
      })

      if (authError) throw authError

      // 2. Success message
      setSuccess(true)
    } catch (err: any) {
      setError(err.message || 'Failed to sign up')
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#020617] text-slate-200">
        <div className="w-full max-w-md p-8 bg-[#0f172a] rounded-2xl border border-slate-800 text-center">
          <div className="text-5xl mb-4">📧</div>
          <h2 className="text-2xl font-bold text-white mb-2">Check your email</h2>
          <p className="text-slate-400 mb-6">
            We sent a confirmation link to <span className="text-cyan-400">{email}</span>.
            <br />Click it to activate your account.
          </p>
          <Link href="/login" className="text-cyan-500 hover:underline">
            Back to Sign In
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#020617] text-slate-200">
      <div className="w-full max-w-md p-8 space-y-8 bg-[#0f172a] rounded-2xl border border-slate-800 shadow-2xl">
        <div className="text-center">
          <h2 className="text-3xl font-bold tracking-tight text-white">Create Account</h2>
          <p className="mt-2 text-sm text-slate-400">Join the Trinity Workspace</p>
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleSignup}>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-300">Full Name</label>
              <input
                type="text"
                required
                className="mt-1 block w-full rounded-lg bg-slate-900 border border-slate-700 px-4 py-3 text-slate-100 focus:ring-cyan-500 focus:border-cyan-500 outline-none"
                placeholder="John Doe"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300">Email</label>
              <input
                type="email"
                required
                className="mt-1 block w-full rounded-lg bg-slate-900 border border-slate-700 px-4 py-3 text-slate-100 focus:ring-cyan-500 focus:border-cyan-500 outline-none"
                placeholder="name@company.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300">Password</label>
              <input
                type="password"
                required
                className="mt-1 block w-full rounded-lg bg-slate-900 border border-slate-700 px-4 py-3 text-slate-100 focus:ring-cyan-500 focus:border-cyan-500 outline-none"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          {error && <div className="p-3 rounded-lg bg-red-900/30 border border-red-800 text-red-200 text-sm">{error}</div>}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 px-4 rounded-lg text-sm font-semibold text-white bg-gradient-to-r from-cyan-600 to-blue-600 hover:opacity-90 transition-all disabled:opacity-50"
          >
            {loading ? 'Creating account...' : 'Sign Up'}
          </button>

          <p className="text-center text-sm text-slate-500">
            Already have an account?{' '}
            <Link href="/login" className="font-medium text-cyan-500 hover:text-cyan-400">
              Sign in
            </Link>
          </p>
        </form>
      </div>
    </div>
  )
}
