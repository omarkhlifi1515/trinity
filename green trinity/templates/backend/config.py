import os
from urllib.parse import urlparse

basedir = os.path.abspath(os.path.dirname(__file__))

class Config:
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'you-will-never-guess'
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    UPLOAD_FOLDER = 'uploads'
    SECURE_DOCUMENT_FOLDER = 'secure_documents'
    
    # Database Configuration
    # Priority: DATABASE_URL env var (Render/production) > local Google Drive SQLite > local SQLite
    database_url = os.environ.get('DATABASE_URL')
    
    if database_url:
        # Running in cloud (Render with Postgres)
        # Use sslmode=prefer for better compatibility with Render's managed Postgres
        if '?sslmode=' in database_url:
            SQLALCHEMY_DATABASE_URI = database_url
        elif '?' in database_url:
            SQLALCHEMY_DATABASE_URI = database_url + '&sslmode=prefer'
        else:
            SQLALCHEMY_DATABASE_URI = database_url + '?sslmode=prefer'
        
        # Connection pool settings for Render Postgres
        SQLALCHEMY_ENGINE_OPTIONS = {
            'pool_size': 5,
            'pool_recycle': 3600,
            'pool_pre_ping': True,
            'connect_args': {
                'connect_timeout': 10,
                'keepalives': 1,
                'keepalives_idle': 30
            }
        }
    else:
        # Local development: try Google Drive path, fallback to local
        GOOGLE_DRIVE_DB_PATH = os.environ.get('GOOGLE_DRIVE_DB_PATH') or \
            r'C:/Users/msi/Google Drive/Project/hr_data.db'
        
        if GOOGLE_DRIVE_DB_PATH:
            db_dir = os.path.dirname(GOOGLE_DRIVE_DB_PATH)
            if db_dir and not os.path.exists(db_dir):
                try:
                    os.makedirs(db_dir, exist_ok=True)
                except Exception:
                    GOOGLE_DRIVE_DB_PATH = os.path.join(basedir, 'hr_data.db')
        
        DATABASE_PATH = GOOGLE_DRIVE_DB_PATH if GOOGLE_DRIVE_DB_PATH else os.path.join(basedir, 'hr_data.db')
        SQLALCHEMY_DATABASE_URI = 'sqlite:///' + DATABASE_PATH.replace('\\', '/')
        
        # SQLite doesn't need connection pooling options
        SQLALCHEMY_ENGINE_OPTIONS = {}

