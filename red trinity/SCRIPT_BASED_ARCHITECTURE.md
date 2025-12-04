# Red Trinity - Script-Based Architecture

## 🎯 Overview

Red Trinity has been restructured to use **scripts** instead of direct tool execution. When you click a button in the Android app, it triggers a script that performs the attack.

## 📁 Structure

```
red trinity/
├── parrot_backend/
│   ├── server.py          # Simple Flask server
│   ├── scripts/           # Attack scripts directory
│   │   ├── nmap_quick.sh
│   │   ├── nmap_full.sh
│   │   ├── zap_quick.sh
│   │   ├── sqlmap_basic.sh
│   │   └── ... (more scripts)
│   └── requirements.txt
└── android_app/
    └── ... (Android app)
```

## 🚀 How It Works

1. **Android App** → Sends attack request to server
2. **Server** → Finds script for that attack
3. **Script** → Executes the penetration testing tool
4. **Results** → Returned to Android app

## 📝 Available Scripts

### Network Scans
- `nmap_quick.sh` - Quick Nmap scan
- `nmap_full.sh` - Full port scan
- `nmap_os.sh` - OS detection
- `nmap_vuln.sh` - Vulnerability scan

### Web Attacks
- `zap_quick.sh` - OWASP ZAP quick scan
- `zap_full.sh` - OWASP ZAP full scan
- `zap_spider.sh` - OWASP ZAP spider
- `sqlmap_basic.sh` - SQL injection test
- `nikto_scan.sh` - Nikto web scan
- `dirb_scan.sh` - Directory brute force
- `gobuster_scan.sh` - Gobuster directory scan

### Reconnaissance
- `whois_lookup.sh` - Whois lookup
- `dig_scan.sh` - DNS enumeration

## 🔧 Setup

### Step 1: Make Scripts Executable

```bash
cd "red trinity/parrot_backend"
bash make_scripts_executable.sh
```

Or manually:
```bash
chmod +x scripts/*.sh
```

### Step 2: Start Server

```bash
cd "red trinity/parrot_backend"
python3 server.py
```

### Step 3: Use from Android App

1. Open Android app
2. Go to **Attacks** tab
3. Select attack category
4. Click attack button
5. Enter target (e.g., `192.168.1.100` or `http://example.com`)
6. Execute

## ➕ Adding New Scripts

### Step 1: Create Script

Create a new `.sh` file in `scripts/` directory:

```bash
#!/bin/bash
# My Custom Attack Script
# Usage: ./my_attack.sh TARGET

TARGET="$1"

if [ -z "$TARGET" ]; then
    echo "Error: Target not specified"
    exit 1
fi

echo "Starting My Attack on $TARGET..."
# Your attack commands here

exit $?
```

### Step 2: Make Executable

```bash
chmod +x scripts/my_attack.sh
```

### Step 3: Add to Attack Library

Edit `server.py`, add to `ATTACK_LIBRARY`:

```python
'my_category': [
    {'id': 'my_attack', 'name': 'My Attack', 'script': 'my_attack.sh', 'description': 'My attack description'},
]
```

### Step 4: Restart Server

```bash
python3 server.py
```

## 🔍 Script Format

All scripts follow this format:

```bash
#!/bin/bash
# Script Name
# Usage: ./script_name.sh TARGET

TARGET="$1"

if [ -z "$TARGET" ]; then
    echo "Error: Target not specified"
    exit 1
fi

echo "Starting attack on $TARGET..."
# Attack commands here

exit $?
```

## 📊 API Endpoints

### List Attacks
```
GET /api/attacks/list
Authorization: Bearer SECRET_KEY
```

### Execute Attack
```
POST /api/attacks/execute
Authorization: Bearer SECRET_KEY
Content-Type: application/json

{
    "attack_id": "nmap_quick",
    "category": "network_scans",
    "target": "192.168.1.100"
}
```

### Dashboard Stats
```
GET /api/dashboard/stats
Authorization: Bearer SECRET_KEY
```

## ✅ Benefits

- ✅ **Simple** - Just scripts, no complex tool execution logic
- ✅ **Maintainable** - Easy to add new attacks
- ✅ **Portable** - Scripts work on any Linux system
- ✅ **Testable** - Can test scripts independently
- ✅ **Flexible** - Easy to modify attack behavior

## 🎉 Ready to Use!

Red Trinity is now a simple script-based server. Just:
1. Start the server
2. Use from Android app
3. Scripts handle everything!

