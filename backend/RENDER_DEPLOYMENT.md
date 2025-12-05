# Render Deployment Guide

## Project Structure Analysis

Your project has:
- **Location**: `app.py` is in the **root directory** (`backend/`)
- **Flask instance**: `app = Flask(__name__)` in `app.py` (line 14)
- **Alternative**: `run.py` uses blueprint structure with `app = create_app()`

## Solution

### Option 1: Use `app.py` (Current Setup) ✅ RECOMMENDED

**Render Start Command:**
```bash
gunicorn app:app --bind 0.0.0.0:$PORT
```

**Why this works:**
- `app.py` is in the root directory
- Flask instance is named `app`
- Command format: `gunicorn <module>:<variable>`

### Option 2: Use `run.py` (Blueprint Structure)

**Render Start Command:**
```bash
gunicorn run:app --bind 0.0.0.0:$PORT
```

**Why this works:**
- `run.py` imports from `app` package
- Creates app with `app = create_app()`
- Uses modern blueprint structure

## Render Configuration

### Method 1: Using render.yaml (Recommended)

The `render.yaml` file is already created. Render will automatically detect it.

**Settings:**
- **Build Command**: `pip install -r requirements.txt`
- **Start Command**: `gunicorn app:app --bind 0.0.0.0:$PORT`
- **Environment**: Python 3

### Method 2: Manual Configuration in Render Dashboard

1. **Go to Render Dashboard** → Your Service → Settings

2. **Build Command:**
   ```
   pip install -r requirements.txt
   ```

3. **Start Command:**
   ```
   gunicorn app:app --bind 0.0.0.0:$PORT
   ```

4. **Environment Variables:**
   - `SECRET_KEY`: Generate a secure random key
   - `FLASK_ENV`: `production`
   - `PORT`: Automatically set by Render (don't override)

## Troubleshooting

### Error: "Failed to find attribute 'app' in 'app'"

**Cause**: Gunicorn is looking in the `app/` package folder instead of `app.py` file.

**Solution**: Use one of these:
- `gunicorn app:app` (if `app.py` is in root) ✅
- `gunicorn run:app` (if using blueprint structure)
- Make sure you're in the correct directory

### Error: "ModuleNotFoundError"

**Solution**: 
- Ensure `requirements.txt` includes all dependencies
- Check that `gunicorn` is in requirements.txt
- Verify all imports in `app.py` are correct

### Error: "Port already in use"

**Solution**: 
- Render sets `$PORT` automatically
- Use `--bind 0.0.0.0:$PORT` (not a fixed port)
- Don't hardcode port numbers

## Environment Variables

Add these in Render Dashboard → Environment:

```bash
SECRET_KEY=your-secret-key-here
FLASK_ENV=production
DATABASE_URL=your-database-url (if using database)
```

## File Structure for Render

```
backend/
├── app.py              ← Flask app (root level)
├── run.py              ← Alternative entry point
├── requirements.txt    ← Dependencies
├── render.yaml        ← Render config (auto-detected)
├── Procfile           ← Alternative config
├── gunicorn_config.py ← Gunicorn config file
├── app/               ← Package folder
│   ├── __init__.py
│   ├── models.py
│   └── ...
├── templates/         ← Templates
├── static/            ← Static files
└── uploads/          ← Upload folder
```

## Quick Deploy Checklist

- [ ] `requirements.txt` includes `gunicorn`
- [ ] `render.yaml` or Start Command is configured
- [ ] `SECRET_KEY` environment variable is set
- [ ] All dependencies are in `requirements.txt`
- [ ] No hardcoded ports (use `$PORT`)
- [ ] Database connection string (if using database)
- [ ] Upload folders will be created automatically

## Testing Locally

Before deploying, test Gunicorn locally:

```bash
# Install gunicorn
pip install gunicorn

# Test the command
gunicorn app:app --bind 0.0.0.0:5000

# Or with config file
gunicorn app:app -c gunicorn_config.py
```

Visit `http://localhost:5000` to verify it works.

## Recommended: Use app.py

Since your current setup uses `app.py` directly, use:

**Start Command:**
```
gunicorn app:app --bind 0.0.0.0:$PORT
```

This is the simplest and matches your current structure!

