'use client';

import { useEffect, useState } from 'react'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import DashboardContent from '@/components/dashboard/DashboardContent'

export default function DashboardPage() {
  const [user, setUser] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const unsubscribe = FirebaseAuthClient.onAuthStateChanged((firebaseUser) => {
      if (firebaseUser) {
        // Transform Firebase user to the shape expected by DashboardContent
        const dashboardUser = {
          id: firebaseUser.uid,
          email: firebaseUser.email!,
          role: 'employee', // Default role, can be fetched from Firestore if needed
          department: undefined
        }
        setUser(dashboardUser)
      } else {
        setUser(null)
      }
      setLoading(false)
    })

    return () => unsubscribe()
  }, [])

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  if (!user) {
    return null // AuthGuard will handle redirect
  }

  return <DashboardContent user={user} />
}

