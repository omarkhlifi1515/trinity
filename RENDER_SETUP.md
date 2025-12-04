# Render Deployment Setup Guide

This guide shows exactly what to configure in Render's dashboard for both `webtrinity` and `agenttrinity` services.

## Database Connection String

Your Postgres database URL (from Render):
```
postgresql://trinity_5mxn_user:6q8XxiTlIiEN7NS9ehPiOGENlWeU4Pqr@dpg-d4o6t22dbo4c73ab07t0-a.oregon-postgres.render.com/trinity_5mxn
```

---

## Service 1: WebTrinity

### In Render Dashboard

1. **Create a new Web Service**
   - Repository: `trinity` (or your GitHub repo)
   - Root Directory: `webtrinity`
   - Runtime: Python 3
   - Build Command: `pip install -r requirements.txt`
   - Start Command: (leave default or use Procfile)

2. **Environment Variables** (add these key-value pairs):

| Key | Value |
|-----|-------|
| `DATABASE_URL` | `postgresql://trinity_5mxn_user:6q8XxiTlIiEN7NS9ehPiOGENlWeU4Pqr@dpg-d4o6t22dbo4c73ab07t0-a.oregon-postgres.render.com/trinity_5mxn` |
| `FLASK_ENV` | `production` |

3. Click **Deploy** — Render will use `webtrinity/Procfile` to run `gunicorn wsgi:app`.

---

## Service 2: AgentTrinity

### In Render Dashboard

1. **Create a new Web Service**
   - Repository: `trinity` (same as above)
   - Root Directory: `agenttrinity`
   - Runtime: Python 3
   - Build Command: `pip install -r requirements.txt`
   - Start Command: (leave default or use Procfile)

2. **Environment Variables** (add these key-value pairs):

| Key | Value |
|-----|-------|
| `DATABASE_URL` | `postgresql://trinity_5mxn_user:6q8XxiTlIiEN7NS9ehPiOGENlWeU4Pqr@dpg-d4o6t22dbo4c73ab07t0-a.oregon-postgres.render.com/trinity_5mxn` |

3. Click **Deploy** — Render will use `agenttrinity/Procfile` to run `gunicorn agent_server:app`.

---

## Verification

After both services deploy successfully:

- **WebTrinity** should be accessible at `https://<your-webtrinity-service>.onrender.com/`
  - Check `/api/stats` endpoint: `GET https://<your-webtrinity-service>.onrender.com/api/stats`

- **AgentTrinity** should be accessible at `https://<your-agenttrinity-service>.onrender.com/`
  - Check health: `GET https://<your-agenttrinity-service>.onrender.com/health`
  - Both services read from the same `DATABASE_URL` and will auto-create tables on first request.

---

## Notes

- The `DATABASE_URL` is the same for both services — they share the same Postgres database.
- Render automatically injects environment variables from the dashboard into the running container.
- If you ever need to update the DB credentials, edit the environment variable in Render's dashboard and redeploy.
- `.env` files in the repository are **not** deployed to Render; always use the Render dashboard for secrets.
