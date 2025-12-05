"""Simple smoke test for the Trinity C2 service.

Run this script while the service is running locally (DRY_RUN is recommended).
It will enqueue a small nmap scan against `127.0.0.1` and poll for status.
"""
import time
import requests

BASE = 'http://127.0.0.1:8000'

def enqueue_scan():
    resp = requests.post(f'{BASE}/api/scan', json={'target': '127.0.0.1', 'type': 'nmap'})
    resp.raise_for_status()
    return resp.json()

def poll_status(scan_id):
    url = f'{BASE}/api/scan/{scan_id}'
    for _ in range(30):
        r = requests.get(url)
        if r.status_code == 200:
            data = r.json()
            print('Status:', data.get('status'))
            if data.get('status') in ('completed', 'failed'):
                print('Output:\n', data.get('output')[:2000])
                return
        else:
            print('Scan not found yet, status code', r.status_code)
        time.sleep(2)
    print('Timed out waiting for scan to finish')

def main():
    res = enqueue_scan()
    print('Enqueued:', res)
    scan_id = res.get('scan_id')
    if scan_id:
        poll_status(scan_id)

if __name__ == '__main__':
    main()
