'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Calendar, ArrowLeft, CheckCircle } from 'lucide-react'
import Link from 'next/link'

export default function MarkAttendancePage() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [attendanceType, setAttendanceType] = useState<'checkin' | 'checkout'>('checkin')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      // TODO: Implement API call to mark attendance
      console.log('Marking attendance:', attendanceType)
      await new Promise(resolve => setTimeout(resolve, 1000))
      router.push('/dashboard/attendance')
    } catch (error) {
      console.error('Error marking attendance:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-8">
      <div className="mb-8">
        <Link
          href="/dashboard/attendance"
          className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Attendance
        </Link>
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <Calendar className="w-6 h-6 text-orange-600" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Mark Attendance</h1>
            <p className="text-gray-600">Record your check-in or check-out</p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow p-6 max-w-md">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-4">
              Attendance Type *
            </label>
            <div className="space-y-3">
              <label className="flex items-center p-4 border-2 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors">
                <input
                  type="radio"
                  name="attendanceType"
                  value="checkin"
                  checked={attendanceType === 'checkin'}
                  onChange={(e) => setAttendanceType('checkin')}
                  className="mr-3"
                />
                <div>
                  <div className="font-medium text-gray-900">Check In</div>
                  <div className="text-sm text-gray-600">Record your arrival time</div>
                </div>
              </label>
              <label className="flex items-center p-4 border-2 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors">
                <input
                  type="radio"
                  name="attendanceType"
                  value="checkout"
                  checked={attendanceType === 'checkout'}
                  onChange={(e) => setAttendanceType('checkout')}
                  className="mr-3"
                />
                <div>
                  <div className="font-medium text-gray-900">Check Out</div>
                  <div className="text-sm text-gray-600">Record your departure time</div>
                </div>
              </label>
            </div>
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-center gap-2 text-blue-800">
              <Calendar className="w-5 h-5" />
              <span className="font-medium">
                {new Date().toLocaleDateString('en-US', { 
                  weekday: 'long', 
                  year: 'numeric', 
                  month: 'long', 
                  day: 'numeric' 
                })}
              </span>
            </div>
            <div className="text-blue-700 mt-2">
              Current Time: {new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
            </div>
          </div>

          <div className="flex items-center justify-end gap-4 pt-4 border-t">
            <Link
              href="/dashboard/attendance"
              className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Cancel
            </Link>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                'Recording...'
              ) : (
                <>
                  <CheckCircle className="w-4 h-4" />
                  Mark {attendanceType === 'checkin' ? 'Check In' : 'Check Out'}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

