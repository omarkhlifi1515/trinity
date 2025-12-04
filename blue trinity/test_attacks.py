"""
Test script to simulate attacks for Blue Trinity SOC agent testing.

This script makes HTTP requests with malicious payloads to test:
- SQL Injection detection
- XSS detection  
- DoS/Flooding detection

Usage:
    python test_attacks.py --url http://127.0.0.1:5000
"""

import argparse
import requests
import time
import random
from concurrent.futures import ThreadPoolExecutor

# Test payloads
SQL_INJECTION_PAYLOADS = [
    "/login?user=admin' OR '1'='1",
    "/search?id=1 UNION SELECT * FROM users",
    "/api/users?id=1; DROP TABLE users;--",
    "/dashboard?user=admin' OR 1=1--",
    "/auth/login?username=admin' OR '1'='1'--",
]

XSS_PAYLOADS = [
    "/search?q=<script>alert('XSS')</script>",
    "/comment?text=<script>document.cookie</script>",
    "/profile?name=<img src=x onerror=alert('XSS')>",
    "/dashboard?msg=<script>alert(document.domain)</script>",
    "/api/data?callback=<script>alert(1)</script>",
]

NORMAL_REQUESTS = [
    "/",
    "/auth/login",
    "/health",
    "/dashboard-user",
    "/tasks",
    "/chat",
]

def make_request(url, path, attack_type="normal"):
    """Make a single HTTP request"""
    try:
        full_url = f"{url.rstrip('/')}{path}"
        response = requests.get(
            full_url,
            timeout=5,
            headers={
                'User-Agent': f'TestBot-{attack_type}/1.0',
            }
        )
        return {
            'path': path,
            'status': response.status_code,
            'attack_type': attack_type,
            'success': True
        }
    except Exception as e:
        return {
            'path': path,
            'status': 0,
            'attack_type': attack_type,
            'success': False,
            'error': str(e)
        }

def test_sql_injection(url, count=5):
    """Test SQL injection detection"""
    print(f"\n[TEST] SQL Injection Attacks ({count} requests)...")
    print("-" * 60)
    
    for payload in SQL_INJECTION_PAYLOADS[:count]:
        result = make_request(url, payload, "SQLi")
        status_icon = "✓" if result['success'] else "✗"
        print(f"{status_icon} {payload[:50]}... -> Status: {result['status']}")
        time.sleep(0.5)  # Small delay between requests

def test_xss(url, count=5):
    """Test XSS detection"""
    print(f"\n[TEST] XSS Attacks ({count} requests)...")
    print("-" * 60)
    
    for payload in XSS_PAYLOADS[:count]:
        result = make_request(url, payload, "XSS")
        status_icon = "✓" if result['success'] else "✗"
        print(f"{status_icon} {payload[:50]}... -> Status: {result['status']}")
        time.sleep(0.5)

def test_dos_flood(url, count=100, threads=10):
    """Test DoS/Flooding detection by making many rapid requests"""
    print(f"\n[TEST] DoS/Flood Attack ({count} requests, {threads} threads)...")
    print("-" * 60)
    print("Making rapid requests to trigger DoS detection...")
    
    # Use a normal endpoint but flood it
    target_path = "/health"
    
    def flood_request():
        return make_request(url, target_path, "DoS")
    
    start_time = time.time()
    with ThreadPoolExecutor(max_workers=threads) as executor:
        futures = [executor.submit(flood_request) for _ in range(count)]
        results = [f.result() for f in futures]
    
    elapsed = time.time() - start_time
    successful = sum(1 for r in results if r['success'])
    
    print(f"✓ Sent {count} requests in {elapsed:.2f} seconds")
    print(f"✓ Successful: {successful}/{count}")
    print(f"✓ Rate: {count/elapsed:.1f} requests/second")
    print("\n[INFO] Blue Trinity should detect this as DoS if threshold is exceeded!")

def test_normal_traffic(url, count=10):
    """Send some normal traffic for comparison"""
    print(f"\n[TEST] Normal Traffic ({count} requests)...")
    print("-" * 60)
    
    for path in NORMAL_REQUESTS[:count]:
        result = make_request(url, path, "normal")
        status_icon = "✓" if result['success'] else "✗"
        print(f"{status_icon} {path} -> Status: {result['status']}")
        time.sleep(0.3)

def main():
    parser = argparse.ArgumentParser(description='Test Blue Trinity SOC agent with simulated attacks')
    parser.add_argument('--url', default='http://127.0.0.1:5000', help='Target URL (default: http://127.0.0.1:5000)')
    parser.add_argument('--sql-count', type=int, default=5, help='Number of SQL injection tests')
    parser.add_argument('--xss-count', type=int, default=5, help='Number of XSS tests')
    parser.add_argument('--dos-count', type=int, default=100, help='Number of requests for DoS test')
    parser.add_argument('--dos-threads', type=int, default=10, help='Threads for DoS test')
    parser.add_argument('--skip-normal', action='store_true', help='Skip normal traffic test')
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("Blue Trinity SOC Agent - Attack Simulation Test")
    print("=" * 60)
    print(f"Target: {args.url}")
    print(f"Make sure:")
    print("  1. Green Trinity app is running on the target URL")
    print("  2. Blue Trinity agent is running: python blue_trinity_agent.py run")
    print("  3. Watch the Blue Trinity console for ALERT messages!")
    print("=" * 60)
    
    input("\nPress ENTER to start tests...")
    
    # Test normal traffic first
    if not args.skip_normal:
        test_normal_traffic(args.url, 5)
        time.sleep(2)
    
    # Test SQL injection
    test_sql_injection(args.url, args.sql_count)
    time.sleep(2)
    
    # Test XSS
    test_xss(args.url, args.xss_count)
    time.sleep(2)
    
    # Test DoS
    test_dos_flood(args.url, args.dos_count, args.dos_threads)
    
    print("\n" + "=" * 60)
    print("Tests completed!")
    print("=" * 60)
    print("\nCheck:")
    print("  1. Blue Trinity console for [ALERT] messages")
    print("  2. blacklist.txt file in 'green trinity local' folder")
    print("  3. SOC Dashboard at http://127.0.0.1:8000")
    print("\nExpected alerts:")
    print("  - SQL Injection Attempt Detected")
    print("  - XSS Attempt Detected")
    print("  - DoS/Flooding Attempt (if threshold exceeded)")

if __name__ == '__main__':
    main()

