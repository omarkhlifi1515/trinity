import { supabase } from './supabase'
import { Session } from '@supabase/supabase-js'

export type UserRole = 'admin' | 'department_head' | 'employee'

export interface User {
  id: string
  email?: string
  role?: UserRole
  department?: string
  full_name?: string
  avatar_url?: string
}

// Fetch user profile from the 'profiles' table
async function getUserProfile(userId: string): Promise<Partial<User>> {
  const { data, error } = await supabase
    .from('profiles')
    .select('*')
    .eq('id', userId)
    .single()

  if (error) {
    console.error('Error fetching profile:', error)
    return {}
  }

  return {
    role: data.role as UserRole,
    department: data.department_id, // Note: Schema uses department_id, old app used department name. You might need a join or separate fetch if name is needed.
    full_name: data.full_name,
    avatar_url: data.avatar_url
  }
}

export async function login(email: string, password: string): Promise<{ user: User, session: Session | null }> {
  const { data, error } = await supabase.auth.signInWithPassword({
    email,
    password,
  })

  if (error) throw error
  if (!data.user) throw new Error('No user returned')

  const profile = await getUserProfile(data.user.id)

  const user: User = {
    id: data.user.id,
    email: data.user.email,
    ...profile
  }

  return { user, session: data.session }
}

export async function signup(email: string, password: string): Promise<{ user: User, session: Session | null }> {
  const { data, error } = await supabase.auth.signUp({
    email,
    password,
  })

  if (error) throw error
  if (!data.user) throw new Error('No user returned')

  // Profile is created by trigger, but might take a ms.
  // We can return the basic user first.
  const user: User = {
    id: data.user.id,
    email: data.user.email,
    role: 'employee' // Default
  }

  return { user, session: data.session }
}

export async function logout(): Promise<void> {
  const { error } = await supabase.auth.signOut()
  if (error) throw error
}

export async function getCurrentUser(): Promise<User | null> {
  const { data: { session } } = await supabase.auth.getSession()
  if (!session?.user) return null

  const profile = await getUserProfile(session.user.id)

  return {
    id: session.user.id,
    email: session.user.email,
    ...profile
  }
}

// Export roles helper for compatibility
export * from './roles'
