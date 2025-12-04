import os
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime, ForeignKey
from sqlalchemy.types import JSON
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from datetime import datetime

DEFAULT_DATABASE_URL = 'postgresql://trinity_5mxn_user:6q8XxiTlIiEN7NS9ehPiOGENlWeU4Pqr@dpg-d4o6t22dbo4c73ab07t0-a.oregon-postgres.render.com/trinity_5mxn'
DATABASE_URL = os.environ.get('DATABASE_URL', DEFAULT_DATABASE_URL)

engine = create_engine(DATABASE_URL, future=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
Base = declarative_base()


class User(Base):
    __tablename__ = 'user'
    id = Column(Integer, primary_key=True)
    username = Column(String(128), nullable=False)
    email = Column(String(256))
    role = Column(String(32))
    phone_number = Column(String(64))
    department_id = Column(Integer, ForeignKey('department.id'))
    status = Column(String(32), default='Active')


class Department(Base):
    __tablename__ = 'department'
    id = Column(Integer, primary_key=True)
    name = Column(String(128), nullable=False)
    manager_id = Column(Integer, ForeignKey('user.id'))


class Task(Base):
    __tablename__ = 'task'
    id = Column(Integer, primary_key=True)
    title = Column(String(256))
    description = Column(Text)
    status = Column(String(32), default='Todo')
    priority = Column(String(32))
    due_date = Column(DateTime)
    creator_id = Column(Integer, ForeignKey('user.id'))
    assignees = Column(JSON)


class Message(Base):
    __tablename__ = 'message'
    id = Column(Integer, primary_key=True)
    content = Column(Text)
    timestamp = Column(DateTime, default=datetime.utcnow)
    user_id = Column(Integer, ForeignKey('user.id'))


def init_db():
    """Create tables if they don't exist."""
    Base.metadata.create_all(bind=engine)


def get_session():
    return SessionLocal()
