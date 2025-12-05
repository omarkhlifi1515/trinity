# Project Trinity

Full-stack command system: Backend (FastAPI), Web Dashboard (Next.js), Mobile (Kotlin Jetpack Compose).

See individual folders for instructions:

- `backend/` — FastAPI service connecting to Supabase and OpenAI.
- `web/` — Next.js frontend (App Router) with Tailwind.
- `mobile/` — Android app (Jetpack Compose) with Retrofit + Hilt.

Quick start (backend):

1. Copy `backend/.env` to `backend/.env.local` and fill in secrets.
2. Create a venv and install requirements:

```powershell
cd "c:\Users\msi\Desktop\New folder (3)\trinity\backend"
python -m venv .venv; .venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

For frontend and mobile instructions see `web/` and `mobile/README_ANDROID.md`.
