/**
 * Local Authentication for React Native
 * Uses JSONBin.io for shared data storage
 * No web app connection needed!
 */

import AsyncStorage from '@react-native-async-storage/async-storage'
import { readUsersFromJSONBin, writeUsersToJSONBin } from './jsonbin'

const AUTH_TOKEN_KEY = '@trinity_auth_token'
const USERS_CACHE_KEY = '@trinity_users_cache'

export type UserRole = 'admin' | 'department_head' | 'employee'

export interface User {
  id: string
  email: string
  password?: string // Hashed, only for internal use
  createdAt?: string
  role?: UserRole // User role: admin, department_head, or employee
  department?: string // Department name (for department heads)
}

// Simple password hashing (same as web app)
function hashPassword(password: string): string {
  // Use base64 encoding (same as web app)
  // In production, use bcrypt
  // For React Native, we need to use a different approach since Buffer might not be available
  if (typeof Buffer !== 'undefined') {
    return Buffer.from(password).toString('base64')
  }
  // Fallback for React Native
  return btoa(password)
}

function verifyPassword(password: string, hashedPassword: string): boolean {
  return hashPassword(password) === hashedPassword
}

// Load users from JSONBin
async function loadUsers(): Promise<User[]> {
  try {
    // Try JSONBin first
    const jsonbinUsers = await readUsersFromJSONBin()
    if (jsonbinUsers.length > 0) {
      // Cache locally
      await AsyncStorage.setItem(USERS_CACHE_KEY, JSON.stringify(jsonbinUsers))
      return jsonbinUsers
    }
    
    // Fallback to local cache
    const cached = await AsyncStorage.getItem(USERS_CACHE_KEY)
    if (cached) {
      return JSON.parse(cached)
    }
    
    return []
  } catch (error) {
    console.error('Error loading users:', error)
    return []
  }
}

// Save users to JSONBin
async function saveUsers(users: User[]): Promise<void> {
  try {
    // Save to JSONBin
    await writeUsersToJSONBin(users)
    // Also cache locally
    await AsyncStorage.setItem(USERS_CACHE_KEY, JSON.stringify(users))
  } catch (error) {
    console.error('Error saving users:', error)
  }
}

// Login
export async function login(email: string, password: string): Promise<{ user: User }> {
  const users = await loadUsers()
  const user = users.find(u => u.email === email)
  
  if (!user || !user.password) {
    throw new Error('Invalid email or password')
  }
  
  if (!verifyPassword(password, user.password)) {
    throw new Error('Invalid email or password')
  }
  
  // Store user session (without password)
  const { password: _, ...userWithoutPassword } = user
  await AsyncStorage.setItem(AUTH_TOKEN_KEY, JSON.stringify(userWithoutPassword))
  
  return { user: userWithoutPassword }
}

// Re-export role functions
export * from './roles'

// Signup
export async function signup(email: string, password: string): Promise<{ user: User }> {
  const users = await loadUsers()
  
  // Check if user exists
  if (users.find(u => u.email === email)) {
    throw new Error('User already exists')
  }
  
  // Determine role based on email
  let role: UserRole = 'employee'
  let department: string | undefined = undefined
  
  if (email.toLowerCase() === 'admin@gmail.com') {
    role = 'admin'
  }
  // You can add more logic here to assign department_head role
  
  // Create new user
  const newUser: User = {
    id: Date.now().toString(),
    email,
    password: hashPassword(password),
    createdAt: new Date().toISOString(),
    role,
    department,
  }
  
  users.push(newUser)
  await saveUsers(users)
  
  // Store user session (without password)
  const { password: _, ...userWithoutPassword } = newUser
  await AsyncStorage.setItem(AUTH_TOKEN_KEY, JSON.stringify(userWithoutPassword))
  
  return { user: userWithoutPassword }
}

// Logout
export async function logout(): Promise<void> {
  await AsyncStorage.removeItem(AUTH_TOKEN_KEY)
}

// Get current user
export async function getCurrentUser(): Promise<User | null> {
  try {
    const stored = await AsyncStorage.getItem(AUTH_TOKEN_KEY)
    if (stored) {
      return JSON.parse(stored)
    }
    return null
  } catch (error) {
    console.error('Error getting current user:', error)
    return null
  }
}

