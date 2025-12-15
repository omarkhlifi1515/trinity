'use client'

import { useEffect, useState } from 'react'
import { CalendarDays, Plus, CheckCircle2, XCircle, Clock } from 'lucide-react'
import Link from 'next/link'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import { getUserProfile } from '@/lib/firebase/users'
import { getAllLeaves, getUserLeaves, Leave, updateLeaveStatus } from '@/lib/firebase/leaves'

export default function LeavesContent() {
  const [leaves, setLeaves] = useState<Leave[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<'all' | 'pending' | 'approved' | 'rejected'>('all')
  const [user, setUser] = useState<any>(null)
  const [canApprove, setCanApprove] = useState(false)
  const [canRequest, setCanRequest] = useState(true)

  useEffect(() => {
    loadUser()
  }, [])

  const loadUser = async () => {
    try {
      const firebaseUser = FirebaseAuthClient.getCurrentUser();
      // If not logged in yet, wait for auth state change?
      // Actually best practice is to assume auth guard wraps this page or rely on onAuthStateChanged
      // For simplicity, we trigger reload if we get user
      if (firebaseUser) {
        setupUser(firebaseUser);
      } else {
        FirebaseAuthClient.onAuthStateChanged((user) => {
          if (user) setupUser(user);
        });
      }
    } catch (e) {
      console.error(e);
    }
  }

  const setupUser = async (firebaseUser: any) => {
    const profile = await getUserProfile(firebaseUser.uid);
    const userWithRole = {
      id: firebaseUser.uid,
      email: firebaseUser.email!,
      role: profile?.role || 'employee',
      department: profile?.department,
    };
    setUser(userWithRole);
    setCanApprove(userWithRole.role === 'admin' || userWithRole.role === 'chef');

    loadData(userWithRole);
  }

  const loadData = async (currentUser: any) => {
    try {
      setLoading(true)
      let leavesData: Leave[] = [];

      if (currentUser.role === 'employee') {
        leavesData = await getUserLeaves(currentUser.id);
      } else {
        // Admin or Chef sees all (or filter by dept for chef later)
        leavesData = await getAllLeaves();
      }

      setLeaves(leavesData)
    } catch (error) {
      console.error('Error loading data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (leaveId: string, action: 'approved' | 'rejected') => {
    if (!user) return;
    try {
      await updateLeaveStatus(leaveId, action, user.id);
      // Reload data
      loadData(user);
    } catch (error) {
      console.error('Error approving leave:', error)
      alert('Error updating leave status')
    }
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
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Dates
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Reason
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
                  (new Date(leave.endDate).getTime() - new Date(leave.startDate).getTime()) / (1000 * 60 * 60 * 24)
                ) + 1
                return (
                  <tr key={leave.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 capitalize">
                      {leave.type}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(leave.startDate).toLocaleDateString()} - {new Date(leave.endDate).toLocaleDateString()}
                      <div className="text-xs text-gray-400">({days} days)</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">
                      {leave.reason}
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
                              onClick={() => handleApprove(leave.id, 'approved')}
                              className="text-green-600 hover:text-green-900 font-medium"
                            >
                              Approve
                            </button>
                            <span className="text-gray-300">|</span>
                            <button
                              onClick={() => handleApprove(leave.id, 'rejected')}
                              className="text-red-600 hover:text-red-900 font-medium"
                            >
                              Reject
                            </button>
                          </>
                        )}
                        {!canApprove && leave.status === 'pending' && (
                          <span className="text-gray-400">Pending Review</span>
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
