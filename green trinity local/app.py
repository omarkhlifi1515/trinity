"""
Legacy single-file Flask app entry point (local-only).

This file is now a thin wrapper around the modular application in the
`app` package so that:
- `python app.py` and `python run.py` behave the same
- All routes (including `chef.hr_dashboard`) come from `app/__init__.py`
"""

from app import create_app

app = create_app()

if __name__ == "__main__":
    # Local development server
    app.run(host="0.0.0.0", debug=True)