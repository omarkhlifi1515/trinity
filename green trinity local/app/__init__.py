from flask import Flask, render_template, redirect, url_for, jsonify, request
from config import Config
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_login import LoginManager
import os
import logging
from logging.handlers import RotatingFileHandler
from sqlalchemy import MetaData, Table, create_engine
from datetime import datetime
import time

db = SQLAlchemy()
bcrypt = Bcrypt()
login_manager = LoginManager()
login_manager.login_view = 'auth.login'
login_manager.unauthorized_handler(lambda: redirect(url_for('auth.login')))

def create_app(config_class=Config):
    app = Flask(__name__)
    app.config.from_object(config_class)

    db.init_app(app)
    bcrypt.init_app(app)
    login_manager.init_app(app)

    # Import models BEFORE creating tables
    from app.models import User, Task, Presence, Role, Employee

    # Create upload folders and initialize database
    with app.app_context():
        if not os.path.exists(app.config['UPLOAD_FOLDER']):
            os.makedirs(app.config['UPLOAD_FOLDER'])
        if not os.path.exists(app.config['SECURE_DOCUMENT_FOLDER']):
            os.makedirs(app.config['SECURE_DOCUMENT_FOLDER'])
        
        # Initialize database tables
        db.create_all()

        # Ensure Roles and Admin exist (Fix for Render deployments)
        create_initial_data(db, bcrypt)

    # Register Blueprints
    from app.auth.routes import auth as auth_blueprint
    app.register_blueprint(auth_blueprint, url_prefix='/auth')

    from app.user.routes import user as user_blueprint
    app.register_blueprint(user_blueprint)

    from app.chef.routes import chef as chef_blueprint
    app.register_blueprint(chef_blueprint)

    from app.admin.routes import admin as admin_blueprint
    app.register_blueprint(admin_blueprint)

    # Setup access logging for Blue Trinity SOC monitoring
    # Use the Flask app root path so it works regardless of where we run from.
    access_log_path = os.path.join(app.root_path, 'access.log')
    
    @app.before_request
    def log_request_info():
        """Log request info for SOC monitoring"""
        pass  # We'll log after response
    
    @app.after_request
    def log_access(response):
        """Log HTTP access in Apache/Nginx common log format for Blue Trinity"""
        try:
            # Get client IP (handle proxies)
            client_ip = request.environ.get('HTTP_X_FORWARDED_FOR', request.environ.get('REMOTE_ADDR', '127.0.0.1'))
            if ',' in client_ip:
                client_ip = client_ip.split(',')[0].strip()
            
            # Format timestamp like Apache: [04/Dec/2025:16:30:45 +0000]
            timestamp = datetime.utcnow().strftime('[%d/%b/%Y:%H:%M:%S +0000]')
            
            # Request line: "METHOD /path HTTP/1.1"
            request_line = f'"{request.method} {request.full_path.rstrip("?")} HTTP/1.1"'
            
            # Status code and response size
            status = response.status_code
            size = response.content_length if response.content_length else 0
            
            # User agent
            user_agent = request.headers.get('User-Agent', '-')
            
            # Common Log Format: IP - - [timestamp] "request" status size "referer" "user-agent"
            log_line = f'{client_ip} - - {timestamp} {request_line} {status} {size} "-" "{user_agent}"\n'
            
            # Append to access.log
            with open(access_log_path, 'a', encoding='utf-8') as f:
                f.write(log_line)
        except Exception as e:
            # Don't break the app if logging fails
            app.logger.error(f"Access logging error: {e}")
        
        return response

    @app.route('/')
    def index():
        from flask import session as flask_session, render_template
        from app.forms import LoginForm
        from flask_login import current_user

        # If not logged in, show login form
        if not flask_session.get('_user_id'):
            form = LoginForm()
            return render_template('login.html', form=form)

        # If logged in, redirect based on role
        if current_user.is_authenticated:
            if current_user.has_role('ADMIN'):
                return redirect(url_for('admin.dashboard_admin'))
            if current_user.has_role('CHEF'):
                return redirect(url_for('chef.dashboard_chef'))
            return redirect(url_for('user.dashboard_user'))

        return render_template('login.html', form=LoginForm())

    @app.route('/health', methods=['GET'])
    def health():
        try:
            db.session.execute(db.text('SELECT 1'))
            return jsonify({'status': 'healthy', 'database': 'connected'}), 200
        except Exception as e:
            return jsonify({'status': 'unhealthy', 'error': str(e)}), 503

    @app.errorhandler(404)
    def not_found_error(error):
        return render_template('404.html'), 404

    @app.errorhandler(500)
    def internal_error(error):
        db.session.rollback()
        return render_template('500.html'), 500

    if not app.debug:
        if not os.path.exists('logs'):
            os.mkdir('logs')
        file_handler = RotatingFileHandler('logs/trnity.log', maxBytes=10240, backupCount=10)
        file_handler.setFormatter(logging.Formatter(
            '%(asctime)s %(levelname)s: %(message)s [in %(pathname)s:%(lineno)d]'))
        file_handler.setLevel(logging.INFO)
        app.logger.addHandler(file_handler)

        app.logger.setLevel(logging.INFO)
        app.logger.info('Trnity startup')

    return app

def create_initial_data(db, bcrypt):
    from app.models import Role, User
    try:
        roles = ['ADMIN', 'CHEF', 'USER']
        for role_name in roles:
            role = Role.query.filter_by(name=role_name).first()
            if not role:
                role = Role(name=role_name)
                db.session.add(role)
        db.session.commit()

        admin_user = User.query.filter_by(username='admin').first()
        admin_role = Role.query.filter_by(name='ADMIN').first()
        
        if not admin_user:
            hashed_pw = bcrypt.generate_password_hash('admin').decode('utf-8')
            admin_user = User(username='admin', password=hashed_pw)
            admin_user.roles.append(admin_role)
            db.session.add(admin_user)
            db.session.commit()
    except Exception as e:
        print(f"Initial data setup error: {e}")
        db.session.rollback()
