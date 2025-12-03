from flask import render_template, redirect, url_for, flash, request, jsonify
from flask_login import login_required
from app import db
from app.chef import chef
from app.models import User, Task, Presence, Role, Employee
from app.forms import TaskAssignmentForm, AccessControlForm, EmployeeForm
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

# --- HR Portal Employee Management Routes ---

@chef.route('/hr/employees', methods=['GET', 'POST'])
@login_required
@roles_required('CHEF')
def hr_employees():
    """HR Portal - View and manage all employees"""
    form = EmployeeForm()
    
    if form.validate_on_submit():
        # Add new employee
        new_employee = Employee(
            name=form.name.data,
            role=form.role.data,
            status=form.status.data,
            contact_info=form.contact_info.data or None
        )
        db.session.add(new_employee)
        db.session.commit()
        flash(f'Employee "{new_employee.name}" added successfully!', 'success')
        return redirect(url_for('chef.hr_employees'))
    
    # Get all employees
    employees = Employee.query.order_by(Employee.name).all()
    return render_template('hr_employees.html', form=form, employees=employees)

@chef.route('/hr/employee/add', methods=['GET', 'POST'])
@login_required
@roles_required('CHEF')
def hr_add_employee():
    """Add a new employee"""
    form = EmployeeForm()
    
    if form.validate_on_submit():
        employee = Employee(
            name=form.name.data,
            role=form.role.data,
            status=form.status.data,
            contact_info=form.contact_info.data or None
        )
        db.session.add(employee)
        db.session.commit()
        flash(f'Employee "{employee.name}" added successfully!', 'success')
        return redirect(url_for('chef.hr_employees'))
    
    return render_template('hr_employee_form.html', form=form, title='Add Employee')

@chef.route('/hr/employee/<int:employee_id>/edit', methods=['GET', 'POST'])
@login_required
@roles_required('CHEF')
def hr_edit_employee(employee_id):
    """Edit an existing employee"""
    employee = Employee.query.get_or_404(employee_id)
    form = EmployeeForm(obj=employee)
    
    if form.validate_on_submit():
        employee.name = form.name.data
        employee.role = form.role.data
        employee.status = form.status.data
        employee.contact_info = form.contact_info.data or None
        employee.updated_at = datetime.utcnow()
        db.session.commit()
        flash(f'Employee "{employee.name}" updated successfully!', 'success')
        return redirect(url_for('chef.hr_employees'))
    
    return render_template('hr_employee_form.html', form=form, employee=employee, title='Edit Employee')

@chef.route('/hr/employee/<int:employee_id>/delete', methods=['POST'])
@login_required
@roles_required('CHEF')
def hr_delete_employee(employee_id):
    """Delete an employee"""
    employee = Employee.query.get_or_404(employee_id)
    name = employee.name
    db.session.delete(employee)
    db.session.commit()
    flash(f'Employee "{name}" deleted successfully!', 'success')
    return redirect(url_for('chef.hr_employees'))

@chef.route('/hr/employee/<int:employee_id>/toggle-status', methods=['POST'])
@login_required
@roles_required('CHEF')
def hr_toggle_employee_status(employee_id):
    """Toggle employee status between Active and Absent"""
    employee = Employee.query.get_or_404(employee_id)
    employee.status = 'Absent' if employee.status == 'Active' else 'Active'
    employee.updated_at = datetime.utcnow()
    db.session.commit()
    return jsonify({
        'success': True,
        'status': employee.status,
        'message': f'Employee status updated to {employee.status}'
    })

@chef.route('/hr/dashboard')
@login_required
@roles_required('CHEF')
def hr_dashboard():
    """HR Portal Dashboard with statistics"""
    total_employees = Employee.query.count()
    active_employees = Employee.query.filter_by(status='Active').count()
    absent_employees = Employee.query.filter_by(status='Absent').count()
    chefs_count = Employee.query.filter_by(role='Chef').count()
    employees_count = Employee.query.filter_by(role='Employee').count()
    
    # Recent employees
    recent_employees = Employee.query.order_by(Employee.created_at.desc()).limit(5).all()
    
    stats = {
        'total': total_employees,
        'active': active_employees,
        'absent': absent_employees,
        'chefs': chefs_count,
        'employees': employees_count
    }
    
    return render_template('hr_dashboard.html', stats=stats, recent_employees=recent_employees)
