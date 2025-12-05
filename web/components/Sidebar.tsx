"use client"
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import React from 'react'

/**
 * Dark/Cyberpunk vertical sidebar.
 * Tailwind classes create a slate/teal neon look.
 */
export default function Sidebar() {
  const path = usePathname()

  const linkClass = (href: string) =>
    `block px-4 py-3 rounded-md transition-colors duration-150 ${
      path === href
        ? 'bg-slate-800 text-cyan-300 ring-1 ring-cyan-500'
        : 'text-slate-300 hover:bg-slate-700 hover:text-cyan-200'
    }`

  return (
    <aside className="w-64 min-h-screen bg-slate-900 text-slate-200 border-r border-slate-800">
      <div className="p-4 flex items-center gap-3 border-b border-slate-800">
        <div className="w-10 h-10 bg-gradient-to-br from-cyan-500 to-violet-600 rounded-md flex items-center justify-center text-black font-bold">T</div>
        <div>
          <div className="text-lg font-semibold">Trinity</div>
          <div className="text-xs text-slate-400">Command System</div>
        </div>
      </div>

      <nav className="p-4 space-y-2">
        <Link href="/dashboard" className={linkClass('/dashboard')}>
          Dashboard
        </Link>
        <Link href="/chat" className={linkClass('/chat')}>
          Chat
        </Link>
      </nav>
    </aside>
  )
}
