from flask import render_template, redirect, url_for, flash, abort, send_from_directory, jsonify, request
from flask_login import login_required, current_user
from app import db
from app.user import user
from app.models import Task, Message, Document, Presence
from app.forms import PresenceForm, TaskProofForm, ChatMessageForm
from werkzeug.utils import secure_filename
import os
from functools import wraps
from datetime import datetime

def roles_required(*required_roles):
    def wrapper(fn):
        @wraps(fn)
        def decorated_view(*args, **kwargs):
            if not current_user.is_authenticated:
                return abort(401)
            if not all(current_user.has_role(role) for role in required_roles):
                abort(403)
            return fn(*args, **kwargs)
        return decorated_view
    return wrapper

@user.route('/dashboard-user', methods=['GET', 'POST'])
@login_required
@roles_required('USER')
def dashboard_user():
    form = PresenceForm()
    if form.validate_on_submit():
        presence = Presence(user_id=current_user.id)
        db.session.add(presence)
        db.session.commit()
        flash('You have been marked present for today!', 'success')
        return redirect(url_for('user.dashboard_user'))
    
    today_presence = Presence.query.filter_by(user_id=current_user.id).filter(db.func.date(Presence.timestamp) == datetime.utcnow().date()).first()
    is_present_today = today_presence is not None
            
    return render_template('dashboarduser.html', form=form, is_present_today=is_present_today)

@user.route('/tasks')
@login_required
@roles_required('USER')
def tasks():
    form = TaskProofForm()
    user_tasks = current_user.tasks.all()
    return render_template('tasks.html', tasks=user_tasks, form=form)

@user.route('/upload_proof/<int:task_id>', methods=['POST'])
@login_required
@roles_required('USER')
def upload_proof(task_id):
    form = TaskProofForm()
    if form.validate_on_submit() and form.proof.data:
        task_to_update = Task.query.get_or_404(task_id)
        if task_to_update.employee != current_user:
            abort(403)
        
        file = form.proof.data
        filename = secure_filename(file.filename)
        upload_folder = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), 'uploads')
        os.makedirs(upload_folder, exist_ok=True)
        file.save(os.path.join(upload_folder, filename))
        
        task_to_update.status = 'Completed'
        task_to_update.proof_file = filename
        
        db.session.commit()
        flash('Proof uploaded successfully!', 'success')
    else:
        flash('No file selected or invalid file type.', 'warning')
            
    return redirect(url_for('user.tasks'))

@user.route('/chat', methods=['GET', 'POST'])
@login_required
@roles_required('USER')
def chat():
    form = ChatMessageForm()
    if form.validate_on_submit():
        new_message = Message(text=form.message.data, user_id=current_user.id)
        db.session.add(new_message)
        db.session.commit()
        return jsonify({"status": "success"})
    if request.method == 'POST':
        errors = form.errors
        return jsonify({"status": "failure", "errors": errors}), 400
    
    return render_template('chat.html', form=form)

@user.route('/api/messages')
@login_required
def api_messages():
    messages = Message.query.order_by(Message.timestamp.asc()).all()
    return jsonify([{
        'id': msg.id,
        'user': msg.author.username if msg.author else 'Unknown',
        'user_id': msg.user_id,
        'text': msg.text,
        'timestamp': msg.timestamp.isoformat() if msg.timestamp else datetime.utcnow().isoformat()
    } for msg in messages])

@user.route('/documents')
@login_required
@roles_required('USER')
def documents():
    user_docs = current_user.documents.all()
    return render_template('documents.html', documents=user_docs)

@user.route('/download/<filename>')
@login_required
def download(filename):
    user_docs = current_user.documents.all()
    if any(doc.filename == filename for doc in user_docs):
        secure_folder = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), 'secure_documents')
        return send_from_directory(secure_folder, filename, as_attachment=True)
    abort(403)
