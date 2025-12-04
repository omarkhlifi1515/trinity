"""
Blue Trinity Agent (App 3)

Two functions:
- Manager: connects to shared SQLite DB and generates warning letters for absent employees; provides health summary
- Defender: watches `access.log` in real-time and detects SQLi, XSS, and DoS/flood attacks; appends attackers to blacklist

Configuration section: set `DRIVE_PATH` and filenames to your Google Drive folder.

Usage examples:
- Run manager once:
    python blue_trinity_agent.py manager --once
- Run manager periodically every 86400 seconds (daily):
    python blue_trinity_agent.py manager --interval 86400
- Start log monitor:
    python blue_trinity_agent.py monitor
- Run both manager (periodic) and monitor together:
    python blue_trinity_agent.py run --interval 86400
- Serve a tiny health endpoint on port 8000:
    python blue_trinity_agent.py serve --port 8000

Dependencies: watchdog
    pip install watchdog

"""

import argparse
import os
import sqlite3
import time
from datetime import datetime, timezone
import re
from collections import deque, defaultdict
import threading
import json
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from http.server import HTTPServer, BaseHTTPRequestHandler

# -------------------- Configuration --------------------
# Base directory of this git repo (Trinity)
REPO_BASE = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

# Default path where the HR app (Green Trinity Local) lives.
# This is where `local_data.db` and optional `access.log` will be.
DEFAULT_DRIVE_PATH = os.path.join(REPO_BASE, "green trinity local")

# Allow overriding via env var if you ever move things:
#   set BLUE_TRINITY_DRIVE_PATH=C:/some/other/path
DRIVE_PATH = os.environ.get("BLUE_TRINITY_DRIVE_PATH", DEFAULT_DRIVE_PATH)

# Use the same DB filename as Green Trinity Local
DB_FILENAME = "local_data.db"           # SQLite DB created by the HR app locally
LOG_FILENAME = "access.log"             # Optional access log file written by web server / proxy
REPORTS_DIRNAME = "blue_trinity_reports"  # folder (inside DRIVE_PATH) to save warning letters
BLACKLIST_FILENAME = "blacklist.txt"      # file in DRIVE_PATH to append blacklisted IPs / attacks

# Manager settings
MANAGER_DEFAULT_INTERVAL = 86400  # default to once per day (seconds)

# Defender settings
DOS_THRESHOLD = 50   # number of requests
DOS_WINDOW = 60      # seconds (time window to count requests)
TAIL_READ_CHUNK = 8192

# -------------------- Derived paths --------------------
DB_PATH = os.path.join(DRIVE_PATH, DB_FILENAME)
LOG_PATH = os.path.join(DRIVE_PATH, LOG_FILENAME)
REPORTS_PATH = os.path.join(DRIVE_PATH, REPORTS_DIRNAME)
BLACKLIST_PATH = os.path.join(DRIVE_PATH, BLACKLIST_FILENAME)

# -------------------- Helpers & Utilities --------------------

def ensure_directories():
    os.makedirs(REPORTS_PATH, exist_ok=True)
    # ensure blacklist exists
    if not os.path.exists(BLACKLIST_PATH):
        with open(BLACKLIST_PATH, "w", encoding="utf-8") as f:
            f.write("# Blue Trinity blacklist\n")
    
    # Print database path for debugging
    print(f"[Config] 📁 Database path: {DB_PATH}")
    print(f"[Config] 📁 Database exists: {os.path.exists(DB_PATH)}")
    if os.path.exists(DB_PATH):
        size = os.path.getsize(DB_PATH)
        print(f"[Config] 📊 Database size: {size} bytes")


def now_ts():
    return datetime.now(timezone.utc).isoformat()


# -------------------- Manager: Automation --------------------
class Manager:
    def __init__(self, db_path=DB_PATH, reports_path=REPORTS_PATH):
        self.db_path = db_path
        self.reports_path = reports_path

    def _find_employee_table(self, conn):
        # look for a table with 'employee' or 'user' in the name; prefer 'employees'
        cur = conn.cursor()
        cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
        tables = [r[0] for r in cur.fetchall()]
        if 'employees' in tables:
            return 'employees'
        for t in tables:
            if 'employee' in t.lower():
                return t
        for t in tables:
            if 'user' in t.lower():
                return t
        return None

    def run_once(self):
        if not os.path.exists(self.db_path):
            print(f"[Manager] ❌ Database not found at {self.db_path}")
            print(f"[Manager] Please ensure Green Trinity Local has created the database.")
            return None

        print(f"[Manager] 📊 Reading database: {self.db_path}")
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        cur = conn.cursor()
        
        # List all tables for debugging
        cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
        all_tables = [r[0] for r in cur.fetchall()]
        print(f"[Manager] 📋 Found tables: {', '.join(all_tables)}")
        
        # Find employee table
        employee_table = self._find_employee_table(conn)
        if not employee_table:
            print("[Manager] ❌ No employee/user table found in DB.")
            print(f"[Manager] Available tables: {', '.join(all_tables)}")
            conn.close()
            return None
        
        print(f"[Manager] ✅ Using table: {employee_table}")

        # Get employee table columns
        cur.execute(f"PRAGMA table_info('{employee_table}')")
        emp_cols = [r['name'] for r in cur.fetchall()]

        # Determine fields to select from employee table
        id_col = emp_cols[0] if len(emp_cols) > 0 else 'id'
        name_col = None
        status_col = None
        user_id_col = None
        
        for c in emp_cols:
            low = c.lower()
            if not name_col and ('name' in low or 'fullname' in low):
                name_col = c
            if not status_col and 'status' in low:
                status_col = c
            if not user_id_col and 'user_id' in low:
                user_id_col = c
        
        if not name_col:
            name_col = emp_cols[1] if len(emp_cols) > 1 else id_col
        if not status_col:
            for candidate in ['status', 'state', 'attendance', 'present']:
                if candidate in emp_cols:
                    status_col = candidate
        
        if not status_col:
            print("[Manager] Could not find a 'Status' column in the employees table.")
            conn.close()
            return None

        # Check if presence table exists
        cur.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='presence'")
        has_presence_table = cur.fetchone() is not None
        
        # Get today's date for presence check
        today = datetime.now(timezone.utc).date()
        
        # Build presence query if table exists
        presence_join = ""
        if has_presence_table and user_id_col:
            # Get presence table structure
            cur.execute("PRAGMA table_info('presence')")
            pres_cols = [r['name'] for r in cur.fetchall()]
            pres_user_col = None
            pres_timestamp_col = None
            
            for c in pres_cols:
                low = c.lower()
                if 'user_id' in low:
                    pres_user_col = c
                if 'timestamp' in low or 'date' in low or 'time' in low:
                    pres_timestamp_col = c
            
            if pres_user_col and pres_timestamp_col:
                # Query to get users who marked presence today
                # Use strftime for SQLite date comparison (more reliable than DATE())
                presence_join = f"""
                LEFT JOIN (
                    SELECT DISTINCT {pres_user_col} as pres_user_id
                    FROM presence
                    WHERE strftime('%Y-%m-%d', {pres_timestamp_col}) = '{today.isoformat()}'
                ) pres ON pres.pres_user_id = {employee_table}.{user_id_col}
                """
        
        # Fetch employees with presence data
        select_cols = f"{id_col}, {name_col}, {status_col}"
        if user_id_col:
            select_cols += f", {employee_table}.{user_id_col}"
        
        query = f"""
        SELECT {select_cols}, 
               CASE WHEN pres.pres_user_id IS NOT NULL THEN 1 ELSE 0 END as is_present_today
        FROM {employee_table}
        {presence_join if presence_join else ""}
        """
        
        try:
            print(f"[Manager] 🔍 Executing query on {employee_table}...")
            cur.execute(query)
            rows = cur.fetchall()
            print(f"[Manager] 📈 Found {len(rows)} employee records")
        except Exception as e:
            # Fallback to simple query if join fails
            print(f"[Manager] ⚠️  Warning: Could not join presence table: {e}")
            print(f"[Manager] 🔄 Falling back to simple query...")
            cur.execute(f"SELECT {id_col}, {name_col}, {status_col} {f', {user_id_col}' if user_id_col else ''} FROM {employee_table}")
            rows = cur.fetchall()
            print(f"[Manager] 📈 Found {len(rows)} employee records (fallback query)")
            # Add is_present_today = 0 for all
            rows = [list(r) + [0] for r in rows]

        active = 0
        absent = 0
        present_today = 0
        generated = []
        
        if len(rows) == 0:
            print(f"[Manager] ⚠️  No employees found in {employee_table} table.")
            print(f"[Manager] 💡 Tip: Add employees in Green Trinity Local HR Portal first.")
            conn.close()
            return {
                'active': 0,
                'absent': 0,
                'present_today': 0,
                'reports': []
            }
        
        print(f"[Manager] 🔄 Processing {len(rows)} employees...")
        for r in rows:
            # Handle both Row and tuple
            if isinstance(r, sqlite3.Row):
                status = r[status_col]
                is_present = r.get('is_present_today', 0) if has_presence_table else 0
                idv = r[id_col]
                namev = r[name_col]
            else:
                status = r[2] if len(r) > 2 else None
                is_present = r[-1] if len(r) > 3 and has_presence_table else 0
                idv = r[0]
                namev = r[1]
            
            try:
                status_str = str(status).strip().lower() if status else ''
            except Exception:
                status_str = ''
            
            # Count present today - ONLY if they actually marked presence (not just active status)
            if is_present == 1:
                present_today += 1
            
            # Count active/absent based on HR status (separate from presence)
            if status_str == 'active' or status_str == 'a':
                active += 1
            elif status_str == 'absent':
                absent += 1
                # Generate warning letter for absent employees
                fname = f"warning_{idv}_{sanitize_filename(str(namev))}_{datetime.now(timezone.utc).strftime('%Y%m%d')}.txt"
                path = os.path.join(self.reports_path, fname)
                content = generate_warning_letter(namev, idv)
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(content)
                generated.append(path)
            else:
                # Default to active if status unclear
                active += 1

        conn.close()

        # Dashboard output with real-time presence
        print(f"[Manager] ✅ Health Summary:")
        print(f"[Manager]    📊 Active Personnel: {active}")
        print(f"[Manager]    ⚠️  Absent Personnel: {absent}")
        print(f"[Manager]    ✅ Present Today: {present_today}")
        print(f"[Manager]    📄 Reports Generated: {len(generated)}")
        
        if generated:
            for g in generated:
                print(f"[Manager]    📝 {g}")

        return {
            'active': active, 
            'absent': absent, 
            'present_today': present_today,
            'reports': generated,
            'reports_generated': len(generated)
        }


def sanitize_filename(name: str) -> str:
    return re.sub(r'[^A-Za-z0-9_.-]', '_', name)


def generate_warning_letter(name, emp_id=None):
    date_str = datetime.now(timezone.utc).strftime('%Y-%m-%d')
    lines = [
        f"Date: {date_str}",
        "", 
        f"To: {name}",
        "", 
        "Subject: Warning Notice - Unauthorised Absence",
        "",
        f"Dear {name},",
        "",
        "This letter is to inform you that our records indicate that you have been marked as 'Absent'. Continued absence without appropriate notification or authorization is a violation of company policy.",
        "",
        "Please contact your manager immediately and provide any supporting documentation for your absence.",
        "",
        "Regards,",
        "HR Department",
    ]
    if emp_id is not None:
        lines.insert(3, f"Employee ID: {emp_id}")
    return '\n'.join(lines)


# -------------------- Defender: Log Monitor --------------------
class LogMonitor(FileSystemEventHandler):
    def __init__(self, log_path=LOG_PATH, blacklist_path=BLACKLIST_PATH, dos_threshold=DOS_THRESHOLD, dos_window=DOS_WINDOW):
        super().__init__()
        self.log_path = os.path.abspath(log_path)
        self.blacklist_path = blacklist_path
        self.offset = 0
        self.ip_windows = defaultdict(deque)  # ip -> deque(timestamps)
        self.dos_threshold = dos_threshold
        self.dos_window = dos_window
        # prepare last offset
        if os.path.exists(self.log_path):
            self.offset = os.path.getsize(self.log_path)
        else:
            self.offset = 0

    def on_modified(self, event):
        try:
            if os.path.abspath(event.src_path) != self.log_path:
                return
        except Exception:
            return
        self._read_new_lines()

    def _read_new_lines(self):
        try:
            with open(self.log_path, 'r', encoding='utf-8', errors='replace') as f:
                f.seek(self.offset)
                new = f.read()
                new_len = len(new)
                if new_len == 0:
                    return
                lines = new.splitlines()
                for line in lines:
                    self.process_line(line)
                # update offset
                self.offset = f.tell()
        except FileNotFoundError:
            # log not yet created
            return
        except Exception as e:
            print(f"[Monitor] Error reading log: {e}")

    def process_line(self, line: str):
        # Basic parsing: get IP and path
        ip = extract_ip(line)
        path = extract_path(line)
        ts = time.time()

        # Rule 1: SQL keywords in URL params
        if path and re.search(r'\b(select|union|drop)\b', path, re.I):
            self.alert('SQL Injection Attempt Detected', ip, path, line)

        # Rule 2: Script tags
        if '<script' in line.lower() or (path and '<script' in path.lower()):
            self.alert('XSS Attempt Detected', ip, path, line)

        # Rule 3: DoS - count by IP
        if ip:
            q = self.ip_windows[ip]
            q.append(ts)
            # remove older than window
            while q and (ts - q[0] > self.dos_window):
                q.popleft()
            request_count = len(q)
            if request_count >= self.dos_threshold:
                # Enhanced DoS alert with request count
                self.alert(f'DoS/Flooding Attempt ({request_count} requests in {self.dos_window}s)', ip, path, line)

    def alert(self, reason: str, ip: str, path: str, line: str):
        # Silent alert - just log to file, no console pop-up
        time_str = now_ts()
        # Only log to file, no console output
        logger_msg = f"[ALERT] {reason} | ip={ip} | path={path} | time={time_str}"
        # Write to log file instead of console
        try:
            log_file = os.path.join(DRIVE_PATH, 'blue_trinity_alerts.log')
            with open(log_file, 'a', encoding='utf-8') as f:
                f.write(logger_msg + '\n')
        except:
            pass  # Silent fail

        # Append to blacklist with details
        entry = {
            'time': time_str,
            'reason': reason,
            'ip': ip,
            'path': path,
            'line': line
        }
        self.append_blacklist(entry)

    def append_blacklist(self, entry: dict):
        # Avoid exact duplicate lines (basic check)
        try:
            with open(self.blacklist_path, 'r', encoding='utf-8', errors='ignore') as f:
                existing = f.read()
        except FileNotFoundError:
            existing = ''
        jline = json.dumps(entry, ensure_ascii=False)
        if jline in existing:
            return
        with open(self.blacklist_path, 'a', encoding='utf-8') as f:
            f.write(jline + '\n')


# -------------------- Log parsing helpers --------------------
_ip_re = re.compile(r'(?P<ip>\d{1,3}(?:\.\d{1,3}){3})')
_req_re = re.compile(r'"(?P<method>GET|POST|PUT|DELETE|HEAD)\s+(?P<path>[^\s]+)')


def extract_ip(line: str):
    m = _ip_re.search(line)
    return m.group('ip') if m else None


def extract_path(line: str):
    m = _req_re.search(line)
    return m.group('path') if m else None


# -------------------- Simple HTTP health endpoint --------------------
class HealthState:
    def __init__(self):
        self.active = 0
        self.absent = 0
        self.present_today = 0
        self.last_update = None
        self.blacklisted_ips = set()
        self.reports_generated = 0

    def update(self, active, absent, present_today=0, reports_generated=0):
        self.active = active
        self.absent = absent
        self.present_today = present_today
        self.reports_generated = reports_generated
        self.last_update = now_ts()
    
    def update_blacklist(self):
        """Update blacklisted IPs from blacklist file"""
        try:
            if os.path.exists(BLACKLIST_PATH):
                with open(BLACKLIST_PATH, 'r', encoding='utf-8', errors='ignore') as f:
                    lines = [ln.strip() for ln in f.readlines() if ln.strip() and not ln.startswith("#")]
                    ips = set()
                    for ln in lines:
                        try:
                            entry = json.loads(ln)
                            ip = entry.get('ip', '')
                            if ip:
                                ips.add(ip)
                        except:
                            continue
                    self.blacklisted_ips = ips
        except Exception:
            pass


class SimpleHandler(BaseHTTPRequestHandler):
    state = HealthState()

    def _read_recent_alerts(self, limit: int = 50):
        """Read the last `limit` JSON lines from the blacklist file."""
        try:
            if not os.path.exists(BLACKLIST_PATH):
                return []
            with open(BLACKLIST_PATH, "r", encoding="utf-8", errors="ignore") as f:
                lines = [ln.strip() for ln in f.readlines() if ln.strip() and not ln.startswith("#")]
            lines = lines[-limit:]
            alerts = []
            for ln in lines:
                try:
                    alerts.append(json.loads(ln))
                except Exception:
                    continue
            return alerts
        except Exception:
            return []

    def do_GET(self):
        if self.path.startswith('/health'):
            # Update blacklist before returning health
            self.state.update_blacklist()
            payload = {
                'active': self.state.active,
                'absent': self.state.absent,
                'present_today': self.state.present_today,
                'last_update': self.state.last_update,
                'blacklisted_ips': len(self.state.blacklisted_ips),
                'reports_generated': self.state.reports_generated
            }
            body = json.dumps(payload).encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return

        if self.path.startswith('/alerts'):
            # Update blacklist before returning alerts
            self.state.update_blacklist()
            alerts = self._read_recent_alerts()
            body = json.dumps({'alerts': alerts, 'count': len(alerts)}).encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return
        
        if self.path.startswith('/api/stats'):
            # Real-time stats endpoint
            self.state.update_blacklist()
            alerts = self._read_recent_alerts()
            stats = {
                'active': self.state.active,
                'absent': self.state.absent,
                'present_today': self.state.present_today,
                'blacklisted_ips': len(self.state.blacklisted_ips),
                'reports_generated': self.state.reports_generated,
                'alert_count': len(alerts),
                'last_update': self.state.last_update
            }
            body = json.dumps(stats).encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return

        # default: Cyberpunk SOC Dashboard
        if self.path == '/' or self.path == '/index.html':
            # Update state before rendering
            self.state.update_blacklist()
            alerts = self._read_recent_alerts()
            rows_html = ""
            alert_count = len(alerts)
            for idx, a in enumerate(reversed(alerts[-50:])):  # Last 50 alerts
                reason = a.get('reason', '')
                alert_class = 'alert-sqli' if 'SQL' in reason else 'alert-xss' if 'XSS' in reason else 'alert-dos'
                badge_class = 'badge-sqli' if 'SQL' in reason else 'badge-xss' if 'XSS' in reason else 'badge-dos'
                rows_html += (
                    f"<tr class='{alert_class}' data-index='{idx}'>"
                    f"<td class='time-cell'>{a.get('time','').replace('T',' ').replace('Z','')}</td>"
                    f"<td class='reason-cell'><span class='badge {badge_class}'>{reason}</span></td>"
                    f"<td class='ip-cell'>{a.get('ip','')}</td>"
                    f"<td class='path-cell'><code>{a.get('path','')[:60]}{'...' if len(a.get('path','')) > 60 else ''}</code></td>"
                    "</tr>"
                )
            if not rows_html:
                rows_html = "<tr><td colspan='4' class='no-alerts'>No threats detected. System secure.</td></tr>"

            html = f"""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BLUE TRINITY | SOC DASHBOARD</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Rajdhani:wght@300;400;600;700&display=swap');
        
        * {{ margin: 0; padding: 0; box-sizing: border-box; }}
        
        body {{
            font-family: 'Rajdhani', sans-serif;
            background: #0a0a0f;
            color: #00ffff;
            overflow-x: hidden;
            position: relative;
            min-height: 100vh;
        }}
        
        /* Animated background grid */
        body::before {{
            content: '';
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background-image: 
                linear-gradient(rgba(0, 255, 255, 0.03) 1px, transparent 1px),
                linear-gradient(90deg, rgba(0, 255, 255, 0.03) 1px, transparent 1px);
            background-size: 50px 50px;
            animation: gridMove 20s linear infinite;
            pointer-events: none;
            z-index: 0;
        }}
        
        @keyframes gridMove {{
            0% {{ transform: translate(0, 0); }}
            100% {{ transform: translate(50px, 50px); }}
        }}
        
        /* Scanline effect */
        body::after {{
            content: '';
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: linear-gradient(
                transparent 50%,
                rgba(0, 255, 255, 0.03) 50%
            );
            background-size: 100% 4px;
            animation: scanline 8s linear infinite;
            pointer-events: none;
            z-index: 1000;
        }}
        
        @keyframes scanline {{
            0% {{ transform: translateY(0); }}
            100% {{ transform: translateY(4px); }}
        }}
        
        .container {{
            position: relative;
            z-index: 1;
            padding: 20px;
            max-width: 1920px;
            margin: 0 auto;
        }}
        
        /* Header */
        header {{
            background: linear-gradient(135deg, rgba(0, 255, 255, 0.1), rgba(138, 43, 226, 0.1));
            border: 2px solid #00ffff;
            border-radius: 8px;
            padding: 20px 30px;
            margin-bottom: 30px;
            box-shadow: 
                0 0 20px rgba(0, 255, 255, 0.3),
                inset 0 0 20px rgba(0, 255, 255, 0.1);
            position: relative;
            overflow: hidden;
        }}
        
        header::before {{
            content: '';
            position: absolute;
            top: -50%; left: -50%;
            width: 200%; height: 200%;
            background: linear-gradient(
                45deg,
                transparent,
                rgba(0, 255, 255, 0.1),
                transparent
            );
            animation: shine 3s infinite;
        }}
        
        @keyframes shine {{
            0% {{ transform: translateX(-100%) translateY(-100%) rotate(45deg); }}
            100% {{ transform: translateX(100%) translateY(100%) rotate(45deg); }}
        }}
        
        h1 {{
            font-family: 'Orbitron', monospace;
            font-size: 42px;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 4px;
            background: linear-gradient(90deg, #00ffff, #ff00ff, #00ffff);
            background-size: 200% auto;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            animation: gradientShift 3s ease infinite;
            text-shadow: 0 0 30px rgba(0, 255, 255, 0.5);
            position: relative;
            z-index: 1;
        }}
        
        @keyframes gradientShift {{
            0%, 100% {{ background-position: 0% center; }}
            50% {{ background-position: 100% center; }}
        }}
        
        .subtitle {{
            font-size: 16px;
            color: #888;
            margin-top: 8px;
            letter-spacing: 2px;
            position: relative;
            z-index: 1;
        }}
        
        .status-bar {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 15px;
            font-size: 14px;
            position: relative;
            z-index: 1;
        }}
        
        .status-item {{
            display: flex;
            align-items: center;
            gap: 8px;
        }}
        
        .status-dot {{
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: #00ff00;
            box-shadow: 0 0 10px #00ff00;
            animation: pulse 2s infinite;
        }}
        
        @keyframes pulse {{
            0%, 100% {{ opacity: 1; transform: scale(1); }}
            50% {{ opacity: 0.5; transform: scale(1.2); }}
        }}
        
        /* Stats Grid */
        .stats-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }}
        
        .stat-card {{
            background: linear-gradient(135deg, rgba(0, 20, 40, 0.8), rgba(0, 40, 80, 0.4));
            border: 2px solid #00ffff;
            border-radius: 12px;
            padding: 25px;
            position: relative;
            overflow: hidden;
            transition: all 0.3s ease;
            box-shadow: 
                0 0 20px rgba(0, 255, 255, 0.2),
                inset 0 0 20px rgba(0, 255, 255, 0.05);
        }}
        
        .stat-card:hover {{
            transform: translateY(-5px);
            box-shadow: 
                0 0 30px rgba(0, 255, 255, 0.4),
                inset 0 0 30px rgba(0, 255, 255, 0.1);
        }}
        
        .stat-card::before {{
            content: '';
            position: absolute;
            top: 0; left: -100%;
            width: 100%; height: 100%;
            background: linear-gradient(
                90deg,
                transparent,
                rgba(0, 255, 255, 0.2),
                transparent
            );
            animation: cardScan 3s infinite;
        }}
        
        @keyframes cardScan {{
            0% {{ left: -100%; }}
            100% {{ left: 100%; }}
        }}
        
        .stat-label {{
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 3px;
            color: #888;
            margin-bottom: 10px;
            font-weight: 600;
        }}
        
        .stat-value {{
            font-family: 'Orbitron', monospace;
            font-size: 48px;
            font-weight: 700;
            color: #00ffff;
            text-shadow: 0 0 20px rgba(0, 255, 255, 0.8);
            line-height: 1;
            margin-bottom: 8px;
        }}
        
        .stat-card.alert .stat-value {{
            color: #ff0080;
            text-shadow: 0 0 20px rgba(255, 0, 128, 0.8);
        }}
        
        .stat-card.success .stat-value {{
            color: #00ff80;
            text-shadow: 0 0 20px rgba(0, 255, 128, 0.8);
        }}
        
        .stat-sub {{
            font-size: 13px;
            color: #666;
            letter-spacing: 1px;
        }}
        
        /* Alerts Table */
        .alerts-section {{
            background: linear-gradient(135deg, rgba(0, 20, 40, 0.8), rgba(0, 40, 80, 0.4));
            border: 2px solid #00ffff;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 
                0 0 20px rgba(0, 255, 255, 0.2),
                inset 0 0 20px rgba(0, 255, 255, 0.05);
        }}
        
        .section-header {{
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }}
        
        .section-title {{
            font-family: 'Orbitron', monospace;
            font-size: 24px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 3px;
            color: #00ffff;
            text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
        }}
        
        .alert-count {{
            background: rgba(255, 0, 128, 0.2);
            border: 1px solid #ff0080;
            border-radius: 20px;
            padding: 5px 15px;
            font-family: 'Orbitron', monospace;
            font-size: 18px;
            color: #ff0080;
            box-shadow: 0 0 15px rgba(255, 0, 128, 0.3);
        }}
        
        .table-wrapper {{
            max-height: 500px;
            overflow-y: auto;
            border-radius: 8px;
            border: 1px solid rgba(0, 255, 255, 0.3);
        }}
        
        .table-wrapper::-webkit-scrollbar {{
            width: 8px;
        }}
        
        .table-wrapper::-webkit-scrollbar-track {{
            background: rgba(0, 255, 255, 0.1);
            border-radius: 4px;
        }}
        
        .table-wrapper::-webkit-scrollbar-thumb {{
            background: #00ffff;
            border-radius: 4px;
            box-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
        }}
        
        table {{
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }}
        
        thead {{
            position: sticky;
            top: 0;
            z-index: 10;
        }}
        
        th {{
            background: linear-gradient(180deg, rgba(0, 255, 255, 0.2), rgba(0, 255, 255, 0.1));
            padding: 15px;
            text-align: left;
            font-family: 'Orbitron', monospace;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 2px;
            color: #00ffff;
            border-bottom: 2px solid #00ffff;
            font-weight: 700;
        }}
        
        td {{
            padding: 12px 15px;
            border-bottom: 1px solid rgba(0, 255, 255, 0.1);
            color: #ccc;
        }}
        
        tr {{
            transition: all 0.2s ease;
        }}
        
        tr:hover {{
            background: rgba(0, 255, 255, 0.05);
            transform: scale(1.01);
        }}
        
        .alert-sqli {{
            border-left: 3px solid #ff0080;
        }}
        
        .alert-xss {{
            border-left: 3px solid #ffaa00;
        }}
        
        .alert-dos {{
            border-left: 3px solid #ff0080;
        }}
        
        .badge {{
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
        }}
        
        .badge-sqli {{
            background: rgba(255, 0, 128, 0.2);
            border: 1px solid #ff0080;
            color: #ff0080;
            box-shadow: 0 0 10px rgba(255, 0, 128, 0.3);
        }}
        
        .badge-xss {{
            background: rgba(255, 170, 0, 0.2);
            border: 1px solid #ffaa00;
            color: #ffaa00;
            box-shadow: 0 0 10px rgba(255, 170, 0, 0.3);
        }}
        
        .badge-dos {{
            background: rgba(255, 0, 128, 0.2);
            border: 1px solid #ff0080;
            color: #ff0080;
            box-shadow: 0 0 10px rgba(255, 0, 128, 0.3);
        }}
        
        .time-cell {{
            font-family: 'Orbitron', monospace;
            font-size: 12px;
            color: #888;
        }}
        
        .ip-cell {{
            font-family: 'Courier New', monospace;
            color: #00ffff;
            font-weight: 600;
        }}
        
        .path-cell code {{
            background: rgba(0, 255, 255, 0.1);
            padding: 2px 6px;
            border-radius: 4px;
            color: #00ffff;
            font-size: 12px;
        }}
        
        .no-alerts {{
            text-align: center;
            padding: 40px !important;
            color: #00ff80;
            font-size: 16px;
            letter-spacing: 2px;
        }}
        
        /* Glitch effect for alerts */
        @keyframes glitch {{
            0%, 100% {{ transform: translate(0); }}
            20% {{ transform: translate(-2px, 2px); }}
            40% {{ transform: translate(-2px, -2px); }}
            60% {{ transform: translate(2px, 2px); }}
            80% {{ transform: translate(2px, -2px); }}
        }}
        
        .alert-sqli:hover {{
            animation: glitch 0.3s;
        }}
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>BLUE TRINITY</h1>
            <div class="subtitle">SECURITY OPERATIONS CENTER | REAL-TIME THREAT MONITORING</div>
            <div class="status-bar">
                <div class="status-item">
                    <div class="status-dot"></div>
                    <span>SYSTEM ONLINE</span>
                </div>
                <div class="status-item">
                    <span>LAST UPDATE: {self.state.last_update or 'N/A'}</span>
                </div>
                <div class="status-item">
                    <span>THREATS DETECTED: <strong style="color: #ff0080;">{alert_count}</strong></span>
                </div>
            </div>
        </header>
        
        <div class="stats-grid">
            <div class="stat-card success">
                <div class="stat-label">Present Today</div>
                <div class="stat-value" id="stat-present">{self.state.present_today}</div>
                <div class="stat-sub">Marked Presence Today</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Active Personnel</div>
                <div class="stat-value" id="stat-active">{self.state.active}</div>
                <div class="stat-sub">HR Database Status</div>
            </div>
            <div class="stat-card alert">
                <div class="stat-label">Absent Personnel</div>
                <div class="stat-value" id="stat-absent">{self.state.absent}</div>
                <div class="stat-sub">Requires Attention</div>
            </div>
            <div class="stat-card danger">
                <div class="stat-label">Security Alerts</div>
                <div class="stat-value" id="stat-alerts">{alert_count}</div>
                <div class="stat-sub">Threats Detected</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Blacklisted IPs</div>
                <div class="stat-value" id="stat-blacklist">{len(self.state.blacklisted_ips)}</div>
                <div class="stat-sub">Blocked Attackers</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Reports Generated</div>
                <div class="stat-value" id="stat-reports">{self.state.reports_generated}</div>
                <div class="stat-sub">Warning Letters</div>
            </div>
        </div>
        
        <div class="alerts-section">
            <div class="section-header">
                <div class="section-title">THREAT LOG</div>
                <div class="alert-count">{alert_count} ALERTS</div>
            </div>
            <div class="table-wrapper">
                <table>
                    <thead>
                        <tr>
                            <th>TIMESTAMP</th>
                            <th>THREAT TYPE</th>
                            <th>SOURCE IP</th>
                            <th>TARGET PATH</th>
                        </tr>
                    </thead>
                    <tbody>
                        {rows_html}
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    
    <script>
        let lastAlertCount = {alert_count};
        let lastUpdateTime = '{self.state.last_update or ""}';
        
        // Real-time updates without page reload
        function updateDashboard() {{
            // Update stats
            fetch('/api/stats')
                .then(r => r.json())
                .then(data => {{
                    // Update stat cards
                    const presentEl = document.getElementById('stat-present');
                    const activeEl = document.getElementById('stat-active');
                    const absentEl = document.getElementById('stat-absent');
                    const alertsEl = document.getElementById('stat-alerts');
                    const blacklistEl = document.getElementById('stat-blacklist');
                    const reportsEl = document.getElementById('stat-reports');
                    
                    if (presentEl && data.present_today !== undefined) {{
                        animateValue('stat-present', parseInt(presentEl.textContent) || 0, data.present_today, 300);
                    }}
                    if (activeEl && data.active !== undefined) {{
                        animateValue('stat-active', parseInt(activeEl.textContent) || 0, data.active, 300);
                    }}
                    if (absentEl && data.absent !== undefined) {{
                        animateValue('stat-absent', parseInt(absentEl.textContent) || 0, data.absent, 300);
                    }}
                    if (alertsEl && data.alert_count !== undefined) {{
                        animateValue('stat-alerts', parseInt(alertsEl.textContent) || 0, data.alert_count, 300);
                    }}
                    if (blacklistEl && data.blacklisted_ips !== undefined) {{
                        animateValue('stat-blacklist', parseInt(blacklistEl.textContent) || 0, data.blacklisted_ips, 300);
                    }}
                    if (reportsEl && data.reports_generated !== undefined) {{
                        animateValue('stat-reports', parseInt(reportsEl.textContent) || 0, data.reports_generated, 300);
                    }}
                    
                    // Update last update time
                    if (data.last_update) {{
                        const statusBar = document.querySelector('.status-item:nth-child(2) span');
                        if (statusBar && statusBar.textContent.includes('LAST UPDATE')) {{
                            statusBar.textContent = 'LAST UPDATE: ' + (data.last_update.replace('T', ' ').replace('Z', '') || 'N/A');
                        }}
                    }}
                }})
                .catch(err => console.error('Stats fetch error:', err));
            
            // Update alerts table without reload
            fetch('/alerts')
                .then(r => r.json())
                .then(data => {{
                    const alerts = data.alerts || [];
                    const count = alerts.length;
                    
                    // Update alert count badge
                    const countEl = document.querySelector('.alert-count');
                    if (countEl) {{
                        countEl.textContent = count + ' ALERTS';
                    }}
                    
                    // Only update table if alerts changed
                    if (count !== lastAlertCount) {{
                        updateAlertsTable(alerts);
                        lastAlertCount = count;
                    }}
                }})
                .catch(err => console.error('Alerts fetch error:', err));
        }}
        
        function updateAlertsTable(alerts) {{
            const tbody = document.querySelector('tbody');
            if (!tbody) return;
            
            let rowsHtml = '';
            if (alerts.length === 0) {{
                rowsHtml = "<tr><td colspan='4' class='no-alerts'>No threats detected. System secure.</td></tr>";
            }} else {{
                alerts.slice().reverse().slice(0, 50).forEach((a, idx) => {{
                    const reason = a.reason || '';
                    const alertClass = reason.includes('SQL') ? 'alert-sqli' : reason.includes('XSS') ? 'alert-xss' : 'alert-dos';
                    const badgeClass = reason.includes('SQL') ? 'badge-sqli' : reason.includes('XSS') ? 'badge-xss' : 'badge-dos';
                    const time = (a.time || '').replace('T', ' ').replace('Z', '');
                    const path = (a.path || '').substring(0, 60) + ((a.path || '').length > 60 ? '...' : '');
                    
                    rowsHtml += `
                        <tr class="${{alertClass}}" data-index="${{idx}}">
                            <td class="time-cell">${{time}}</td>
                            <td class="reason-cell"><span class="badge ${{badgeClass}}">${{reason}}</span></td>
                            <td class="ip-cell">${{a.ip || ''}}</td>
                            <td class="path-cell"><code>${{path}}</code></td>
                        </tr>
                    `;
                }});
            }}
            
            tbody.innerHTML = rowsHtml;
        }}
        
        // Auto-refresh every 2 seconds (real-time)
        setInterval(updateDashboard, 2000);
        
        // Initial update
        updateDashboard();
        
        // Animate numbers
        function animateValue(id, start, end, duration) {{
            const obj = document.getElementById(id);
            if (!obj) return;
            if (start === end) return;
            const range = end - start;
            const increment = range / (duration / 16);
            let current = start;
            const timer = setInterval(() => {{
                current += increment;
                if ((increment > 0 && current >= end) || (increment < 0 && current <= end)) {{
                    obj.textContent = end;
                    clearInterval(timer);
                }} else {{
                    obj.textContent = Math.floor(current);
                }}
            }}, 16);
        }}
        
        // Animate on load
        window.addEventListener('load', () => {{
            animateValue('stat-present', 0, {self.state.present_today}, 1000);
            animateValue('stat-active', 0, {self.state.active}, 1000);
            animateValue('stat-absent', 0, {self.state.absent}, 1000);
            animateValue('stat-alerts', 0, {alert_count}, 1000);
            
            // Add entrance animation to table rows
            const rows = document.querySelectorAll('tbody tr');
            rows.forEach((row, idx) => {{
                row.style.opacity = '0';
                row.style.transform = 'translateX(-20px)';
                setTimeout(() => {{
                    row.style.transition = 'all 0.3s ease';
                    row.style.opacity = '1';
                    row.style.transform = 'translateX(0)';
                }}, idx * 50);
            }});
        }});
        
        // Add glitch effect on hover for alert rows
        document.querySelectorAll('.alert-sqli, .alert-xss, .alert-dos').forEach(row => {{
            row.addEventListener('mouseenter', function() {{
                this.style.animation = 'glitch 0.3s';
            }});
        }});
    </script>
</body>
</html>
"""
            body = html.encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'text/html; charset=utf-8')
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return

        self.send_response(404)
        self.end_headers()


# -------------------- CLI / Runner --------------------

def run_manager_once_and_update_state(state: HealthState):
    m = Manager()
    res = m.run_once()
    if res and state is not None:
        state.update(
            res.get('active', 0), 
            res.get('absent', 0),
            res.get('present_today', 0),
            res.get('reports_generated', 0)
        )


def start_monitor_in_thread():
    ensure_directories()
    handler = LogMonitor()
    observer = Observer()
    # watch parent directory because Watchdog requires directory
    parent = os.path.dirname(handler.log_path) or '.'
    observer.schedule(handler, path=parent, recursive=False)
    observer.start()
    print(f"[Monitor] Watching {handler.log_path} for changes...")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()


def start_http_server(port: int, state: HealthState):
    SimpleHandler.state = state
    server = HTTPServer(('0.0.0.0', port), SimpleHandler)
    print(f"[Serve] Health endpoint running on http://0.0.0.0:{port}/health")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        server.server_close()


def manager_loop(interval: int, state: HealthState, stop_event: threading.Event):
    print(f"[Manager] Starting manager loop with interval {interval} seconds")
    while not stop_event.is_set():
        run_manager_once_and_update_state(state)
        # sleep with ability to stop
        for _ in range(int(max(1, interval))):
            if stop_event.is_set():
                break
            time.sleep(1)


def main():
    # Initialize directories and print config
    ensure_directories()
    
    parser = argparse.ArgumentParser(description='Blue Trinity Agent')
    sub = parser.add_subparsers(dest='cmd')

    p_mgr = sub.add_parser('manager', help='Run manager tasks (once or periodically)')
    p_mgr.add_argument('--once', action='store_true', help='Run manager once and exit')
    p_mgr.add_argument('--interval', type=int, default=MANAGER_DEFAULT_INTERVAL, help='Interval seconds for periodic manager runs')

    p_mon = sub.add_parser('monitor', help='Start log monitor')

    p_run = sub.add_parser('run', help='Run both manager and monitor')
    p_run.add_argument('--interval', type=int, default=MANAGER_DEFAULT_INTERVAL, help='Manager interval seconds')

    p_serve = sub.add_parser('serve', help='Serve a tiny health endpoint')
    p_serve.add_argument('--port', type=int, default=8000)

    args = parser.parse_args()

    ensure_directories()

    if args.cmd == 'manager':
        state = HealthState()
        if args.once:
            run_manager_once_and_update_state(state)
            return
        # periodic
        stop_event = threading.Event()
        try:
            manager_loop(args.interval, state, stop_event)
        except KeyboardInterrupt:
            stop_event.set()
        return

    if args.cmd == 'monitor':
        start_monitor_in_thread()
        return

    if args.cmd == 'serve':
        state = HealthState()
        # perform one manager run to seed state
        run_manager_once_and_update_state(state)
        start_http_server(args.port, state)
        return

    if args.cmd == 'run':
        state = HealthState()
        # start manager thread
        stop_event = threading.Event()
        mgr_thread = threading.Thread(target=manager_loop, args=(args.interval, state, stop_event), daemon=True)
        mgr_thread.start()
        # start http server in separate thread for health display
        srv_thread = threading.Thread(target=start_http_server, args=(8000, state), daemon=True)
        srv_thread.start()
        # start monitor in main thread
        try:
            handler = LogMonitor()
            observer = Observer()
            parent = os.path.dirname(handler.log_path) or '.'
            observer.schedule(handler, path=parent, recursive=False)
            observer.start()
            print("[Run] Manager + Monitor started. Press Ctrl-C to exit.")
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            print('[Run] Shutting down...')
            stop_event.set()
            observer.stop()
            observer.join()
        return

    parser.print_help()


if __name__ == '__main__':
    main()
