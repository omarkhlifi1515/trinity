import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from datetime import datetime

# Import shared models
import sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'webtrinity'))
from models_shared import Base, User, Department, Task, Message

# DATABASE_URL must be provided via environment variable (never hardcode secrets)
DATABASE_URL = os.environ.get('DATABASE_URL')
if not DATABASE_URL:
    raise ValueError('DATABASE_URL environment variable is required. Set it before running the agent.')

engine = create_engine(DATABASE_URL, future=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)


def init_db():
    """Create tables if they don't exist."""
    Base.metadata.create_all(bind=engine)


def get_session():
    return SessionLocal()

