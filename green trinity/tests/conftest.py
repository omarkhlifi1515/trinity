import pytest
from app import create_app, db
from config import Config

class TestConfig(Config):
    TESTING = True
    SQLALCHEMY_DATABASE_URI = 'sqlite://'
    WTF_CSRF_ENABLED = False # Disable CSRF for testing

@pytest.fixture
def client():
    app = create_app(TestConfig)
    with app.test_client() as client:
        with app.app_context():
            from app.models import User, Role # Import here
            db.create_all()
            # Create user roles
            if Role.query.filter_by(name='CHEF').first() is None:
                db.session.add(Role(name='CHEF'))
            if Role.query.filter_by(name='USER').first() is None:
                db.session.add(Role(name='USER'))
            db.session.commit()
            # Create a default admin user
            if User.query.filter_by(username='chef').first() is None:
                from app import bcrypt
                chef_role = Role.query.filter_by(name='CHEF').first()
                user_role = Role.query.filter_by(name='USER').first()
                hashed_password = bcrypt.generate_password_hash('chef').decode('utf-8')
                chef = User(username='chef', password=hashed_password, department='Management')
                chef.roles.append(chef_role)
                chef.roles.append(user_role)
                db.session.add(chef)
                db.session.commit()
        yield client
        with app.app_context():
            db.drop_all()
