import { View, StyleSheet, ScrollView } from 'react-native'
import { Text, Card, FAB } from 'react-native-paper'
import { Users } from 'lucide-react-native'
import { useAuthStore } from '@/store/authStore'
import { canAddEmployees } from '@/lib/roles'

export default function EmployeesScreen() {
  const { user } = useAuthStore()
  const canAdd = canAddEmployees(user)
  
  return (
    <View style={styles.container}>
      <ScrollView style={styles.scrollView}>
        <Card style={styles.card}>
          <Card.Content>
            <View style={styles.emptyState}>
              <Users size={48} color="#9ca3af" />
              <Text variant="titleLarge" style={styles.emptyTitle}>
                No Employees Yet
              </Text>
              <Text variant="bodyMedium" style={styles.emptyText}>
                {canAdd ? 'Add your first employee to get started' : 'No employees available'}
              </Text>
            </View>
          </Card.Content>
        </Card>
      </ScrollView>
      {canAdd && (
        <FAB
          icon="plus"
          style={styles.fab}
          onPress={() => {}}
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

