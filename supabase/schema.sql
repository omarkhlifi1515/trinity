-- Trinity HRM Database Schema for Supabase
-- Run this in your Supabase SQL Editor

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users Table (for authentication - separate from auth.users)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL, -- Hashed password
    role TEXT DEFAULT 'employee' CHECK (role IN ('admin', 'department_head', 'employee')),
    department TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security for users
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Users can view all users (for directory)
-- Note: For custom auth, we'll use service role or disable RLS initially
CREATE POLICY "Users are viewable by everyone"
    ON users FOR SELECT
    USING (true);

-- Allow inserts/updates for now (we'll add proper auth later)
-- In production, use Supabase Auth and proper RLS policies
CREATE POLICY "Allow all operations on users"
    ON users FOR ALL
    USING (true)
    WITH CHECK (true);

-- Create index for email lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Employees Table
CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    department TEXT,
    position TEXT,
    hire_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Departments Table
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT UNIQUE NOT NULL,
    description TEXT,
    head_id UUID, -- User ID of department head
    employee_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT,
    assigned_to UUID, -- Can reference employees(id) or users(id) - flexible
    assigned_by UUID NOT NULL, -- User ID who created the task
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'in_progress', 'completed', 'cancelled')),
    priority TEXT NOT NULL DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
    due_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Leaves Table
CREATE TABLE IF NOT EXISTS leaves (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL, -- User ID (can reference users table or be flexible)
    type TEXT NOT NULL CHECK (type IN ('sick', 'vacation', 'personal', 'emergency', 'other')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected', 'cancelled')),
    approved_by UUID, -- User ID who approved
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_user UUID NOT NULL, -- User ID who sent
    to_user UUID, -- User ID who receives (NULL for broadcast)
    subject TEXT NOT NULL,
    content TEXT NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Attendance Table
CREATE TABLE IF NOT EXISTS attendance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_id UUID NOT NULL, -- User ID
    date DATE NOT NULL,
    check_in TIME,
    check_out TIME,
    status TEXT NOT NULL DEFAULT 'present' CHECK (status IN ('present', 'absent', 'late', 'half_day', 'on_leave')),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(employee_id, date)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_by ON tasks(assigned_by);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_leaves_employee_id ON leaves(employee_id);
CREATE INDEX IF NOT EXISTS idx_leaves_status ON leaves(status);
CREATE INDEX IF NOT EXISTS idx_messages_from_user ON messages(from_user);
CREATE INDEX IF NOT EXISTS idx_messages_to_user ON messages(to_user);
CREATE INDEX IF NOT EXISTS idx_messages_read ON messages(read);
CREATE INDEX IF NOT EXISTS idx_attendance_employee_id ON attendance(employee_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);

-- Enable Row Level Security
ALTER TABLE employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE departments ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE leaves ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance ENABLE ROW LEVEL SECURITY;

-- Row Level Security Policies

-- Employees: Admins can see all, others can see all (for directory)
CREATE POLICY "Employees are viewable by everyone"
    ON employees FOR SELECT
    USING (true);

CREATE POLICY "Only admins can insert employees"
    ON employees FOR INSERT
    WITH CHECK (true);  -- Allow all for now

CREATE POLICY "Only admins can update employees"
    ON employees FOR UPDATE
    USING (true);  -- Allow all for now

-- Departments: Everyone can view, only admins can modify
CREATE POLICY "Departments are viewable by everyone"
    ON departments FOR SELECT
    USING (true);

CREATE POLICY "Only admins can manage departments"
    ON departments FOR ALL
    USING (true);  -- Allow all for now

-- Tasks: Users can see tasks assigned to them or assigned by them
-- Note: Since we're using custom auth, we'll simplify the policies
CREATE POLICY "Users can view their own tasks"
    ON tasks FOR SELECT
    USING (true);  -- Allow all for now

CREATE POLICY "Admins and department heads can create tasks"
    ON tasks FOR INSERT
    WITH CHECK (true);  -- Allow all for now

CREATE POLICY "Users can update their own tasks"
    ON tasks FOR UPDATE
    USING (true);  -- Allow all for now

-- Leaves: Users can see their own leaves, admins/dept heads can see all
-- Simplified for custom auth - allow all for now
CREATE POLICY "Users can view their own leaves"
    ON leaves FOR SELECT
    USING (true);  -- Allow all for now, can restrict later

CREATE POLICY "Users can create their own leave requests"
    ON leaves FOR INSERT
    WITH CHECK (true);  -- Allow all for now

CREATE POLICY "Admins and department heads can approve leaves"
    ON leaves FOR UPDATE
    USING (true);  -- Allow all for now, can restrict later

-- Messages: Users can see messages to/from them
CREATE POLICY "Users can view their own messages"
    ON messages FOR SELECT
    USING (true);  -- Allow all for now

CREATE POLICY "Users can send messages"
    ON messages FOR INSERT
    WITH CHECK (true);  -- Allow all for now

CREATE POLICY "Users can update their own messages"
    ON messages FOR UPDATE
    USING (true);  -- Allow all for now

-- Attendance: Users can see their own attendance, admins can see all
CREATE POLICY "Users can view their own attendance"
    ON attendance FOR SELECT
    USING (true);  -- Allow all for now

CREATE POLICY "Users can mark their own attendance"
    ON attendance FOR INSERT
    WITH CHECK (true);  -- Allow all for now

CREATE POLICY "Users can update their own attendance"
    ON attendance FOR UPDATE
    USING (true);  -- Allow all for now

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers to auto-update updated_at
CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leaves_updated_at BEFORE UPDATE ON leaves
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_messages_updated_at BEFORE UPDATE ON messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_attendance_updated_at BEFORE UPDATE ON attendance
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

