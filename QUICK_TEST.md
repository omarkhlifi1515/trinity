# Quick Test Commands

## 🟢 Green Trinity

```powershell
cd "green trinity local"
.\.venv\Scripts\Activate.ps1
python app.py
# Open: http://localhost:5000
# Login: admin / admin
```

## 🔵 Blue Trinity

```powershell
cd "blue trinity"
pip install watchdog

# Test Manager
python blue_trinity_agent.py manager --once

# Test Monitor
python blue_trinity_agent.py monitor

# Test Dashboard
python blue_trinity_agent.py serve --port 8000
# Open: http://localhost:8000

# Run Everything
python blue_trinity_agent.py run --interval 3600
```

## 🔴 Red Trinity

```powershell
cd "red trinity\parrot_backend"
python server.py
# Server runs on: http://0.0.0.0:8888

# Test Health
Invoke-WebRequest http://localhost:8888/api/health

# Test Attack (PowerShell)
$headers = @{"Authorization" = "Bearer red-trinity-secret-key-change-in-production"}
$body = @{attack_id="nmap_scan"; category="network_scans"; target="127.0.0.1"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8888/api/attacks/execute" -Method POST -Headers $headers -Body $body -ContentType "application/json"
```

## 📱 Android App

1. Get Windows PC IP: `ipconfig | findstr IPv4`
2. Open app → Settings
3. Enter IP and port 8888
4. Test connection
5. Execute attacks!

