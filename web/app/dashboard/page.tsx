'use client';

import { useEffect, useState } from 'react'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import { getUserProfile } from '@/lib/firebase/users'
import DashboardContent from '@/components/dashboard/DashboardContent'

export default function DashboardPage() {
  const [user, setUser] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const unsubscribe = FirebaseAuthClient.onAuthStateChanged(async (firebaseUser) => {
      if (firebaseUser) {
        // Fetch user profile from Firestore
        const profile = await getUserProfile(firebaseUser.uid);

        if (profile) {
          const dashboardUser = {
            id: firebaseUser.uid,
            email: firebaseUser.email!,
            role: profile.role,
            department: profile.department,
            displayName: profile.displayName,
          };
          setUser(dashboardUser);
        } else {
          // Fallback if profile doesn't exist
          const dashboardUser = {
            id: firebaseUser.uid,
            email: firebaseUser.email!,
            role: 'employee',
            department: undefined,
          };
          setUser(dashboardUser);
        }
      } else {
        setUser(null);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    )
  }

  if (!user) {
    return null // AuthGuard will handle redirect
  }

  return <DashboardContent user={user} />
}

