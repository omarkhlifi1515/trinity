import os
from typing import Optional, Callable

from fastapi import Header, HTTPException, status, Depends

try:
    from supabase import create_client
except Exception:
    create_client = None  # optional import; document install in README

import jwt


def get_supabase_client():
    """Return a Supabase client using environment variables.

    NOTE: In local/dev, this will raise if env vars are missing.
    """
    url = os.environ.get("SUPABASE_URL")
    key = os.environ.get("SUPABASE_SERVICE_ROLE_KEY") or os.environ.get("SUPABASE_ANON_KEY")
    if not url or not key:
        raise RuntimeError("SUPABASE_URL and SUPABASE_*_KEY must be set in env")
    if create_client is None:
        raise RuntimeError("supabase-py is not installed. pip install supabase")
    return create_client(url, key)


def _verify_jwt(token: str):
    """Verify a Supabase JWT using SUPABASE_JWT_SECRET if present.

    If `SUPABASE_JWT_SECRET` is not set, this function returns None.
    """
    secret = os.environ.get("SUPABASE_JWT_SECRET")
    if not secret:
        return None
    try:
        payload = jwt.decode(token, secret, algorithms=["HS256"], options={"verify_aud": False})
        return payload
    except Exception:
        return None


def get_current_user(authorization: Optional[str] = Header(None), x_user_id: Optional[str] = Header(None), x_user_role: Optional[str] = Header(None), x_user_department: Optional[str] = Header(None)):
    """Auth dependency.

    Priority:
    - If an `Authorization: Bearer <token>` header is provided and `SUPABASE_JWT_SECRET` is set,
      decode and use the token.
    - Otherwise, fall back to lightweight header-based stub (`x-user-id`, `x-user-role`, `x-user-department`).
    """
    # Bearer token path
    if authorization and authorization.lower().startswith("bearer "):
        token = authorization.split(" ", 1)[1]
        payload = _verify_jwt(token)
        if payload:
            # Supabase stores sub as user id
            return {
                "id": payload.get("sub"),
                "role": payload.get("role") or "employee",
                "department": payload.get("department") or "general",
            }

    # Fallback to header stub (development)
    if x_user_id:
        return {"id": x_user_id, "role": x_user_role or "employee", "department": x_user_department or "general"}

    raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing authentication")


def require_role(role: str):
    def _dep(current_user=Depends(get_current_user)):
        if not current_user:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED)
        if current_user.get("role") != role and current_user.get("role") != "admin":
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Insufficient role")
        return current_user

    return _dep
