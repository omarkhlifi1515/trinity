import { redirect } from 'next/navigation'
import { getCurrentUser } from '@/lib/auth/local-auth'
import Sidebar from '@/components/layout/Sidebar'

export default async function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const user = await getCurrentUser()

  if (!user) {
    redirect('/')
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Sidebar user={user} />
      <main className="lg:pl-64">
        {children}
      </main>
    </div>
  )
}

