import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import MessagesContent from '@/components/messages/MessagesContent'

export default async function MessagesPage() {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return <MessagesContent />
}

