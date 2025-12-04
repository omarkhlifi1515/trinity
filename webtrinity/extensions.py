"""Flask extensions (database, etc.) initialized here to avoid circular imports."""
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()
