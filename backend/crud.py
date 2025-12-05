"""Simple Supabase CRUD helper utilities.

These functions centralize common patterns used by the API endpoints.
"""
from typing import Any, Dict, List, Optional
import os
import logging
from database import get_client_singleton

supabase = get_client_singleton()


def list_rows(table: str, select: str = '*', limit: Optional[int] = None) -> List[Dict[str, Any]]:
    q = supabase.table(table).select(select)
    if limit:
        q = q.limit(limit)
    res = q.execute()
    # supabase-py returns an object with `.data` or dict with 'data'
    data = getattr(res, 'data', None) or (res.get('data') if isinstance(res, dict) else None)
    return data or []


def get_row(table: str, row_id: Any) -> Optional[Dict[str, Any]]:
    try:
        res = supabase.table(table).select('*').eq('id', row_id).single().execute()
        return getattr(res, 'data', None) or (res.get('data') if isinstance(res, dict) else None)
    except Exception:
        logging.exception('get_row failed')
        return None


def insert_row(table: str, payload: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    try:
        res = supabase.table(table).insert(payload).execute()
        return getattr(res, 'data', None) or (res.get('data') if isinstance(res, dict) else None)
    except Exception:
        logging.exception('insert_row failed')
        return None


def update_row(table: str, row_id: Any, payload: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    try:
        res = supabase.table(table).update(payload).eq('id', row_id).execute()
        return getattr(res, 'data', None) or (res.get('data') if isinstance(res, dict) else None)
    except Exception:
        logging.exception('update_row failed')
        return None


def delete_row(table: str, row_id: Any) -> bool:
    try:
        res = supabase.table(table).delete().eq('id', row_id).execute()
        # check rows affected
        data = getattr(res, 'data', None) or (res.get('data') if isinstance(res, dict) else None)
        return True if data else False
    except Exception:
        logging.exception('delete_row failed')
        return False


def count_rows(table: str) -> int:
    try:
        res = supabase.table(table).select('id', count='exact').execute()
        if hasattr(res, 'count') and res.count is not None:
            return int(res.count)
        if isinstance(res, dict):
            return int(res.get('count') or len(res.get('data', [])))
        data = getattr(res, 'data', None)
        return len(data) if data is not None else 0
    except Exception:
        logging.exception('count_rows failed')
        return 0
