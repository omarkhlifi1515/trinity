import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import TasksContent from '@/components/tasks/TasksContent'

export default async function TasksPage() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <TasksContent />
}

