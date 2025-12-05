import os
import logging
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
import openai

from models import ChatRequest, StatsResponse
from database import get_client_singleton
from crud import (
    list_rows,
    get_row,
    insert_row,
    update_row,
    delete_row,
    count_rows,
)
from models import (
    ProfileModel,
    SectorModel,
    GradeModel,
    ChatLogModel,
    AIAnalysisModel,
)
from redis import Redis
from rq import Queue
from fastapi import Depends
from auth import get_current_user, require_admin

# Redis / RQ setup - used to enqueue analysis jobs handled by `worker.process_analysis`.
REDIS_URL = os.getenv("REDIS_URL", "redis://redis:6379")
redis_conn = Redis.from_url(REDIS_URL)
queue = Queue("default", connection=redis_conn)

load_dotenv()

APP_ORIGINS = ["*"]

app = FastAPI(title="Trinity Backend")

# Basic CORS setup - in production restrict this to known origins
app.add_middleware(
    CORSMiddleware,
    allow_origins=APP_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configure OpenAI
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-3.5-turbo")

if not OPENAI_API_KEY:
    logging.warning("OPENAI_API_KEY not set; AI endpoints will fail until configured.")
else:
    openai.api_key = OPENAI_API_KEY

# Supabase client (singleton)
supabase = get_client_singleton()


@app.post("/chat")
async def chat_endpoint(req: ChatRequest):
    """Accept a user message, generate an AI response, and persist both messages.

    Flow:
    1. Build a system prompt to set Trinity AI behavior.
    2. Call OpenAI's ChatCompletion API.
    3. Insert both user message and AI response into `chat_logs` table in Supabase.
    4. Return the AI reply to the client.
    """
    if not req.message:
        raise HTTPException(status_code=400, detail="Message is required")

    system_prompt = (
        "You are Trinity AI, a concise and professional assistant that helps employees with company operations. "
        "Answer clearly and provide actionable steps when appropriate."
    )

    try:
        # Use OpenAI ChatCompletion for a safe, predictable chat interface.
        response = openai.ChatCompletion.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": req.message},
            ],
            max_tokens=512,
            temperature=0.2,
        )

        ai_text = response["choices"][0]["message"]["content"].strip()

    except Exception as e:
        logging.exception("OpenAI request failed")
        raise HTTPException(status_code=500, detail=f"AI generation failed: {e}")

    # Persist both user and assistant messages into Supabase `chat_logs` table.
    # The table is expected to have columns: user_id, message, role, created_at (optional).
    try:
        entries = [
            {"user_id": req.user_id, "message": req.message, "role": "user"},
            {"user_id": req.user_id, "message": ai_text, "role": "assistant"},
        ]

        # Attempt to insert rows. Compatibility note: supabase client libs differ on method names.
        # Using .table(...).insert(...).execute() pattern which is supported by supabase-py.
        insert_res = supabase.table(os.getenv("CHAT_LOGS_TABLE", "chat_logs")).insert(entries).execute()
        logging.debug("Inserted chat logs: %s", insert_res)
    except Exception:
        # Log insertion failure but still return a successful AI response to the caller.
        logging.exception("Failed to insert chat logs into Supabase")

    return {"reply": ai_text}


@app.post('/ai/analyze')
async def enqueue_analysis(payload: dict, current_user: dict = Depends(get_current_user)):
    """Enqueue an AI analysis job for background processing. Returns the job id.

    Expected payload keys: `text` (required), optional `user_id`, `meta`.
    """
    text = payload.get('text') if isinstance(payload, dict) else None
    if not text:
        raise HTTPException(status_code=400, detail='text is required')

    job = queue.enqueue('worker.process_analysis', payload)
    return {"job_id": job.get_id()}


@app.get("/dashboard/stats", response_model=StatsResponse)
async def dashboard_stats():
    """Return basic dashboard statistics, currently total employees (profiles count).

    Uses Supabase `profiles` table to compute a simple count. Some Supabase clients
    return count in different shapes; this function attempts to be robust.
    """
    table_name = os.getenv("PROFILES_TABLE", "profiles")
    try:
        # Request an exact count from Supabase. The returned object varies by client.
        res = supabase.table(table_name).select("id", count="exact").execute()

        total = None
        # Try to read a `.count` attribute (object response)
        if hasattr(res, "count") and res.count is not None:
            total = int(res.count)
        else:
            # Fallbacks for dict-like responses
            if isinstance(res, dict):
                total = int(res.get("count") or len(res.get("data", [])))
            else:
                data = getattr(res, "data", None)
                total = len(data) if data is not None else 0

    except Exception:
        logging.exception("Failed to query profiles count from Supabase")
        total = 0

    return StatsResponse(total_employees=total)


## Simple CRUD endpoints for `profiles` (example). Repeat patterns for other tables.


@app.get("/profiles")
async def list_profiles(limit: int = 100):
    table = os.getenv("PROFILES_TABLE", "profiles")
    rows = list_rows(table, select='*', limit=limit)
    return {"data": rows}


@app.post("/profiles")
async def create_profile(payload: dict, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("PROFILES_TABLE", "profiles")
    created = insert_row(table, payload)
    if created is None:
        raise HTTPException(status_code=500, detail="Failed to create profile")
    return {"data": created}


@app.get("/profiles/{profile_id}")
async def get_profile(profile_id: int):
    table = os.getenv("PROFILES_TABLE", "profiles")
    row = get_row(table, profile_id)
    if not row:
        raise HTTPException(status_code=404, detail="Profile not found")
    return {"data": row}


@app.put("/profiles/{profile_id}")
async def update_profile(profile_id: int, payload: dict, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("PROFILES_TABLE", "profiles")
    updated = update_row(table, profile_id, payload)
    if updated is None:
        raise HTTPException(status_code=500, detail="Failed to update profile")
    return {"data": updated}


@app.delete("/profiles/{profile_id}")
async def delete_profile(profile_id: int, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("PROFILES_TABLE", "profiles")
    ok = delete_row(table, profile_id)
    if not ok:
        raise HTTPException(status_code=500, detail="Failed to delete profile")
    return {"deleted": True}


### Sectors CRUD


@app.get("/sectors")
async def list_sectors(limit: int = 100):
    table = os.getenv("SECTORS_TABLE", "sectors")
    rows = list_rows(table, select='*', limit=limit)
    return {"data": rows}


@app.post("/sectors")
async def create_sector(payload: SectorModel, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("SECTORS_TABLE", "sectors")
    created = insert_row(table, payload.dict(exclude_unset=True))
    if created is None:
        raise HTTPException(status_code=500, detail="Failed to create sector")
    return {"data": created}


@app.put("/sectors/{sector_id}")
async def update_sector(sector_id: int, payload: SectorModel, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("SECTORS_TABLE", "sectors")
    updated = update_row(table, sector_id, payload.dict(exclude_unset=True))
    if updated is None:
        raise HTTPException(status_code=500, detail="Failed to update sector")
    return {"data": updated}


@app.get("/sectors/{sector_id}")
async def get_sector(sector_id: int):
    table = os.getenv("SECTORS_TABLE", "sectors")
    row = get_row(table, sector_id)
    if not row:
        raise HTTPException(status_code=404, detail="Sector not found")
    return {"data": row}


@app.delete("/sectors/{sector_id}")
async def delete_sector(sector_id: int, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("SECTORS_TABLE", "sectors")
    ok = delete_row(table, sector_id)
    if not ok:
        raise HTTPException(status_code=500, detail="Failed to delete sector")
    return {"deleted": True}


### Grades CRUD


@app.get("/grades")
async def list_grades(limit: int = 100):
    table = os.getenv("GRADES_TABLE", "grades")
    rows = list_rows(table, select='*', limit=limit)
    return {"data": rows}


@app.post("/grades")
async def create_grade(payload: GradeModel, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("GRADES_TABLE", "grades")
    created = insert_row(table, payload.dict(exclude_unset=True))
    if created is None:
        raise HTTPException(status_code=500, detail="Failed to create grade")
    return {"data": created}


@app.put("/grades/{grade_id}")
async def update_grade(grade_id: int, payload: GradeModel, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("GRADES_TABLE", "grades")
    updated = update_row(table, grade_id, payload.dict(exclude_unset=True))
    if updated is None:
        raise HTTPException(status_code=500, detail="Failed to update grade")
    return {"data": updated}


@app.get("/grades/{grade_id}")
async def get_grade(grade_id: int):
    table = os.getenv("GRADES_TABLE", "grades")
    row = get_row(table, grade_id)
    if not row:
        raise HTTPException(status_code=404, detail="Grade not found")
    return {"data": row}


@app.delete("/grades/{grade_id}")
async def delete_grade(grade_id: int, current_user: dict = Depends(get_current_user), admin: dict = Depends(require_admin)):
    table = os.getenv("GRADES_TABLE", "grades")
    ok = delete_row(table, grade_id)
    if not ok:
        raise HTTPException(status_code=500, detail="Failed to delete grade")
    return {"deleted": True}


### Chat logs (read-only write via /chat endpoint)


@app.get("/chat_logs")
async def list_chat_logs(limit: int = 200):
    table = os.getenv("CHAT_LOGS_TABLE", "chat_logs")
    rows = list_rows(table, select='*', limit=limit)
    return {"data": rows}


### AI Analysis endpoints


@app.get('/ai/analysis')
async def list_analyses(limit: int = 100):
    table = os.getenv("AI_ANALYSIS_TABLE", "ai_analysis")
    rows = list_rows(table, select='*', limit=limit)
    return {"data": rows}


@app.get('/ai/analysis/{analysis_id}')
async def get_analysis(analysis_id: int):
    table = os.getenv("AI_ANALYSIS_TABLE", "ai_analysis")
    row = get_row(table, analysis_id)
    if not row:
        raise HTTPException(status_code=404, detail='Analysis not found')
    return {"data": row}

