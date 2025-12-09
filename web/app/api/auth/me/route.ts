import { NextRequest, NextResponse } from 'next/server'
import { getCurrentUser, verifyToken } from '@/lib/auth/local-auth'

export async function GET(request: NextRequest) {
  // Try to get user from cookie (web) or Authorization header (mobile)
  let user = await getCurrentUser()
  
  // If no user from cookie, try Authorization header (for mobile)
  if (!user) {
    const authHeader = request.headers.get('Authorization')
    if (authHeader?.startsWith('Bearer ')) {
      const token = authHeader.substring(7)
      const payload = await verifyToken(token)
      if (payload) {
        user = {
          id: payload.userId,
          email: payload.email,
        }
      }
    }
  }
  
  if (!user) {
    const response = NextResponse.json(
      { error: 'Not authenticated' },
      { status: 401 }
    )
    response.headers.set('Access-Control-Allow-Origin', '*')
    return response
  }
  
  const response = NextResponse.json({ user })
  response.headers.set('Access-Control-Allow-Origin', '*')
  return response
}

export async function OPTIONS() {
  return new NextResponse(null, {
    status: 200,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'POST, GET, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    },
  })
}

