from app import create_app, db, bcrypt
from app.models import User, Role

app = create_app()

with app.app_context():
    # 1. Create the ADMIN role if it doesn't exist
    admin_role = Role.query.filter_by(name='ADMIN').first()
    if not admin_role:
        admin_role = Role(name='ADMIN')
        db.session.add(admin_role)
        print("Created ADMIN role.")

    # 2. Create the CHEF role if it doesn't exist (just in case)
    chef_role = Role.query.filter_by(name='CHEF').first()
    if not chef_role:
        chef_role = Role(name='CHEF')
        db.session.add(chef_role)
        print("Created CHEF role.")

    # 3. Create the Admin user
    user = User.query.filter_by(username='admin').first()
    if not user:
        hashed_password = bcrypt.generate_password_hash('admin').decode('utf-8')
        user = User(username='admin', password=hashed_password)
        user.roles.append(admin_role)
        db.session.add(user)
        print("Created user 'admin' with password 'admin'.")
    else:
        # Ensure existing admin user has the role
        if admin_role not in user.roles:
            user.roles.append(admin_role)
            print("Added ADMIN role to existing 'admin' user.")
        
        # Reset password to 'admin' to be sure
        user.password = bcrypt.generate_password_hash('admin').decode('utf-8')
        print("Reset password for 'admin' to 'admin'.")

    db.session.commit()
    print("Done! You can now log in as 'admin'.")