'use client'

import { usePathname, useRouter } from 'next/navigation'
import {
  LayoutDashboard,
  Users,
  Building2,
  Calendar,
  Briefcase,
  MessageSquare,
  CalendarDays,
  LogOut,
  Menu,
  X,
  Shield
} from 'lucide-react'
import { useState, useEffect } from 'react'
import { getUserProfile } from '@/lib/firebase/users'
import { isAdmin, getRoleDisplayName } from '@/lib/auth/roles'

interface User {
  id: string
  email: string
  role?: string
  department?: string
  displayName?: string
}

interface SidebarProps {
  user: User | null
}

const allNavigation = [
  { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard, roles: ['admin', 'chef', 'employee'] },
  { name: 'Employees', href: '/dashboard/employees', icon: Users, roles: ['admin', 'chef'] },
  { name: 'Departments', href: '/dashboard/departments', icon: Building2, roles: ['admin', 'chef'] },
  { name: 'Tasks', href: '/dashboard/tasks', icon: Briefcase, roles: ['admin', 'chef', 'employee'] },
  { name: 'Attendance', href: '/dashboard/attendance', icon: Calendar, roles: ['admin', 'chef', 'employee'] },
  { name: 'Leaves', href: '/dashboard/leaves', icon: CalendarDays, roles: ['admin', 'chef', 'employee'] },
  { name: 'Messages', href: '/dashboard/messages', icon: MessageSquare, roles: ['admin', 'chef', 'employee'] },
  { name: 'Admin', href: '/dashboard/admin', icon: Shield, roles: ['admin'] },
]

export default function Sidebar({ user: initialUser }: SidebarProps) {
  const pathname = usePathname()
  const router = useRouter()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [user, setUser] = useState<User | null>(initialUser)

  useEffect(() => {
    // Fetch full user profile with role
    const loadUserProfile = async () => {
      if (initialUser?.id) {
        const profile = await getUserProfile(initialUser.id);
        if (profile) {
          setUser({
            id: initialUser.id,
            email: initialUser.email,
            role: profile.role,
            department: profile.department,
            displayName: profile.displayName,
          });
        } else {
          setUser(initialUser);
        }
      }
    };
    loadUserProfile();
  }, [initialUser]);

  if (!user) return null;

  const handleSignOut = async () => {
    try {
      const { FirebaseAuthClient } = await import('@/lib/firebase/auth')
      await FirebaseAuthClient.logout()
      router.push('/')
      router.refresh()
    } catch (error) {
      console.error('Logout error:', error)
      router.push('/')
    }
  }

  // Filter navigation based on user role
  const userRole = user.role || 'employee';
  const navigation = allNavigation.filter(item =>
    item.roles.includes(userRole) || isAdmin(user)
  )

  const roleDisplay = getRoleDisplayName(userRole);

  return (
    <>
      {/* Mobile menu button */}
      <div className="lg:hidden fixed top-0 left-0 right-0 z-50 bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-900">Trinity HRM</h1>
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="p-2 rounded-md text-gray-600 hover:text-gray-900"
        >
          {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Sidebar */}
      <div className={`
        fixed inset-y-0 left-0 z-40 w-64 bg-white border-r border-gray-200 transform transition-transform duration-300 ease-in-out
        lg:translate-x-0
        ${mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="flex flex-col h-full pt-16 lg:pt-0">
          {/* Logo */}
          <div className="flex items-center px-6 py-4 border-b border-gray-200">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg flex items-center justify-center">
                <Building2 className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-lg font-bold text-gray-900">Trinity HRM</h1>
                <p className="text-xs text-gray-500">HR Management</p>
              </div>
            </div>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
            {navigation.map((item) => {
              const isActive = pathname === item.href
              return (
                <a
                  key={item.name}
                  href={item.href}
                  className={`
                    flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors
                    ${isActive
                      ? 'bg-blue-50 text-blue-700'
                      : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900'
                    }
                  `}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <item.icon className={`w-5 h-5 ${isActive ? 'text-blue-600' : 'text-gray-500'}`} />
                  {item.name}
                </a>
              )
            })}
          </nav>

          {/* User section */}
          <div className="border-t border-gray-200 p-4">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-purple-600 rounded-full flex items-center justify-center">
                <span className="text-white font-semibold text-sm">
                  {user?.email?.charAt(0).toUpperCase()}
                </span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 truncate">
                  {user?.email}
                </p>
                <p className="text-xs text-gray-500">{roleDisplay}</p>
              </div>
            </div>
            <button
              onClick={handleSignOut}
              className="w-full flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-lg transition-colors"
            >
              <LogOut className="w-4 h-4" />
              Sign Out
            </button>
          </div>
        </div>
      </div>

      {/* Overlay for mobile */}
      {mobileMenuOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-30 lg:hidden"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}
    </>
  )
}

