-- Trinity HRM Database Schema for Supabase - PART 3: Row Level Security
-- Run this THIRD in your Supabase SQL Editor

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE departments ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE leaves ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance ENABLE ROW LEVEL SECURITY;

-- Row Level Security Policies
-- Drop existing policies first to avoid conflicts

-- Users: Allow all for now (custom auth)
DROP POLICY IF EXISTS "Users are viewable by everyone" ON users;
CREATE POLICY "Users are viewable by everyone"
    ON users FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "Allow all operations on users" ON users;
CREATE POLICY "Allow all operations on users"
    ON users FOR ALL
    USING (true)
    WITH CHECK (true);

-- Employees: Allow all for now
DROP POLICY IF EXISTS "Employees are viewable by everyone" ON employees;
CREATE POLICY "Employees are viewable by everyone"
    ON employees FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "Only admins can insert employees" ON employees;
CREATE POLICY "Only admins can insert employees"
    ON employees FOR INSERT
    WITH CHECK (true);  -- Allow all for now

DROP POLICY IF EXISTS "Only admins can update employees" ON employees;
CREATE POLICY "Only admins can update employees"
    ON employees FOR UPDATE
    USING (true);  -- Allow all for now

-- Departments: Allow all for now
DROP POLICY IF EXISTS "Departments are viewable by everyone" ON departments;
CREATE POLICY "Departments are viewable by everyone"
    ON departments FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "Only admins can manage departments" ON departments;
CREATE POLICY "Only admins can manage departments"
    ON departments FOR ALL
    USING (true);  -- Allow all for now

-- Tasks: Allow all for now
DROP POLICY IF EXISTS "Users can view their own tasks" ON tasks;
CREATE POLICY "Users can view their own tasks"
    ON tasks FOR SELECT
    USING (true);  -- Allow all for now

DROP POLICY IF EXISTS "Admins and department heads can create tasks" ON tasks;
CREATE POLICY "Admins and department heads can create tasks"
    ON tasks FOR INSERT
    WITH CHECK (true);  -- Allow all for now

DROP POLICY IF EXISTS "Users can update their own tasks" ON tasks;
CREATE POLICY "Users can update their own tasks"
    ON tasks FOR UPDATE
    USING (true);  -- Allow all for now

-- Leaves: Allow all for now
DROP POLICY IF EXISTS "Users can view their own leaves" ON leaves;
CREATE POLICY "Users can view their own leaves"
    ON leaves FOR SELECT
    USING (true);  -- Allow all for now, can restrict later

DROP POLICY IF EXISTS "Users can create their own leave requests" ON leaves;
CREATE POLICY "Users can create their own leave requests"
    ON leaves FOR INSERT
    WITH CHECK (true);  -- Allow all for now

DROP POLICY IF EXISTS "Admins and department heads can approve leaves" ON leaves;
CREATE POLICY "Admins and department heads can approve leaves"
    ON leaves FOR UPDATE
    USING (true);  -- Allow all for now, can restrict later

-- Messages: Allow all for now
DROP POLICY IF EXISTS "Users can view their own messages" ON messages;
CREATE POLICY "Users can view their own messages"
    ON messages FOR SELECT
    USING (true);  -- Allow all for now

DROP POLICY IF EXISTS "Users can send messages" ON messages;
CREATE POLICY "Users can send messages"
    ON messages FOR INSERT
    WITH CHECK (true);  -- Allow all for now

DROP POLICY IF EXISTS "Users can update their own messages" ON messages;
CREATE POLICY "Users can update their own messages"
    ON messages FOR UPDATE
    USING (true);  -- Allow all for now

-- Attendance: Allow all for now
DROP POLICY IF EXISTS "Users can view their own attendance" ON attendance;
CREATE POLICY "Users can view their own attendance"
    ON attendance FOR SELECT
    USING (true);  -- Allow all for now

DROP POLICY IF EXISTS "Users can mark their own attendance" ON attendance;
CREATE POLICY "Users can mark their own attendance"
    ON attendance FOR INSERT
    WITH CHECK (true);  -- Allow all for now

DROP POLICY IF EXISTS "Users can update their own attendance" ON attendance;
CREATE POLICY "Users can update their own attendance"
    ON attendance FOR UPDATE
    USING (true);  -- Allow all for now

