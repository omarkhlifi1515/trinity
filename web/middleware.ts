import { NextResponse, type NextRequest } from 'next/server'

/**
 * Middleware for Firebase Authentication
 * Note: Firebase uses client-side auth, so we rely on client-side redirects
 * This middleware just ensures proper routing
 */
export async function middleware(request: NextRequest) {
  const response = NextResponse.next({
    request: {
      headers: request.headers,
    },
  })

  // Firebase auth is handled client-side
  // The actual auth check happens in the client components
  // This middleware just ensures clean routing

  return response
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - api (API routes)
     */
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
}
