"""WebTrinity models using Flask-SQLAlchemy.

Inherits from shared declarative models defined in models_shared.py.
"""
from extensions import db
from models_shared import Base as SharedBase, User as SharedUser, Department as SharedDept, Task as SharedTask, Message as SharedMsg
from flask_login import UserMixin


# Re-export shared models but bind them to Flask-SQLAlchemy instance
class User(db.Model, UserMixin, SharedUser):
    __tablename__ = 'user'

    def to_dict(self):
        return {
            'id': self.id,
            'username': self.username,
            'email': self.email,
            'role': self.role,
            'phone_number': self.phone_number,
            'department_id': self.department_id,
            'status': self.status,
        }


class Department(db.Model, SharedDept):
    __tablename__ = 'department'


class Task(db.Model, SharedTask):
    __tablename__ = 'task'


class Message(db.Model, SharedMsg):
    __tablename__ = 'message'

