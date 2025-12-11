import { createBrowserClient } from '@supabase/ssr'

export function createClient() {
  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://nghwpwajcoofbgvsevgf.supabase.co'
  const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'sb_publishable_hahT_e8_6T-6qXE4boTyYQ_Q-w5rFzx'

  return createBrowserClient(
    supabaseUrl,
    supabaseAnonKey
  )
}

// Singleton instance for legacy compatibility (optional)
export const supabase = createClient()


