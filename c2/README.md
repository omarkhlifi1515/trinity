# Project Trinity — C2 Scan Service

This component provides a lightweight HTTP API to enqueue network/security scans (nmap and sqlmap) and run them in background threads so requests return immediately. **Only use this service against hosts you own or have written permission to test.**

Quick features
- POST `/api/scan` — enqueue a scan (nmap or sqlmap). Returns a `scan_id` immediately (202 Accepted).
- GET `/api/scan/<scan_id>` — retrieve status and output when complete.
- GET `/api/health` — health check and `DRY_RUN` indicator.

Environment variables
- `DRY_RUN` (true/false): when true the service simulates scans and does not call external binaries. Strongly recommended for initial testing.
- `MAX_SCAN_TIME` (int, seconds): timeout applied to each scan subprocess (default `300`).
- `ALLOWED_PREFIXES` (comma-separated): optional prefixes to restrict allowed target hostnames/IP starts.
- `ALLOWED_TARGETS` (comma-separated): optional exact allowed targets.
- `DB_PATH`: path to SQLite file (default `trinity.db`).

Security notes
- Always configure `ALLOWED_PREFIXES` or `ALLOWED_TARGETS` in production to avoid abuse.
- Protect the service behind authentication and network controls.
- Sqlmap can be destructive; avoid running against third-party webapps unless explicitly authorized.

Docker (build & run locally)

```powershell
cd c2
docker build -t trinity-c2:local .
docker run --rm -p 8000:8000 -e DRY_RUN=true trinity-c2:local
```

Smoke test (once service is running locally - DRY_RUN recommended)

```powershell
python test_smoke.py
```

Deploying to Render (Docker)
- Create a new Web Service on Render and connect your repository.
- Set the build command to `docker build -t trinity-c2 .` (Render detects Dockerfile automatically if present).
- Set required environment variables in Render's dashboard (at minimum `DRY_RUN=true` during testing).
- Note: Render free tier terminates HTTP requests around 50s; the service uses background threads so the enqueue request returns immediately.

Operational recommendations
- Run the scan worker in an environment with sufficient CPU and I/O limits.
- Add monitoring (logs, job success/failure counts) and rate limiting.
