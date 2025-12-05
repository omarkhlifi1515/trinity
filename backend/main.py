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


# Example RBAC routes
@app.post("/admin/users/{user_id}/role")
async def admin_set_role(user_id: str, role: str, admin=Depends(require_role("admin"))):
    sb = get_supabase_client()
    try:
        sb.table("users").update({"role": role}).eq("id", user_id).execute()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"DB update failed: {e}")
    return {"ok": True}


@app.post("/chef/tasks")
async def chef_assign_task(assignee_id: str, title: str, details: Optional[str] = None, chef=Depends(require_role("chef"))):
    sb = get_supabase_client()
    payload = {"assignee_id": assignee_id, "title": title, "details": details, "created_at": datetime.datetime.utcnow().isoformat(), "creator_id": chef["id"]}
    try:
        sb.table("tasks").insert(payload).execute()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"DB insert failed: {e}")
    return {"ok": True}


@app.get("/me/tasks")
async def me_tasks(current_user=Depends(get_current_user)):
    sb = get_supabase_client()
    try:
        res = sb.table("tasks").select("*").eq("assignee_id", current_user["id"]).order("created_at", desc=True).execute()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"DB query failed: {e}")
    return {"tasks": res.data if hasattr(res, "data") else []}
