import 'react-native-url-polyfill/auto'
import { createClient } from '@supabase/supabase-js'
import AsyncStorage from '@react-native-async-storage/async-storage'

// Using the values from README.md
const supabaseUrl = 'https://nghwpwajcoofbgvsevgf.supabase.co'
const supabaseAnonKey = 'sb_publishable_hahT_e8_6T-6qXE4boTyYQ_Q-w5rFzx'

export const supabase = createClient(supabaseUrl, supabaseAnonKey, {
  auth: {
    storage: AsyncStorage,
    autoRefreshToken: true,
    persistSession: true,
    detectSessionInUrl: false,
  },
})
