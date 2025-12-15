'use client'

import { useEffect, useState } from 'react'
import { Users, Briefcase, Calendar, CalendarDays, MessageSquare, Building2 } from 'lucide-react'

interface User {
  id: string
  email: string
  role?: 'admin' | 'chef' | 'employee'
  department?: string
  displayName?: string
}

interface DashboardContentProps {
  user: User
}

export default function DashboardContent({ user }: DashboardContentProps) {
  const [stats, setStats] = useState({
    employees: 0,
    tasks: 0,
    attendance: 0,
    leaves: 0,
    departments: 0,
    messages: 0,
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStats()
  }, [user])

  const loadStats = async () => {
    try {
      const { getAllUsers, getUsersByDepartment } = await import('@/lib/firebase/users')
      const { getAllTasks, getUserTasks, getCreatedTasks } = await import('@/lib/firebase/tasks')
      const { getAllLeaves, getUserLeaves } = await import('@/lib/firebase/leaves')

      let employeesCount = 0
      let tasksCount = 0
      let leavesCount = 0
      // Mock other stats for now 
      let departmentsCount = 0
      let attendanceCount = 0
      let messagesCount = 0

      if (user.role === 'admin') {
        const [users, tasks, leaves] = await Promise.all([
          getAllUsers(),
          getAllTasks(),
          getAllLeaves()
        ])
        employeesCount = users.length
        tasksCount = tasks.length
        leavesCount = leaves.filter(l => l.status === 'pending').length
      } else if (user.role === 'chef') {
        if (user.department) {
          const users = await getUsersByDepartment(user.department)
          employeesCount = users.length
        }
        const [tasks, leaves] = await Promise.all([
          getCreatedTasks(user.id),
          getAllLeaves()
        ])
        // For Chef, assume they see tasks they created. 
        // Also they might have tasks assigned TO them.
        const assignedTasks = await getUserTasks(user.id);
        const uniqueTaskIds = new Set([...tasks.map(t => t.id), ...assignedTasks.map(t => t.id)])
        tasksCount = uniqueTaskIds.size

        // Leaves: simplistic view - all pending leaves count
        leavesCount = leaves.filter(l => l.status === 'pending').length
      } else {
        // Employee
        const [tasks, leaves] = await Promise.all([
          getUserTasks(user.id),
          getUserLeaves(user.id)
        ])
        tasksCount = tasks.filter(t => t.status !== 'completed').length
        leavesCount = leaves.length
      }

      setStats({
        employees: employeesCount,
        tasks: tasksCount,
        attendance: attendanceCount,
        leaves: leavesCount,
        departments: departmentsCount,
        messages: messagesCount,
      })
    } catch (error) {
      console.error('Error loading stats:', error)
    } finally {
      setLoading(false)
    }
  }

  const userRole = user.role || 'employee'
  const canAddEmp = userRole === 'admin'
  const canAddTask = userRole === 'admin' || userRole === 'chef'

  if (loading) {
    return (
      <div className="flex justify-center p-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="p-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Dashboard</h1>
        <p className="text-gray-600">Welcome back, {user.displayName || user.email?.split('@')[0]}!</p>
        {user.department && <span className="inline-block px-2 py-1 bg-gray-100 rounded text-xs text-gray-500 mt-1 uppercase tracking-wide">{user.department}</span>}
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        {(canAddEmp || userRole === 'chef') && (
          <StatCard
            title={canAddEmp ? "Employees" : "Team Members"}
            value={stats.employees}
            icon={Users}
            color="blue"
            href="/dashboard/employees"
          />
        )}

        {canAddEmp && (
          <StatCard
            title="Departments"
            value={stats.departments}
            icon={Building2}
            color="purple"
            href="/dashboard/departments"
          />
        )}

        <StatCard
          title={userRole === 'employee' ? "My Tasks" : "Tasks"}
          value={stats.tasks}
          icon={Briefcase}
          color="green"
          href="/dashboard/tasks"
        />
        <StatCard
          title="Attendance"
          value={stats.attendance}
          icon={Calendar}
          color="orange"
          href="/dashboard/attendance"
        />
        <StatCard
          title={userRole === 'employee' ? "My Leaves" : "Pending Leaves"}
          value={stats.leaves}
          icon={CalendarDays}
          color="pink"
          href="/dashboard/leaves"
        />
        <StatCard
          title="Messages"
          value={stats.messages}
          icon={MessageSquare}
          color="indigo"
          href="/dashboard/messages"
        />
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {canAddEmp && (
            <a
              href="/dashboard/employees/new"
              className="p-4 border border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors"
            >
              <Users className="w-6 h-6 text-blue-600 mb-2" />
              <h3 className="font-medium text-gray-900">Add Employee</h3>
              <p className="text-sm text-gray-600">Create a new employee record</p>
            </a>
          )}
          {canAddTask && (
            <a
              href="/dashboard/tasks/new"
              className="p-4 border border-gray-200 rounded-lg hover:border-green-500 hover:bg-green-50 transition-colors"
            >
              <Briefcase className="w-6 h-6 text-green-600 mb-2" />
              <h3 className="font-medium text-gray-900">Create Task</h3>
              <p className="text-sm text-gray-600">Assign a new task</p>
            </a>
          )}
          <a
            href="/dashboard/attendance/mark"
            className="p-4 border border-gray-200 rounded-lg hover:border-orange-500 hover:bg-orange-50 transition-colors"
          >
            <Calendar className="w-6 h-6 text-orange-600 mb-2" />
            <h3 className="font-medium text-gray-900">Mark Attendance</h3>
            <p className="text-sm text-gray-600">Record attendance</p>
          </a>
          <a
            href="/dashboard/leaves/new"
            className="p-4 border border-gray-200 rounded-lg hover:border-pink-500 hover:bg-pink-50 transition-colors"
          >
            <CalendarDays className="w-6 h-6 text-pink-600 mb-2" />
            <h3 className="font-medium text-gray-900">Request Leave</h3>
            <p className="text-sm text-gray-600">Submit a leave request</p>
          </a>
        </div>
      </div>
    </div>
  )
}

function StatCard({ title, value, icon: Icon, color, href }: {
  title: string
  value: number
  icon: any
  color: 'blue' | 'green' | 'purple' | 'orange' | 'pink' | 'indigo'
  href?: string
}) {
  const colorClasses = {
    blue: 'bg-blue-500',
    green: 'bg-green-500',
    purple: 'bg-purple-500',
    orange: 'bg-orange-500',
    pink: 'bg-pink-500',
    indigo: 'bg-indigo-500',
  }

  const content = (
    <div className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow cursor-pointer">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-gray-600 mb-1">{title}</p>
          <p className="text-3xl font-bold text-gray-900">{value}</p>
        </div>
        <div className={`${colorClasses[color]} p-3 rounded-lg`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
    </div>
  )

  if (href) {
    return (
      <a href={href}>
        {content}
      </a>
    )
  }

  return content
}
