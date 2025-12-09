/**
 * Local Authentication - Standalone Mobile App
 * Uses JSONBin.io for shared data storage with web and Kotlin apps
 * No web app connection needed!
 * 
 * Set EXPO_PUBLIC_JSONBIN_API_KEY in .env file:
 * EXPO_PUBLIC_JSONBIN_API_KEY=$2a$10$XtgiWhpdzGwCmy0M915kdu9zNMfZi41jHYYGbimNLgjSSBmpFdJKq
 */

// Re-export from auth.ts (local authentication)
export * from './auth'
export type { User } from './auth'

