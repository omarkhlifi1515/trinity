'use client'

import { useEffect, useState } from 'react'
import { Users, Briefcase, Calendar, CalendarDays, MessageSquare, Building2 } from 'lucide-react'

interface User {
  id: string
  email: string
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

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    try {
      // Load stats from your data source
      // These are placeholders - implement based on your storage solution
      // For now, stats are set to 0
    } catch (error) {
      console.error('Error loading stats:', error)
    }
  }

  return (
    <div className="p-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Dashboard</h1>
        <p className="text-gray-600">Welcome back, {user.email?.split('@')[0]}!</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <StatCard
          title="Employees"
          value={stats.employees}
          icon={Users}
          color="blue"
          href="/dashboard/employees"
        />
        <StatCard
          title="Departments"
          value={stats.departments}
          icon={Building2}
          color="purple"
          href="/dashboard/departments"
        />
        <StatCard
          title="Tasks"
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
          title="Leaves"
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
          <a
            href="/dashboard/employees/new"
            className="p-4 border border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors"
          >
            <Users className="w-6 h-6 text-blue-600 mb-2" />
            <h3 className="font-medium text-gray-900">Add Employee</h3>
            <p className="text-sm text-gray-600">Create a new employee record</p>
          </a>
          <a
            href="/dashboard/tasks/new"
            className="p-4 border border-gray-200 rounded-lg hover:border-green-500 hover:bg-green-50 transition-colors"
          >
            <Briefcase className="w-6 h-6 text-green-600 mb-2" />
            <h3 className="font-medium text-gray-900">Create Task</h3>
            <p className="text-sm text-gray-600">Assign a new task</p>
          </a>
          <a
            href="/dashboard/attendance/mark"
            className="p-4 border border-gray-200 rounded-lg hover:border-orange-500 hover:bg-orange-50 transition-colors"
          >
            <Calendar className="w-6 h-6 text-orange-600 mb-2" />
            <h3 className="font-medium text-gray-900">Mark Attendance</h3>
            <p className="text-sm text-gray-600">Record attendance</p>
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

