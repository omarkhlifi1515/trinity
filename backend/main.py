import os
import datetime
from typing import Optional

import httpx
from fastapi import FastAPI, Depends, HTTPException
from pydantic import BaseModel

from deps import get_supabase_client, get_current_user, require_role

app = FastAPI(title="Project Trinity - Backend")


class ChatMessage(BaseModel):
    user_id: str
    content: str
    is_bot_command: Optional[bool] = False
    department: Optional[str] = None


@app.post("/chat/send")
async def chat_send(msg: ChatMessage, current_user=Depends(get_current_user)):
    """Save a message to DB. If it's a bot command, forward to n8n webhook.

    Expects `N8N_WEBHOOK_URL` in env for outbound commands.
    """
    sb = get_supabase_client()
    now = datetime.datetime.utcnow().isoformat()
    payload = {
        "user_id": current_user["id"],
        "content": msg.content,
        "is_bot_command": bool(msg.is_bot_command),
        "department": current_user.get("department"),
        "created_at": now,
    }
    # store message
    try:
        res = sb.table("chat_messages").insert(payload).execute()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"DB insert failed: {e}")

    # determine if should send to n8n
    should_call_n8n = msg.is_bot_command or ("trinity" in msg.content.lower())
    if should_call_n8n:
        n8n = os.environ.get("N8N_WEBHOOK_URL")
        if n8n:
            try:
                async with httpx.AsyncClient() as client:
                    await client.post(n8n, json={"user": current_user, "message": msg.content, "chat_record": payload})
            except Exception:
                # don't fail the request if n8n is unreachable; it's async downstream work
                pass

    return {"ok": True}


class N8NReply(BaseModel):
    chat_user_id: str
    reply: str


@app.post("/chat/webhook")
async def chat_webhook(body: N8NReply):
    """n8n should call this to insert replies into chat history.

    Body: { chat_user_id, reply }
    """
    sb = get_supabase_client()
    now = datetime.datetime.utcnow().isoformat()
    payload = {
        "user_id": body.chat_user_id,
        "content": body.reply,
        "is_bot_command": True,
        "department": None,
        "created_at": now,
        "from_bot": True,
    }
    try:
        sb.table("chat_messages").insert(payload).execute()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"DB insert failed: {e}")
    return {"ok": True}


@app.get("/department/news")
async def department_news(current_user=Depends(get_current_user)):
    """Fetch news rows for the user's department. n8n will update this table."""
    sb = get_supabase_client()
    dept = current_user.get("department")
    try:
        res = sb.table("department_news").select("*").eq("department", dept).order("created_at", desc=True).execute()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"DB query failed: {e}")
    return {"news": res.data if hasattr(res, "data") else []}



# --- MODELS ---
class ChatMessage(BaseModel):
    content: str
    is_bot_command: Optional[bool] = False

class N8NReply(BaseModel):
    chat_user_id: str
    reply: str

class NewsUpdate(BaseModel):
    department: str
    articles: List[Dict[str, Any]]  # List of {title, description}

# --- ROUTES ---

@app.get("/")
def read_root():
    return {"status": "online", "message": "Trinity V2 Backend is Active"}

@app.get("/chat/history")
async def get_chat_history(current_user=Depends(get_current_user)):
    """Fetch chat history for the current user."""
    sb = get_supabase_client()
    try:
        # Get messages for this user, sorted by time
        res = sb.table("chat_messages").select("*").eq("user_id", current_user["id"]).order("created_at", desc=False).execute()
        return {"messages": res.data if hasattr(res, "data") else []}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to fetch history: {e}")

@app.post("/chat/send")
async def chat_send(msg: ChatMessage, current_user=Depends(get_current_user)):
    """Save user message and trigger n8n if needed."""
    sb = get_supabase_client()
    now = datetime.datetime.utcnow().isoformat()
    
    payload = {
        "user_id": current_user["id"],
        "content": msg.content,
        "is_bot_command": msg.is_bot_command,
        "department": current_user.get("department", "general"),
        "from_bot": False,
        "created_at": now,
    }

    try:
        sb.table("chat_messages").insert(payload).execute()
    except Exception as e:
        print(f"DB Error: {e}")
        raise HTTPException(status_code=500, detail="Message save failed")

    # Trigger n8n
    if msg.is_bot_command or "trinity" in msg.content.lower():
        n8n_url = os.environ.get("N8N_WEBHOOK_URL")
        if n8n_url:
            # Fire and forget (don't wait for n8n)
            try:
                async with httpx.AsyncClient() as client:
                    await client.post(n8n_url, json={
                        "user_id": current_user["id"],
                        "user_email": current_user.get("email", ""),
                        "message": msg.content,
                        "department": current_user.get("department", "general")
                    }, timeout=2.0) 
            except Exception as e:
                print(f"N8N Trigger Error: {e}")

    return {"ok": True}

@app.post("/chat/webhook")
async def chat_webhook(body: N8NReply):
    """Receive reply from n8n."""
    sb = get_supabase_client()
    now = datetime.datetime.utcnow().isoformat()
    payload = {
        "user_id": body.chat_user_id,
        "content": body.reply,
        "from_bot": True,
        "created_at": now,
    }
    try:
        sb.table("chat_messages").insert(payload).execute()
    except Exception as e:
        print(f"Webhook Error: {e}")
        raise HTTPException(status_code=500, detail=f"DB insert failed: {e}")
    return {"ok": True}

@app.post("/news/update")
async def update_news(payload: NewsUpdate):
    """Receive news from n8n and save to DB."""
    sb = get_supabase_client()
    
    # Optional: Clear old news for this department first
    # sb.table("department_news").delete().eq("department", payload.department).execute()

    rows = []
    for article in payload.articles:
        rows.append({
            "department": payload.department,
            "title": article.get("title"),
            "body": article.get("description"),
            "created_at": datetime.datetime.utcnow().isoformat()
        })
    
    if rows:
        try:
            sb.table("department_news").insert(rows).execute()
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Failed to save news: {e}")
            
    return {"count": len(rows)}

@app.get("/department/news")
async def department_news(current_user=Depends(get_current_user)):
    sb = get_supabase_client()
    dept = current_user.get("department", "general")
    res = sb.table("department_news").select("*").eq("department", dept).order("created_at", desc=True).limit(10).execute()
    return {"news": res.data if hasattr(res, "data") else []}
