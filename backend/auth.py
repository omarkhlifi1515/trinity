from fastapi import HTTPException, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi import Security
import logging

from database import get_client_singleton

security = HTTPBearer()
supabase = get_client_singleton()


async def get_current_user(credentials: HTTPAuthorizationCredentials = Security(security)):
    """FastAPI dependency to validate Supabase JWT from Authorization header.

    Uses the Supabase client to retrieve the user associated with the access token.
    This avoids raw JWT verification and relies on Supabase's auth API.
    """
    token = credentials.credentials if credentials else None
    if not token:
        raise HTTPException(status_code=401, detail="Missing authorization token")

    try:
        # Attempt to get the user via supabase auth client. Different client versions
        # may expose different methods/shapes, so try common options.
        try:
            res = supabase.auth.get_user(token)
        except TypeError:
            # older/newer clients may require keyword
            try:
                res = supabase.auth.get_user(access_token=token)
            except Exception:
                # final fallback to api namespace
                res = supabase.auth.api.get_user(token)

        # Extract user from possible response shapes
        user = None
        if isinstance(res, dict):
            # supabase-py often returns {'data': {...}} or {'user': {...}}
            user = res.get('user') or res.get('data') or res.get('data', {}).get('user')
        else:
            user = getattr(res, 'user', None) or getattr(res, 'data', None)

        if not user:
            raise Exception('User not found')

        return user

    except Exception as e:
        logging.exception('Failed to validate Supabase token')
        raise HTTPException(status_code=401, detail='Invalid or expired token')


def require_admin(current_user: dict = Depends(get_current_user)):
    """Dependency ensuring the current user has admin privileges.

    This checks a few common places where roles may be stored in the Supabase
    user object: top-level `role`, `app_metadata`, or `user_metadata`.
    """
    # Typical Supabase user shapes may include keys like 'role' or nested metadata.
    role = None
    if isinstance(current_user, dict):
        role = current_user.get('role')
        if not role:
            # Check common metadata places
            app_meta = current_user.get('app_metadata') or {}
            user_meta = current_user.get('user_metadata') or {}
            # app_metadata may contain a 'role' or 'roles' list
            role = app_meta.get('role') or app_meta.get('roles') or user_meta.get('role')
            if isinstance(role, list):
                # if roles list, see if 'admin' is present
                if 'admin' in role:
                    return current_user

    if role == 'admin':
        return current_user

    raise HTTPException(status_code=403, detail='Admin privileges required')
