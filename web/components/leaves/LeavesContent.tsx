'use client'

import { useEffect, useState } from 'react'
import { CalendarDays, Plus, CheckCircle2, XCircle, Clock } from 'lucide-react'
import Link from 'next/link'
import { getLeaves, Leave, getEmployees, Employee } from '@/lib/storage/supabase-storage'
import { canApproveLeaves } from '@/lib/auth/roles'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import { getUserProfile } from '@/lib/firebase/users'

export default function LeavesContent() {
  const [leaves, setLeaves] = useState<Leave[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<'all' | 'pending' | 'approved' | 'rejected'>('all')
  const [user, setUser] = useState<any>(null)
  const [canApprove, setCanApprove] = useState(false)
  const [canRequest, setCanRequest] = useState(true)

  useEffect(() => {
    loadUser()
    loadData()
  }, [])

  const loadUser = async () => {
    try {
      const firebaseUser = FirebaseAuthClient.getCurrentUser();
      if (firebaseUser) {
        const profile = await getUserProfile(firebaseUser.uid);
        const userWithRole = {
          id: firebaseUser.uid,
          email: firebaseUser.email!,
          role: profile?.role || 'employee',
          department: profile?.department,
        };
        setUser(userWithRole);
        setCanApprove(canApproveLeaves(userWithRole));
        setCanRequest(true); // All users can request leaves
      }
    } catch (error) {
      console.error('Error loading user:', error)
    }
  }

  const loadData = async () => {
    try {
      setLoading(true)
      const [leavesData, employeesData] = await Promise.all([
        getLeaves(),
        getEmployees(),
      ])
      setLeaves(leavesData)
      setEmployees(employeesData)
    } catch (error) {
      console.error('Error loading data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (leaveId: string, action: 'approve' | 'reject') => {
    try {
      const res = await fetch(`/api/leaves/${leaveId}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action }),
      })

      if (res.ok) {
        await loadData() // Reload leaves
      } else {
        alert('Failed to update leave status')
      }
    } catch (error) {
      console.error('Error approving leave:', error)
      alert('Error updating leave status')
    }
  }

  const getEmployeeName = (employeeId: string) => {
    const employee = employees.find(e => e.id === employeeId)
    return employee?.name || employee?.email || 'Unknown'
  }

  const filteredLeaves = filter === 'all'
    ? leaves
    : leaves.filter(leave => leave.status === filter)

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'approved':
        return <CheckCircle2 className="w-5 h-5 text-green-600" />
      case 'rejected':
        return <XCircle className="w-5 h-5 text-red-600" />
      default:
        return <Clock className="w-5 h-5 text-yellow-600" />
    }
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Leave Management</h1>
          <p className="text-gray-600">
            {canApprove ? 'Manage employee leave requests' : 'View and request leaves'}
          </p>
        </div>
        {canRequest && (
          <Link
            href="/dashboard/leaves/new"
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-5 h-5" />
            Request Leave
          </Link>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="mb-6 flex gap-2 border-b border-gray-200">
        {(['all', 'pending', 'approved', 'rejected'] as const).map((status) => (
          <button
            key={status}
            onClick={() => setFilter(status)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${filter === status
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
          >
            {status.charAt(0).toUpperCase() + status.slice(1)}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading leaves...</p>
        </div>
      ) : filteredLeaves.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <CalendarDays className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No Leave Requests</h3>
          <p className="text-gray-600 mb-6">
            {canRequest ? 'Submit your first leave request' : 'No leave requests available'}
          </p>
          {canRequest && (
            <Link
              href="/dashboard/leaves/new"
              className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="w-5 h-5" />
              Request Leave
            </Link>
          )}
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Employee
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Dates
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Days
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredLeaves.map((leave) => {
                const days = Math.ceil(
                  (new Date(leave.end_date).getTime() - new Date(leave.start_date).getTime()) / (1000 * 60 * 60 * 24)
                ) + 1
                return (
                  <tr key={leave.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{getEmployeeName(leave.employee_id)}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 capitalize">
                      {leave.type}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(leave.start_date).toLocaleDateString()} - {new Date(leave.end_date).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {days} days
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2">
                        {getStatusIcon(leave.status)}
                        <span className="text-sm text-gray-900 capitalize">{leave.status}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex items-center gap-2">
                        {canApprove && leave.status === 'pending' && (
                          <>
                            <button
                              onClick={() => handleApprove(leave.id, 'approve')}
                              className="text-green-600 hover:text-green-900 font-medium"
                            >
                              Approve
                            </button>
                            <span className="text-gray-300">|</span>
                            <button
                              onClick={() => handleApprove(leave.id, 'reject')}
                              className="text-red-600 hover:text-red-900 font-medium"
                            >
                              Reject
                            </button>
                          </>
                        )}
                        {!canApprove && (
                          <a href={`/dashboard/leaves/${leave.id}`} className="text-blue-600 hover:text-blue-900">
                            View
                          </a>
                        )}
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

