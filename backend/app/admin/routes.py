from flask import Blueprint, render_template, redirect, url_for, flash
from flask_login import login_required, current_user
from app import db
from app.models import User, Role

admin = Blueprint('admin', __name__)

@admin.route('/dashboard-admin')
@login_required
def dashboard_admin():
    # Only allow Admins to see this
    if not current_user.has_role('ADMIN'):
        flash('You are not authorized to view the Admin Dashboard.', 'danger')
        return redirect(url_for('auth.login'))
        
    users = User.query.all()
    return render_template('dashboard_admin.html', users=users)

@admin.route('/promote/chef/<int:user_id>')
@login_required
def promote_chef(user_id):
    if not current_user.has_role('ADMIN'):
        return redirect(url_for('auth.login'))
        
    user = User.query.get_or_404(user_id)
    chef_role = Role.query.filter_by(name='CHEF').first()
    
    if chef_role and chef_role not in user.roles:
        user.roles.append(chef_role)
        db.session.commit()
        flash(f'User {user.username} is now a Chef!', 'success')
        
    return redirect(url_for('admin.dashboard_admin'))

@admin.route('/demote/normal/<int:user_id>')
@login_required
def demote_normal(user_id):
    if not current_user.has_role('ADMIN'):
        return redirect(url_for('auth.login'))

    user = User.query.get_or_404(user_id)
    chef_role = Role.query.filter_by(name='CHEF').first()
    
    if chef_role and chef_role in user.roles:
        user.roles.remove(chef_role)
        db.session.commit()
        flash(f'User {user.username} is now a Normal User.', 'info')

    return redirect(url_for('admin.dashboard_admin'))