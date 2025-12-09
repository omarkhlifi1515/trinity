import { useEffect, useState } from 'react'
import { View, ActivityIndicator } from 'react-native'
import { useRouter } from 'expo-router'
import { getCurrentUser } from '@/lib/supabase'
import { useAuthStore } from '@/store/authStore'

export default function Index() {
  const router = useRouter()
  const { setUser, user } = useAuthStore()
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    checkUser()
  }, [])

  const checkUser = async () => {
    try {
      const currentUser = await getCurrentUser()
      
      if (currentUser) {
        setUser(currentUser)
        router.replace('/(tabs)/dashboard')
      } else {
        router.replace('/(auth)/login')
      }
    } catch (error) {
      console.error('Error checking user:', error)
      router.replace('/(auth)/login')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#fff' }}>
        <ActivityIndicator size="large" color="#3b82f6" />
      </View>
    )
  }

  return null
}

