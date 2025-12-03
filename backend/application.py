"""
Application entry point for Gunicorn.
Uses the modular app factory pattern from app/ package.
"""
from app import create_app

# Create and export the Flask app instance for Gunicorn
app = create_app()

