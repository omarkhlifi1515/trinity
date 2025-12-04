# WebTrinity - Web Application (Starter)

Minimal Flask-based starter for the Trinity Web component. This skeleton implements the basic models and a tiny API to get started.

Quick start

1. Create a Python virtual environment and install dependencies:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
```

2. Run the app:

```powershell
python run.py
```

Files of interest
- `app.py` - Flask application factory and routes
- `models.py` - SQLAlchemy models matching Trinity schema
- `run.py` - simple run script for local development

Render deployment notes
- Add the repository to Render as a Python web service. `webtrinity/Procfile` is provided and runs `gunicorn`.
- Use the `DATABASE_URL` environment variable in Render. A sample is provided in `.env.sample`.

Security note: The repository contains a `.env.sample` showing the database URL placeholder. Do NOT commit secrets to the repository in production.
