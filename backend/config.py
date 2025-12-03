import os

basedir = os.path.abspath(os.path.dirname(__file__))

class Config:
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'you-will-never-guess'
    
    # Google Drive Database Path Configuration
    # Set this to your Google Drive folder path where you want the database to sync
    # Example: C:/Users/YourName/Google Drive/Project/hr_data.db
    # Or use environment variable: GOOGLE_DRIVE_DB_PATH
    GOOGLE_DRIVE_DB_PATH = os.environ.get('GOOGLE_DRIVE_DB_PATH') or \
        r'C:/Users/msi/Google Drive/Project/hr_data.db'
    
    # Ensure the directory exists
    if GOOGLE_DRIVE_DB_PATH:
        db_dir = os.path.dirname(GOOGLE_DRIVE_DB_PATH)
        if db_dir and not os.path.exists(db_dir):
            try:
                os.makedirs(db_dir, exist_ok=True)
            except Exception:
                # If Google Drive path doesn't exist, fall back to local
                GOOGLE_DRIVE_DB_PATH = os.path.join(basedir, 'hr_data.db')
    
    # Use Google Drive path if set, otherwise use local database
    DATABASE_PATH = GOOGLE_DRIVE_DB_PATH if GOOGLE_DRIVE_DB_PATH else os.path.join(basedir, 'hr_data.db')
    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or \
        'sqlite:///' + DATABASE_PATH.replace('\\', '/')
    
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    UPLOAD_FOLDER = 'uploads'
    SECURE_DOCUMENT_FOLDER = 'secure_documents'
