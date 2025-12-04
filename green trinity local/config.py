import os

basedir = os.path.abspath(os.path.dirname(__file__))

class Config:
    # 1. Basic Security Key (Safe for local use)
    SECRET_KEY = 'local-dev-secret-key-change-if-public'

    # 2. Database:
    #    - If POSTGRES_URL / POSTGRES_SYNC_URL / DATABASE_URL is set, use that Postgres DB directly
    #    - Otherwise, fall back to a local SQLite file 'local_data.db'
    POSTGRES_URL = (
        os.environ.get('POSTGRES_URL')
        or os.environ.get('POSTGRES_SYNC_URL')
        or os.environ.get('DATABASE_URL')
    )

    if POSTGRES_URL:
        print("[CONFIG] Using remote Postgres database for local app.")
        SQLALCHEMY_DATABASE_URI = POSTGRES_URL
    else:
        print("[CONFIG] Using local SQLite database 'local_data.db'.")
        SQLALCHEMY_DATABASE_URI = 'sqlite:///' + os.path.join(basedir, 'local_data.db')

    SQLALCHEMY_TRACK_MODIFICATIONS = False

    # 3. File Upload Paths
    UPLOAD_FOLDER = os.path.join(basedir, 'uploads')
    SECURE_DOCUMENT_FOLDER = os.path.join(basedir, 'secure_documents')
