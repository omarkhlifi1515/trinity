"""
Application entry point for Gunicorn.
Uses the modular app factory pattern from app/ package.
"""
import sys
import os

# Add the backend directory to the Python path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app import create_app

# Create and export the Flask app instance for Gunicorn
app = create_app()

