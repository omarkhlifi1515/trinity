"use client"
import React, { useEffect, useState } from 'react'
import Sidebar from '../../components/Sidebar'

type User = any

export default function AdminPage() {
  const [users, setUsers] = useState<User[]>([])
  const [sectors, setSectors] = useState<any[]>([])
  const [grades, setGrades] = useState<any[]>([])

  useEffect(() => {
    loadAll()
  }, [])

  async function loadAll() {
    const backend = process.env.NEXT_PUBLIC_API_URL || ''
    try {
      const [uRes, sRes, gRes] = await Promise.all([
        fetch(`${backend}/admin/users`, { credentials: 'include' }),
        fetch(`${backend}/sectors`, { credentials: 'include' }),
        fetch(`${backend}/grades`, { credentials: 'include' }),
      ])
      if (uRes.ok) setUsers((await uRes.json()).data || [])
      if (sRes.ok) setSectors((await sRes.json()).data || [])
      if (gRes.ok) setGrades((await gRes.json()).data || [])
    } catch (e) {
      // ignore
    }
  }

  async function saveUser(u: User) {
    const backend = process.env.NEXT_PUBLIC_API_URL || ''
    try {
      const res = await fetch(`${backend}/admin/users/${u.id}`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sector_id: u.sector_id, grade_id: u.grade_id, role: u.role }),
      })
      if (res.ok) {
        await loadAll()
      }
    } catch (e) {
      // ignore
    }
  }

  return (
    <div className="flex">
      <Sidebar />
      <main className="flex-1 p-6 bg-[#050505] text-slate-100">
        <h2 className="text-2xl mb-4">Admin — Users</h2>
        <div className="overflow-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="text-slate-400 text-sm">
                <th className="p-2">Name</th>
                <th className="p-2">Email</th>
                <th className="p-2">Sector</th>
                <th className="p-2">Grade</th>
                <th className="p-2">Role</th>
                <th className="p-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} className="border-t border-slate-800">
                  <td className="p-2">{u.full_name || '—'}</td>
                  <td className="p-2">{u.email || '—'}</td>
                  <td className="p-2">
                    <select value={u.sector_id || ''} onChange={(e) => (u.sector_id = Number(e.target.value))} className="bg-[#0b0e14]">
                      <option value="">—</option>
                      {sectors.map((s) => (
                        <option key={s.id} value={s.id}>{s.name}</option>
                      ))}
                    </select>
                  </td>
                  <td className="p-2">
                    <select value={u.grade_id || ''} onChange={(e) => (u.grade_id = Number(e.target.value))} className="bg-[#0b0e14]">
                      <option value="">—</option>
                      {grades.map((g) => (
                        <option key={g.id} value={g.id}>{g.name}</option>
                      ))}
                    </select>
                  </td>
                  <td className="p-2">
                    <select value={u.role || 'employee'} onChange={(e) => (u.role = e.target.value)} className="bg-[#0b0e14]">
                      <option value="employee">employee</option>
                      <option value="admin">admin</option>
                    </select>
                  </td>
                  <td className="p-2">
                    <button onClick={() => saveUser(u)} className="px-3 py-1 bg-cyan-600 rounded-md">Save</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  )
}
