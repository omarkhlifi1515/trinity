# Render Deployment Fix

## Problem Identified

You have a **naming conflict**:
- `app.py` file exists with `app = Flask(__name__)`
- `app/` directory (package) also exists

When Python tries `import app`, it imports the **package directory** (not the file), because packages take precedence. The `app/` package has a `create_app()` function but no top-level `app` variable.

## Solution

Use `run.py` instead, which already creates the app instance:

**Render Start Command:**
```bash
gunicorn run:app --bind 0.0.0.0:$PORT
```

## Why This Works

Looking at `run.py`:
```python
from app import create_app, db
app = create_app()  # ← Creates the app instance
```

So `run.py` has:
- ✅ Module name: `run`
- ✅ Flask instance: `app = create_app()`
- ✅ Command: `gunicorn run:app`

## Updated Files

I've updated:
- ✅ `render.yaml` - Changed to `gunicorn run:app`
- ✅ `Procfile` - Changed to `gunicorn run:app`

## Alternative Solutions

If you want to use `app.py` instead:

### Option 1: Rename app.py
```bash
mv app.py application.py
```
Then use: `gunicorn application:app`

### Option 2: Rename app/ package
```bash
mv app/ trinity_app/
```
Then update all imports and use: `gunicorn app:app`

**But Option 1 (using run.py) is recommended** because:
- ✅ Already set up correctly
- ✅ Uses blueprint structure (better for production)
- ✅ No file renaming needed
- ✅ Works with your existing code

## Verification

Test locally:
```bash
gunicorn run:app --bind 0.0.0.0:5000
```

Should start without errors!

