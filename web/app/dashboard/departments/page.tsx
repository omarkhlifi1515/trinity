import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import DepartmentsContent from '@/components/departments/DepartmentsContent'

export default async function DepartmentsPage() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <DepartmentsContent />
}

