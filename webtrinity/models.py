from flask_login import UserMixin
from sqlalchemy.dialects.postgresql import JSON
from datetime import datetime, timezone
from extensions import db  # Import from extensions


class User(UserMixin, db.Model):
    __tablename__ = 'users'  # Renamed to avoid Postgres reserved word
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(128), nullable=False, unique=True)
    email = db.Column(db.String(256))
    role = db.Column(db.String(32))
    password_hash = db.Column(db.String(256))
    phone_number = db.Column(db.String(64))
    department_id = db.Column(db.Integer, db.ForeignKey('departments.id'))
    status = db.Column(db.String(32), default='Active')

    def to_dict(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'role': self.role,
            'status': self.status,
        }


class Department(db.Model):
    __tablename__ = 'departments'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(128), nullable=False)
    manager_id = db.Column(db.Integer, db.ForeignKey('users.id'))


class Task(db.Model):
    __tablename__ = 'tasks'
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(256))
    description = db.Column(db.Text)
    status = db.Column(db.String(32), default='Todo')
    priority = db.Column(db.String(32))
    due_date = db.Column(db.DateTime)
    creator_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    assignees = db.Column(JSON)


class Message(db.Model):
    __tablename__ = 'messages'
    id = db.Column(db.Integer, primary_key=True)
    content = db.Column(db.Text)
    timestamp = db.Column(db.DateTime, default=lambda: datetime.now(timezone.utc))
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'))

