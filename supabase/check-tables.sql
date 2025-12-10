-- Diagnostic Script: Check if tables and columns exist
-- Run this FIRST to verify Part 1 was successful

-- Check if tables exist
SELECT 
    table_name,
    CASE WHEN EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = t.table_name
    ) THEN 'EXISTS' ELSE 'MISSING' END as status
FROM (VALUES 
    ('users'),
    ('employees'),
    ('departments'),
    ('tasks'),
    ('leaves'),
    ('messages'),
    ('attendance')
) AS t(table_name);

-- Check columns in leaves table
SELECT 
    column_name,
    data_type
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'leaves'
ORDER BY ordinal_position;

-- Check columns in attendance table
SELECT 
    column_name,
    data_type
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'attendance'
ORDER BY ordinal_position;

