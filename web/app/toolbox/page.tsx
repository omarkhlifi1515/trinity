"use client"
import React, { useState } from 'react'
import Sidebar from '../../components/Sidebar'

// Toolbox page fetches tools assigned to the user's sector from the backend

export default function ToolboxPage() {
  const [tools, setTools] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  React.useEffect(() => {
    let mounted = true
    ;(async () => {
      setLoading(true)
      try {
        const backend = process.env.NEXT_PUBLIC_API_URL || ''
        const res = await fetch(`${backend}/toolbox/my-tools`, { credentials: 'include' })
        if (!mounted) return
        if (!res.ok) throw new Error('Failed to load tools')
        const json = await res.json()
        setTools(json.data || [])
      } catch (e: any) {
        setError(e?.message || 'Failed to load')
      } finally {
        setLoading(false)
      }
    })()
    return () => {
      mounted = false
    }
  }, [])

  return (
    <div className="flex bg-[#050505] min-h-screen font-sans">
      <Sidebar />

      <main className="flex-1 flex flex-col h-screen overflow-hidden px-8 py-6">
        <header className="mb-6">
          <h1 className="text-2xl font-semibold text-slate-100 flex items-center gap-3">🧰 Company Toolbox</h1>
          <p className="text-slate-500 text-sm mt-1">Tools assigned to your sector.</p>
        </header>

        <div className="flex-1 overflow-y-auto">
          {loading && <div className="text-slate-400">Loading...</div>}
          {error && <div className="text-rose-400">{error}</div>}

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {tools.map((t: any) => (
              <a key={t.id || t.name} href={t.url || '#'} target="_blank" rel="noreferrer" className="block p-4 bg-[#0f1117] rounded-lg border border-slate-800 hover:border-slate-700 transition">
                <div className="font-medium text-slate-100">{t.name}</div>
                {t.url && <div className="text-xs text-slate-500 mt-1">{t.url}</div>}
              </a>
            ))}
          </div>
        </div>
      </main>
    </div>
  )
}
