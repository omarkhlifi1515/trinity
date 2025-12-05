from flask import Flask, request, jsonify
from dotenv import load_dotenv
import os
from datetime import datetime
from functools import wraps

load_dotenv()

from db import init_db, get_session, Task, Message, User

app = Flask(__name__)

# Load API key from environment — fallback to insecure default if missing
AGENT_API_KEY = os.environ.get('AGENT_API_KEY')
if not AGENT_API_KEY:
    print("⚠️ WARNING: AGENT_API_KEY not found. Using default insecure key.")
    AGENT_API_KEY = "default-insecure-key"


def require_api_key(f):
    """Decorator to check API key in request headers."""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Allow health endpoint without a key
        if request.endpoint == 'health':
            return f(*args, **kwargs)
        # Accept both header variants
        key = request.headers.get('X-API-KEY') or request.headers.get('X-API-Key')
        if key != AGENT_API_KEY:
            return jsonify({'error': 'Unauthorized'}), 401
        return f(*args, **kwargs)
    return decorated_function


@app.before_first_request
def startup():
    # Ensure tables exist
    init_db()


@app.route('/health')
def health():
    return jsonify({'status': 'ok'})


@app.route('/create_task', methods=['POST'])
@require_api_key
def create_task():
    data = request.get_json() or {}
    title = data.get('title')
    description = data.get('description')
    assignees = data.get('assignees', [])
    priority = data.get('priority')
    due_date_raw = data.get('due_date')
    creator_id = data.get('creator_id')

    due_date = None
    if due_date_raw:
        try:
            due_date = datetime.fromisoformat(due_date_raw)
        except Exception:
            due_date = None

    session = get_session()
    try:
        task = Task(
            title=title,
            description=description,
            assignees=assignees,
            priority=priority,
            due_date=due_date,
            creator_id=creator_id,
            status='Todo'
        )
        session.add(task)
        session.commit()
        session.refresh(task)
        result = {
            'id': task.id,
            'title': task.title,
            'description': task.description,
            'assignees': task.assignees,
            'status': task.status
        }
        return jsonify(result), 201
    except Exception as e:
        session.rollback()
        return jsonify({'error': str(e)}), 500
    finally:
        session.close()


@app.route('/send_notification', methods=['POST'])
@require_api_key
def send_notification():
    data = request.get_json() or {}
    user_id = data.get('user_id')
    message = data.get('message')

    session = get_session()
    try:
        msg = Message(content=message, user_id=user_id)
        session.add(msg)
        session.commit()
        session.refresh(msg)
        # Placeholder: integrate with an external provider here
        return jsonify({'id': msg.id, 'user_id': msg.user_id, 'message': msg.content}), 200
    except Exception as e:
        session.rollback()
        return jsonify({'error': str(e)}), 500
    finally:
        session.close()


@app.route('/update_status', methods=['POST'])
@require_api_key
def update_status():
    data = request.get_json() or {}
    entity = (data.get('entity') or '').lower()
    entity_id = data.get('id')
    field = data.get('field')
    value = data.get('value')

    session = get_session()
    try:
        if entity == 'task':
            task = session.get(Task, int(entity_id))
            if not task:
                return jsonify({'error': 'task not found'}), 404
            if field == 'status':
                task.status = value
            elif field == 'priority':
                task.priority = value
            session.add(task)
            session.commit()
            return jsonify({'id': task.id, 'status': task.status}), 200

        elif entity == 'user':
            user = session.get(User, int(entity_id))
            if not user:
                return jsonify({'error': 'user not found'}), 404
            if field == 'status':
                user.status = value
                session.add(user)
                session.commit()
                return jsonify({'id': user.id, 'status': user.status}), 200
            return jsonify({'error': 'unsupported field for user'}), 400

        else:
            return jsonify({'error': 'unsupported entity type'}), 400

    except Exception as e:
        session.rollback()
        return jsonify({'error': str(e)}), 500
    finally:
        session.close()


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
