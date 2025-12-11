import { View, StyleSheet, ScrollView, TouchableOpacity, RefreshControl } from 'react-native'
import { Text, Card, Avatar, ActivityIndicator } from 'react-native-paper'
import { useRouter } from 'expo-router'
import { useAuthStore } from '@/store/authStore'
import { Users, Briefcase, Calendar, CalendarDays, Building2, MessageSquare } from 'lucide-react-native'
import { getUserRole, isAdmin, isDepartmentHead } from '@/lib/roles'
import { useEffect, useState, useCallback } from 'react'
import { supabase } from '@/lib/supabase'

export default function DashboardScreen() {
  const router = useRouter()
  const { user } = useAuthStore()
  const role = getUserRole(user)
  const showEmployees = isAdmin(user)
  const showDepartments = isAdmin(user)

  const [stats, setStats] = useState({
    tasks: 0,
    leaves: 0,
    attendance: 0,
    messages: 0,
    employees: 0,
    departments: 0
  })
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  const fetchStats = async () => {
    if (!user) return

    try {
      // Parallel requests for better performance
      const promises = [
        supabase.from('tasks').select('*', { count: 'exact', head: true }).eq('assigned_to', user.id),
        supabase.from('leaves').select('*', { count: 'exact', head: true }).eq('user_id', user.id),
        // Messages unread
        supabase.from('messages').select('*', { count: 'exact', head: true }).eq('receiver_id', user.id).eq('is_read', false),
        // Attendance days present
        supabase.from('attendance').select('*', { count: 'exact', head: true }).eq('user_id', user.id),
      ]

      if (showEmployees) {
        promises.push(supabase.from('employees').select('*', { count: 'exact', head: true }))
        promises.push(supabase.from('departments').select('*', { count: 'exact', head: true }))
      }

      const results = await Promise.all(promises)

      setStats({
        tasks: results[0].count || 0,
        leaves: results[1].count || 0,
        messages: results[2].count || 0,
        attendance: results[3].count || 0,
        employees: showEmployees && results[4] ? (results[4].count || 0) : 0,
        departments: showEmployees && results[5] ? (results[5].count || 0) : 0,
      })

    } catch (error) {
      console.error('Error fetching stats:', error)
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => {
    fetchStats()
  }, [user])

  const onRefresh = useCallback(() => {
    setRefreshing(true)
    fetchStats()
  }, [user])

  return (
    <ScrollView
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      <View style={styles.header}>
        <View style={styles.userInfo}>
          <Avatar.Text size={48} label={user?.email?.charAt(0).toUpperCase() || 'U'} style={{ backgroundColor: '#6366f1' }} />
          <View style={styles.userDetails}>
            <Text variant="titleMedium" style={styles.userEmail}>
              {user?.email}
            </Text>
            <Text variant="bodySmall" style={styles.userRole}>
              {role === 'admin' ? 'Admin' : role === 'department_head' ? 'Department Head' : 'Employee'}
            </Text>
          </View>
        </View>
      </View>

      <View style={styles.statsContainer}>
        {showEmployees && (
          <StatCard title="Employees" value={stats.employees.toString()} icon={Users} color="#3b82f6" onPress={() => router.push('/(tabs)/employees')} />
        )}
        {showDepartments && (
          <StatCard title="Departments" value={stats.departments.toString()} icon={Building2} color="#8b5cf6" onPress={() => router.push('/(tabs)/departments')} />
        )}
        <StatCard title="My Tasks" value={stats.tasks.toString()} icon={Briefcase} color="#10b981" onPress={() => router.push('/(tabs)/tasks')} />
        <StatCard title="Attendance" value={stats.attendance.toString()} icon={Calendar} color="#f59e0b" onPress={() => router.push('/(tabs)/attendance')} />
        <StatCard title="My Leaves" value={stats.leaves.toString()} icon={CalendarDays} color="#ec4899" onPress={() => router.push('/(tabs)/leaves')} />
        <StatCard title="Unread Msgs" value={stats.messages.toString()} icon={MessageSquare} color="#6366f1" onPress={() => router.push('/(tabs)/messages')} />
      </View>

      <Card style={styles.welcomeCard}>
        <Card.Content>
          <Text variant="titleLarge" style={styles.welcomeTitle}>
            Welcome back!
          </Text>
          <Text variant="bodyMedium" style={styles.welcomeText}>
            Your dashboard is ready. Start managing your HR operations.
            {loading && " Loading stats..."}
          </Text>
        </Card.Content>
      </Card>

      <View style={{ height: 20 }} />
    </ScrollView>
  )
}

function StatCard({ title, value, icon: Icon, color, onPress }: {
  title: string
  value: string
  icon: any
  color: string
  onPress?: () => void
}) {
  const card = (
    <Card style={styles.statCard}>
      <Card.Content>
        <View style={styles.statContent}>
          <View>
            <Text variant="bodySmall" style={styles.statLabel}>
              {title}
            </Text>
            <Text variant="headlineMedium" style={styles.statValue}>
              {value}
            </Text>
          </View>
          <View style={[styles.statIcon, { backgroundColor: color + '20' }]}>
            <Icon size={24} color={color} />
          </View>
        </View>
      </Card.Content>
    </Card>
  )

  if (onPress) {
    return <TouchableOpacity onPress={onPress}>{card}</TouchableOpacity>
  }

  return card
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
  },
  userInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  userDetails: {
    gap: 4,
  },
  userEmail: {
    fontWeight: '600',
    color: '#111827',
  },
  userRole: {
    color: '#6b7280',
    textTransform: 'capitalize'
  },
  statsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: 16,
    gap: 12,
  },
  statCard: {
    width: '47%',
    elevation: 2,
    backgroundColor: '#fff',
    marginBottom: 4,
  },
  statContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  statLabel: {
    color: '#6b7280',
    marginBottom: 4,
  },
  statValue: {
    fontWeight: 'bold',
    color: '#111827',
  },
  statIcon: {
    width: 48,
    height: 48,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  welcomeCard: {
    margin: 16,
    elevation: 2,
    backgroundColor: '#fff',
  },
  welcomeTitle: {
    fontWeight: 'bold',
    marginBottom: 8,
    color: '#111827',
  },
  welcomeText: {
    color: '#6b7280',
  },
})
