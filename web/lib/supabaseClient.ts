/**
 * Frontend Supabase client wrapper.
 *
 * Reads `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_ANON_KEY` from env.
 * Export a single client instance for use in client components.
 */
import { createClient, SupabaseClient } from '@supabase/supabase-js'

const url = process.env.NEXT_PUBLIC_SUPABASE_URL
const key = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY

if (!url || !key) {
  // Keep this runtime check to fail-fast in development if env is missing.
  // In production, ensure the variables are set in your hosting provider.
  throw new Error('Missing NEXT_PUBLIC_SUPABASE_URL or NEXT_PUBLIC_SUPABASE_ANON_KEY')
}

const supabase: SupabaseClient = createClient(url, key)

export default supabase
