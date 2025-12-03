from flask import Flask, render_template, redirect, url_for, jsonify
from config import Config
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_login import LoginManager
import os
import logging
from logging.handlers import RotatingFileHandler

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

    # Import models
    from app.models import User, Task, Presence, Role, Employee

    # Create upload folders and initialize database
    with app.app_context():
        if not os.path.exists(app.config['UPLOAD_FOLDER']):
            os.makedirs(app.config['UPLOAD_FOLDER'])
        if not os.path.exists(app.config['SECURE_DOCUMENT_FOLDER']):
            os.makedirs(app.config['SECURE_DOCUMENT_FOLDER'])
        
        # Initialize database tables
        db.create_all()
        
        # --- FIX: Auto-create Roles and Admin User ---
        create_initial_data(db, bcrypt)
        # ---------------------------------------------

    # Register Blueprints
    from app.auth.routes import auth as auth_blueprint
    app.register_blueprint(auth_blueprint, url_prefix='/auth')

    from app.user.routes import user as user_blueprint
    app.register_blueprint(user_blueprint)

    from app.chef.routes import chef as chef_blueprint
    app.register_blueprint(chef_blueprint)

    from app.admin.routes import admin as admin_blueprint
    app.register_blueprint(admin_blueprint)

    @app.route('/')
    def index():
        from flask import session as flask_session, render_template
        from app.forms import LoginForm
        from flask_login import current_user

        if not flask_session.get('_user_id'):
            form = LoginForm()
            return render_template('login.html', form=form)

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

# --- Helper Function to Create Initial Data ---
def create_initial_data(db, bcrypt):
    from app.models import Role, User
    try:
        # 1. Create Roles
        roles = ['ADMIN', 'CHEF', 'USER']
        for role_name in roles:
            role = Role.query.filter_by(name=role_name).first()
            if not role:
                role = Role(name=role_name)
                db.session.add(role)
        
        db.session.commit()

        # 2. Create Admin User (if missing)
        admin_role = Role.query.filter_by(name='ADMIN').first()
        admin_user = User.query.filter_by(username='admin').first()
        
        if not admin_user:
            hashed_pw = bcrypt.generate_password_hash('admin').decode('utf-8')
            admin_user = User(username='admin', password=hashed_pw)
            admin_user.roles.append(admin_role)
            db.session.add(admin_user)
            db.session.commit()
            print(">>> Auto-created 'admin' user with password 'admin'")
            
    except Exception as e:
        print(f">>> Error creating initial data: {e}")
        db.session.rollback()
