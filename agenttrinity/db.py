import os
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime, ForeignKey
from sqlalchemy.types import JSON
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime

# Securely get URL
DATABASE_URL = os.environ.get('DATABASE_URL')
if DATABASE_URL and DATABASE_URL.startswith("postgres://"):
    DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://", 1)

engine = create_engine(DATABASE_URL, future=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
Base = declarative_base()

class User(Base):
    __tablename__ = 'users'  # Updated to match Web
    id = Column(Integer, primary_key=True)
    username = Column(String(128), nullable=False)
    email = Column(String(256))
    role = Column(String(32))
    phone_number = Column(String(64))
    department_id = Column(Integer, ForeignKey('departments.id'))
    status = Column(String(32), default='Active')

class Department(Base):
    __tablename__ = 'departments'  # Updated
    id = Column(Integer, primary_key=True)
    name = Column(String(128), nullable=False)
    manager_id = Column(Integer, ForeignKey('users.id'))

class Task(Base):
    __tablename__ = 'tasks'  # Updated
    id = Column(Integer, primary_key=True)
    title = Column(String(256))
    description = Column(Text)
    status = Column(String(32), default='Todo')
    priority = Column(String(32))
    due_date = Column(DateTime)
    creator_id = Column(Integer, ForeignKey('users.id'))
    assignees = Column(JSON)

class Message(Base):
    __tablename__ = 'messages'  # Updated
    id = Column(Integer, primary_key=True)
    content = Column(Text)
    timestamp = Column(DateTime, default=datetime.utcnow)
    user_id = Column(Integer, ForeignKey('users.id'))


def init_db():
    Base.metadata.create_all(bind=engine)


def get_session():
    return SessionLocal()

