-- PART 3: Enable RLS and Create Policies
-- Run this after part 2 succeeds

-- Enable Row Level Security
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.departments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.leaves ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

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

