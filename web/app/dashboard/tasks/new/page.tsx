import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import NewTaskPage from '@/components/tasks/NewTaskPage'

export default async function NewTask() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <NewTaskPage />
}

