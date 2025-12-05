
import os
import logging
from typing import List, Dict, Any

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv

from database import get_client_singleton
from crud import list_rows, get_row, insert_row, update_row, delete_row, count_rows
from models import Sector, Tool, Profile, Message, UserUpdate, StatsResponse
from auth import get_current_user, require_admin

load_dotenv()

APP_ORIGINS = ["*"]

app = FastAPI(title="Trinity Backend - Enterprise Hub")

app.add_middleware(
    CORSMiddleware,
    allow_origins=APP_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Supabase client (singleton)
supabase = get_client_singleton()


def _extract_user_id(user_obj: Dict[str, Any]) -> str:
    """Robustly extract a user id from Supabase user object shapes."""
    if not user_obj:
        return ""
    if isinstance(user_obj, dict):
        # possible shapes: {'id': '...'}, or {'user': {...}}, or {'data': {...}}
        if user_obj.get('id'):
            return str(user_obj.get('id'))
        if user_obj.get('user') and isinstance(user_obj.get('user'), dict):
            return str(user_obj.get('user').get('id') or user_obj.get('user').get('sub') or '')
        if user_obj.get('data') and isinstance(user_obj.get('data'), dict):
            return str(user_obj.get('data').get('id') or user_obj.get('data').get('user', {}).get('id') or '')
        # fallback to sub
        return str(user_obj.get('sub') or '')
    return ""


@app.get("/")
async def root():
    return {"status": "online"}


@app.get("/toolbox/my-tools")
async def my_tools(current_user: dict = Depends(get_current_user)):
    """Return tools assigned to the current user's sector."""
    user_id = _extract_user_id(current_user)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid user")

    try:
        # Attempt to find profile by user_id in profiles table
        tbl = os.getenv("PROFILES_TABLE", "profiles")
        res = supabase.table(tbl).select("*").eq("user_id", user_id).single().execute()
        profile = getattr(res, 'data', None) or (res.get('data') if isinstance(res, dict) else None)
        if not profile:
            return {"data": []}
        sector_id = profile.get('sector_id')
        if not sector_id:
            return {"data": []}

        tools_tbl = os.getenv("TOOLS_TABLE", "tools")
        tres = supabase.table(tools_tbl).select("*").eq("sector_id", sector_id).execute()
        tools = getattr(tres, 'data', None) or (tres.get('data') if isinstance(tres, dict) else [])
        return {"data": tools}
    except Exception:
        logging.exception("Failed to fetch tools for user")
        raise HTTPException(status_code=500, detail="Failed to fetch tools")


# --- Messaging APIs ---
@app.get('/messages/conversations')
async def list_conversations(current_user: dict = Depends(get_current_user)):
    user_id = _extract_user_id(current_user)
    msgs = list_rows(os.getenv('MESSAGES_TABLE', 'messages'))
    # find conversation partner ids
    partners = {}
    for m in msgs:
        sid = str(m.get('sender_id'))
        rid = str(m.get('receiver_id'))
        if sid == user_id:
            other = rid
        elif rid == user_id:
            other = sid
        else:
            continue
        # keep last message
        ts = m.get('created_at') or m.get('created') or m.get('id')
        partners[other] = partners.get(other) or {}
        partners[other]['last_message'] = m.get('content')
        partners[other]['last_ts'] = ts

    # convert to list
    convs = [{"user_id": k, "last_message": v.get('last_message'), "last_ts": v.get('last_ts')} for k, v in partners.items()]
    return {"data": convs}


@app.get('/messages/{other_user_id}')
async def get_conversation(other_user_id: str, current_user: dict = Depends(get_current_user)):
    user_id = _extract_user_id(current_user)
    msgs = list_rows(os.getenv('MESSAGES_TABLE', 'messages'))
    conv = [m for m in msgs if (str(m.get('sender_id')) == user_id and str(m.get('receiver_id')) == str(other_user_id)) or (str(m.get('sender_id')) == str(other_user_id) and str(m.get('receiver_id')) == user_id)]
    # sort by created_at if present
    try:
        conv = sorted(conv, key=lambda x: x.get('created_at') or x.get('id'))
    except Exception:
        pass
    return {"data": conv}


@app.post('/messages')
async def send_message(payload: Dict[str, Any], current_user: dict = Depends(get_current_user)):
    user_id = _extract_user_id(current_user)
    receiver_id = payload.get('receiver_id')
    content = payload.get('content')
    if not receiver_id or not content:
        raise HTTPException(status_code=400, detail='receiver_id and content are required')
    row = {
        'sender_id': user_id,
        'receiver_id': str(receiver_id),
        'content': content,
    }
    inserted = insert_row(os.getenv('MESSAGES_TABLE', 'messages'), row)
    if not inserted:
        raise HTTPException(status_code=500, detail='Failed to send message')
    return {"data": inserted}


# --- Admin APIs ---
@app.get('/admin/users')
async def admin_list_users(admin_user: dict = Depends(require_admin)):
    rows = list_rows(os.getenv('PROFILES_TABLE', 'profiles'))
    return {"data": rows}


@app.put('/admin/users/{profile_id}')
async def admin_update_user(profile_id: int, payload: UserUpdate, admin_user: dict = Depends(require_admin)):
    table = os.getenv('PROFILES_TABLE', 'profiles')
    data = payload.model_dump(exclude_none=True)
    updated = update_row(table, profile_id, data)
    if updated is None:
        raise HTTPException(status_code=500, detail='Failed to update user')
    return {"data": updated}


@app.post('/admin/sectors')
async def admin_create_sector(payload: Dict[str, Any], admin_user: dict = Depends(require_admin)):
    created = insert_row(os.getenv('SECTORS_TABLE', 'sectors'), payload)
    if not created:
        raise HTTPException(status_code=500, detail='Failed to create sector')
    return {"data": created}


@app.post('/admin/tools')
async def admin_create_tool(payload: Dict[str, Any], admin_user: dict = Depends(require_admin)):
    created = insert_row(os.getenv('TOOLS_TABLE', 'tools'), payload)
    if not created:
        raise HTTPException(status_code=500, detail='Failed to create tool')
    return {"data": created}


