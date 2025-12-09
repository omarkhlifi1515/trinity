/**
 * JSONBin.io Storage for React Native
 * 
 * All three apps (web, React Native, Kotlin) share the same JSONBin.io database
 * Set EXPO_PUBLIC_JSONBIN_API_KEY in your .env file
 */

import AsyncStorage from '@react-native-async-storage/async-storage'

const JSONBIN_API_URL = 'https://api.jsonbin.io/v3'
const BIN_ID_KEY = '@trinity_jsonbin_bin_id'
const API_KEY_KEY = '@trinity_jsonbin_api_key'

// Get API key from environment or AsyncStorage
export function getApiKey(): string {
  // Try environment variable first
  const envKey = process.env.EXPO_PUBLIC_JSONBIN_API_KEY
  if (envKey) {
    return envKey.trim()
  }
  
  // Fallback: try to get from AsyncStorage (set via settings)
  // For now, return empty - user should set via env
  return ''
}

// Get or set bin ID
export async function getBinId(): Promise<string | null> {
  const stored = await AsyncStorage.getItem(BIN_ID_KEY)
  return stored
}

export async function setBinId(binId: string): Promise<void> {
  await AsyncStorage.setItem(BIN_ID_KEY, binId)
}

// Read users from JSONBin
export async function readUsersFromJSONBin(): Promise<any[]> {
  try {
    const apiKey = getApiKey()
    const binId = await getBinId()
    
    if (!apiKey) {
      console.warn('⚠️ JSONBin API key not set. Set EXPO_PUBLIC_JSONBIN_API_KEY in .env')
      return []
    }
    
    if (!binId) {
      console.log('📝 No bin ID found. Will create one on first write.')
      return []
    }
    
    const response = await fetch(`${JSONBIN_API_URL}/b/${binId}/latest`, {
      method: 'GET',
      headers: {
        'X-Master-Key': apiKey.trim(),
        'X-Bin-Meta': 'false',
      },
    })
    
    if (!response.ok) {
      if (response.status === 404) {
        console.log('📝 Bin not found, will create on first write')
        return []
      }
      const errorText = await response.text()
      console.error(`JSONBin API error: ${response.status} - ${errorText}`)
      return []
    }
    
    const data = await response.json()
    const users = Array.isArray(data) ? data : []
    if (users.length > 0) {
      console.log(`✅ Loaded ${users.length} users from JSONBin`)
    }
    return users
  } catch (error: any) {
    console.error('Error reading from JSONBin:', error.message)
    return []
  }
}

// Write users to JSONBin
export async function writeUsersToJSONBin(users: any[]): Promise<boolean> {
  try {
    const apiKey = getApiKey()
    let binId = await getBinId()
    
    if (!apiKey) {
      console.warn('⚠️ JSONBin API key not set. Cannot save to JSONBin.')
      return false
    }
    
    // Create bin if it doesn't exist
    if (!binId) {
      const createResponse = await fetch(`${JSONBIN_API_URL}/b`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Master-Key': apiKey.trim(),
          'X-Bin-Name': 'Trinity HRM Users',
          'X-Bin-Private': 'true',
        },
        body: JSON.stringify(users),
      })
      
      if (!createResponse.ok) {
        const errorText = await createResponse.text()
        console.error(`Failed to create bin: ${createResponse.status} - ${errorText}`)
        return false
      }
      
      const createData = await createResponse.json()
      binId = createData.metadata.id
      await setBinId(binId)
      console.log('✅ Created new JSONBin:', binId)
      return true
    }
    
    // Update existing bin
    const updateResponse = await fetch(`${JSONBIN_API_URL}/b/${binId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-Master-Key': apiKey.trim(),
      },
      body: JSON.stringify(users),
    })
    
    if (!updateResponse.ok) {
      const errorText = await updateResponse.text()
      console.error(`Failed to update bin: ${updateResponse.status} - ${errorText}`)
      return false
    }
    
    console.log('✅ Saved users to JSONBin')
    return true
  } catch (error: any) {
    console.error('Error writing to JSONBin:', error.message)
    return false
  }
}

