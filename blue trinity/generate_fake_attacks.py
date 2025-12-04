"""
Quick test: Generate fake attack log entries directly to access.log

This is useful for testing Blue Trinity without needing the Flask app running.
It writes fake log entries in Apache/Nginx common log format.

Usage:
    python generate_fake_attacks.py
"""

import os
from datetime import datetime
import time
import random

# Path to access.log (same as Blue Trinity expects)
GREEN_TRINITY_LOCAL_PATH = r"C:\Users\msi\Documents\GitHub\trinity\green trinity local"
ACCESS_LOG_PATH = os.path.join(GREEN_TRINITY_LOCAL_PATH, "access.log")

# Fake IPs for testing
FAKE_IPS = [
    "192.168.1.100",
    "10.0.0.50",
    "172.16.0.25",
    "203.0.113.42",
    "198.51.100.99",
]

def write_log_entry(ip, method, path, status=200, size=1024, user_agent="Mozilla/5.0"):
    """Write a log entry in Apache common log format"""
    timestamp = datetime.utcnow().strftime('[%d/%b/%Y:%H:%M:%S +0000]')
    request_line = f'"{method} {path} HTTP/1.1"'
    log_line = f'{ip} - - {timestamp} {request_line} {status} {size} "-" "{user_agent}"\n'
    
    with open(ACCESS_LOG_PATH, 'a', encoding='utf-8') as f:
        f.write(log_line)
    
    return log_line.strip()

def generate_normal_traffic(count=10):
    """Generate normal traffic"""
    print(f"[GEN] Generating {count} normal requests...")
    normal_paths = [
        "/",
        "/auth/login",
        "/health",
        "/dashboard-user",
        "/tasks",
    ]
    
    for _ in range(count):
        ip = random.choice(FAKE_IPS)
        path = random.choice(normal_paths)
        write_log_entry(ip, "GET", path)
        time.sleep(0.1)

def generate_sql_injection_attacks(count=5):
    """Generate SQL injection attack log entries"""
    print(f"[GEN] Generating {count} SQL injection attacks...")
    
    sql_paths = [
        "/login?user=admin' OR '1'='1",
        "/search?id=1 UNION SELECT * FROM users",
        "/api/users?id=1; DROP TABLE users;--",
        "/dashboard?user=admin' OR 1=1--",
        "/auth/login?username=admin' OR '1'='1'--",
    ]
    
    for i in range(count):
        ip = random.choice(FAKE_IPS)
        path = sql_paths[i % len(sql_paths)]
        write_log_entry(ip, "GET", path, status=400)
        print(f"  ✓ SQLi: {path[:50]}...")
        time.sleep(0.2)

def generate_xss_attacks(count=5):
    """Generate XSS attack log entries"""
    print(f"[GEN] Generating {count} XSS attacks...")
    
    xss_paths = [
        "/search?q=<script>alert('XSS')</script>",
        "/comment?text=<script>document.cookie</script>",
        "/profile?name=<img src=x onerror=alert('XSS')>",
        "/dashboard?msg=<script>alert(document.domain)</script>",
        "/api/data?callback=<script>alert(1)</script>",
    ]
    
    for i in range(count):
        ip = random.choice(FAKE_IPS)
        path = xss_paths[i % len(xss_paths)]
        write_log_entry(ip, "GET", path, status=400)
        print(f"  ✓ XSS: {path[:50]}...")
        time.sleep(0.2)

def generate_dos_attack(count=60):
    """Generate DoS/flooding attack (many requests from same IP)"""
    print(f"[GEN] Generating DoS attack ({count} rapid requests from same IP)...")
    
    attacker_ip = "192.168.1.666"  # Suspicious IP
    target_path = "/health"
    
    for i in range(count):
        write_log_entry(attacker_ip, "GET", target_path)
        if (i + 1) % 10 == 0:
            print(f"  ✓ Sent {i + 1}/{count} requests...")
        time.sleep(0.05)  # Very rapid requests
    
    print(f"  ✓ DoS attack complete: {count} requests from {attacker_ip}")

def main():
    print("=" * 60)
    print("Blue Trinity - Fake Attack Log Generator")
    print("=" * 60)
    print(f"Writing to: {ACCESS_LOG_PATH}")
    print("\nThis will append fake attack log entries to access.log")
    print("Make sure Blue Trinity agent is running to detect them!")
    print("=" * 60)
    
    if not os.path.exists(GREEN_TRINITY_LOCAL_PATH):
        print(f"\n[ERROR] Path not found: {GREEN_TRINITY_LOCAL_PATH}")
        print("Please update GREEN_TRINITY_LOCAL_PATH in this script.")
        return
    
    # Ensure access.log exists
    if not os.path.exists(ACCESS_LOG_PATH):
        print(f"\n[INFO] Creating access.log at {ACCESS_LOG_PATH}")
        with open(ACCESS_LOG_PATH, 'w', encoding='utf-8') as f:
            f.write("# Access log for Blue Trinity SOC monitoring\n")
    
    input("\nPress ENTER to generate attacks...")
    
    # Generate attacks
    generate_normal_traffic(5)
    time.sleep(1)
    
    generate_sql_injection_attacks(5)
    time.sleep(1)
    
    generate_xss_attacks(5)
    time.sleep(1)
    
    generate_dos_attack(60)
    
    print("\n" + "=" * 60)
    print("Attack generation complete!")
    print("=" * 60)
    print("\nCheck Blue Trinity console for [ALERT] messages:")
    print("  - SQL Injection Attempt Detected")
    print("  - XSS Attempt Detected")
    print("  - DoS/Flooding Attempt")
    print("\nView SOC Dashboard: http://127.0.0.1:8000")

if __name__ == '__main__':
    main()

