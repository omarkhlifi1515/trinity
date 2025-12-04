from app import db, login_manager
from flask_login import UserMixin
from datetime import datetime

# Association table for User and Role many-to-many relationship
user_roles = db.Table('user_roles',
    db.Column('user_id', db.Integer, db.ForeignKey('user.id'), primary_key=True),
    db.Column('role_id', db.Integer, db.ForeignKey('role.id'), primary_key=True)
)

class User(UserMixin, db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(64), index=True, unique=True)
    password = db.Column(db.String(128))
    department = db.Column(db.String(64), default='General')
    roles = db.relationship('Role', secondary=user_roles, lazy='subquery',
                            backref=db.backref('users', lazy=True))
    messages = db.relationship('Message', backref='author', lazy='dynamic')
    tasks = db.relationship('Task', backref='employee', lazy='dynamic')
    documents = db.relationship('Document', backref='owner', lazy='dynamic')
    presence = db.relationship('Presence', backref='user', lazy='dynamic')

    def has_role(self, role_name):
        return any(role.name == role_name for role in self.roles)

class Role(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), unique=True)

class Message(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    text = db.Column(db.String(200))
    timestamp = db.Column(db.DateTime, index=True, default=datetime.utcnow)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))

class Task(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    description = db.Column(db.String(200))
    status = db.Column(db.String(64), default='Pending')
    proof_file = db.Column(db.String(128))
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))

class Document(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    filename = db.Column(db.String(128))
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))

class Presence(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))

class Employee(db.Model):
    """HR Portal Employee Model - Stored in Google Drive Database"""
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False, index=True)
    role = db.Column(db.String(20), nullable=False, default='Employee')  # Chef or Employee
    status = db.Column(db.String(20), nullable=False, default='Active')  # Active or Absent
    contact_info = db.Column(db.String(200))  # Email, phone, etc.
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Link to User account if exists
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=True)
    user = db.relationship('User', backref=db.backref('employee_profile', uselist=False))
    
    def __repr__(self):
        return f'<Employee {self.name} - {self.role}>'
    
    def to_dict(self):
        """Convert employee to dictionary for JSON responses"""
        return {
            'id': self.id,
            'name': self.name,
            'role': self.role,
            'status': self.status,
            'contact_info': self.contact_info,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }

@login_manager.user_loader
def load_user(user_id):
    # Make the user loader resilient: if the database is unavailable or
    # an OperationalError occurs, return None so Flask-Login treats the
    # visitor as anonymous instead of raising a 500 error.
    try:
        return db.session.get(User, int(user_id))
    except Exception as e:
        # Avoid importing app at module scope; use flask.current_app for logging
        try:
            from flask import current_app
            current_app.logger.exception('Error loading user %s: %s', user_id, e)
        except Exception:
            # If logging also fails, silently ignore to avoid cascading errors
            pass
        return None
