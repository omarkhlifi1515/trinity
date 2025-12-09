import { View, StyleSheet, ScrollView } from 'react-native'
import { Text, Card, Button } from 'react-native-paper'
import { Calendar } from 'lucide-react-native'

export default function AttendanceScreen() {
  return (
    <View style={styles.container}>
      <ScrollView style={styles.scrollView}>
        <Card style={styles.card}>
          <Card.Content>
            <View style={styles.emptyState}>
              <Calendar size={48} color="#9ca3af" />
              <Text variant="titleLarge" style={styles.emptyTitle}>
                No Attendance Records
              </Text>
              <Text variant="bodyMedium" style={styles.emptyText}>
                Mark your attendance to get started
              </Text>
              <Button
                mode="contained"
                style={styles.button}
                onPress={() => {}}
              >
                Mark Attendance
              </Button>
            </View>
          </Card.Content>
        </Card>
      </ScrollView>
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
  button: {
    marginTop: 8,
  },
})

