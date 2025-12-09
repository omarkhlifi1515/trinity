import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import AttendanceContent from '@/components/attendance/AttendanceContent'

export default async function AttendancePage() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <AttendanceContent />
}

