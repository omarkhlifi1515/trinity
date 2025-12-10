-- Trinity HRM Database Schema for Supabase - PART 2: Indexes (SAFE VERSION)
-- Run this SECOND in your Supabase SQL Editor
-- This version checks if columns exist before creating indexes

-- Create indexes for better performance
-- Only create indexes if tables AND columns exist

-- Users table indexes
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'users' 
        AND column_name = 'email'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
    END IF;
END $$;

-- Tasks table indexes
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'tasks' 
        AND column_name = 'assigned_to'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON tasks(assigned_to);
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'tasks' 
        AND column_name = 'assigned_by'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_tasks_assigned_by ON tasks(assigned_by);
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'tasks' 
        AND column_name = 'status'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
    END IF;
END $$;

-- Leaves table indexes
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'leaves' 
        AND column_name = 'employee_id'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_leaves_employee_id ON leaves(employee_id);
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'leaves' 
        AND column_name = 'status'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_leaves_status ON leaves(status);
    END IF;
END $$;

-- Messages table indexes
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'messages' 
        AND column_name = 'from_user'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_messages_from_user ON messages(from_user);
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'messages' 
        AND column_name = 'to_user'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_messages_to_user ON messages(to_user);
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'messages' 
        AND column_name = 'read'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_messages_read ON messages(read);
    END IF;
END $$;

-- Attendance table indexes
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'attendance' 
        AND column_name = 'employee_id'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_attendance_employee_id ON attendance(employee_id);
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'attendance' 
        AND column_name = 'date'
    ) THEN
        CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);
    END IF;
END $$;

