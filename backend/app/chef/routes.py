from flask import render_template, redirect, url_for, flash
from flask_login import login_required
from app import db
from app.chef import chef
from app.models import User, Task, Presence, Role
from app.forms import TaskAssignmentForm, AccessControlForm
from app.user.routes import roles_required
from datetime import datetime

@chef.route('/dashboard-chef')
@login_required
@roles_required('CHEF')
def dashboard_chef():
    return render_template('dashboardchef.html')

@chef.route('/employees')
@login_required
@roles_required('CHEF')
def employees():
    employee_list = []
    users = User.query.all()
    for user in users:
        if user.has_role('CHEF'):
            continue
        
        today_presence = Presence.query.filter_by(user_id=user.id).filter(db.func.date(Presence.timestamp) == datetime.utcnow().date()).first()
        status = "Present" if today_presence else "Absent"
        last_seen = user.presence.order_by(Presence.timestamp.desc()).first()
        
        employee_list.append({
            "username": user.username, 
            "status": status, 
            "last_seen": last_seen.timestamp.strftime("%Y-%m-%d %H:%M:%S") if last_seen else "Never",
            "department": user.department or "General"
        })

    return render_template('employees.html', employees=employee_list)

@chef.route('/task-management', methods=['GET', 'POST'])
@login_required
@roles_required('CHEF')
def task_management():
    form = TaskAssignmentForm()
    form.employee.choices = [(user.id, user.username) for user in User.query.all() if not user.has_role('CHEF')]

    if form.validate_on_submit():
        user_to_assign = User.query.get(form.employee.data)
        new_task = Task(description=form.description.data, employee=user_to_assign)
        db.session.add(new_task)
        db.session.commit()
        flash(f'Task "{new_task.description}" assigned to {user_to_assign.username} successfully!', 'success')
        return redirect(url_for('chef.task_management'))

    all_tasks_query = Task.query.all()
    tasks_by_user = {}
    for task in all_tasks_query:
        if task.employee.username not in tasks_by_user:
            tasks_by_user[task.employee.username] = []
        tasks_by_user[task.employee.username].append(task)
    return render_template('task-management.html', form=form, all_tasks=tasks_by_user)


@chef.route('/access-control', methods=['GET', 'POST'])
@login_required
@roles_required('CHEF')
def access_control():
    form = AccessControlForm()
    form.employee.choices = [(user.id, user.username) for user in User.query.all() if not user.has_role('CHEF')]
    
    if form.validate_on_submit():
        user_to_update = User.query.get(form.employee.data)
        user_to_update.department = form.department.data
        db.session.commit()
        flash(f"Successfully updated {user_to_update.username}'s department to {form.department.data}.", 'success')
        return redirect(url_for('chef.access_control'))

    users = User.query.all()
    return render_template('access-control.html', form=form, users=users)
