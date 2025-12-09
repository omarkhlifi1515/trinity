import { create } from 'zustand'
import type { User } from '@/lib/supabase'

interface AuthState {
  user: User | null
  setUser: (user: User | null) => void
  signOut: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
  signOut: () => set({ user: null }),
}))

