import supabase from './supabase'

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || ''

// Helper to get the current session token
async function getAuthHeaders() {
  const { data } = await supabase.auth.getSession()
  const token = data.session?.access_token
  
  if (!token) {
    console.warn('No active session token found')
    return {}
  }
  
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
}

export async function apiGet(path: string) {
  const headers = await getAuthHeaders()
  
  // Ensure we don't double-slash (e.g., base/ + /path)
  const endpoint = path.startsWith('/') ? path : `/${path}`
  
  const res = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'GET',
    headers: headers as HeadersInit,
  })

  if (res.status === 401) {
    throw new Error('Unauthorized: Please log in again.')
  }

  if (!res.ok) throw new Error(`GET ${path} failed: ${res.status}`)
  
  return res.json()
}

export async function apiPost(path: string, body: any) {
  const headers = await getAuthHeaders()
  const endpoint = path.startsWith('/') ? path : `/${path}`

  const res = await fetch(`${BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: headers as HeadersInit,
    body: JSON.stringify(body),
  })

  if (!res.ok) throw new Error(`POST ${path} failed: ${res.status}`)
  
  return res.json()
}
