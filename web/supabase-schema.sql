-- Trinity HRM Database Schema for Supabase
-- Run this SQL in your Supabase SQL Editor
-- This version uses explicit verification to ensure tables exist

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- STEP 1: CLEANUP - Drop all existing objects
-- ============================================

-- Drop policies (ignore errors)
DO $$ 
BEGIN
  DROP POLICY IF EXISTS "Users can view their own profile" ON public.profiles;
  DROP POLICY IF EXISTS "Users can update their own profile" ON public.profiles;
  DROP POLICY IF EXISTS "Admins can view all profiles" ON public.profiles;
  DROP POLICY IF EXISTS "Everyone can view departments" ON public.departments;
  DROP POLICY IF EXISTS "Admins and HR can manage departments" ON public.departments;
  DROP POLICY IF EXISTS "Users can view their own employee record" ON public.employees;
  DROP POLICY IF EXISTS "Admins and HR can view all employees" ON public.employees;
  DROP POLICY IF EXISTS "Admins and HR can manage employees" ON public.employees;
  DROP POLICY IF EXISTS "Users can view tasks assigned to them" ON public.tasks;
  DROP POLICY IF EXISTS "Managers can view tasks in their department" ON public.tasks;
  DROP POLICY IF EXISTS "Users can create tasks" ON public.tasks;
  DROP POLICY IF EXISTS "Users can update their assigned tasks" ON public.tasks;
  DROP POLICY IF EXISTS "Admins and HR can manage all tasks" ON public.tasks;
  DROP POLICY IF EXISTS "Users can view their own attendance" ON public.attendance;
  DROP POLICY IF EXISTS "Users can create their own attendance" ON public.attendance;
  DROP POLICY IF EXISTS "Admins and HR can view all attendance" ON public.attendance;
  DROP POLICY IF EXISTS "Admins and HR can manage attendance" ON public.attendance;
  DROP POLICY IF EXISTS "Users can view their own leaves" ON public.leaves;
  DROP POLICY IF EXISTS "Users can create their own leave requests" ON public.leaves;
  DROP POLICY IF EXISTS "Managers can view leaves in their department" ON public.leaves;
  DROP POLICY IF EXISTS "Managers can approve/reject leaves" ON public.leaves;
  DROP POLICY IF EXISTS "Users can view messages sent to them" ON public.messages;
  DROP POLICY IF EXISTS "Users can send messages" ON public.messages;
  DROP POLICY IF EXISTS "Users can update their received messages" ON public.messages;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

-- Drop triggers
DROP TRIGGER IF EXISTS update_profiles_updated_at ON public.profiles;
DROP TRIGGER IF EXISTS update_departments_updated_at ON public.departments;
DROP TRIGGER IF EXISTS update_employees_updated_at ON public.employees;
DROP TRIGGER IF EXISTS update_tasks_updated_at ON public.tasks;
DROP TRIGGER IF EXISTS update_attendance_updated_at ON public.attendance;
DROP TRIGGER IF EXISTS update_leaves_updated_at ON public.leaves;
DROP TRIGGER IF EXISTS update_messages_updated_at ON public.messages;
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

-- Drop tables (CASCADE handles dependencies)
DROP TABLE IF EXISTS public.messages CASCADE;
DROP TABLE IF EXISTS public.leaves CASCADE;
DROP TABLE IF EXISTS public.attendance CASCADE;
DROP TABLE IF EXISTS public.tasks CASCADE;
DROP TABLE IF EXISTS public.employees CASCADE;
DROP TABLE IF EXISTS public.departments CASCADE;
DROP TABLE IF EXISTS public.profiles CASCADE;

-- Drop functions
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;
DROP FUNCTION IF EXISTS public.handle_new_user() CASCADE;

-- ============================================
-- STEP 2: CREATE TABLES (one at a time with verification)
-- ============================================

-- Create profiles table
CREATE TABLE public.profiles (
  id UUID PRIMARY KEY,
  email TEXT,
  full_name TEXT,
  role TEXT DEFAULT 'employee' CHECK (role IN ('admin', 'hr', 'manager', 'employee')),
  department_id UUID,
  position TEXT,
  phone TEXT,
  avatar_url TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Verify profiles table was created
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT FROM pg_tables 
    WHERE schemaname = 'public' 
    AND tablename = 'profiles'
  ) THEN
    RAISE EXCEPTION 'Failed to create profiles table';
  END IF;
END $$;

-- Create departments table
CREATE TABLE public.departments (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  description TEXT,
  manager_id UUID,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create employees table
CREATE TABLE public.employees (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID UNIQUE NOT NULL,
  employee_id TEXT UNIQUE NOT NULL,
  hire_date DATE,
  salary DECIMAL(10, 2),
  status TEXT DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'terminated', 'on_leave')),
  address TEXT,
  emergency_contact_name TEXT,
  emergency_contact_phone TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create tasks table
CREATE TABLE public.tasks (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  assigned_to UUID,
  assigned_by UUID,
  department_id UUID,
  status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'in_progress', 'completed', 'cancelled')),
  priority TEXT DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
  due_date TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create attendance table
CREATE TABLE public.attendance (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID NOT NULL,
  date DATE NOT NULL,
  check_in TIMESTAMPTZ,
  check_out TIMESTAMPTZ,
  status TEXT DEFAULT 'present' CHECK (status IN ('present', 'absent', 'late', 'half_day', 'holiday')),
  notes TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, date)
);

-- Create leaves table
CREATE TABLE public.leaves (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  user_id UUID NOT NULL,
  leave_type TEXT NOT NULL CHECK (leave_type IN ('annual', 'sick', 'personal', 'maternity', 'paternity', 'unpaid')),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  days_requested INTEGER NOT NULL,
  reason TEXT,
  status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected', 'cancelled')),
  approved_by UUID,
  approved_at TIMESTAMPTZ,
  rejection_reason TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create messages table
CREATE TABLE public.messages (
  id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
  sender_id UUID NOT NULL,
  receiver_id UUID,
  subject TEXT NOT NULL,
  content TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  is_important BOOLEAN DEFAULT FALSE,
  parent_message_id UUID,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- STEP 3: ADD FOREIGN KEY CONSTRAINTS
-- ============================================

ALTER TABLE public.profiles
ADD CONSTRAINT fk_profiles_department
FOREIGN KEY (department_id) REFERENCES public.departments(id);

ALTER TABLE public.departments
ADD CONSTRAINT fk_departments_manager
FOREIGN KEY (manager_id) REFERENCES public.profiles(id);

ALTER TABLE public.employees
ADD CONSTRAINT employees_user_id_fkey
FOREIGN KEY (user_id) REFERENCES public.profiles(id);

ALTER TABLE public.tasks
ADD CONSTRAINT tasks_assigned_to_fkey
FOREIGN KEY (assigned_to) REFERENCES public.profiles(id);

ALTER TABLE public.tasks
ADD CONSTRAINT tasks_assigned_by_fkey
FOREIGN KEY (assigned_by) REFERENCES public.profiles(id);

ALTER TABLE public.tasks
ADD CONSTRAINT tasks_department_id_fkey
FOREIGN KEY (department_id) REFERENCES public.departments(id);

ALTER TABLE public.attendance
ADD CONSTRAINT attendance_user_id_fkey
FOREIGN KEY (user_id) REFERENCES public.profiles(id);

ALTER TABLE public.leaves
ADD CONSTRAINT leaves_user_id_fkey
FOREIGN KEY (user_id) REFERENCES public.profiles(id);

ALTER TABLE public.leaves
ADD CONSTRAINT leaves_approved_by_fkey
FOREIGN KEY (approved_by) REFERENCES public.profiles(id);

ALTER TABLE public.messages
ADD CONSTRAINT messages_sender_id_fkey
FOREIGN KEY (sender_id) REFERENCES public.profiles(id);

ALTER TABLE public.messages
ADD CONSTRAINT messages_receiver_id_fkey
FOREIGN KEY (receiver_id) REFERENCES public.profiles(id);

ALTER TABLE public.messages
ADD CONSTRAINT messages_parent_message_id_fkey
FOREIGN KEY (parent_message_id) REFERENCES public.messages(id);

-- Optional: Link to auth.users
DO $$
BEGIN
  ALTER TABLE public.profiles
  ADD CONSTRAINT profiles_id_fkey 
  FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE;
EXCEPTION WHEN OTHERS THEN
  -- Ignore if constraint already exists or auth.users not accessible
  NULL;
END $$;

-- ============================================
-- STEP 4: CREATE INDEXES
-- ============================================
CREATE INDEX idx_profiles_department ON public.profiles(department_id);
CREATE INDEX idx_employees_user ON public.employees(user_id);
CREATE INDEX idx_tasks_assigned_to ON public.tasks(assigned_to);
CREATE INDEX idx_tasks_status ON public.tasks(status);
CREATE INDEX idx_attendance_user_date ON public.attendance(user_id, date);
CREATE INDEX idx_leaves_user ON public.leaves(user_id);
CREATE INDEX idx_leaves_status ON public.leaves(status);
CREATE INDEX idx_messages_receiver ON public.messages(receiver_id);
CREATE INDEX idx_messages_sender ON public.messages(sender_id);

-- ============================================
-- STEP 5: ENABLE ROW LEVEL SECURITY
-- ============================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.departments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.leaves ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- ============================================
-- STEP 6: CREATE RLS POLICIES
-- ============================================

-- Profiles policies
CREATE POLICY "Users can view their own profile"
  ON public.profiles FOR SELECT
  USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile"
  ON public.profiles FOR UPDATE
  USING (auth.uid() = id);

CREATE POLICY "Admins can view all profiles"
  ON public.profiles FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid() AND p.role IN ('admin', 'hr')
    )
  );

-- Departments policies
CREATE POLICY "Everyone can view departments"
  ON public.departments FOR SELECT
  USING (true);

CREATE POLICY "Admins and HR can manage departments"
  ON public.departments FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid() AND p.role IN ('admin', 'hr')
    )
  );

-- Employees policies
CREATE POLICY "Users can view their own employee record"
  ON public.employees FOR SELECT
  USING (user_id = auth.uid());

CREATE POLICY "Admins and HR can view all employees"
  ON public.employees FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid() AND p.role IN ('admin', 'hr')
    )
  );

CREATE POLICY "Admins and HR can manage employees"
  ON public.employees FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid() AND p.role IN ('admin', 'hr')
    )
  );

-- Tasks policies
CREATE POLICY "Users can view tasks assigned to them"
  ON public.tasks FOR SELECT
  USING (assigned_to = auth.uid());

CREATE POLICY "Managers can view tasks in their department"
  ON public.tasks FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      JOIN public.departments d ON p.department_id = d.id
      WHERE p.id = auth.uid() 
      AND (p.role IN ('manager', 'admin', 'hr') OR d.manager_id = auth.uid())
      AND tasks.department_id = d.id
    )
  );

CREATE POLICY "Users can create tasks"
  ON public.tasks FOR INSERT
  WITH CHECK (assigned_by = auth.uid());

CREATE POLICY "Users can update their assigned tasks"
  ON public.tasks FOR UPDATE
  USING (assigned_to = auth.uid());

CREATE POLICY "Admins and HR can manage all tasks"
  ON public.tasks FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid() AND p.role IN ('admin', 'hr')
    )
  );

-- Attendance policies
CREATE POLICY "Users can view their own attendance"
  ON public.attendance FOR SELECT
  USING (user_id = auth.uid());

CREATE POLICY "Users can create their own attendance"
  ON public.attendance FOR INSERT
  WITH CHECK (user_id = auth.uid());

CREATE POLICY "Admins and HR can view all attendance"
  ON public.attendance FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid() AND p.role IN ('admin', 'hr')
    )
  );

CREATE POLICY "Admins and HR can manage attendance"
  ON public.attendance FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      WHERE p.id = auth.uid() AND p.role IN ('admin', 'hr')
    )
  );

-- Leaves policies
CREATE POLICY "Users can view their own leaves"
  ON public.leaves FOR SELECT
  USING (user_id = auth.uid());

CREATE POLICY "Users can create their own leave requests"
  ON public.leaves FOR INSERT
  WITH CHECK (user_id = auth.uid());

CREATE POLICY "Managers can view leaves in their department"
  ON public.leaves FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      JOIN public.departments d ON p.department_id = d.id
      WHERE (p.role IN ('manager', 'admin', 'hr') OR d.manager_id = auth.uid())
      AND EXISTS (
        SELECT 1 FROM public.profiles p2
        WHERE p2.id = leaves.user_id AND p2.department_id = d.id
      )
    )
  );

CREATE POLICY "Managers can approve/reject leaves"
  ON public.leaves FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.profiles p
      JOIN public.departments d ON p.department_id = d.id
      WHERE (p.role IN ('manager', 'admin', 'hr') OR d.manager_id = auth.uid())
      AND EXISTS (
        SELECT 1 FROM public.profiles p2
        WHERE p2.id = leaves.user_id AND p2.department_id = d.id
      )
    )
  );

-- Messages policies
CREATE POLICY "Users can view messages sent to them"
  ON public.messages FOR SELECT
  USING (receiver_id = auth.uid() OR sender_id = auth.uid());

CREATE POLICY "Users can send messages"
  ON public.messages FOR INSERT
  WITH CHECK (sender_id = auth.uid());

CREATE POLICY "Users can update their received messages"
  ON public.messages FOR UPDATE
  USING (receiver_id = auth.uid());

-- ============================================
-- STEP 7: CREATE FUNCTIONS AND TRIGGERS
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers for updated_at
CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE ON public.profiles
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON public.departments
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON public.employees
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON public.tasks
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_attendance_updated_at BEFORE UPDATE ON public.attendance
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leaves_updated_at BEFORE UPDATE ON public.leaves
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_messages_updated_at BEFORE UPDATE ON public.messages
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to create profile when user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, email, full_name)
  VALUES (
    NEW.id,
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email)
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to create profile on user signup
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
