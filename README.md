# Project Trinity (v2 - Enterprise Edition)

This workspace contains a monorepo scaffold for Project Trinity with:

- `backend/` — FastAPI backend (Supabase, Pydantic v2)
- `web/` — Next.js 14 (App Router) frontend (Tailwind + Shadcn)
- `mobile/` — (placeholder) React Native (Expo) mobile app

This commit adds core starter files for the backend and web app: role-aware APIs, chat endpoints integrated with n8n, a Supabase client for Next.js, and a role-based sidebar + dashboard/chat pages.

Environment variables (minimum):

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY` or `SUPABASE_SERVICE_ROLE_KEY` (for server)
- `N8N_WEBHOOK_URL` (optional — used by backend to forward bot commands)
- `NEXT_PUBLIC_API_URL` (URL to the FastAPI backend, used by the web app)
- `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_ANON_KEY` (used by web client)

Backend quickstart (Windows PowerShell):

```powershell
cd C:/Users/msi/Desktop/proj/trinity/backend
python -m venv .venv; .\.venv\Scripts\Activate.ps1
pip install fastapi uvicorn pydantic supabase httpx
setx SUPABASE_URL "https://your.supabase.url"
setx SUPABASE_SERVICE_ROLE_KEY "your_service_role_key"
setx N8N_WEBHOOK_URL "https://n8n.example/webhook"
uvicorn main:app --reload --port 8000
```

Web quickstart (Next.js):

```powershell
cd C:/Users/msi/Desktop/proj/trinity/web
# create a Next project and install deps as needed (this repo only adds component files)
npm init -y
npm install next react react-dom @supabase/supabase-js
# add Tailwind/shadcn as desired
setx NEXT_PUBLIC_API_URL "http://localhost:8000"
setx NEXT_PUBLIC_SUPABASE_URL "https://your.supabase.url"
setx NEXT_PUBLIC_SUPABASE_ANON_KEY "your_anon_key"
npm run dev
```

Notes & next steps

- The backend uses a simple header-based stub auth for development (`x-user-id`, `x-user-role`, `x-user-department`). Replace with real Supabase JWT validation in `backend/deps.py`.
- Database tables expected (examples): `users`, `chat_messages`, `department_news`, `tasks`, `payslips`.
- n8n should POST back to `/chat/webhook` to insert bot replies.
- Mobile app (Expo) scaffold is not added yet — I can scaffold it next using `expo init` and `expo-router` with NativeWind.

If you want I can:

- Add full Supabase JWT validation and a migrations SQL file
- Scaffold the Expo mobile app and wire NativeWind
- Add CI and Render/Deploy manifests (Render for web, Supabase for DB)
