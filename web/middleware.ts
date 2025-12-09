import { NextResponse, type NextRequest } from 'next/server'
import { verifyToken } from '@/lib/auth/local-auth'

export async function middleware(request: NextRequest) {
  const token = request.cookies.get('auth-token')?.value
  let user = null

  if (token) {
    user = await verifyToken(token)
  }

  // Log for debugging
  if (request.nextUrl.pathname === '/dashboard') {
    console.log('Dashboard access attempt - User:', user ? user.email : 'none')
  }

  // Protect dashboard routes
  if (request.nextUrl.pathname.startsWith('/dashboard')) {
    if (!user) {
      console.log('No user found, redirecting to login')
      const url = request.nextUrl.clone()
      url.pathname = '/'
      return NextResponse.redirect(url)
    }
  }

  // Redirect authenticated users away from login/signup
  if (
    (request.nextUrl.pathname === '/' || 
     request.nextUrl.pathname === '/login' || 
     request.nextUrl.pathname === '/signup') && 
    user
  ) {
    const url = request.nextUrl.clone()
    url.pathname = '/dashboard'
    return NextResponse.redirect(url)
  }

  return NextResponse.next()
}

export const config = {
  matcher: [
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
}
