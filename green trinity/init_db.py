#!/usr/bin/env python3
"""
Initialize HR Portal Database
Creates the database file in Google Drive location and sets up all tables
"""

from app import create_app, db
from app.models import User, Role, Employee
from config import Config
import os

def init_database():
    """Initialize the database with tables and default data"""
    app = create_app()
    
    with app.app_context():
        # Create all tables
        print("Creating database tables...")
        db.create_all()
        print(f"✓ Database created at: {Config.DATABASE_PATH}")
        
        # Create default roles
        print("Creating default roles...")
        chef_role = Role.query.filter_by(name='CHEF').first()
        user_role = Role.query.filter_by(name='USER').first()
        
        if not chef_role:
            chef_role = Role(name='CHEF')
            db.session.add(chef_role)
            print("✓ Created CHEF role")
        
        if not user_role:
            user_role = Role(name='USER')
            db.session.add(user_role)
            print("✓ Created USER role")
        
        db.session.commit()
        
        # Create default admin user (Chef)
        print("Creating default admin user...")
        from app import bcrypt
        admin_user = User.query.filter_by(username='chef').first()
        if not admin_user:
            hashed_password = bcrypt.generate_password_hash('chef').decode('utf-8')
            admin_user = User(
                username='chef',
                password=hashed_password,
                department='Management'
            )
            admin_user.roles.append(chef_role)
            admin_user.roles.append(user_role)
            db.session.add(admin_user)
            db.session.commit()
            print("✓ Created default admin user: chef / chef")
        
        # Create default employee record for admin
        admin_employee = Employee.query.filter_by(name='Admin Chef').first()
        if not admin_employee:
            admin_employee = Employee(
                name='Admin Chef',
                role='Chef',
                status='Active',
                contact_info='admin@company.com',
                user_id=admin_user.id
            )
            db.session.add(admin_employee)
            db.session.commit()
            print("✓ Created default employee record for admin")
        
        print("\n" + "="*50)
        print("Database initialization complete!")
        print(f"Database location: {Config.DATABASE_PATH}")
        print("="*50)
        print("\nDefault login credentials:")
        print("  Username: chef")
        print("  Password: chef")
        print("\nYou can now start the application!")

if __name__ == '__main__':
    init_database()

