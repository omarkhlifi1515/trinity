import { useEffect, useState, useCallback } from 'react'
import { View, StyleSheet, ScrollView, RefreshControl } from 'react-native'
import { Text, Card, FAB, Chip, ActivityIndicator } from 'react-native-paper'
import { Briefcase, Calendar } from 'lucide-react-native'
import { useAuthStore } from '@/store/authStore'
import { canAddTasks } from '@/lib/roles'
import { supabase } from '@/lib/supabase'
import { format } from 'date-fns'

interface Task {
  id: string
  title: string
  description: string | null
  status: string
  priority: string
  due_date: string | null
  created_at: string
}

export default function TasksScreen() {
  const { user } = useAuthStore()
  const canAdd = canAddTasks(user)
  const [tasks, setTasks] = useState<Task[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  const fetchTasks = async () => {
    try {
      // Logic depends on RLS policies.
      // "Users can view tasks assigned to them"
      // "Managers can view tasks in their department"

      // We just select * and let RLS handle visibility filtering? 
      // Or we can be explicit. Ideally RLS handles it.
      const { data, error } = await supabase
        .from('tasks')
        .select('*')
        .order('created_at', { ascending: false })

      if (error) throw error
      setTasks(data || [])
    } catch (error) {
      console.error('Error fetching tasks:', error)
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => {
    fetchTasks()
  }, [])

  const onRefresh = useCallback(() => {
    setRefreshing(true)
    fetchTasks()
  }, [])

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed': return '#10b981' // success
      case 'in_progress': return '#3b82f6' // primary
      case 'pending': return '#f59e0b' // warning
      case 'cancelled': return '#ef4444' // error
      default: return '#6b7280'
    }
  }

  return (
    <View style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
      >
        {loading ? (
          <ActivityIndicator size="large" style={{ marginTop: 20 }} />
        ) : tasks.length === 0 ? (
          <Card style={styles.card}>
            <Card.Content>
              <View style={styles.emptyState}>
                <Briefcase size={48} color="#9ca3af" />
                <Text variant="titleLarge" style={styles.emptyTitle}>
                  No Tasks Yet
                </Text>
                <Text variant="bodyMedium" style={styles.emptyText}>
                  {canAdd ? 'Create your first task to get started' : 'No tasks assigned yet'}
                </Text>
              </View>
            </Card.Content>
          </Card>
        ) : (
          tasks.map(task => (
            <Card key={task.id} style={styles.taskCard}>
              <Card.Content>
                <View style={styles.taskHeader}>
                  <Text variant="titleMedium" style={styles.taskTitle}>{task.title}</Text>
                  <Chip
                    textStyle={{ color: '#fff', fontSize: 10 }}
                    style={{ backgroundColor: getStatusColor(task.status), height: 24 }}
                  >
                    {task.status.replace('_', ' ').toUpperCase()}
                  </Chip>
                </View>
                {task.description && (
                  <Text variant="bodyMedium" style={styles.description} numberOfLines={2}>
                    {task.description}
                  </Text>
                )}
                <View style={styles.footer}>
                  <View style={styles.dateContainer}>
                    <Calendar size={14} color="#6b7280" />
                    <Text variant="bodySmall" style={styles.dateText}>
                      {task.due_date ? format(new Date(task.due_date), 'MMM d, yyyy') : 'No due date'}
                    </Text>
                  </View>
                </View>
              </Card.Content>
            </Card>
          ))
        )}
        <View style={{ height: 80 }} />
      </ScrollView>
      {canAdd && (
        <FAB
          icon="plus"
          style={styles.fab}
          onPress={() => {
            // Navigation to create task screen usually
          }}
        />
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
  },
  scrollView: {
    flex: 1,
    padding: 16,
  },
  card: {
    elevation: 2,
    backgroundColor: '#fff',
  },
  taskCard: {
    marginBottom: 12,
    elevation: 2,
    backgroundColor: '#fff',
  },
  taskHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  taskTitle: {
    flex: 1,
    fontWeight: 'bold',
    marginRight: 8,
  },
  description: {
    color: '#4b5563',
    marginBottom: 12,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  dateContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  dateText: {
    color: '#6b7280',
  },
  emptyState: {
    alignItems: 'center',
    padding: 32,
    gap: 16,
  },
  emptyTitle: {
    fontWeight: 'bold',
    color: '#111827',
  },
  emptyText: {
    color: '#6b7280',
    textAlign: 'center',
  },
  fab: {
    position: 'absolute',
    margin: 16,
    right: 0,
    bottom: 0,
    backgroundColor: '#3b82f6',
  },
})
