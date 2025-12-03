"""
Chef/Admin database management routes.
Allows authorized chefs to view, manage, and delete employee data.
"""

from flask import Blueprint, render_template, request, jsonify, redirect, url_for, flash
from flask_login import login_required, current_user
from app import db
from app.models import User, Employee, Role
from functools import wraps
from datetime import datetime

admin_bp = Blueprint('admin', __name__, url_prefix='/admin')

def chef_required(f):
    """Decorator to check if user is a chef/admin"""
    @wraps(f)
    @login_required
    def decorated_function(*args, **kwargs):
        if not current_user.has_role('Chef') and not current_user.has_role('Admin'):
            flash('Access denied. Chef/Admin privileges required.', 'danger')
            return redirect(url_for('main.index'))
        return f(*args, **kwargs)
    return decorated_function

@admin_bp.route('/dashboard')
@chef_required
def dashboard():
    """Main admin/chef dashboard"""
    total_employees = db.session.query(Employee).count()
    active_employees = db.session.query(Employee).filter_by(status='Active').count()
    absent_employees = db.session.query(Employee).filter_by(status='Absent').count()
    
    # Recent activity
    recent_changes = db.session.query(Employee).order_by(Employee.updated_at.desc()).limit(10).all()
    
    stats = {
        'total': total_employees,
        'active': active_employees,
        'absent': absent_employees,
        'recent_changes': recent_changes
    }
    
    return render_template('admin/dashboard.html', stats=stats)

@admin_bp.route('/employees', methods=['GET'])
@chef_required
def list_employees():
    """List all employees with filter options"""
    page = request.args.get('page', 1, type=int)
    status_filter = request.args.get('status', None)
    role_filter = request.args.get('role', None)
    
    query = db.session.query(Employee)
    
    if status_filter:
        query = query.filter_by(status=status_filter)
    if role_filter:
        query = query.filter_by(role=role_filter)
    
    employees = query.paginate(page=page, per_page=20)
    
    return render_template('admin/employees.html', employees=employees, status_filter=status_filter, role_filter=role_filter)

@admin_bp.route('/employee/<int:emp_id>', methods=['GET', 'POST'])
@chef_required
def edit_employee(emp_id):
    """Edit employee information"""
    employee = db.session.get(Employee, emp_id)
    
    if not employee:
        return jsonify({'error': 'Employee not found'}), 404
    
    if request.method == 'POST':
        data = request.get_json() if request.is_json else request.form
        
        # Update allowed fields
        if 'name' in data:
            employee.name = data['name']
        if 'status' in data:
            employee.status = data['status']
        if 'role' in data:
            employee.role = data['role']
        if 'contact_info' in data:
            employee.contact_info = data['contact_info']
        
        employee.updated_at = datetime.utcnow()
        db.session.commit()
        
        if request.is_json:
            return jsonify({'success': True, 'employee': employee.to_dict()})
        else:
            flash('Employee updated successfully.', 'success')
            return redirect(url_for('admin.list_employees'))
    
    if request.is_json:
        return jsonify(employee.to_dict())
    
    return render_template('admin/edit_employee.html', employee=employee)

@admin_bp.route('/employee/<int:emp_id>/delete', methods=['POST'])
@chef_required
def delete_employee(emp_id):
    """Delete an employee record (Chef authorization required)"""
    employee = db.session.get(Employee, emp_id)
    
    if not employee:
        if request.is_json:
            return jsonify({'error': 'Employee not found'}), 404
        flash('Employee not found.', 'danger')
        return redirect(url_for('admin.list_employees'))
    
    try:
        # Log deletion for audit
        employee_name = employee.name
        db.session.delete(employee)
        db.session.commit()
        
        if request.is_json:
            return jsonify({'success': True, 'message': f'Employee {employee_name} deleted successfully'})
        else:
            flash(f'Employee {employee_name} deleted successfully.', 'success')
    except Exception as e:
        db.session.rollback()
        if request.is_json:
            return jsonify({'error': str(e)}), 500
        flash(f'Error deleting employee: {str(e)}', 'danger')
    
    return redirect(url_for('admin.list_employees'))

@admin_bp.route('/database/stats', methods=['GET'])
@chef_required
def database_stats():
    """Get database statistics (JSON API)"""
    stats = {
        'total_employees': db.session.query(Employee).count(),
        'active_employees': db.session.query(Employee).filter_by(status='Active').count(),
        'absent_employees': db.session.query(Employee).filter_by(status='Absent').count(),
        'total_users': db.session.query(User).count(),
        'database_type': 'PostgreSQL' if 'postgresql' in db.engine.url.drivername else 'SQLite',
        'timestamp': datetime.utcnow().isoformat()
    }
    return jsonify(stats)

@admin_bp.route('/database/export', methods=['GET'])
@chef_required
def export_data():
    """Export employee data as JSON"""
    employees = db.session.query(Employee).all()
    data = [emp.to_dict() for emp in employees]
    
    return jsonify({
        'total': len(data),
        'timestamp': datetime.utcnow().isoformat(),
        'employees': data
    })

@admin_bp.route('/bulk-delete', methods=['POST'])
@chef_required
def bulk_delete():
    """Delete multiple employees (chef authorization)"""
    data = request.get_json()
    emp_ids = data.get('employee_ids', [])
    
    if not emp_ids:
        return jsonify({'error': 'No employees specified'}), 400
    
    try:
        deleted_count = db.session.query(Employee).filter(Employee.id.in_(emp_ids)).delete()
        db.session.commit()
        
        return jsonify({
            'success': True,
            'deleted_count': deleted_count,
            'message': f'{deleted_count} employees deleted successfully'
        })
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500
