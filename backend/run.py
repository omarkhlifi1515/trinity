from app import create_app, db
from app.models import User, Role

app = create_app()

@app.shell_context_processor
def make_shell_context():
    return {'db': db, 'User': User, 'Role': Role}

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
        # Create user roles
        if Role.query.filter_by(name='CHEF').first() is None:
            db.session.add(Role(name='CHEF'))
        if Role.query.filter_by(name='USER').first() is None:
            db.session.add(Role(name='USER'))
        db.session.commit()

        # Create a default admin user
        if User.query.filter_by(username='chef').first() is None:
            from app.models import Role
            from app import bcrypt
            chef_role = Role.query.filter_by(name='CHEF').first()
            user_role = Role.query.filter_by(name='USER').first()
            hashed_password = bcrypt.generate_password_hash('chef').decode('utf-8')
            chef = User(username='chef', password=hashed_password, department='Management')
            chef.roles.append(chef_role)
            chef.roles.append(user_role)
            db.session.add(chef)
            db.session.commit()
    app.run(host='0.0.0.0', debug=True)
