import { cookies } from 'next/headers'
import { SignJWT, jwtVerify } from 'jose'

const SECRET_KEY = new TextEncoder().encode(
  process.env.AUTH_SECRET || 'your-secret-key-change-this-in-production-min-32-chars'
)

const COOKIE_NAME = 'auth-token'
const COOKIE_MAX_AGE = 60 * 60 * 24 * 7 // 7 days

export type UserRole = 'admin' | 'chef' | 'employee'

export interface User {
  id: string
  email: string
  role?: UserRole
  department?: string
  createdAt: string
}

// NOTE: This file is largely legacy now that we use Firebase Client SDK.
// It is kept for any remaining JWT util needs.

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

export async function getCurrentUser(): Promise<User | null> {
  // This function meant to run on server.
  // In Firebase model, we verify ID token if needed.
  // For now, return null to force client-side checks.
  return null;
}
