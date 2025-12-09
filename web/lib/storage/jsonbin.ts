/**
 * JSONBin.io Storage
 * 
 * Free JSON storage API - no OAuth needed!
 * Get your API key from: https://jsonbin.io/
 * 
 * Free tier includes:
 * - Unlimited bins
 * - 10,000 requests/month
 * - Perfect for small apps
 */

const JSONBIN_API_URL = 'https://api.jsonbin.io/v3'
const BIN_ID_KEY = 'TRINITY_HRM_BIN_ID'

// Get API key from environment or use a default (you should set your own)
function getApiKey(): string {
  const key = process.env.JSONBIN_API_KEY || ''
  // Trim and validate
  return key.trim()
}

// Get or create bin ID
function getBinId(): string | null {
  if (typeof window !== 'undefined') {
    return localStorage.getItem(BIN_ID_KEY)
  }
  
  // Skip in Edge Runtime
  if (typeof process === 'undefined' || typeof process.cwd !== 'function') {
    return null
  }
  
  // Server-side: check environment first, then file
  if (process.env.JSONBIN_BIN_ID) {
    return process.env.JSONBIN_BIN_ID
  }
  
  // Try to read from file (synchronous, only in Node.js runtime)
  // Skip in Edge Runtime - require check happens before this
  try {
    // Only use require in Node.js runtime (not Edge Runtime)
    // This check prevents Edge Runtime from executing this code
    if (typeof require !== 'undefined' && typeof process !== 'undefined' && process.cwd) {
      const fs = require('fs')
      const path = require('path')
      const binIdFile = path.join(process.cwd(), 'data', 'jsonbin-id.txt')
      if (fs.existsSync(binIdFile)) {
        return fs.readFileSync(binIdFile, 'utf-8').trim()
      }
    }
  } catch (e) {
    // Ignore errors (especially Edge Runtime errors)
  }
  
  return null
}

async function setBinId(binId: string): Promise<void> {
  if (typeof window !== 'undefined') {
    localStorage.setItem(BIN_ID_KEY, binId)
    return
  }
  
  // Skip in Edge Runtime
  if (typeof process === 'undefined' || typeof process.cwd !== 'function') {
    return
  }
  
  try {
    // Use require only in Node.js runtime
    if (typeof require !== 'undefined') {
      const fs = require('fs')
      const path = require('path')
      const dataDir = path.join(process.cwd(), 'data')
      if (!fs.existsSync(dataDir)) {
        fs.mkdirSync(dataDir, { recursive: true })
      }
      const binIdFile = path.join(dataDir, 'jsonbin-id.txt')
      fs.writeFileSync(binIdFile, binId, 'utf-8')
      console.log('💾 Saved JSONBin ID to file:', binId)
    }
  } catch (e: any) {
    // Silently fail if can't write
    if (!e.message?.includes('Edge Runtime') && !e.message?.includes('process.cwd')) {
      console.warn('Could not save bin ID to file:', e.message)
    }
  }
}

// Read users from JSONBin
export async function readUsersFromJSONBin(): Promise<any[]> {
  try {
    const apiKey = getApiKey()
    const binId = getBinId()
    
    if (!apiKey) {
      console.warn('⚠️ JSONBin API key not set. Using local storage.')
      return []
    }
    
    if (!binId) {
      console.log('📝 No bin ID found. Will create one on first write.')
      return []
    }
    
    const response = await fetch(`${JSONBIN_API_URL}/b/${binId}/latest`, {
      method: 'GET',
      headers: {
        'X-Master-Key': apiKey.trim(), // Trim whitespace
        'X-Bin-Meta': 'false',
      },
    })
    
    if (!response.ok) {
      if (response.status === 404) {
        console.log('📝 Bin not found, will create on first write')
        return []
      }
      const errorText = await response.text()
      console.error(`JSONBin API error: ${response.status} ${response.statusText} - ${errorText}`)
      throw new Error(`JSONBin API error: ${response.statusText}`)
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
    let binId = getBinId()
    
    if (!apiKey) {
      console.warn('⚠️ JSONBin API key not set. Cannot save to JSONBin.')
      return false
    }
    
    // Create bin if it doesn't exist
    if (!binId) {
      // Validate API key format
      if (!apiKey || apiKey.length < 20) {
        console.error('⚠️ Invalid JSONBin API key format. Key should be at least 20 characters.')
        throw new Error('Invalid API key format')
      }
      
      const createResponse = await fetch(`${JSONBIN_API_URL}/b`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Master-Key': apiKey.trim(), // Trim whitespace
          'X-Bin-Name': 'Trinity HRM Users',
          'X-Bin-Private': 'true',
        },
        body: JSON.stringify(users),
      })
      
      if (!createResponse.ok) {
        const errorText = await createResponse.text()
        console.error(`Failed to create bin: ${createResponse.status} ${createResponse.statusText}`)
        console.error(`Error details: ${errorText}`)
        throw new Error(`Failed to create bin: ${createResponse.statusText} - ${errorText}`)
      }
      
      const createData = await createResponse.json()
      binId = createData.metadata.id
      await setBinId(binId)
      console.log('✅ Created new JSONBin:', binId)
      console.log('💡 Add this to your .env.local: JSONBIN_BIN_ID=' + binId)
      return true
    }
    
    // Update existing bin
    const updateResponse = await fetch(`${JSONBIN_API_URL}/b/${binId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-Master-Key': apiKey.trim(), // Trim whitespace
      },
      body: JSON.stringify(users),
    })
    
    if (!updateResponse.ok) {
      const errorText = await updateResponse.text()
      throw new Error(`Failed to update bin: ${updateResponse.statusText} - ${errorText}`)
    }
    
    console.log('✅ Updated JSONBin successfully')
    return true
  } catch (error: any) {
    console.error('Error writing to JSONBin:', error.message)
    return false
  }
}

// Fallback: Read from local file
// Note: This only works in Node.js runtime, not Edge Runtime
export function readUsersFromLocal(): any[] {
  // Skip in browser or Edge Runtime
  if (typeof window !== 'undefined' || typeof process === 'undefined') {
    return []
  }
  
  // Check if we're in Edge Runtime by trying to access process.cwd
  try {
    // This will throw in Edge Runtime
    if (typeof process.cwd !== 'function') {
      return []
    }
  } catch {
    return []
  }
  
  // Skip in Edge Runtime
  if (typeof process === 'undefined' || typeof process.cwd !== 'function') {
    return []
  }
  
  try {
    // Use require only in Node.js runtime
    if (typeof require !== 'undefined') {
      const fs = require('fs')
      const path = require('path')
      const usersFile = path.join(process.cwd(), 'data', 'users.json')
      if (fs.existsSync(usersFile)) {
        return JSON.parse(fs.readFileSync(usersFile, 'utf-8'))
      }
    }
  } catch (error: any) {
    // Silently fail in Edge Runtime or if file doesn't exist
    if (error.message?.includes('process.cwd') || 
        error.message?.includes('Edge Runtime') ||
        error.message?.includes('require')) {
      return []
    }
    // Only log real errors
    if (error.code !== 'ENOENT') {
      console.error('Error reading local users file:', error.message)
    }
  }
  return []
}

// Fallback: Write to local file
// Note: This only works in Node.js runtime, not Edge Runtime
export function writeUsersToLocal(users: any[]): void {
  // Skip in browser or Edge Runtime
  if (typeof window !== 'undefined' || typeof process === 'undefined') {
    return
  }
  
  // Check if we're in Edge Runtime
  try {
    if (typeof process.cwd !== 'function') {
      return
    }
  } catch {
    return
  }
  
  // Skip in Edge Runtime
  if (typeof process === 'undefined' || typeof process.cwd !== 'function') {
    return
  }
  
  try {
    // Use require only in Node.js runtime
    if (typeof require !== 'undefined') {
      const fs = require('fs')
      const path = require('path')
      const dataDir = path.join(process.cwd(), 'data')
      if (!fs.existsSync(dataDir)) {
        fs.mkdirSync(dataDir, { recursive: true })
      }
      const usersFile = path.join(dataDir, 'users.json')
      fs.writeFileSync(usersFile, JSON.stringify(users, null, 2))
    }
  } catch (error: any) {
    // Silently fail in Edge Runtime
    if (error.message?.includes('process.cwd') || 
        error.message?.includes('Edge Runtime') ||
        error.message?.includes('require')) {
      return
    }
    console.error('Error writing local users file:', error.message)
  }
}

