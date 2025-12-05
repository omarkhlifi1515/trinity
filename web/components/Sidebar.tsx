"use client"
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import React from 'react'

export default function Sidebar() {
  const path = usePathname()

  const navItems = [
    { name: 'Dashboard', href: '/dashboard', icon: '📊' },
    { name: 'Toolbox', href: '/toolbox', icon: '🧰' },
    { name: 'Trinity Chat', href: '/chat', icon: '🤖' },
  ]

  return (
    <aside className="w-64 min-h-screen bg-[#0b0e14] border-r border-slate-800 flex flex-col">
      {/* Brand Header */}
      <div className="p-6 flex items-center gap-3 border-b border-slate-800/50">
        <div className="w-8 h-8 bg-gradient-to-tr from-cyan-500 to-blue-600 rounded-lg flex items-center justify-center text-white font-bold shadow-lg shadow-cyan-500/20">
          T
        </div>
        <div>
          <div className="text-sm font-bold text-slate-100 tracking-wide">TRINITY</div>
          <div className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold">Workspace</div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-1">
        {navItems.map((item) => {
          const isActive = path === item.href
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-all duration-200 group ${
                isActive
                  ? 'bg-cyan-500/10 text-cyan-400'
                  : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
              }`}
            >
              <span className={`text-lg opacity-80 group-hover:opacity-100 ${isActive ? 'scale-110' : ''}`}>
                {item.icon}
              </span>
              {item.name}
            </Link>
          )
        })}
      </nav>

      {/* User Footer (Optional Placeholder) */}
      <div className="p-4 border-t border-slate-800/50">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-slate-700 border border-slate-600"></div>
          <div className="text-xs">
            <div className="text-slate-200 font-medium">Admin User</div>
            <div className="text-slate-500">admin@trinity.ai</div>
          </div>
        </div>
      </div>
    </aside>
  )
}
