/**
 * Supabase Data Storage for Trinity HRM
 * Replaces JSONBin.io with Supabase for better real-time sync and queries
 */

import { supabase } from '@/lib/supabase/client'

// Data Models (matching database schema)
export interface Employee {
  id: string
  name: string
  email: string
  phone?: string
  department?: string
  position?: string
  hire_date?: string
  created_at: string
  updated_at?: string
}

export interface Task {
  id: string
  title: string
  description?: string
  assigned_to?: string
  assigned_by: string
  status: 'pending' | 'in_progress' | 'completed' | 'cancelled'
  priority: 'low' | 'medium' | 'high' | 'urgent'
  due_date?: string
  created_at: string
  updated_at?: string
}

export interface Leave {
  id: string
  employee_id: string
  type: 'sick' | 'vacation' | 'personal' | 'emergency' | 'other'
  start_date: string
  end_date: string
  reason?: string
  status: 'pending' | 'approved' | 'rejected' | 'cancelled'
  approved_by?: string
  created_at: string
  updated_at?: string
}

export interface Message {
  id: string
  from_user: string
  to_user?: string
  subject: string
  content: string
  read: boolean
  created_at: string
  updated_at?: string
}

export interface Department {
  id: string
  name: string
  description?: string
  head_id?: string
  employee_count: number
  created_at: string
  updated_at?: string
}

export interface Attendance {
  id: string
  employee_id: string
  date: string
  check_in?: string
  check_out?: string
  status: 'present' | 'absent' | 'late' | 'half_day' | 'on_leave'
  notes?: string
  created_at: string
  updated_at?: string
}

// Employees
export async function getEmployees(): Promise<Employee[]> {
  const { data, error } = await supabase
    .from('employees')
    .select('*')
    .order('created_at', { ascending: false })

  if (error) {
    console.error('Error fetching employees:', error)
    return []
  }

  return data || []
}

export async function addEmployee(employee: Omit<Employee, 'id' | 'created_at' | 'updated_at'>): Promise<Employee | null> {
  try {
    const { data, error } = await supabase
      .from('employees')
      .insert({
        ...employee,
        id: crypto.randomUUID(), // Generate UUID for web
        created_at: new Date().toISOString(),
      })
      .select()
      .single()

    if (error) {
      console.error('Error adding employee:', error)
      return null
    }

    return data
  } catch (error: any) {
    console.error('Error adding employee:', error)
    return null
  }
}

export async function updateEmployee(id: string, updates: Partial<Employee>): Promise<boolean> {
  const { error } = await supabase
    .from('employees')
    .update(updates)
    .eq('id', id)

  if (error) {
    console.error('Error updating employee:', error)
    return false
  }

  return true
}

// Tasks
export async function getTasks(userId?: string): Promise<Task[]> {
  let query = supabase
    .from('tasks')
    .select('*')
    .order('created_at', { ascending: false })

  // If userId provided, filter to user's tasks
  if (userId) {
    // This will be handled by RLS, but we can also filter client-side
  }

  const { data, error } = await query

  if (error) {
    console.error('Error fetching tasks:', error)
    return []
  }

  return data || []
}

export async function addTask(task: Omit<Task, 'id' | 'created_at' | 'updated_at'>): Promise<Task | null> {
  try {
    const { data, error } = await supabase
      .from('tasks')
      .insert({
        ...task,
        id: generateUUID(),
        created_at: new Date().toISOString(),
      })
      .select()
      .single()

    if (error) {
      console.error('Error adding task:', error)
      return null
    }

    return data
  } catch (error: any) {
    console.error('Error adding task:', error)
    return null
  }
}

export async function updateTask(id: string, updates: Partial<Task>): Promise<boolean> {
  const { error } = await supabase
    .from('tasks')
    .update(updates)
    .eq('id', id)

  if (error) {
    console.error('Error updating task:', error)
    return false
  }

  return true
}

// Leaves
export async function getLeaves(userId?: string): Promise<Leave[]> {
  let query = supabase
    .from('leaves')
    .select('*')
    .order('created_at', { ascending: false })

  if (userId) {
    query = query.eq('employee_id', userId)
  }

  const { data, error } = await query

  if (error) {
    console.error('Error fetching leaves:', error)
    return []
  }

  return data || []
}

export async function addLeave(leave: Omit<Leave, 'id' | 'created_at' | 'updated_at'>): Promise<Leave | null> {
  try {
    const { data, error } = await supabase
      .from('leaves')
      .insert({
        ...leave,
        id: generateUUID(),
        created_at: new Date().toISOString(),
      })
      .select()
      .single()

    if (error) {
      console.error('Error adding leave:', error)
      return null
    }

    return data
  } catch (error: any) {
    console.error('Error adding leave:', error)
    return null
  }
}

export async function updateLeave(id: string, updates: Partial<Leave>): Promise<boolean> {
  const { error } = await supabase
    .from('leaves')
    .update(updates)
    .eq('id', id)

  if (error) {
    console.error('Error updating leave:', error)
    return false
  }

  return true
}

export async function approveLeave(leaveId: string, approverId: string): Promise<boolean> {
  return updateLeave(leaveId, { status: 'approved', approved_by: approverId })
}

export async function rejectLeave(leaveId: string, approverId: string): Promise<boolean> {
  return updateLeave(leaveId, { status: 'rejected', approved_by: approverId })
}

// Messages
export async function getMessages(userId?: string): Promise<Message[]> {
  let query = supabase
    .from('messages')
    .select('*')
    .order('created_at', { ascending: false })

  if (userId) {
    query = query.or(`from_user.eq.${userId},to_user.eq.${userId},to_user.is.null`)
  }

  const { data, error } = await query

  if (error) {
    console.error('Error fetching messages:', error)
    return []
  }

  return data || []
}

export async function addMessage(message: Omit<Message, 'id' | 'created_at' | 'updated_at' | 'read'>): Promise<Message | null> {
  try {
    const { data, error } = await supabase
      .from('messages')
      .insert({
        ...message,
        id: generateUUID(),
        read: false,
        created_at: new Date().toISOString(),
      })
      .select()
      .single()

    if (error) {
      console.error('Error adding message:', error)
      return null
    }

    return data
  } catch (error: any) {
    console.error('Error adding message:', error)
    return null
  }
}

export async function markMessageAsRead(messageId: string): Promise<boolean> {
  const { error } = await supabase
    .from('messages')
    .update({ read: true })
    .eq('id', messageId)

  if (error) {
    console.error('Error marking message as read:', error)
    return false
  }

  return true
}

// Departments
export async function getDepartments(): Promise<Department[]> {
  const { data, error } = await supabase
    .from('departments')
    .select('*')
    .order('name', { ascending: true })

  if (error) {
    console.error('Error fetching departments:', error)
    return []
  }

  return data || []
}

export async function addDepartment(department: Omit<Department, 'id' | 'created_at' | 'updated_at' | 'employee_count'>): Promise<Department | null> {
  try {
    const { data, error } = await supabase
      .from('departments')
      .insert({
        ...department,
        id: generateUUID(),
        employee_count: 0,
        created_at: new Date().toISOString(),
      })
      .select()
      .single()

    if (error) {
      console.error('Error adding department:', error)
      return null
    }

    return data
  } catch (error: any) {
    console.error('Error adding department:', error)
    return null
  }
}

// Attendance
export async function getAttendance(userId?: string): Promise<Attendance[]> {
  let query = supabase
    .from('attendance')
    .select('*')
    .order('date', { ascending: false })

  if (userId) {
    query = query.eq('employee_id', userId)
  }

  const { data, error } = await query

  if (error) {
    console.error('Error fetching attendance:', error)
    return []
  }

  return data || []
}

export async function markAttendance(attendance: Omit<Attendance, 'id' | 'created_at' | 'updated_at'>): Promise<Attendance | null> {
  try {
    // Use upsert to handle duplicate dates
    const { data, error } = await supabase
      .from('attendance')
      .upsert({
        ...attendance,
        id: generateUUID(),
        created_at: new Date().toISOString(),
      }, { onConflict: 'employee_id,date' })
      .select()
      .single()

    if (error) {
      console.error('Error marking attendance:', error)
      return null
    }

    return data
  } catch (error: any) {
    console.error('Error marking attendance:', error)
    return null
  }
}

// Real-time subscriptions
export function subscribeToMessages(callback: (message: Message) => void) {
  return supabase
    .channel('messages')
    .on('postgres_changes', { event: 'INSERT', schema: 'public', table: 'messages' }, (payload) => {
      callback(payload.new as Message)
    })
    .subscribe()
}

export function subscribeToLeaves(callback: (leave: Leave) => void) {
  return supabase
    .channel('leaves')
    .on('postgres_changes', { event: '*', schema: 'public', table: 'leaves' }, (payload) => {
      callback(payload.new as Leave)
    })
    .subscribe()
}

export function subscribeToTasks(callback: (task: Task) => void) {
  return supabase
    .channel('tasks')
    .on('postgres_changes', { event: '*', schema: 'public', table: 'tasks' }, (payload) => {
      callback(payload.new as Task)
    })
    .subscribe()
}

