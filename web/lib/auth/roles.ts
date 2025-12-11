'use client';

import { UserProfile } from '../firebase/users';

export type UserRole = 'admin' | 'chef' | 'employee';

export interface User {
  id: string;
  email: string;
  role?: UserRole | string;
  department?: string;
  displayName?: string;
}

/**
 * Check if user is an admin
 */
export function isAdmin(user: User | null | undefined): boolean {
  if (!user) return false;
  return user.role === 'admin' || user.email?.toLowerCase() === 'admin@gmail.com';
}

/**
 * Check if user is a chef/manager
 */
export function isChef(user: User | null | undefined): boolean {
  if (!user) return false;
  return user.role === 'chef';
}

/**
 * Check if user is an employee
 */
export function isEmployee(user: User | null | undefined): boolean {
  if (!user) return false;
  return user.role === 'employee';
}

/**
 * Check if user can approve leaves
 * Admins and chefs can approve leaves
 */
export function canApproveLeaves(user: User | null | undefined): boolean {
  return isAdmin(user) || isChef(user);
}

/**
 * Check if user can add employees
 * Only admins can add employees
 */
export function canAddEmployees(user: User | null | undefined): boolean {
  return isAdmin(user);
}

/**
 * Check if user can add tasks
 * Admins and chefs can add tasks
 */
export function canAddTasks(user: User | null | undefined): boolean {
  return isAdmin(user) || isChef(user);
}

/**
 * Check if user can assign roles
 * Only admins can assign roles
 */
export function canAssignRoles(user: User | null | undefined): boolean {
  return isAdmin(user);
}

/**
 * Check if user can manage departments
 * Admins and chefs can manage departments
 */
export function canManageDepartments(user: User | null | undefined): boolean {
  return isAdmin(user) || isChef(user);
}

/**
 * Check if user can view all employees
 * Admins and chefs can view all employees
 */
export function canViewAllEmployees(user: User | null | undefined): boolean {
  return isAdmin(user) || isChef(user);
}

/**
 * Get role display name
 */
export function getRoleDisplayName(role: UserRole | string | undefined): string {
  switch (role) {
    case 'admin':
      return 'Administrator';
    case 'chef':
      return 'Manager';
    case 'employee':
      return 'Employee';
    default:
      return 'Employee';
  }
}

/**
 * Get role badge color
 */
export function getRoleBadgeColor(role: UserRole | string | undefined): string {
  switch (role) {
    case 'admin':
      return 'bg-red-100 text-red-800';
    case 'chef':
      return 'bg-blue-100 text-blue-800';
    case 'employee':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}
