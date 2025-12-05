from flask import Flask, jsonify, request, render_template, redirect, url_for, flash
from flask_login import LoginManager, login_user, login_required, logout_user, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from dotenv import load_dotenv
import os

# Custom imports
from extensions import db
from models import User, Department, Task, Message

load_dotenv()

app = Flask(__name__, template_folder='templates')

# --- CONFIG ---
DEFAULT_DATABASE_URL = os.environ.get('DATABASE_URL', 'sqlite:///local_data.db')
if DEFAULT_DATABASE_URL.startswith("postgres://"):
    DEFAULT_DATABASE_URL = DEFAULT_DATABASE_URL.replace("postgres://", "postgresql://", 1)

app.config['SQLALCHEMY_DATABASE_URI'] = DEFAULT_DATABASE_URL
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', 'trinity-super-secret')

# --- INIT EXTENSIONS ---
db.init_app(app)

login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'

@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))

# --- AUTO CREATE TABLES ---
with app.app_context():
    db.create_all()

# --- AUTH ROUTES ---

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('dashboard'))
    
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        
        user = User.query.filter_by(username=username).first()
        
        # simplified login for demo (add hash checking in production)
        if user:
            login_user(user)
            return redirect(url_for('dashboard'))
        else:
            flash('Invalid username or password', 'danger')
            
    return render_template('login.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form.get('username')
        email = request.form.get('email')
        
        if User.query.filter_by(username=username).first():
            flash('Username exists', 'warning')
            return redirect(url_for('register'))
            
        new_user = User(username=username, email=email)
        db.session.add(new_user)
        db.session.commit()
        
        flash('Registration successful! Please login.', 'success')
        return redirect(url_for('login'))
        
    return render_template('register.html')

@app.route('/logout')
@login_required
def logout():
    logout_user()
    flash('You have been logged out.', 'info')
    return redirect(url_for('login'))

@app.route('/')
def index():
    if current_user.is_authenticated:
        return redirect(url_for('dashboard'))
    return redirect(url_for('login'))

# --- THE 15 PROFESSIONAL GUI ROUTES ---

@app.route('/dashboard')
@login_required
def dashboard():
    return render_template('dashboard.html', page="Dashboard")

@app.route('/live-map')
@login_required
def live_map():
    return render_template('dashboard.html', page="Live Attack Map")

@app.route('/tasks')
@login_required
def tasks():
    tasks = Task.query.all()
    return render_template('dashboard.html', page="Task Manager", data=tasks)

@app.route('/users')
@login_required
def users():
    users = User.query.all()
    return render_template('dashboard.html', page="User Management", data=users)

@app.route('/scanner/nmap')
@login_required
def tool_nmap():
    return render_template('dashboard.html', page="Nmap Scanner")

@app.route('/scanner/sqlmap')
@login_required
def tool_sqlmap():
    return render_template('dashboard.html', page="SQLMap Injection")

@app.route('/scanner/nikto')
@login_required
def tool_nikto():
    return render_template('dashboard.html', page="Nikto Vulnerability")

@app.route('/scanner/nuclei')
@login_required
def tool_nuclei():
    return render_template('dashboard.html', page="Nuclei Templates")

@app.route('/chat')
@login_required
def chat():
    return render_template('dashboard.html', page="Encrypted Chat")

@app.route('/files')
@login_required
def files():
    return render_template('dashboard.html', page="Exfiltrated Files")

@app.route('/logs')
@login_required
def logs():
    return render_template('dashboard.html', page="System Logs")

@app.route('/api-keys')
@login_required
def api_keys():
    return render_template('dashboard.html', page="API Key Vault")

@app.route('/departments')
@login_required
def departments():
    return render_template('dashboard.html', page="Department Ops")

@app.route('/settings')
@login_required
def settings():
    return render_template('dashboard.html', page="System Settings")

@app.route('/help')
@login_required
def help_page():
    return render_template('dashboard.html', page="Documentation")

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5000)))
