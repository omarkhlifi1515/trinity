import { View, StyleSheet, ScrollView, TouchableOpacity } from 'react-native'
import { Text, Card, Avatar } from 'react-native-paper'
import { useRouter } from 'expo-router'
import { useAuthStore } from '@/store/authStore'
import { Users, Briefcase, Calendar, CalendarDays, Building2, MessageSquare } from 'lucide-react-native'

export default function DashboardScreen() {
  const router = useRouter()
  const { user } = useAuthStore()

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <View style={styles.userInfo}>
          <Avatar.Text size={48} label={user?.email?.charAt(0).toUpperCase() || 'U'} />
          <View style={styles.userDetails}>
            <Text variant="titleMedium" style={styles.userEmail}>
              {user?.email}
            </Text>
            <Text variant="bodySmall" style={styles.userRole}>
              Employee
            </Text>
          </View>
        </View>
      </View>

      <View style={styles.statsContainer}>
        <StatCard title="Employees" value="0" icon={Users} color="#3b82f6" onPress={() => router.push('/(tabs)/employees')} />
        <StatCard title="Departments" value="0" icon={Building2} color="#8b5cf6" onPress={() => router.push('/(tabs)/departments')} />
        <StatCard title="Tasks" value="0" icon={Briefcase} color="#10b981" onPress={() => router.push('/(tabs)/tasks')} />
        <StatCard title="Attendance" value="0" icon={Calendar} color="#f59e0b" onPress={() => router.push('/(tabs)/attendance')} />
        <StatCard title="Leaves" value="0" icon={CalendarDays} color="#ec4899" onPress={() => router.push('/(tabs)/leaves')} />
        <StatCard title="Messages" value="0" icon={MessageSquare} color="#6366f1" onPress={() => router.push('/(tabs)/messages')} />
      </View>

      <Card style={styles.welcomeCard}>
        <Card.Content>
          <Text variant="titleLarge" style={styles.welcomeTitle}>
            Welcome back!
          </Text>
          <Text variant="bodyMedium" style={styles.welcomeText}>
            Your dashboard is ready. Start managing your HR operations.
          </Text>
        </Card.Content>
      </Card>
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

