"""
Application entry point for Gunicorn.
This file imports the app from app.py to avoid naming conflicts with the app/ package.
"""
# Import the app instance from app.py explicitly
# This avoids the naming conflict where Python would import the app/ package instead
import importlib.util
import os

# Get the absolute path to app.py
app_py_path = os.path.join(os.path.dirname(__file__), 'app.py')

# Load app.py as a module explicitly
spec = importlib.util.spec_from_file_location("app_module", app_py_path)
app_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(app_module)

# Export the app instance for Gunicorn
app = app_module.app

