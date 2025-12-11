'use client';

import { AuthGuard } from '@/components/auth/AuthGuard'
import Sidebar from '@/components/layout/Sidebar'
import { useEffect, useState } from 'react'
import { FirebaseAuthClient } from '@/lib/firebase/auth'

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const [user, setUser] = useState<any>(null)

  useEffect(() => {
    // Listen to auth state
    const unsubscribe = FirebaseAuthClient.onAuthStateChanged((firebaseUser) => {
      if (firebaseUser) {
        setUser({
          id: firebaseUser.uid,
          email: firebaseUser.email || '',
        })
        console.log('Dashboard layout: User is PRESENT:', firebaseUser.email)
      } else {
        setUser(null)
        console.log('Dashboard layout: User is MISSING!')
      }
    })

    return () => unsubscribe()
  }, [])

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50">
        <Sidebar user={user} />
        <main className="lg:pl-64">
          {children}
        </main>
      </div>
    </AuthGuard>
  )
}

