-- Trinity HRM Database Schema for Supabase - PART 2: Indexes
-- Run this SECOND in your Supabase SQL Editor
-- IMPORTANT: Make sure Part 1 ran successfully first!

-- Create indexes for better performance
-- Users table indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
    END IF;
END $$;

-- Tasks table indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tasks') THEN
        CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON tasks(assigned_to);
        CREATE INDEX IF NOT EXISTS idx_tasks_assigned_by ON tasks(assigned_by);
        CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
    END IF;
END $$;

-- Leaves table indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'leaves') THEN
        CREATE INDEX IF NOT EXISTS idx_leaves_employee_id ON leaves(employee_id);
        CREATE INDEX IF NOT EXISTS idx_leaves_status ON leaves(status);
    END IF;
END $$;

-- Messages table indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'messages') THEN
        CREATE INDEX IF NOT EXISTS idx_messages_from_user ON messages(from_user);
        CREATE INDEX IF NOT EXISTS idx_messages_to_user ON messages(to_user);
        CREATE INDEX IF NOT EXISTS idx_messages_read ON messages(read);
    END IF;
END $$;

-- Attendance table indexes
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'attendance') THEN
        CREATE INDEX IF NOT EXISTS idx_attendance_employee_id ON attendance(employee_id);
        CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);
    END IF;
END $$;

