#!/usr/bin/env python3
"""
Quick test script to verify Red Trinity backend is accessible
"""

import requests
import sys

def test_connection(base_url="http://127.0.0.1:8888"):
    """Test connection to Red Trinity backend"""
    print(f"Testing connection to: {base_url}")
    print("-" * 60)
    
    try:
        # Test health endpoint
        print("1. Testing /api/health endpoint...")
        response = requests.get(f"{base_url}/api/health", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print(f"   ✓ Health check passed!")
            print(f"   Status: {data.get('status')}")
            print(f"   System: {data.get('system')}")
        else:
            print(f"   ✗ Health check failed: {response.status_code}")
            return False
        
        # Test attacks list (requires auth)
        print("\n2. Testing /api/attacks/list endpoint (with auth)...")
        headers = {
            "Authorization": "Bearer red-trinity-secret-key-change-in-production"
        }
        response = requests.get(f"{base_url}/api/attacks/list", headers=headers, timeout=5)
        if response.status_code == 200:
            data = response.json()
            total = data.get('total', 0)
            print(f"   ✓ Attacks list loaded!")
            print(f"   Total attacks available: {total}")
        else:
            print(f"   ✗ Attacks list failed: {response.status_code} {response.text}")
            return False
        
        print("\n" + "=" * 60)
        print("✓ All tests passed! Backend is ready for Android app.")
        print("=" * 60)
        return True
        
    except requests.exceptions.ConnectionError:
        print("✗ Connection Error: Cannot connect to server")
        print("  • Is the server running? (python3 server.py)")
        print("  • Check IP address and port")
        return False
    except requests.exceptions.Timeout:
        print("✗ Timeout: Server not responding")
        print("  • Check if server is running")
        return False
    except Exception as e:
        print(f"✗ Error: {e}")
        return False

if __name__ == '__main__':
    # Allow custom URL
    url = sys.argv[1] if len(sys.argv) > 1 else "http://127.0.0.1:8888"
    success = test_connection(url)
    sys.exit(0 if success else 1)

