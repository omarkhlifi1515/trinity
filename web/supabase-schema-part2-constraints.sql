-- PART 2: Add Foreign Keys and Indexes
-- Run this after part 1 succeeds

-- Add foreign key constraints
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

-- Create indexes
CREATE INDEX idx_profiles_department ON public.profiles(department_id);
CREATE INDEX idx_employees_user ON public.employees(user_id);
CREATE INDEX idx_tasks_assigned_to ON public.tasks(assigned_to);
CREATE INDEX idx_tasks_status ON public.tasks(status);
CREATE INDEX idx_attendance_user_date ON public.attendance(user_id, date);
CREATE INDEX idx_leaves_user ON public.leaves(user_id);
CREATE INDEX idx_leaves_status ON public.leaves(status);
CREATE INDEX idx_messages_receiver ON public.messages(receiver_id);
CREATE INDEX idx_messages_sender ON public.messages(sender_id);

