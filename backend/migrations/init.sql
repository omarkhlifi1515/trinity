-- Initial schema for Project Trinity

-- users
create table if not exists users (
  id uuid primary key default gen_random_uuid(),
  email text unique,
  full_name text,
  role text default 'employee',
  department text default 'general',
  created_at timestamptz default now()
);

-- chat messages
create table if not exists chat_messages (
  id uuid primary key default gen_random_uuid(),
  user_id uuid,
  content text not null,
  is_bot_command boolean default false,
  from_bot boolean default false,
  department text,
  created_at timestamptz default now()
);

-- department news
create table if not exists department_news (
  id uuid primary key default gen_random_uuid(),
  department text not null,
  title text,
  body text,
  created_at timestamptz default now()
);

-- tasks
create table if not exists tasks (
  id uuid primary key default gen_random_uuid(),
  assignee_id uuid,
  creator_id uuid,
  title text,
  details text,
  status text default 'open',
  created_at timestamptz default now()
);

-- payslips (metadata only; files stored in storage)
create table if not exists payslips (
  id uuid primary key default gen_random_uuid(),
  user_id uuid,
  file_path text,
  uploaded_by uuid,
  created_at timestamptz default now()
);
