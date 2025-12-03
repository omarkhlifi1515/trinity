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

    # Import models BEFORE creating tables
    from app.models import User, Task, Presence, Role, Employee

    # Create upload folders and initialize database
    with app.app_context():
        if not os.path.exists(app.config['UPLOAD_FOLDER']):
            os.makedirs(app.config['UPLOAD_FOLDER'])
        if not os.path.exists(app.config['SECURE_DOCUMENT_FOLDER']):
            os.makedirs(app.config['SECURE_DOCUMENT_FOLDER'])
        
        # Initialize database tables (models must be imported first)
        db.create_all()

    from app.auth.routes import auth as auth_blueprint
    app.register_blueprint(auth_blueprint, url_prefix='/auth')

    from app.user.routes import user as user_blueprint
    app.register_blueprint(user_blueprint)

    from app.chef.routes import chef as chef_blueprint
    app.register_blueprint(chef_blueprint)

    @app.route('/')
    def index():
        # For anonymous visitors render the login page as the main page
        # without triggering a DB lookup via the user_loader. If a
        # session _user_id exists we fall through and let Flask-Login
        # resolve the user and redirect to the appropriate dashboard.
        from flask import session as flask_session, render_template
        from app.forms import LoginForm
        from flask_login import current_user

        # If no user is stored in the session, show the login form
        if not flask_session.get('_user_id'):
            form = LoginForm()
            return render_template('login.html', form=form)

        # If a session _user_id exists, check the authenticated user and route
        if current_user.is_authenticated:
            if current_user.has_role('CHEF'):
                return redirect(url_for('chef.dashboard_chef'))
            return redirect(url_for('user.dashboard_user'))

        # Fallback: render login form
        return render_template('login.html', form=LoginForm())

    @app.route('/health', methods=['GET'])
    def health():
        """Health check endpoint for monitoring (Render, K8s, etc.)"""
        try:
            # Quick DB check
            db.session.execute(db.text('SELECT 1'))
            return jsonify({'status': 'healthy', 'database': 'connected'}), 200
        except Exception as e:
            return jsonify({'status': 'unhealthy', 'error': str(e)}), 503

    # Error Handlers
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