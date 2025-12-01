import json
import datetime
import os
from functools import wraps
from flask import Flask, render_template, redirect, url_for, flash, abort, send_from_directory, jsonify, request
from flask_login import LoginManager, UserMixin, login_user, login_required, logout_user, current_user
from flask_wtf import FlaskForm
from flask_wtf.file import FileField, FileAllowed
from wtforms import StringField, PasswordField, SubmitField, TextAreaField, SelectField
from wtforms.validators import DataRequired, EqualTo, ValidationError, Length
from flask_bcrypt import Bcrypt
from werkzeug.utils import secure_filename

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret-key'
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['SECURE_DOCUMENT_FOLDER'] = 'secure_documents'


bcrypt = Bcrypt(app)
login_manager = LoginManager(app)
login_manager.login_view = 'login'
login_manager.unauthorized_handler(lambda: redirect(url_for('login')))

# --- Data File Constants ---
USER_DATA_FILE = 'users.json'
CHAT_DATA_FILE = 'chat_history.json'
TASK_DATA_FILE = 'tasks.json'
DOCUMENTS_DATA_FILE = 'documents.json'
PRESENCE_DATA_FILE = 'presence.json'
MAX_CHAT_MESSAGES = 100
DEPARTMENTS = ["General", "Engineering", "Analytics", "Security", "HR"]


# --- Data Loading/Saving Functions ---
def load_data(file_path, default_data):
    try:
        with open(file_path, 'r') as f:
            return json.load(f)
    except (FileNotFoundError, json.JSONDecodeError):
        return default_data

def save_data(file_path, data):
    with open(file_path, 'w') as f:
        json.dump(data, f, indent=4)

# Wrappers for specific data files
def load_users(): return load_data(USER_DATA_FILE, {})
def save_users(data): save_data(USER_DATA_FILE, data)
def load_messages(): return load_data(CHAT_DATA_FILE, [])
def save_messages(data): save_data(CHAT_DATA_FILE, data[-MAX_CHAT_MESSAGES:])
def load_tasks(): return load_data(TASK_DATA_FILE, {})
def save_tasks(data): save_data(TASK_DATA_FILE, data)
def load_documents(): return load_data(DOCUMENTS_DATA_FILE, {})
def load_presence(): return load_data(PRESENCE_DATA_FILE, {})
def save_presence(data): save_data(PRESENCE_DATA_FILE, data)


# --- Initial User Setup ---
users = load_users()
if 'chef' not in users:
    users['chef'] = { "password": bcrypt.generate_password_hash("chef").decode('utf-8'), "roles": ["CHEF", "USER"], "department": "Management" }
    save_users(users)


class User(UserMixin):
    def __init__(self, id, roles):
        self.id = id
        self.roles = roles

def roles_required(*required_roles):
    def wrapper(fn):
        @wraps(fn)
        def decorated_view(*args, **kwargs):
            if not current_user.is_authenticated:
                return login_manager.unauthorized()
            if not all(role in current_user.roles for role in required_roles):
                abort(403)
            return fn(*args, **kwargs)
        return decorated_view
    return wrapper

@login_manager.user_loader
def load_user(user_id):
    current_users = load_users()
    if user_id in current_users:
        return User(user_id, current_users[user_id]["roles"])
    return None

# --- Forms ---
class RegistrationForm(FlaskForm):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    confirm_password = PasswordField('Confirm Password', validators=[DataRequired(), EqualTo('password')])
    submit = SubmitField('Register')
    def validate_username(self, username):
        if username.data in load_users():
            raise ValidationError('That username is already taken.')

class LoginForm(FlaskForm):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    submit = SubmitField('Login')

class ChatMessageForm(FlaskForm):
    message = TextAreaField('Message', validators=[DataRequired(), Length(min=1, max=200)])
    submit = SubmitField('Send')
    
class TaskProofForm(FlaskForm):
    proof = FileField('Proof of Completion', validators=[FileAllowed(['jpg', 'png', 'pdf', 'txt'])])
    submit = SubmitField('Upload')

class PresenceForm(FlaskForm):
    submit = SubmitField('Mark as Present')

class TaskAssignmentForm(FlaskForm):
    employee = SelectField('Employee', validators=[DataRequired()])
    description = TextAreaField('Task Description', validators=[DataRequired(), Length(min=5, max=200)])
    submit = SubmitField('Assign Task')
    
class AccessControlForm(FlaskForm):
    employee = SelectField('Employee', validators=[DataRequired()])
    department = SelectField('Department', choices=DEPARTMENTS, validators=[DataRequired()])
    submit = SubmitField('Update Access')

# --- Auth Routes ---
@app.route('/register', methods=['GET', 'POST'])
def register():
    form = RegistrationForm()
    if form.validate_on_submit():
        current_users = load_users()
        hashed_password = bcrypt.generate_password_hash(form.password.data).decode('utf-8')
        current_users[form.username.data] = {"password": hashed_password, "roles": ["USER"], "department": "General"}
        save_users(current_users)
        flash('Your account has been created!', 'success')
        return redirect(url_for('login'))
    return render_template('register.html', title='Register', form=form)

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('dashboard_chef' if "CHEF" in current_user.roles else 'dashboard_user'))
    form = LoginForm()
    if form.validate_on_submit():
        user_data = load_users().get(form.username.data)
        if user_data and bcrypt.check_password_hash(user_data['password'], form.password.data):
            user = User(form.username.data, user_data["roles"])
            login_user(user)
            return redirect(url_for('dashboard_chef' if "CHEF" in user.roles else 'dashboard_user'))
        else:
            flash('Login Unsuccessful. Please check username and password', 'danger')
    return render_template('login.html', form=form)

@app.route('/')
def index():
    return redirect(url_for('login'))
    
@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('login'))

# --- User Routes ---
@app.route('/dashboard-user', methods=['GET', 'POST'])
@login_required
@roles_required('USER')
def dashboard_user():
    form = PresenceForm()
    if form.validate_on_submit():
        presence_data = load_presence()
        presence_data[current_user.id] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        save_presence(presence_data)
        flash('You have been marked present for today!', 'success')
        return redirect(url_for('dashboard_user'))
    
    presence_data = load_presence().get(current_user.id)
    is_present_today = False
    if presence_data:
        last_seen_date = datetime.datetime.strptime(presence_data, "%Y-%m-%d %H:%M:%S").date()
        if last_seen_date == datetime.date.today():
            is_present_today = True
            
    return render_template('dashboarduser.html', form=form, is_present_today=is_present_today)

@app.route('/tasks')
@login_required
@roles_required('USER')
def tasks():
    form = TaskProofForm()
    user_tasks = load_tasks().get(current_user.id, [])
    return render_template('tasks.html', tasks=user_tasks, form=form)

@app.route('/upload_proof/<int:task_id>', methods=['POST'])
@login_required
@roles_required('USER')
def upload_proof(task_id):
    form = TaskProofForm()
    if form.validate_on_submit() and form.proof.data:
        all_tasks = load_tasks()
        user_tasks = all_tasks.get(current_user.id, [])
        task_to_update = next((task for task in user_tasks if task['id'] == task_id), None)
        
        if task_to_update:
            file = form.proof.data
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            
            task_to_update['status'] = 'Completed'
            task_to_update['proof_file'] = filename
            
            save_tasks(all_tasks)
            flash('Proof uploaded successfully!', 'success')
        else:
            flash('Task not found.', 'danger')
    else:
        flash('No file selected or invalid file type.', 'warning')
            
    return redirect(url_for('tasks'))

@app.route('/chat', methods=['GET', 'POST'])
@login_required
@roles_required('USER')
def chat():
    form = ChatMessageForm()
    if form.validate_on_submit():
        messages = load_messages()
        new_message = {
            "user": current_user.id, 
            "text": form.message.data, 
            "timestamp": datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }
        messages.append(new_message)
        save_messages(messages)
        # Always return JSON for form submissions since frontend expects it
        return jsonify({"status": "success"})
    # If not a form submission, or if validation failed for a GET request, render the page
    # For POST requests where validation fails, we should also return JSON
    if request.method == 'POST':
        errors = form.errors
        return jsonify({"status": "failure", "errors": errors}), 400 # Bad request
    
    return render_template('chat.html', form=form)

@app.route('/api/messages')
@login_required
def api_messages():
    messages = load_messages()
    return jsonify(messages)

@app.route('/documents')
@login_required
@roles_required('USER')
def documents():
    user_docs = load_documents().get(current_user.id, [])
    return render_template('documents.html', documents=user_docs)

@app.route('/download/<filename>')
@login_required
def download(filename):
    user_docs = load_documents().get(current_user.id, [])
    if any(doc['filename'] == filename for doc in user_docs):
        return send_from_directory(app.config['SECURE_DOCUMENT_FOLDER'], filename, as_attachment=True)
    abort(403)


# --- Chef Routes ---
@app.route('/dashboard-chef')
@login_required
@roles_required('CHEF')
def dashboard_chef():
    return render_template('dashboardchef.html')

@app.route('/employees')
@login_required
@roles_required('CHEF')
def employees():
    all_users = load_users()
    presence_data = load_presence()
    employee_list = []
    
    for username, data in all_users.items():
        if "CHEF" in data['roles']:
            continue
            
        last_seen_str = presence_data.get(username)
        status = "Absent"
        if last_seen_str:
            last_seen_date = datetime.datetime.strptime(last_seen_str, "%Y-%m-%d %H:%M:%S").date()
            if last_seen_date == datetime.date.today():
                status = "Present"
        
        employee_list.append({"username": username, "status": status, "last_seen": last_seen_str or "Never"})

    return render_template('employees.html', employees=employee_list)

@app.route('/task-management', methods=['GET', 'POST'])
@login_required
@roles_required('CHEF')
def task_management():
    form = TaskAssignmentForm()
    all_users = load_users()
    form.employee.choices = [(user, user) for user, data in all_users.items() if 'CHEF' not in data['roles']]

    if form.validate_on_submit():
        all_tasks = load_tasks()
        user_to_assign = form.employee.data
        
        if user_to_assign not in all_tasks:
            all_tasks[user_to_assign] = []
            
        max_id = max([task['id'] for user_tasks in all_tasks.values() for task in user_tasks] or [0])
        
        new_task = { "id": max_id + 1, "description": form.description.data, "status": "Pending", "proof_file": None }
        
        all_tasks[user_to_assign].append(new_task)
        save_tasks(all_tasks)
        flash(f'Task "{new_task["description"]}" assigned to {user_to_assign} successfully!', 'success')
        return redirect(url_for('task_management'))

    all_tasks = load_tasks()
    return render_template('task-management.html', form=form, all_tasks=all_tasks)


@app.route('/access-control', methods=['GET', 'POST'])
@login_required
@roles_required('CHEF')
def access_control():
    form = AccessControlForm()
    all_users = load_users()
    form.employee.choices = [(user, user) for user, data in all_users.items() if 'CHEF' not in data['roles']]
    
    if form.validate_on_submit():
        user_to_update = form.employee.data
        new_department = form.department.data
        
        if user_to_update in all_users:
            all_users[user_to_update]['department'] = new_department
            save_users(all_users)
            flash(f"Successfully updated {user_to_update}'s department to {new_department}.", 'success')
        else:
            flash(f"User {user_to_update} not found.", 'danger')
        return redirect(url_for('access_control'))

    return render_template('access-control.html', form=form, users=all_users)

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)