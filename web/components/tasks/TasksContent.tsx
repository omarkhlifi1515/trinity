'use client'

import { useEffect, useState } from 'react'
import { Briefcase, Plus, CheckCircle2, Circle, Clock } from 'lucide-react'
import Link from 'next/link'
import { Task, getAllTasks, getUserTasks, updateTask } from '@/lib/firebase/tasks'
import { FirebaseAuthClient } from '@/lib/firebase/auth'
import { getUserProfile } from '@/lib/firebase/users'

export default function TasksContent() {
  const [tasks, setTasks] = useState<Task[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<'all' | 'pending' | 'in-progress' | 'completed'>('all')
  const [user, setUser] = useState<any>(null)
  const [canAdd, setCanAdd] = useState(false)

  useEffect(() => {
    loadUser()
  }, [])

  const loadUser = async () => {
    try {
      const firebaseUser = FirebaseAuthClient.getCurrentUser();
      if (firebaseUser) {
        setupUser(firebaseUser);
      } else {
        FirebaseAuthClient.onAuthStateChanged((user) => {
          if (user) setupUser(user);
        });
      }
    } catch (error) {
      console.error('Error loading user:', error)
    }
  }

  const setupUser = async (firebaseUser: any) => {
    const profile = await getUserProfile(firebaseUser.uid);
    const userWithRole = {
      id: firebaseUser.uid,
      email: firebaseUser.email!,
      role: profile?.role || 'employee',
    };
    setUser(userWithRole);
    setCanAdd(userWithRole.role === 'admin' || userWithRole.role === 'chef');
    loadTasks(userWithRole);
  }

  const loadTasks = async (currentUser: any) => {
    try {
      setLoading(true)
      let data: Task[] = [];
      if (currentUser.role === 'admin' || currentUser.role === 'chef') {
        // Admin sees all, Chef sees all (simplified) or implementation could filter by chef's created
        data = await getAllTasks();
      } else {
        data = await getUserTasks(currentUser.id);
      }
      setTasks(data)
    } catch (error) {
      console.error('Error loading tasks:', error)
    } finally {
      setLoading(false)
    }
  }

  const filteredTasks = filter === 'all'
    ? tasks
    : tasks.filter(task => task.status === filter)

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle2 className="w-5 h-5 text-green-600" />
      case 'in-progress':
        return <Clock className="w-5 h-5 text-blue-600" />
      default:
        return <Circle className="w-5 h-5 text-gray-400" />
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high':
        return 'bg-red-100 text-red-800'
      case 'medium':
        return 'bg-yellow-100 text-yellow-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Tasks</h1>
          <p className="text-gray-600">Manage and track tasks</p>
        </div>
        {canAdd && (
          <Link
            href="/dashboard/tasks/new"
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-5 h-5" />
            Create Task
          </Link>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="mb-6 flex gap-2 border-b border-gray-200">
        {(['all', 'pending', 'in-progress', 'completed'] as const).map((status) => (
          <button
            key={status}
            onClick={() => setFilter(status)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${filter === status
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
          >
            {status.charAt(0).toUpperCase() + status.slice(1).replace('-', ' ')}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading tasks...</p>
        </div>
      ) : filteredTasks.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <Briefcase className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No Tasks Yet</h3>
          <p className="text-gray-600 mb-6">
            {canAdd ? 'Create your first task to get started' : 'No tasks assigned yet'}
          </p>
          {canAdd && (
            <Link
              href="/dashboard/tasks/new"
              className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="w-5 h-5" />
              Create Task
            </Link>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredTasks.map((task) => (
            <div key={task.id} className="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-2">
                  {getStatusIcon(task.status)}
                  <h3 className="font-semibold text-gray-900">{task.title}</h3>
                </div>
                <span className={`px-2 py-1 text-xs font-medium rounded ${getPriorityColor(task.priority)}`}>
                  {task.priority}
                </span>
              </div>
              {task.description && (
                <p className="text-sm text-gray-600 mb-4 line-clamp-2">{task.description}</p>
              )}
              <div className="flex items-center justify-between text-xs text-gray-500">
                {task.dueDate && <span>Due: {new Date(task.dueDate).toLocaleDateString()}</span>}
                <a href={`/dashboard/tasks/${task.id}`} className="text-blue-600 hover:text-blue-700">
                  View →
                </a>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
