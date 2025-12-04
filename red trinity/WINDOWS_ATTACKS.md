# Red Trinity - Windows Attack Scripts

## 🪟 Windows-Only Attack Server

Red Trinity is now a **Windows-based attack server** that uses PowerShell scripts for all attacks.

## 📁 Structure

```
red trinity/parrot_backend/
├── server.py              # Flask server (executes PowerShell scripts)
├── scripts/               # PowerShell attack scripts (.ps1)
│   ├── nmap_scan.ps1
│   ├── ping_sweep.ps1
│   ├── port_scan.ps1
│   ├── dns_lookup.ps1
│   ├── smb_enum.ps1
│   ├── rdp_check.ps1
│   └── ... (more Windows scripts)
└── requirements.txt
```

## 🚀 Available Windows Attacks

### Network Scans
- **Nmap Network Scan** - Quick network scan using Nmap
- **Nmap Full Port Scan** - Scan all 65535 ports
- **Ping Sweep** - Discover active hosts on network
- **Port Scanner** - Scan common ports using PowerShell

### Reconnaissance
- **DNS Lookup** - DNS record enumeration (A, MX, NS, TXT)
- **Whois Lookup** - Domain information lookup

### Web Attacks
- **HTTP Enumeration** - Enumerate HTTP services and common paths

### Windows-Specific Attacks
- **SMB Enumeration** - Enumerate SMB shares
- **RDP Check** - Check RDP accessibility
- **Service Enumeration** - Enumerate Windows services
- **Registry Scan** - Scan Windows registry (local machine only)

## 🔧 Setup

### Step 1: Install Python Dependencies

```powershell
cd "red trinity\parrot_backend"
pip install -r requirements.txt
```

### Step 2: Set Execution Policy (if needed)

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Step 3: Start Server

```powershell
python server.py
```

### Step 4: Use from Android App

1. Open Android app
2. Go to **Attacks** tab
3. Select attack category
4. Click attack button
5. Enter target (e.g., `192.168.1.100` or `example.com`)
6. Execute

## 📝 Script Format

All scripts are PowerShell (.ps1) files:

```powershell
# Script Name
# Usage: .\script_name.ps1 TARGET

param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

Write-Host "Starting attack on $Target..." -ForegroundColor Green

# Attack commands here

exit $LASTEXITCODE
```

## ➕ Adding New Windows Attacks

### Step 1: Create PowerShell Script

Create `scripts/my_attack.ps1`:

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$Target
)

Write-Host "Attacking $Target..." -ForegroundColor Green
# Your Windows attack commands here
exit 0
```

### Step 2: Add to Attack Library

Edit `server.py`, add to `ATTACK_LIBRARY`:

```python
'my_category': [
    {'id': 'my_attack', 'name': 'My Attack', 'script': 'my_attack.ps1', 'description': 'Description'},
]
```

### Step 3: Restart Server

```powershell
python server.py
```

## 🎯 Windows-Specific Features

- **PowerShell Native** - Uses built-in Windows PowerShell cmdlets
- **WMI Integration** - Windows Management Instrumentation support
- **Registry Access** - Local registry scanning
- **Service Enumeration** - Windows service discovery
- **SMB/RDP Checks** - Windows-specific protocol testing

## ⚠️ Requirements

- **Windows OS** - Server must run on Windows
- **PowerShell** - Built into Windows (5.1+)
- **Nmap** (optional) - For advanced network scanning
- **Admin Rights** (some attacks) - For registry/service enumeration

## ✅ Benefits

- ✅ **Windows Native** - Uses Windows PowerShell
- ✅ **No Linux Dependencies** - Pure Windows attacks
- ✅ **Easy to Extend** - Just add PowerShell scripts
- ✅ **Built-in Tools** - Uses Windows built-in commands

## 🎉 Ready to Use!

Red Trinity is now a **Windows-only attack server**. All attacks use PowerShell scripts!

