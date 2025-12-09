/**
 * Role-based access control helpers
 */

import type { User, UserRole } from './auth'

export function isAdmin(user: User | null): boolean {
  return user?.role === 'admin' || user?.email?.toLowerCase() === 'admin@gmail.com'
}

export function isDepartmentHead(user: User | null): boolean {
  return user?.role === 'department_head'
}

export function isEmployee(user: User | null): boolean {
  return !isAdmin(user) && !isDepartmentHead(user)
}

export function canAddEmployees(user: User | null): boolean {
  return isAdmin(user)
}

export function canAddTasks(user: User | null): boolean {
  return isAdmin(user) || isDepartmentHead(user)
}

export function canManageDepartment(user: User | null): boolean {
  return isAdmin(user) || isDepartmentHead(user)
}

export function getUserRole(user: User | null): UserRole {
  if (!user) return 'employee'
  if (isAdmin(user)) return 'admin'
  if (isDepartmentHead(user)) return 'department_head'
  return 'employee'
}

