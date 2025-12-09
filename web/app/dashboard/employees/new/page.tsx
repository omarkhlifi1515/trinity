import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import NewEmployeePage from '@/components/employees/NewEmployeePage'

export default async function NewEmployee() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <NewEmployeePage />
}

