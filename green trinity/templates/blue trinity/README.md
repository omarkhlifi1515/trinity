Blue Trinity (App 3) - Agent

This repository folder contains the Blue Trinity Agent script `blue_trinity_agent.py`.

Purpose
- Manager: Connects to the same SQLite DB used by App 1 (HR Portal). Generates "Warning Letter" text files for employees whose Status == "Absent" and saves them to a reports folder inside the Drive.
- Defender: Watches the `access.log` file in real-time and detects SQL injection attempts, XSS attempts, and DoS/flood attempts. Alerts to console and appends attacker details to `blacklist.txt` in the Drive folder.

Configuration
- Edit the top of `blue_trinity_agent.py` and set `DRIVE_PATH` to your Google Drive folder where `hr_data.db` and `access.log` live.
- Default filenames are configured via `DB_FILENAME`, `LOG_FILENAME`, `REPORTS_DIRNAME`, and `BLACKLIST_FILENAME`.

Dependencies
- Python 3.8+
- watchdog

Install dependencies:

```powershell
python -m pip install watchdog
```

Quick usage
- Run manager once:

```powershell
python blue_trinity_agent.py manager --once
```

- Start log monitor:

```powershell
python blue_trinity_agent.py monitor
```

- Run both (manager periodically + monitor + tiny health endpoint on port 8000):

```powershell
python blue_trinity_agent.py run --interval 86400
```

- Serve a tiny health endpoint only:

```powershell
python blue_trinity_agent.py serve --port 8000
```

Scheduling
- To run the Manager daily, add a Windows Task Scheduler entry calling the `manager --once` command once per day.
- Alternatively, run the script with `run --interval 86400` on a machine that stays online.

Security notes
- The Defender monitors logs and appends attackers to `blacklist.txt`. Review any blacklisted entries before taking automated action.
- Avoid deploying the Parrot/penetration-tools backend to a public host; it runs system commands and should remain on a dedicated, private environment.

