import os
from dotenv import load_dotenv
from supabase import create_client, Client
import logging

# Load environment variables from .env if present
load_dotenv()

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_KEY = os.getenv("SUPABASE_KEY")

if not SUPABASE_URL or not SUPABASE_KEY:
    logging.warning("SUPABASE_URL or SUPABASE_KEY not set; ensure environment variables are configured")


def get_supabase() -> Client:
    """Create and return a Supabase client using environment variables.

    This function centralizes client creation so other modules can import it.
    """
    return create_client(SUPABASE_URL, SUPABASE_KEY)


# Provide a module-level client for convenience in quick scripts.
_supabase_client = None


def get_client_singleton() -> Client:
    global _supabase_client
    if _supabase_client is None:
        _supabase_client = get_supabase()
    return _supabase_client
