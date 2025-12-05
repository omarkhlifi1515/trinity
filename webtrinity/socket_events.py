from flask_socketio import emit, join_room
from flask_login import current_user
from extensions import db, socketio
from models import Task, User
from datetime import datetime


@socketio.on('connect')
def handle_connect():
    try:
        if current_user.is_authenticated:
            # Join a room specific to their role (manager or employee)
            join_room(f"role_{current_user.role}")
            # Also join a personal room for private reminders
            join_room(f"user_{current_user.id}")
            print(f"User {current_user.username} connected to channel: role_{current_user.role}")
    except Exception as e:
        print('connect error:', e)


@socketio.on('assign_task')
def handle_assign_task(data):
    """
    Manager sends this event. Server saves it and notifies employees instantly.
    """
    try:
        if not getattr(current_user, 'role', None) == 'manager':
            return

        title = data.get('title')
        assigned_to_id = data.get('assignee_id') or None

        # 1. Save to DB
        new_task = Task(
            title=title,
            status='Todo',
            creator_id=getattr(current_user, 'id', None),
            assignees=[assigned_to_id] if assigned_to_id else []
        )
        db.session.add(new_task)
        db.session.commit()

        # 2. REAL-TIME NOTIFICATION
        payload = {
            'id': new_task.id,
            'title': new_task.title,
            'status': 'Todo',
            'timestamp': datetime.now().strftime("%H:%M")
        }

        if assigned_to_id:
            emit('new_task_alert', payload, room=f"user_{assigned_to_id}")
        else:
            emit('new_task_alert', payload, room='role_employee')
    except Exception as e:
        print('assign_task error:', e)


@socketio.on('complete_task')
def handle_complete_task(data):
    try:
        task_id = data.get('task_id')
        task = Task.query.get(task_id)
        if task:
            task.status = 'Done'
            db.session.commit()
            # Notify the managers immediately
            emit('task_completed_alert', {
                'task_id': task_id,
                'user': getattr(current_user, 'username', 'unknown')
            }, room='role_manager')
    except Exception as e:
        print('complete_task error:', e)
