'use client'

import { useEffect, useState } from 'react'
import { Calendar, CheckCircle2, XCircle, Clock, LogOut } from 'lucide-react'
import { getAttendanceByDate, checkIn, checkOut, AttendanceRecord } from '@/lib/firebase/attendance'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import { getUserProfile } from '@/lib/firebase/users'

export default function AttendanceContent() {
  const [attendance, setAttendance] = useState<AttendanceRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0])
  const [user, setUser] = useState<any>(null);
  const [isCheckedIn, setIsCheckedIn] = useState(false);

  useEffect(() => {
    loadUser();
  }, []);

  useEffect(() => {
    loadAttendance()
  }, [selectedDate])

  const loadUser = async () => {
    try {
      const u = FirebaseAuthClient.getCurrentUser();
      if (u) {
        const profile = await getUserProfile(u.uid);
        setUser({ ...u, ...profile });
        checkTodayStatus(u.uid);
      } else {
        FirebaseAuthClient.onAuthStateChanged(async (user) => {
          if (user) {
            const profile = await getUserProfile(user.uid);
            setUser({ ...user, ...profile });
            checkTodayStatus(user.uid);
          }
        });
      }
    } catch (e) { console.error(e); }
  }

  const checkTodayStatus = async (uid: string) => {
    // Check if user has a record for today to toggle button state
    const today = new Date().toISOString().split('T')[0];
    const records = await getAttendanceByDate(today);
    const myRecord = records.find(r => r.employeeId === uid);
    if (myRecord && !myRecord.checkOut) {
      setIsCheckedIn(true);
    } else {
      setIsCheckedIn(false);
    }
  }

  const loadAttendance = async () => {
    try {
      setLoading(true)
      const data = await getAttendanceByDate(selectedDate)
      setAttendance(data)
    } catch (error) {
      console.error('Error loading attendance:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAttendanceAction = async () => {
    if (!user) return;
    try {
      if (isCheckedIn) {
        await checkOut(user.uid);
        alert('Checked out successfully!');
        setIsCheckedIn(false); // Can check in again? Usually one shift per day logic, but simple toggle for now.
      } else {
        await checkIn(user.uid, user.displayName || user.email);
        alert('Checked in successfully!');
        setIsCheckedIn(true);
      }
      loadAttendance();
    } catch (error: any) {
      console.error('Error marking attendance:', error)
      alert(error.message || 'Failed to mark attendance')
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'present':
        return <CheckCircle2 className="w-5 h-5 text-green-600" />
      case 'absent':
        return <XCircle className="w-5 h-5 text-red-600" />
      case 'late':
        return <Clock className="w-5 h-5 text-orange-600" />
      default:
        return <Clock className="w-5 h-5 text-gray-400" />
    }
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Attendance</h1>
          <p className="text-gray-600">Track employee attendance</p>
        </div>
        <button
          onClick={handleAttendanceAction}
          className={`flex items-center gap-2 px-4 py-2 text-white rounded-lg transition-colors ${isCheckedIn ? 'bg-red-600 hover:bg-red-700' : 'bg-green-600 hover:bg-green-700'
            }`}
        >
          {isCheckedIn ? <LogOut className="w-5 h-5" /> : <CheckCircle2 className="w-5 h-5" />}
          {isCheckedIn ? 'Check Out' : 'Check In'}
        </button>
      </div>

      {/* Date Selector */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Select Date
        </label>
        <input
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
        />
      </div>

      {loading ? (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading attendance...</p>
        </div>
      ) : attendance.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <Calendar className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No Attendance Records</h3>
          <p className="text-gray-600 mb-6">No records found for this date.</p>
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
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Check In
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Check Out
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {attendance.map((record) => (
                <tr key={record.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">{record.employeeName || 'Unknown'}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      {getStatusIcon(record.status)}
                      <span className="text-sm text-gray-900 capitalize">{record.status}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {record.checkIn || '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {record.checkOut || '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
