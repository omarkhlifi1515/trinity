"""
Project Trinity - C2 Scan Service

This Flask app provides endpoints to enqueue network/security scans (nmap, sqlmap)
and runs them in background threads so HTTP requests return immediately.

IMPORTANT SAFETY NOTES:
- Only run scans against targets you own or have explicit written permission to test.
- Configure `ALLOWED_PREFIXES` or `ALLOWED_TARGETS` environment variables to restrict allowed targets.
- For development or CI you can set `DRY_RUN=true` to simulate scans without executing external tools.

Deploy considerations:
- Render (Free Tier) times out HTTP after ~50s, so all scans are run in background threads.
"""
import os
import re
import uuid
import json
import shlex
import sqlite3
import threading
import subprocess
from datetime import datetime, timezone
from typing import Optional
from flask import Flask, request, jsonify, g
from dotenv import load_dotenv
import logging
import ipaddress

load_dotenv()

DB_PATH = os.getenv('DB_PATH', 'trinity.db')
DRY_RUN = os.getenv('DRY_RUN', 'false').lower() in ('1', 'true', 'yes')
MAX_SCAN_TIME = int(os.getenv('MAX_SCAN_TIME', '300'))  # seconds timeout for subprocess
ALLOWED_PREFIXES = [p.strip() for p in os.getenv('ALLOWED_PREFIXES', '').split(',') if p.strip()]
ALLOWED_TARGETS = [t.strip() for t in os.getenv('ALLOWED_TARGETS', '').split(',') if t.strip()]

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger('trinity-c2')

app = Flask(__name__)


def get_db_conn():
    """Return a new sqlite3 connection for the current thread/request."""
    conn = sqlite3.connect(DB_PATH, check_same_thread=False)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    conn = get_db_conn()
    cur = conn.cursor()
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS scans (
            id TEXT PRIMARY KEY,
            target TEXT NOT NULL,
            type TEXT NOT NULL,
            command TEXT,
            status TEXT NOT NULL,
            output TEXT,
            created_at TEXT NOT NULL
        )
        """
    )
    conn.commit()
    conn.close()


def validate_target(target: str) -> bool:
    """Validate target is an IP address or hostname and optionally check against allowed lists.

    Returns True if allowed, False otherwise.
    """
    target = target.strip()
    # Quick IP check
    try:
        ipaddress.ip_address(target)
        is_ip = True
    except Exception:
        is_ip = False

    # Hostname basic validation (allow letters, digits, hyphen, dot)
    if not is_ip:
        if not re.match(r"^[A-Za-z0-9.-]+$", target):
            return False

    # If ALLOWED_TARGETS set, require exact match
    if ALLOWED_TARGETS:
        if target not in ALLOWED_TARGETS:
            return False

    # If ALLOWED_PREFIXES set, require one prefix match (useful for internal CIDR-like prefixes)
    if ALLOWED_PREFIXES:
        allowed = False
        for pref in ALLOWED_PREFIXES:
            if target.startswith(pref):
                allowed = True
                break
        if not allowed:
            return False

    return True


def insert_scan_row(scan_id: str, target: str, scan_type: str, command: Optional[str]):
    conn = get_db_conn()
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO scans (id, target, type, command, status, output, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
        (scan_id, target, scan_type, command or '', 'pending', '', datetime.now(timezone.utc).isoformat()),
    )
    conn.commit()
    conn.close()


def update_scan_row(scan_id: str, status: str, output: Optional[str]):
    conn = get_db_conn()
    cur = conn.cursor()
    cur.execute(
        "UPDATE scans SET status = ?, output = ? WHERE id = ?",
        (status, output or '', scan_id),
    )
    conn.commit()
    conn.close()


def read_scan_row(scan_id: str):
    conn = get_db_conn()
    cur = conn.cursor()
    cur.execute("SELECT * FROM scans WHERE id = ?", (scan_id,))
    row = cur.fetchone()
    conn.close()
    return dict(row) if row else None


def run_subprocess(command: list, timeout: int) -> (int, str, str):
    """Run subprocess safely (no shell), capture stdout/stderr, return (returncode, stdout, stderr).

    If DRY_RUN is enabled this returns simulated output.
    """
    if DRY_RUN:
        logger.info("DRY_RUN enabled - simulating command: %s", ' '.join(map(shlex.quote, command)))
        fake_output = f"[DRY RUN] Would run: {' '.join(command)}\n"
        fake_output += "Sample result: port 80/tcp open\n"
        return 0, fake_output, ''

    logger.info("Executing command: %s", ' '.join(map(shlex.quote, command)))
    try:
        proc = subprocess.run(command, capture_output=True, text=True, timeout=timeout)
        return proc.returncode, proc.stdout, proc.stderr
    except subprocess.TimeoutExpired as e:
        logger.exception('Command timed out')
        return 124, '', f'Timeout after {timeout} seconds'
    except Exception as e:
        logger.exception('Command execution failed')
        return 1, '', str(e)


def worker(scan_id: str, target: str, scan_type: str):
    """Background worker that runs the scan and updates the DB."""
    try:
        logger.info('Worker started for %s (%s)', scan_id, scan_type)
        update_scan_row(scan_id, 'running', '')

        if scan_type == 'nmap':
            # Example: service/version detection with default scripts; keep output moderate
            cmd = ['nmap', '-sV', '-Pn', target]
        elif scan_type == 'sqlmap':
            # sqlmap expects a URL; we put target as --url. Use --batch to avoid prompts.
            # Use python interpreter to run the cloned sqlmap
            sqlmap_path = os.path.join('/opt/sqlmap', 'sqlmap.py')
            if os.path.exists(sqlmap_path):
                cmd = ['python3', sqlmap_path, '--url', target, '--batch']
            else:
                # sqlmap not available - return failure
                update_scan_row(scan_id, 'failed', 'sqlmap not available in container')
                return
        else:
            update_scan_row(scan_id, 'failed', f'Unsupported scan type: {scan_type}')
            return

        # Save command used
        try:
            conn = get_db_conn()
            cur = conn.cursor()
            cur.execute("UPDATE scans SET command = ? WHERE id = ?", (' '.join(cmd), scan_id))
            conn.commit()
            conn.close()
        except Exception:
            logger.exception('Failed to save command to DB')

        rc, out, err = run_subprocess(cmd, timeout=MAX_SCAN_TIME)

        full_output = ''
        if out:
            full_output += out
        if err:
            full_output += '\n[STDERR]\n' + err

        if rc == 0:
            update_scan_row(scan_id, 'completed', full_output)
        elif rc == 124:
            update_scan_row(scan_id, 'failed', 'Scan timed out')
        else:
            update_scan_row(scan_id, 'failed', full_output or f'Exit code {rc}')

    except Exception:
        logger.exception('Worker encountered unexpected error')
        update_scan_row(scan_id, 'failed', 'Internal worker error')


@app.route('/api/scan', methods=['POST'])
def api_scan():
    """Start a scan in the background and return immediately with a scan id."""
    data = request.get_json() or {}
    target = (data.get('target') or '').strip()
    scan_type = (data.get('type') or '').strip().lower()

    if not target or not scan_type:
        return jsonify({'error': 'target and type are required'}), 400

    if scan_type not in ('nmap', 'sqlmap'):
        return jsonify({'error': 'type must be one of [nmap, sqlmap]'}), 400

    if not validate_target(target):
        return jsonify({'error': 'target validation failed or target not allowed'}), 400

    scan_id = str(uuid.uuid4())
    insert_scan_row(scan_id, target, scan_type, '')

    # Start background worker thread
    t = threading.Thread(target=worker, args=(scan_id, target, scan_type), daemon=True)
    t.start()

    return jsonify({'status': 'started', 'scan_id': scan_id}), 202


@app.route('/api/scan/<scan_id>', methods=['GET'])
def api_scan_status(scan_id: str):
    row = read_scan_row(scan_id)
    if not row:
        return jsonify({'error': 'scan not found'}), 404
    return jsonify({
        'scan_id': row['id'],
        'target': row['target'],
        'type': row['type'],
        'status': row['status'],
        'command': row['command'],
        'output': row['output'],
        'created_at': row['created_at'],
    })


@app.route('/api/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'dry_run': DRY_RUN})


if __name__ == '__main__':
    init_db()
    app.run(host='0.0.0.0', port=int(os.getenv('PORT', 8000)))
