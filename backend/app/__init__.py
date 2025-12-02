from flask import Flask, render_template, redirect, url_for
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

    # Create upload folders if they don't exist
    with app.app_context():
        if not os.path.exists(app.config['UPLOAD_FOLDER']):
            os.makedirs(app.config['UPLOAD_FOLDER'])
        if not os.path.exists(app.config['SECURE_DOCUMENT_FOLDER']):
            os.makedirs(app.config['SECURE_DOCUMENT_FOLDER'])

    from app.auth.routes import auth as auth_blueprint
    app.register_blueprint(auth_blueprint, url_prefix='/auth')

    from app.user.routes import user as user_blueprint
    app.register_blueprint(user_blueprint)

    from app.chef.routes import chef as chef_blueprint
    app.register_blueprint(chef_blueprint)

    @app.route('/')
    def index():
        from flask_login import current_user
        if current_user.is_authenticated:
            from app.models import User
            if current_user.has_role('CHEF'):
                return redirect(url_for('chef.dashboard_chef'))
            return redirect(url_for('user.dashboard_user'))
        return redirect(url_for('auth.login'))

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