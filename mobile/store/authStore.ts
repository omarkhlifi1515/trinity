import { create } from 'zustand'
import type { User } from '@/lib/auth'

interface AuthState {
  user: User | null
  setUser: (user: User | null) => void
  signOut: () => void
  isAuthenticated: boolean
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  setUser: (user) => set({ user, isAuthenticated: !!user }),
  signOut: () => set({ user: null, isAuthenticated: false }),
}))
