from flask import Flask, jsonify, request, render_template, redirect, url_for, flash
from dotenv import load_dotenv
import os
from flask_login import LoginManager, login_user, logout_user, login_required, current_user

# 1. Import db from extensions
from extensions import db
# 2. Import models so SQLAlchemy knows they exist
from models import User, Department, Task, Message

load_dotenv()

app = Flask(__name__, template_folder='templates')

# Config
DEFAULT_DATABASE_URL = os.environ.get('DATABASE_URL', 'sqlite:///local_data.db')
# Fix Render's postgres:// -> postgresql://
if DEFAULT_DATABASE_URL.startswith("postgres://"):
    DEFAULT_DATABASE_URL = DEFAULT_DATABASE_URL.replace("postgres://", "postgresql://", 1)

app.config['SQLALCHEMY_DATABASE_URI'] = DEFAULT_DATABASE_URL
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', 'trinity-secret-key')

# 3. Initialize DB with the app
db.init_app(app)

# 4. AUTO-CREATE TABLES (fix for missing tables)
with app.app_context():
    print("🛠️ Checking database tables...")
    db.create_all()
    print("✅ Database tables ready.")

# Setup Flask-Login
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'


@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))


# Authentication Routes
@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        user = User.query.filter_by(username=username).first()
        
        if user and getattr(user, 'check_password', lambda p: False)(password):
            login_user(user)
            return redirect(url_for('dashboard'))
        else:
            return render_template('login.html', error='Invalid username or password'), 401
    
    return render_template('login.html')


@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('login'))


@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form.get('username')
        email = request.form.get('email')
        password = request.form.get('password')
        
        if User.query.filter_by(username=username).first():
            return render_template('register.html', error='Username already exists'), 400
        
        user = User(username=username, email=email)
        if hasattr(user, 'set_password'):
            user.set_password(password)
        db.session.add(user)
        db.session.commit()
        
        login_user(user)
        return redirect(url_for('dashboard'))
    
    return render_template('register.html')


# Protected Routes
@app.route('/')
def index():
    if current_user.is_authenticated:
        return redirect(url_for('dashboard'))
    return redirect(url_for('login'))


@app.route('/dashboard')
@login_required
def dashboard():
    stats = {
        'total_tasks': Task.query.count(),
        'my_tasks': Task.query.count(),  # Simplified; should filter by assignees
        'done': Task.query.filter_by(status='Done').count(),
        'in_progress': Task.query.filter_by(status='In Progress').count(),
        'todo': Task.query.filter_by(status='Todo').count(),
    }
    return render_template('dashboard.html', stats=stats, current_user=current_user)


# API Routes (Protected)
@app.route('/api/tasks')
@login_required
def my_tasks():
    tasks = Task.query.all()  # Simplified; should filter by current_user
    return jsonify([{
        'id': t.id,
        'title': t.title,
        'status': t.status,
        'priority': t.priority,
        'due_date': t.due_date.isoformat() if t.due_date else None,
    } for t in tasks])


@app.route('/api/stats')
@login_required
def stats():
    total_tasks = Task.query.count()
    done = Task.query.filter_by(status='Done').count()
    absent = User.query.filter_by(status='Absent').count()
    return jsonify({
        'total_tasks': total_tasks,
        'done': done,
        'absent_users': absent
    })


@app.route('/api/users')
@login_required
def list_users():
    # Only admins or managers can list users
    if current_user.role not in ['admin', 'manager']:
        return jsonify({'error': 'Forbidden'}), 403
    
    users = User.query.all()
    return jsonify([u.to_dict() for u in users])


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5000)))

