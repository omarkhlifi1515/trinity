Starter project for Trinity — minimal, working baseline for two apps.

Structure:
- starter/web_basic: Simple HR portal (Flask + SQLite)
- starter/agent_basic: Simple agent automation API (Flask + SQLite)

Quick start (PowerShell):

# Web HR portal
cd starter/web_basic
python -m venv .venv; .\.venv\Scripts\Activate
python -m pip install -r requirements.txt
python app.py
# open http://127.0.0.1:5001

# Agent automation API
cd ../agent_basic
python -m venv .venv; .\.venv\Scripts\Activate
python -m pip install -r requirements.txt
python app.py
# agent API at http://127.0.0.1:5002

Notes:
- These are minimal demo apps for fast prototyping. They use SQLite files in each folder.
- You can expand authentication, validation, and deployment when ready.