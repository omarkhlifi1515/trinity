-- Trinity HRM Database Schema for Supabase - PART 2: Indexes (Simple Version)
-- Run this SECOND in your Supabase SQL Editor
-- Use this version if the main part 2 gives errors
-- IMPORTANT: Run check-tables.sql FIRST to verify Part 1 was successful!

-- Create indexes for better performance
-- Each index is created separately so one failure doesn't stop the rest

-- Users
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_users_email ON users(email); EXCEPTION WHEN OTHERS THEN NULL; END $$;

-- Tasks
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON tasks(assigned_to); EXCEPTION WHEN OTHERS THEN NULL; END $$;
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_tasks_assigned_by ON tasks(assigned_by); EXCEPTION WHEN OTHERS THEN NULL; END $$;
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status); EXCEPTION WHEN OTHERS THEN NULL; END $$;

-- Leaves (these might fail if employee_id doesn't exist - check Part 1!)
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_leaves_employee_id ON leaves(employee_id); EXCEPTION WHEN OTHERS THEN RAISE NOTICE 'Skipped idx_leaves_employee_id: %', SQLERRM; END $$;
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_leaves_status ON leaves(status); EXCEPTION WHEN OTHERS THEN NULL; END $$;

-- Messages
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_messages_from_user ON messages(from_user); EXCEPTION WHEN OTHERS THEN NULL; END $$;
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_messages_to_user ON messages(to_user); EXCEPTION WHEN OTHERS THEN NULL; END $$;
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_messages_read ON messages(read); EXCEPTION WHEN OTHERS THEN NULL; END $$;

-- Attendance (this might fail if employee_id doesn't exist - check Part 1!)
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_attendance_employee_id ON attendance(employee_id); EXCEPTION WHEN OTHERS THEN RAISE NOTICE 'Skipped idx_attendance_employee_id: %', SQLERRM; END $$;
DO $$ BEGIN CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date); EXCEPTION WHEN OTHERS THEN NULL; END $$;

