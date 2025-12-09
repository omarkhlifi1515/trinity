import { cookies } from 'next/headers'
import { SignJWT, jwtVerify } from 'jose'

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
  role?: UserRole // User role: admin, department_head, or employee
  department?: string // Department name (for department heads)
}

// Simple in-memory user store (replace with database in production)
let users: User[] = []
let usersLoaded = false

// Load users from JSONBin or local file
async function loadUsers() {
  // Skip in browser or Edge Runtime
  if (typeof window !== 'undefined' || usersLoaded) {
    return
  }
  
  // Skip in Edge Runtime (middleware)
  if (typeof process === 'undefined' || typeof process.cwd !== 'function') {
    usersLoaded = true
    return
  }

  try {
    // Try JSONBin first (free cloud storage)
    const { readUsersFromJSONBin, readUsersFromLocal } = await import('@/lib/storage/jsonbin')
    const jsonbinUsers = await readUsersFromJSONBin()
    
    if (jsonbinUsers.length > 0) {
      users = jsonbinUsers
      console.log('✅ Loaded users from JSONBin')
    } else {
      // Fallback to local file (only in Node.js runtime)
      try {
        users = readUsersFromLocal()
        if (users.length > 0) {
          console.log('✅ Loaded users from local file')
        }
      } catch (e: any) {
        // Silently fail in Edge Runtime
        if (!e.message?.includes('Edge Runtime') && !e.message?.includes('process.cwd')) {
          console.error('Error loading from local file:', e)
        }
      }
    }
  } catch (error: any) {
    // Silently fail in Edge Runtime
    if (error.message?.includes('Edge Runtime') || error.message?.includes('process.cwd')) {
      usersLoaded = true
      return
    }
    console.error('Error loading users:', error)
    users = []
  }
  
  usersLoaded = true
}

async function saveUsers() {
  // Skip in browser or Edge Runtime
  if (typeof window !== 'undefined') {
    return
  }
  
  // Skip in Edge Runtime (middleware)
  if (typeof process === 'undefined' || typeof process.cwd !== 'function') {
    return
  }

  try {
    // Try JSONBin first (free cloud storage)
    const { writeUsersToJSONBin, writeUsersToLocal } = await import('@/lib/storage/jsonbin')
    const success = await writeUsersToJSONBin(users)
    
    if (success) {
      console.log('✅ Saved users to JSONBin')
    } else {
      // Fallback to local file (only in Node.js runtime)
      try {
        writeUsersToLocal(users)
        console.log('✅ Saved users to local file (JSONBin unavailable)')
      } catch (e: any) {
        // Silently fail in Edge Runtime
        if (!e.message?.includes('Edge Runtime') && !e.message?.includes('process.cwd')) {
          console.error('Error saving to local file:', e)
        }
      }
    }
  } catch (error: any) {
    // Silently fail in Edge Runtime
    if (error.message?.includes('Edge Runtime') || error.message?.includes('process.cwd')) {
      return
    }
    console.error('Error saving users:', error)
  }
}

// Load users on module load (async)
if (typeof window === 'undefined') {
  loadUsers().catch(console.error)
}

export async function createToken(userId: string, email: string): Promise<string> {
  const token = await new SignJWT({ userId, email })
    .setProtectedHeader({ alg: 'HS256' })
    .setIssuedAt()
    .setExpirationTime('7d')
    .sign(SECRET_KEY)
  
  return token
}

export async function verifyToken(token: string): Promise<{ userId: string; email: string } | null> {
  try {
    const { payload } = await jwtVerify(token, SECRET_KEY)
    return {
      userId: payload.userId as string,
      email: payload.email as string,
    }
  } catch (error) {
    return null
  }
}

export async function getCurrentUser(): Promise<{ id: string; email: string } | null> {
  const cookieStore = await cookies()
  const token = cookieStore.get(COOKIE_NAME)?.value
  
  if (!token) {
    return null
  }
  
  const payload = await verifyToken(token)
  if (!payload) {
    return null
  }
  
  return {
    id: payload.userId,
    email: payload.email,
  }
}

export async function setAuthCookie(token: string) {
  const cookieStore = await cookies()
  cookieStore.set(COOKIE_NAME, token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: COOKIE_MAX_AGE,
    path: '/',
  })
}

export async function clearAuthCookie() {
  const cookieStore = await cookies()
  cookieStore.delete(COOKIE_NAME)
}

export function hashPassword(password: string): string {
  // Simple hash for demo (use bcrypt in production)
  // In production, use: const bcrypt = require('bcrypt'); return bcrypt.hashSync(password, 10);
  return Buffer.from(password).toString('base64')
}

export function verifyPassword(password: string, hashedPassword: string): boolean {
  // Simple verification for demo (use bcrypt in production)
  // In production, use: return bcrypt.compareSync(password, hashedPassword);
  return hashPassword(password) === hashedPassword
}

export async function createUser(email: string, password: string): Promise<User> {
  await loadUsers()
  
  // Check if user already exists
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
  
  const user: User = {
    id: Date.now().toString(),
    email,
    password: hashPassword(password),
    createdAt: new Date().toISOString(),
    role,
    department,
  }
  
  users.push(user)
  await saveUsers()
  
  return user
}

export async function authenticateUser(email: string, password: string): Promise<User | null> {
  await loadUsers()
  
  const user = users.find(u => u.email === email)
  
  if (!user) {
    return null
  }
  
  if (!verifyPassword(password, user.password)) {
    return null
  }
  
  return user
}

export async function getUserById(userId: string): Promise<User | null> {
  await loadUsers()
  return users.find(u => u.id === userId) || null
}

