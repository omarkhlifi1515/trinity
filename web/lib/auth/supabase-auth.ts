/**
 * Supabase-based Authentication
 * Replaces JSONBin.io with Supabase for user storage
 */

import { cookies } from 'next/headers'
import { SignJWT, jwtVerify } from 'jose'
import { createClient } from '@/lib/supabase/server'

const SECRET_KEY = new TextEncoder().encode(
  process.env.AUTH_SECRET || 'your-secret-key-change-this-in-production-min-32-chars'
)

const COOKIE_NAME = 'auth-token'
const COOKIE_MAX_AGE = 60 * 60 * 24 * 7 // 7 days

export type UserRole = 'admin' | 'department_head' | 'employee'

export interface User {
  id: string
  email: string
  password: string // Hashed in production
  createdAt: string
  role?: UserRole
  department?: string
}

// Load users from Supabase
async function loadUsers(): Promise<User[]> {
  try {
    const supabase = await createClient()
    
    // Use Supabase auth.users table or a custom users table
    // For now, we'll use a custom approach with Supabase storage
    // In production, you should use Supabase Auth
    
    // Try to get users from a custom table if it exists
    const { data, error } = await supabase
      .from('users')
      .select('*')
    
    if (error) {
      // Table doesn't exist yet, return empty array
      // Users will be created in local storage first
      return []
    }
    
    return (data || []).map((u: any) => ({
      id: u.id,
      email: u.email,
      password: u.password,
      createdAt: u.created_at || new Date().toISOString(),
      role: u.role as UserRole,
      department: u.department,
    }))
  } catch (error) {
    console.error('Error loading users from Supabase:', error)
    return []
  }
}

// Save users to Supabase
async function saveUsers(users: User[]): Promise<boolean> {
  try {
    const supabase = await createClient()
    
    // Upsert users (create table if needed)
    // Note: You should create a users table in Supabase
    // For now, we'll use local storage as fallback
    
    const usersToSave = users.map(u => ({
      id: u.id,
      email: u.email,
      password: u.password,
      created_at: u.createdAt,
      role: u.role || 'employee',
      department: u.department,
    }))
    
    // Try to upsert
    const { error } = await supabase
      .from('users')
      .upsert(usersToSave, { onConflict: 'id' })
    
    if (error) {
      console.error('Error saving users to Supabase:', error)
      return false
    }
    
    return true
  } catch (error) {
    console.error('Error saving users to Supabase:', error)
    return false
  }
}

// Hash password
async function hashPassword(password: string): Promise<string> {
  // Simple hash for demo (use bcrypt in production)
  const encoder = new TextEncoder()
  const data = encoder.encode(password)
  const hash = await crypto.subtle.digest('SHA-256', data)
  return Array.from(new Uint8Array(hash))
    .map(b => b.toString(16).padStart(2, '0'))
    .join('')
}

// Verify password
async function verifyPassword(password: string, hash: string): Promise<boolean> {
  const passwordHash = await hashPassword(password)
  return passwordHash === hash
}

// Create JWT token
async function createToken(user: Omit<User, 'password'>): Promise<string> {
  const token = await new SignJWT({ userId: user.id, email: user.email, role: user.role })
    .setProtectedHeader({ alg: 'HS256' })
    .setIssuedAt()
    .setExpirationTime('7d')
    .sign(SECRET_KEY)
  
  return token
}

// Verify JWT token
async function verifyToken(token: string): Promise<{ userId: string; email: string; role?: UserRole } | null> {
  try {
    const { payload } = await jwtVerify(token, SECRET_KEY)
    return {
      userId: payload.userId as string,
      email: payload.email as string,
      role: payload.role as UserRole,
    }
  } catch {
    return null
  }
}

// Get current user from cookie
export async function getCurrentUser(): Promise<User | null> {
  try {
    const cookieStore = await cookies()
    const token = cookieStore.get(COOKIE_NAME)?.value
    
    if (!token) {
      return null
    }
    
    const payload = await verifyToken(token)
    if (!payload) {
      return null
    }
    
    // Load user from Supabase
    const users = await loadUsers()
    const user = users.find(u => u.id === payload.userId)
    
    if (!user) {
      return null
    }
    
    return {
      id: user.id,
      email: user.email,
      createdAt: user.createdAt,
      role: user.role,
      department: user.department,
    }
  } catch (error) {
    console.error('Error getting current user:', error)
    return null
  }
}

// Authenticate user
export async function authenticate(email: string, password: string): Promise<User | null> {
  const users = await loadUsers()
  const user = users.find(u => u.email.toLowerCase() === email.toLowerCase())
  
  if (!user) {
    return null
  }
  
  const isValid = await verifyPassword(password, user.password)
  if (!isValid) {
    return null
  }
  
  return {
    id: user.id,
    email: user.email,
    createdAt: user.createdAt,
    role: user.role,
    department: user.department,
  }
}

// Create user
export async function createUser(email: string, password: string, role: UserRole = 'employee', department?: string): Promise<User> {
  const users = await loadUsers()
  
  // Check if user exists
  if (users.some(u => u.email.toLowerCase() === email.toLowerCase())) {
    throw new Error('User already exists')
  }
  
  const hashedPassword = await hashPassword(password)
  const newUser: User = {
    id: crypto.randomUUID(),
    email: email.toLowerCase(),
    password: hashedPassword,
    createdAt: new Date().toISOString(),
    role,
    department,
  }
  
  // Add to array and save
  users.push(newUser)
  await saveUsers(users)
  
  return {
    id: newUser.id,
    email: newUser.email,
    createdAt: newUser.createdAt,
    role: newUser.role,
    department: newUser.department,
  }
}

// Set auth cookie
export async function setAuthCookie(user: Omit<User, 'password'>) {
  const token = await createToken(user)
  const cookieStore = await cookies()
  
  cookieStore.set(COOKIE_NAME, token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: COOKIE_MAX_AGE,
    path: '/',
  })
}

// Clear auth cookie
export async function clearAuthCookie() {
  const cookieStore = await cookies()
  cookieStore.delete(COOKIE_NAME)
}

