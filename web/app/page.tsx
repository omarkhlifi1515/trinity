import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import LoginPage from '@/components/auth/LoginPage'

export default async function Home() {
  const user = await getCurrentUser()

  // If user is logged in, redirect to dashboard
  if (user) {
    redirect('/dashboard')
  }

  // Show login page if no user
  return <LoginPage />
}

