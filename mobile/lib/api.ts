import { supabase } from './supabase';

const API_URL = process.env.EXPO_PUBLIC_API_URL;

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

export async function apiRequest(endpoint: string, method: HttpMethod = 'GET', body?: any) {
  if (!API_URL) {
    console.error('EXPO_PUBLIC_API_URL is not defined');
    throw new Error('Configuration error');
  }

  try {
    const { data: { session } } = await supabase.auth.getSession();
    const token = session?.access_token;

    if (!token) {
        // Handle unauthenticated state if necessary, or let the backend reject it
        console.warn('No Supabase session token found.');
    }

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    };

    const config: RequestInit = {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    };

    // Ensure endpoint starts with / or handle it
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    const response = await fetch(`${API_URL}${cleanEndpoint}`, config);

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`API Request failed: ${response.status} ${errorText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('API Request Error:', error);
    throw error;
  }
}