import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import SignupPage from '@/components/auth/SignupPage'

export default async function Signup() {
  const user = await getCurrentUser()

  if (user) {
    redirect('/dashboard')
  }

  return <SignupPage />
}

