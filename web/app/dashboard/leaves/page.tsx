import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import LeavesContent from '@/components/leaves/LeavesContent'

export default async function LeavesPage() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <LeavesContent />
}

