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
from datetime import datetime
import re
from collections import deque, defaultdict
import threading
import json
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from http.server import HTTPServer, BaseHTTPRequestHandler

# -------------------- Configuration --------------------
# Set the path to the Google Drive folder where App 1 stores its files.
# Example on Windows: r"G:/My Drive/Project"
DRIVE_PATH = r"G:/My Drive/Project"  # <-- change this to your Drive folder

DB_FILENAME = "hr_data.db"          # name of the sqlite DB file used by App 1
LOG_FILENAME = "access.log"         # name of the access log file written by App 1
REPORTS_DIRNAME = "blue_trinity_reports"  # folder (inside Drive) to save warning letters
BLACKLIST_FILENAME = "blacklist.txt"     # file in Drive to append blacklisted IPs / attacks

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


def now_ts():
    return datetime.utcnow().isoformat() + "Z"


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
            print(f"[Manager] Database not found at {self.db_path}")
            return None

        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        table = self._find_employee_table(conn)
        if not table:
            print("[Manager] No employee/user table found in DB; please verify table name.")
            conn.close()
            return None

        cur = conn.cursor()
        # get columns
        cur.execute(f"PRAGMA table_info('{table}')")
        cols = [r['name'] for r in cur.fetchall()]

        # determine fields to select
        id_col = cols[0] if len(cols) > 0 else 'id'
        name_col = None
        status_col = None
        email_col = None
        for c in cols:
            low = c.lower()
            if not name_col and ('name' in low or 'fullname' in low):
                name_col = c
            if not status_col and 'status' in low:
                status_col = c
            if not email_col and 'email' in low:
                email_col = c
        if not name_col:
            name_col = cols[1] if len(cols) > 1 else id_col
        if not status_col:
            # try common column names
            for candidate in ['status', 'state', 'attendance', 'present']:
                if candidate in cols:
                    status_col = candidate
        if not status_col:
            print("[Manager] Could not find a 'Status' column in the employees table. Expected column named 'status' or similar.")
            conn.close()
            return None

        # fetch rows
        cur.execute(f"SELECT {id_col}, {name_col}, {status_col} { (', ' + email_col) if email_col else '' } FROM {table}")
        rows = cur.fetchall()

        active = 0
        absent = 0
        generated = []
        for r in rows:
            status = (r[status_col] if isinstance(r, sqlite3.Row) else r[2])
            try:
                status_str = str(status).strip().lower()
            except Exception:
                status_str = ''
            if status_str == 'absent' or status_str == 'a':
                absent += 1
                # generate report
                idv = r[id_col]
                namev = r[name_col]
                emailv = r[email_col] if email_col else ''
                fname = f"warning_{idv}_{sanitize_filename(str(namev))}_{datetime.utcnow().strftime('%Y%m%d')}.txt"
                path = os.path.join(self.reports_path, fname)
                content = generate_warning_letter(namev, idv)
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(content)
                generated.append(path)
            else:
                active += 1

        conn.close()

        # dashboard output
        print(f"[Manager] Health: Active={active} | Absent={absent} | Reports generated={len(generated)}")
        for g in generated:
            print(f"[Manager] Generated: {g}")

        return {'active': active, 'absent': absent, 'reports': generated}


def sanitize_filename(name: str) -> str:
    return re.sub(r'[^A-Za-z0-9_.-]', '_', name)


def generate_warning_letter(name, emp_id=None):
    date_str = datetime.utcnow().strftime('%Y-%m-%d')
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
            if len(q) >= self.dos_threshold:
                self.alert('DoS/Flooding Attempt', ip, path, line)

    def alert(self, reason: str, ip: str, path: str, line: str):
        # Print red alert
        red = '\033[91m'
        endc = '\033[0m'
        time_str = now_ts()
        msg = f"{red}[ALERT] {reason} | ip={ip} | path={path} | time={time_str}{endc}"
        print(msg)

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
        self.last_update = None

    def update(self, active, absent):
        self.active = active
        self.absent = absent
        self.last_update = now_ts()


class SimpleHandler(BaseHTTPRequestHandler):
    state = HealthState()

    def do_GET(self):
        if self.path.startswith('/health'):
            payload = {
                'active': self.state.active,
                'absent': self.state.absent,
                'last_update': self.state.last_update
            }
            body = json.dumps(payload).encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return
        # default: simple HTML
        if self.path == '/' or self.path == '/index.html':
            html = f"<html><body><h1>Blue Trinity - Health</h1><p>Active: {self.state.active}</p><p>Absent: {self.state.absent}</p><p>Last update: {self.state.last_update}</p></body></html>"
            body = html.encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'text/html')
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
        state.update(res['active'], res['absent'])


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
