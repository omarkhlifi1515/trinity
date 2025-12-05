import os
from flask import Flask, request, jsonify, render_template, redirect, url_for
from db import init_db, Task, get_session

app = Flask(__name__)

# --- CONFIGURATION ---
AGENT_API_KEY = os.environ.get('AGENT_API_KEY', 'default-insecure-key')

# --- DATABASE INIT ---
with app.app_context():
    init_db()

# --- MIDDLEWARE (Security) ---
@app.before_request
def require_api_key():
    # Allow the GUI (Home) and Static files without a key
    allowed_routes = ['home', 'add_task_web', 'static', 'health']
    if request.endpoint in allowed_routes:
        return
    
    # API endpoints still need the key
    key = request.headers.get('X-API-KEY')
    if key != AGENT_API_KEY:
        return jsonify({'error': 'Unauthorized'}), 401

# --- GUI ROUTES (For your Browser) ---

@app.route('/', methods=['GET'])
def home():
    session = get_session()
    # Fetch all tasks from DB to show in the GUI
    tasks = session.query(Task).order_by(Task.id.desc()).all()
    session.close()
    return render_template('dashboard.html', tasks=tasks)

@app.route('/add_task_web', methods=['POST'])
def add_task_web():
    # This route handles the form submission from the GUI
    title = request.form.get('title')
    description = request.form.get('description')
    
    session = get_session()
    new_task = Task(title=title, description=description, status='Pending')
    session.add(new_task)
    session.commit()
    session.close()
    
    # Reload the dashboard
    return redirect(url_for('home'))

# --- API ROUTES (For the Android App / Scripts) ---

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "running"})

@app.route('/create_task', methods=['POST'])
def create_task():
    data = request.json
    session = get_session()
    try:
        new_task = Task(title=data.get('title'), description=data.get('description'), status='Pending')
        session.add(new_task)
        session.commit()
        return jsonify({'id': new_task.id, 'status': 'Created'}), 201
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        session.close()

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 10000))
    app.run(host='0.0.0.0', port=port)
