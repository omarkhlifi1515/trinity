"""Shared Trinity database models.

Both webtrinity and agenttrinity import from this module to ensure schema consistency.
"""
from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey
from sqlalchemy.types import JSON
from sqlalchemy.ext.declarative import declarative_base
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash

Base = declarative_base()


class User(Base):
    __tablename__ = 'user'
    id = Column(Integer, primary_key=True)
    username = Column(String(128), nullable=False, unique=True)
    email = Column(String(256), unique=True)
    password_hash = Column(String(256))
    role = Column(String(32), default='employee')  # admin, manager, employee
    phone_number = Column(String(64))
    department_id = Column(Integer, ForeignKey('department.id'))
    status = Column(String(32), default='Active')  # Active, Absent, On Leave

    def set_password(self, password):
        """Hash and store password."""
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        """Verify password hash."""
        return check_password_hash(self.password_hash, password)


class Department(Base):
    __tablename__ = 'department'
    id = Column(Integer, primary_key=True)
    name = Column(String(128), nullable=False, unique=True)
    manager_id = Column(Integer, ForeignKey('user.id'))


class Task(Base):
    __tablename__ = 'task'
    id = Column(Integer, primary_key=True)
    title = Column(String(256), nullable=False)
    description = Column(Text)
    status = Column(String(32), default='Todo')  # Todo, In Progress, Done
    priority = Column(String(32), default='Medium')  # Low, Medium, High
    due_date = Column(DateTime)
    creator_id = Column(Integer, ForeignKey('user.id'))
    assignees = Column(JSON, default=[])  # List of user IDs
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class Message(Base):
    __tablename__ = 'message'
    id = Column(Integer, primary_key=True)
    content = Column(Text, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)
    user_id = Column(Integer, ForeignKey('user.id'))

