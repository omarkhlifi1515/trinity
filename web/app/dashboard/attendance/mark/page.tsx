import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import MarkAttendancePage from '@/components/attendance/MarkAttendancePage'

export default async function MarkAttendance() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <MarkAttendancePage />
}

