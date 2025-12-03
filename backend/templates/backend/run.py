from app import create_app, db
from app.models import User, Role

app = create_app()

@app.shell_context_processor
def make_shell_context():
    return {'db': db, 'User': User, 'Role': Role}

if __name__ == '__main__':
    with app.app_context():
        # Database tables are created automatically by app/__init__.py
        # Run init_db.py for full initialization with default data
        pass
    app.run(host='0.0.0.0', debug=True)
