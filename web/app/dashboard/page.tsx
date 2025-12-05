"use client"
import React, { useEffect, useState } from 'react'
import supabase from '../../lib/supabaseClient'
import { useRouter } from 'next/navigation'
import Sidebar from '../../components/Sidebar'

type Stats = { total_employees: number }

export default function DashboardPage() {
  const router = useRouter()
  const [sessionChecked, setSessionChecked] = useState(false)
  const [stats, setStats] = useState<Stats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Check for a logged-in session on mount. If missing, redirect to /login.
  useEffect(() => {
    let mounted = true
    ;(async () => {
      const { data, error } = await supabase.auth.getSession()
      if (!mounted) return
      if (error) {
        console.error('Session check failed', error)
        router.push('/login')
        return
      }

      if (!data.session) {
        router.push('/login')
        return
      }

      setSessionChecked(true)
    })()

    return () => {
      mounted = false
    }
  }, [router])

  // Fetch stats from Python backend once session is verified.
  useEffect(() => {
    if (!sessionChecked) return

    const fetchStats = async () => {
      setLoading(true)
      setError(null)
      try {
        const backend = process.env.NEXT_PUBLIC_API_URL || ''
        const res = await fetch(`${backend}/dashboard/stats`)
        if (!res.ok) throw new Error('Failed to load stats')
        const json = await res.json()
        setStats(json)
      } catch (err: any) {
        setError(err?.message || 'Error fetching stats')
      } finally {
        setLoading(false)
      }
    }

    fetchStats()
  }, [sessionChecked])

  return (
    <div className="flex">
      <Sidebar />

      <main className="flex-1 min-h-screen p-8 bg-gradient-to-b from-[#02030a] via-[#020615] to-[#051024] text-slate-100">
        <h2 className="text-3xl font-bold mb-6 text-cyan-300">Dashboard</h2>

        {loading && <div className="text-slate-400">Loading stats…</div>}
        {error && <div className="text-rose-400">{error}</div>}

        {stats && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="p-6 rounded-xl bg-gradient-to-br from-slate-800 to-slate-900 ring-1 ring-slate-700">
              <div className="text-sm text-slate-400">Total Employees</div>
              <div className="mt-2 text-4xl font-bold text-cyan-300">{stats.total_employees}</div>
              <div className="mt-3 text-xs text-slate-500">Profiles stored in Supabase</div>
            </div>

            <div className="p-6 rounded-xl bg-gradient-to-br from-slate-800 to-slate-900 ring-1 ring-slate-700">
              <div className="text-sm text-slate-400">Workspace</div>
              <div className="mt-2 text-2xl font-semibold text-violet-300">Trinity Command</div>
              <div className="mt-3 text-xs text-slate-500">Manage sectors, grades, and assistants</div>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}
