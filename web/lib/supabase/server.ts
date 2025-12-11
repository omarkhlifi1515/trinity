import { createServerClient, type CookieOptions } from '@supabase/ssr'
import { cookies } from 'next/headers'

export async function createClient() {
  console.log('Server Client: createClient called')
  const cookieStore = cookies()

  // Debug log to confirm what cookies are actually visible to the RSC
  const allCookiesRef = cookieStore.getAll()
  const authCookie = allCookiesRef.find(c => c.name.includes('auth-token'))
  console.log(`Server Client: CookieStore check: ${allCookiesRef.length} cookies. Auth cookie found: ${!!authCookie}`)

  if (authCookie) {
    // Log first 50 chars of cookie value to diagnose corruption
    const cookiePreview = authCookie.value.substring(0, 50)
    console.log(`Server Client: Auth cookie preview: ${cookiePreview}...`)

    // Try to parse as base64-encoded JSON
    try {
      const decoded = Buffer.from(authCookie.value, 'base64').toString('utf-8')
      const parsed = JSON.parse(decoded)
      console.log(`Server Client: Cookie contains session for user: ${parsed?.user?.id || 'unknown'}`)
      console.log(`Server Client: Session expires at: ${parsed?.expires_at ? new Date(parsed.expires_at * 1000).toISOString() : 'unknown'}`)
    } catch (e) {
      console.log(`Server Client: Failed to parse cookie - likely corrupted or invalid format`)
    }
  }

  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://nghwpwajcoofbgvsevgf.supabase.co'
  const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'sb_publishable_hahT_e8_6T-6qXE4boTyYQ_Q-w5rFzx'

  return createServerClient(
    supabaseUrl,
    supabaseAnonKey,
    {
      cookies: {
        getAll() {
          return cookieStore.getAll()
        },
        setAll(cookiesToSet: { name: string, value: string, options: CookieOptions }[]) {
          try {
            cookiesToSet.forEach(({ name, value, options }) =>
              cookieStore.set(name, value, options)
            )
          } catch {
            // The `setAll` method was called from a Server Component.
            // This can be ignored if you have middleware refreshing
            // user sessions.
          }
        },
      },
      auth: {
        // CRITICAL: Disable caching for Next.js 14 Server Components
        // Without this, getUser() fails even with valid cookies
        flowType: 'pkce',
        detectSessionInUrl: false,
        persistSession: true,
        autoRefreshToken: true,
      },
      global: {
        // Force no-cache for all auth requests in Server Components
        fetch: (url: RequestInfo | URL, options: RequestInit = {}) => {
          return fetch(url, {
            ...options,
            cache: 'no-store',
          })
        },
      },
    }
  )
}

