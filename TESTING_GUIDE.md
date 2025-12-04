# Complete Testing Guide - Trinity Project

This guide covers testing all three Trinity applications: Green Trinity (HR Portal), Blue Trinity (SOC Agent), and Red Trinity (Attack Platform).

---

## 🟢 Green Trinity Local - HR Portal Testing

### Step 1: Start Green Trinity Local

```powershell
cd "green trinity local"
.\.venv\Scripts\Activate.ps1
python app.py
```

**Expected Output:**
```
 * Running on http://127.0.0.1:5000
```

### Step 2: Access Web Interface

1. **Open browser:** `http://localhost:5000`
2. **Login as Admin:**
   - Username: `admin`
   - Password: `admin`

### Step 3: Test HR Features

#### Test 1: Add Employees
1. Go to **HR Dashboard** or **Manage Employees**
2. Click **Add Employee**
3. Fill in:
   - Name: `John Doe`
   - Status: `Active`
   - Department: `IT`
4. Click **Save**
5. **Verify:** Employee appears in list

#### Test 2: Mark Presence
1. **Logout** from admin
2. **Login as regular user** (or create one)
3. Go to **Dashboard**
4. Click **"Mark as Present"** button
5. **Verify:** Message shows "You have been marked present for today!"

#### Test 3: Check Database
```powershell
# Check if database was created
Test-Path "green trinity local\local_data.db"
# Should return: True

# Check database size
(Get-Item "green trinity local\local_data.db").Length
# Should show file size > 0
```

### Step 4: Verify Database Sync

1. **Check database exists:** `green trinity local\local_data.db`
2. **Verify tables:** Should have `employee`, `user`, `presence` tables
3. **Check data:** Employees and presence records should be visible

---

## 🔵 Blue Trinity - SOC Agent Testing

### Step 1: Install Dependencies

```powershell
cd "blue trinity"
pip install watchdog
```

### Step 2: Test Manager (HR Data Analysis)

```powershell
python blue_trinity_agent.py manager --once
```

**Expected Output:**
```
[Config] 📁 Database path: C:\Users\msi\Documents\GitHub\trinity\green trinity local\local_data.db
[Config] 📁 Database exists: True
[Config] 📊 Database size: XXXXX bytes
[Manager] 📊 Reading database: ...
[Manager] 📋 Found tables: employee, user, role, presence, ...
[Manager] ✅ Using table: employee
[Manager] ✅ Health Summary:
[Manager]    📊 Active Personnel: X
[Manager]    ⚠️  Absent Personnel: Y
[Manager]    ✅ Present Today: Z
[Manager]    📄 Reports Generated: N
```

**Verify:**
- ✅ Shows correct Active/Absent counts
- ✅ Shows Present Today count (only those who marked presence)
- ✅ Generates warning letters for absent employees
- ✅ Reports saved in `blue_trinity_reports/` folder

### Step 3: Test Log Monitor (Security Detection)

#### Option A: Generate Test Attacks

```powershell
cd "blue trinity"
python generate_fake_attacks.py
```

This creates fake attack entries in `access.log`

#### Option B: Start Monitor

```powershell
python blue_trinity_agent.py monitor
```

**Expected Output:**
```
[Monitor] Watching: C:\Users\msi\Documents\GitHub\trinity\green trinity local\access.log
[Monitor] ✅ Log file ready
```

**Test Attacks:**
1. **SQL Injection:** Should detect patterns like `' OR '1'='1`
2. **XSS Attack:** Should detect `<script>` tags
3. **DoS Attack:** Should detect rapid requests from same IP

**Verify:**
- ✅ Alerts appear in console
- ✅ IPs added to `blacklist.txt`
- ✅ Blacklist file contains JSON entries

### Step 4: Test SOC Dashboard

```powershell
python blue_trinity_agent.py serve --port 8000
```

**Access Dashboard:**
1. **Open browser:** `http://localhost:8000`
2. **Verify:**
   - ✅ Shows 6 stat cards (Present, Active, Absent, Alerts, Blacklisted IPs, Reports)
   - ✅ Threat log table shows recent attacks
   - ✅ Real-time updates (refresh page)
   - ✅ Cyberpunk styling visible

### Step 5: Test Full Integration

```powershell
# Run both manager and monitor together
python blue_trinity_agent.py run --interval 3600
```

**This runs:**
- Manager: Checks HR data every hour
- Monitor: Watches access.log in real-time
- Dashboard: Available on port 8000

---

## 🔴 Red Trinity - Attack Platform Testing

### Step 1: Start Windows Attack Server

```powershell
cd "red trinity\parrot_backend"
python server.py
```

**Expected Output:**
```
============================================================
Red Trinity - Windows Attack Server
============================================================
Server running on 0.0.0.0:8888
Scripts directory: C:\Users\msi\Documents\GitHub\trinity\red trinity\parrot_backend\scripts
Platform: Windows

Available attack categories:
  - network_scans: 4 attacks
  - reconnaissance: 2 attacks
  - web_attacks: 1 attacks
  - windows_attacks: 4 attacks
============================================================
 * Running on http://0.0.0.0:8888
```

### Step 2: Test Server Health

```powershell
# Test health endpoint
Invoke-WebRequest -Uri "http://localhost:8888/api/health"
```

**Expected Response:**
```json
{
  "status": "online",
  "timestamp": "2025-12-04T...",
  "system": "Windows",
  "version": "2.0.0",
  "type": "windows-script-based"
}
```

### Step 3: Test Attack Scripts Manually

#### Test Nmap Script
```powershell
cd "red trinity\parrot_backend\scripts"
powershell -ExecutionPolicy Bypass -File .\nmap_scan.ps1 127.0.0.1
```

#### Test Ping Sweep
```powershell
powershell -ExecutionPolicy Bypass -File .\ping_sweep.ps1 192.168.1
```

#### Test DNS Lookup
```powershell
powershell -ExecutionPolicy Bypass -File .\dns_lookup.ps1 google.com
```

**Verify:**
- ✅ Scripts execute without errors
- ✅ Output shows results
- ✅ Exit code is 0 for success

### Step 4: Test API Endpoints

#### Test List Attacks
```powershell
$headers = @{
    "Authorization" = "Bearer red-trinity-secret-key-change-in-production"
}
Invoke-RestMethod -Uri "http://localhost:8888/api/attacks/list" -Headers $headers
```

**Expected:** JSON with all attack categories and attacks

#### Test Execute Attack
```powershell
$body = @{
    attack_id = "nmap_scan"
    category = "network_scans"
    target = "127.0.0.1"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8888/api/attacks/execute" `
    -Method POST `
    -Headers $headers `
    -Body $body `
    -ContentType "application/json"
```

**Expected:** JSON response with `success: true` and output

#### Test Dashboard Stats
```powershell
Invoke-RestMethod -Uri "http://localhost:8888/api/dashboard/stats" -Headers $headers
```

**Expected:** JSON with attack statistics

### Step 5: Test Android App

#### Step 5.1: Get Server IP

```powershell
ipconfig | findstr IPv4
# Example output: IPv4 Address. . . . . . . . . . . : 192.168.1.50
```

#### Step 5.2: Configure Android App

1. **Open Red Trinity Android app**
2. **Go to Settings tab**
3. **Enter:**
   - Workstation IP: `192.168.1.50` (your Windows PC IP)
   - Port: `8888`
4. **Tap "TEST CONNECTION"**
5. **Verify:** Shows "✓ Connected!"

#### Step 5.3: Test Attacks from App

1. **Go to Attacks tab**
2. **Select category:** Network Scans
3. **Tap:** "Nmap Network Scan"
4. **Enter target:** `127.0.0.1`
5. **Tap:** "Execute"
6. **Verify:**
   - ✅ Shows "Attack executed successfully"
   - ✅ Dashboard updates with stats

#### Step 5.4: Test Dashboard

1. **Go to Dashboard tab**
2. **Verify:**
   - ✅ Shows total attacks count
   - ✅ Shows successful/failed counts
   - ✅ Shows recent attacks list
   - ✅ Connection indicator is green
   - ✅ Auto-refreshes every 5 seconds

#### Step 5.5: Test Different Attack Types

**Network Scans:**
- Nmap Network Scan → `127.0.0.1`
- Ping Sweep → `192.168.1`
- Port Scanner → `127.0.0.1`

**Reconnaissance:**
- DNS Lookup → `google.com`
- Whois Lookup → `example.com`

**Web Attacks:**
- HTTP Enumeration → `http://example.com`

**Windows Attacks:**
- SMB Enumeration → `192.168.1.100`
- RDP Check → `192.168.1.100`
- Registry Scan → (no target needed)

---

## 🔄 Integration Testing

### Test 1: Green ↔ Blue Trinity Sync

1. **Start Green Trinity:**
   ```powershell
   cd "green trinity local"
   python app.py
   ```

2. **Add employees** via web interface

3. **Mark presence** as a user

4. **Run Blue Trinity:**
   ```powershell
   cd "blue trinity"
   python blue_trinity_agent.py manager --once
   ```

5. **Verify:**
   - ✅ Blue Trinity shows correct Active/Absent counts
   - ✅ Present Today matches users who marked presence
   - ✅ Warning letters generated for absent employees

### Test 2: Blue Trinity Log Monitoring

1. **Start Green Trinity** (generates access.log)

2. **Start Blue Trinity Monitor:**
   ```powershell
   cd "blue trinity"
   python blue_trinity_agent.py monitor
   ```

3. **Generate test attacks:**
   ```powershell
   python generate_fake_attacks.py
   ```

4. **Verify:**
   - ✅ Blue Trinity detects attacks
   - ✅ Alerts appear in console
   - ✅ IPs added to blacklist.txt
   - ✅ Dashboard shows alerts

### Test 3: Red Trinity Full Flow

1. **Start Red Trinity Server:**
   ```powershell
   cd "red trinity\parrot_backend"
   python server.py
   ```

2. **Connect Android App:**
   - Configure IP and port
   - Test connection

3. **Execute Multiple Attacks:**
   - Run 3-4 different attacks
   - Check dashboard stats

4. **Verify:**
   - ✅ All attacks execute
   - ✅ Stats update correctly
   - ✅ Recent attacks list populated
   - ✅ Success/failure counts accurate

---

## ✅ Quick Test Checklist

### Green Trinity
- [ ] Server starts on port 5000
- [ ] Can login as admin
- [ ] Can add employees
- [ ] Can mark presence
- [ ] Database created (`local_data.db`)
- [ ] Tables exist (employee, user, presence)

### Blue Trinity
- [ ] Manager reads database correctly
- [ ] Shows correct Active/Absent/Present counts
- [ ] Generates warning letters
- [ ] Monitor detects SQL injection
- [ ] Monitor detects XSS attacks
- [ ] Monitor detects DoS attacks
- [ ] Dashboard accessible on port 8000
- [ ] Dashboard shows all 6 stat cards
- [ ] Threat log displays attacks

### Red Trinity
- [ ] Server starts on port 8888
- [ ] Health endpoint works
- [ ] Can list attacks via API
- [ ] Can execute attacks via API
- [ ] Scripts execute successfully
- [ ] Android app connects
- [ ] Can execute attacks from app
- [ ] Dashboard shows stats
- [ ] Recent attacks list works

---

## 🐛 Troubleshooting

### Green Trinity Issues

**Database not created:**
- Check if app started successfully
- Verify write permissions in folder
- Check console for errors

**Can't login:**
- Default credentials: `admin` / `admin`
- Check if user table exists in database

### Blue Trinity Issues

**Shows 0 for all counts:**
- Verify database path is correct
- Check if Green Trinity is using SQLite (not Postgres)
- Ensure employees exist in database

**Monitor not detecting attacks:**
- Check if `access.log` exists
- Verify log file path
- Check file permissions

**Dashboard not loading:**
- Verify port 8000 is not in use
- Check firewall settings
- Try different port: `--port 8001`

### Red Trinity Issues

**Scripts not executing:**
- Check PowerShell execution policy: `Get-ExecutionPolicy`
- Set policy: `Set-ExecutionPolicy RemoteSigned -Scope CurrentUser`
- Verify scripts exist in `scripts/` folder

**Android app can't connect:**
- Verify server is running
- Check IP address is correct
- Ensure phone and PC on same network
- Check Windows Firewall allows port 8888

**Attacks fail:**
- Check if required tools installed (e.g., nmap)
- Verify target is accessible
- Check script output for errors

---

## 📊 Expected Results Summary

| Component | Test | Expected Result |
|-----------|------|----------------|
| **Green Trinity** | Add Employee | Employee appears in list |
| **Green Trinity** | Mark Presence | Presence record created |
| **Blue Trinity** | Manager Run | Shows Active/Absent/Present counts |
| **Blue Trinity** | Monitor | Detects attacks, adds to blacklist |
| **Blue Trinity** | Dashboard | Shows 6 stat cards + threat log |
| **Red Trinity** | Server Start | Runs on port 8888 |
| **Red Trinity** | Execute Attack | Returns success + output |
| **Red Trinity** | Android App | Connects and executes attacks |

---

## 🎉 Success Criteria

All apps are working correctly if:

1. ✅ **Green Trinity:** Can manage employees and track presence
2. ✅ **Blue Trinity:** Monitors security and shows dashboard
3. ✅ **Red Trinity:** Executes Windows attacks from Android app
4. ✅ **Integration:** Blue Trinity reads Green Trinity data
5. ✅ **All components:** Start without errors and respond to requests

---

## 🚀 Quick Start Testing

**Fastest way to test everything:**

```powershell
# Terminal 1: Green Trinity
cd "green trinity local"
python app.py

# Terminal 2: Blue Trinity
cd "blue trinity"
python blue_trinity_agent.py run --interval 3600

# Terminal 3: Red Trinity
cd "red trinity\parrot_backend"
python server.py

# Then:
# - Open http://localhost:5000 (Green Trinity)
# - Open http://localhost:8000 (Blue Trinity Dashboard)
# - Use Android app to connect to Red Trinity
```

All three apps should now be running and testable! 🎯

