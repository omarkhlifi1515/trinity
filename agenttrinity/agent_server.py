import os
from flask import Flask, request, jsonify
# Use standard SQLAlchemy imports if db.py is giving trouble
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from db import init_db, Task, get_session

app = Flask(__name__)

# --- CONFIGURATION ---
AGENT_API_KEY = os.environ.get('AGENT_API_KEY')
if not AGENT_API_KEY:
    print("⚠️ WARNING: AGENT_API_KEY not found. Using default insecure key.")
    AGENT_API_KEY = "default-insecure-key"

# --- DATABASE INIT ---
with app.app_context():
    init_db()

# --- MIDDLEWARE ---
@app.before_request
def require_api_key():
    # Allow health checks without authentication
    if request.endpoint == 'health':
        return
    
    # Check for API Key in headers
    key = request.headers.get('X-API-KEY')
    if key != AGENT_API_KEY:
        return jsonify({'error': 'Unauthorized'}), 401

# --- ROUTES ---

# 1. Health Check (ONLY DEFINE THIS ONCE)
@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "running", "agent": "Trinity-01"})

# 2. Get Tasks
@app.route('/tasks', methods=['GET'])
def get_tasks():
    session = get_session()
    try:
        tasks = session.query(Task).filter_by(status='Pending').all()
        return jsonify([{
            'id': t.id, 
            'command': t.description, 
            'status': t.status
        } for t in tasks])
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        session.close()

# 3. Create Task (For the Mobile App to talk to)
@app.route('/create_task', methods=['POST'])
def create_task():
    data = request.json
    title = data.get('title')
    description = data.get('description')
    
    session = get_session()
    try:
        new_task = Task(title=title, description=description, status='Pending')
        session.add(new_task)
        session.commit()
        return jsonify({'id': new_task.id, 'status': 'Created'}), 201
    except Exception as e:
        session.rollback()
        return jsonify({'error': str(e)}), 500
    finally:
        session.close()

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 10000))
    app.run(host='0.0.0.0', port=port)
