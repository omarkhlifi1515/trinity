/**
 * Centralized Data Storage for Trinity HRM
 * Uses Supabase for real-time sync and better data access
 * 
 * This file re-exports from supabase-storage.ts to ensure
 * all components use Supabase instead of JSONBin.io
 */

// Re-export everything from supabase-storage
export * from './supabase-storage'

// Re-export types with camelCase aliases for backward compatibility
export type {
  Employee,
  Task,
  Leave,
  Message,
  Department,
  Attendance,
} from './supabase-storage'
