// Local Auth API - No Supabase needed!
// The mobile app will use the same API endpoints as the web app
// 
// IMPORTANT: Update this URL to match your web app URL
// For local development: http://localhost:3000
// For production: https://your-domain.com
// 
// To set it, create a .env file in the mobile folder:
// EXPO_PUBLIC_API_URL=http://localhost:3000

import AsyncStorage from '@react-native-async-storage/async-storage'

const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:3000'
const AUTH_TOKEN_KEY = '@trinity_auth_token'

export interface User {
  id: string
  email: string
}

export interface AuthResponse {
  user: User
}

// Login function
export async function login(email: string, password: string): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Login failed')
  }

  const data = await response.json()
  
  // Store user info and token in AsyncStorage
  if (data.user) {
    await AsyncStorage.setItem(AUTH_TOKEN_KEY, JSON.stringify(data.user))
    if (data.token) {
      await AsyncStorage.setItem('@trinity_auth_token_value', data.token)
    }
  }
  
  return data
}

// Signup function
export async function signup(email: string, password: string): Promise<AuthResponse> {
  const response = await fetch(`${API_BASE_URL}/api/auth/signup`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  })

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || 'Signup failed')
  }

  const data = await response.json()
  
  // Store user info and token in AsyncStorage
  if (data.user) {
    await AsyncStorage.setItem(AUTH_TOKEN_KEY, JSON.stringify(data.user))
    if (data.token) {
      await AsyncStorage.setItem('@trinity_auth_token_value', data.token)
    }
  }
  
  return data
}

// Logout function
export async function logout(): Promise<void> {
  await AsyncStorage.removeItem(AUTH_TOKEN_KEY)
  await AsyncStorage.removeItem('@trinity_auth_token_value')
  try {
    await fetch(`${API_BASE_URL}/api/auth/logout`, {
      method: 'POST',
    })
  } catch (error) {
    // Ignore logout errors
  }
}

// Get current user
export async function getCurrentUser(): Promise<User | null> {
  try {
    // First try to get from AsyncStorage (faster)
    const storedUser = await AsyncStorage.getItem(AUTH_TOKEN_KEY)
    if (storedUser) {
      return JSON.parse(storedUser)
    }
    
    // Fallback: Try API with token
    const token = await AsyncStorage.getItem('@trinity_auth_token_value')
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    }
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    const response = await fetch(`${API_BASE_URL}/api/auth/me`, {
      method: 'GET',
      headers,
    })

    if (response.ok) {
      const data = await response.json()
      if (data.user) {
        await AsyncStorage.setItem(AUTH_TOKEN_KEY, JSON.stringify(data.user))
        return data.user
      }
    }
    
    return null
  } catch (error) {
    console.error('Error getting current user:', error)
    return null
  }
}

