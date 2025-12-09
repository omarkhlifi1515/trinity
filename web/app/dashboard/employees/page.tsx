import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import EmployeesContent from '@/components/employees/EmployeesContent'

export default async function EmployeesPage() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <EmployeesContent />
}

