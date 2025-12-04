from flask import render_template, redirect, url_for, flash, request
from flask_login import login_user, logout_user, current_user
from app import db, bcrypt
from app.auth import auth
from app.models import User, Role
from app.forms import LoginForm, RegistrationForm
from urllib.parse import urlparse, urljoin


def is_safe_url(target):
    """Ensure the redirect target is on the same host to prevent open redirects."""
    host_url = request.host_url
    ref_url = urlparse(host_url)
    test_url = urlparse(urljoin(host_url, target))
    return test_url.scheme in ('http', 'https') and ref_url.netloc == test_url.netloc

@auth.route('/register', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('user.dashboard_user'))
    form = RegistrationForm()
    if form.validate_on_submit():
        hashed_password = bcrypt.generate_password_hash(form.password.data).decode('utf-8')
        user = User(username=form.username.data, password=hashed_password)
        
        # Default role for new users
        user_role = Role.query.filter_by(name='USER').first()
        if user_role:
            user.roles.append(user_role)
            
        db.session.add(user)
        db.session.commit()
        flash('Your account has been created!', 'success')
        return redirect(url_for('auth.login'))
    return render_template('register.html', title='Register', form=form)

@auth.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        # Redirect if already logged in
        if current_user.has_role('ADMIN'):
            return redirect(url_for('admin.dashboard_admin'))
        if current_user.has_role('CHEF'):
            return redirect(url_for('chef.dashboard_chef'))
        return redirect(url_for('user.dashboard_user'))

    form = LoginForm()
    if form.validate_on_submit():
        user = User.query.filter_by(username=form.username.data).first()
        if user and bcrypt.check_password_hash(user.password, form.password.data):
            login_user(user)
            
            next_page = request.args.get('next')
            if next_page and not is_safe_url(next_page):
                next_page = None

            if next_page:
                return redirect(next_page)

            # --- Redirect based on Role ---
            if user.has_role('ADMIN'):
                return redirect(url_for('admin.dashboard_admin'))
            if user.has_role('CHEF'):
                return redirect(url_for('chef.dashboard_chef'))
            # ------------------------------
            
            return redirect(url_for('user.dashboard_user'))
        else:
            flash('Login Unsuccessful. Please check username and password', 'danger')
    return render_template('login.html', form=form)

@auth.route('/logout')
def logout():
    logout_user()
    return redirect(url_for('auth.login'))