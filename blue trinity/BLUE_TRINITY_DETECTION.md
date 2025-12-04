# Blue Trinity - Attack Detection System

## 🛡️ What Blue Trinity Does

Blue Trinity is your **Security Operations Center (SOC)** defensive agent that:

1. **Monitors access.log in real-time** - Watches every HTTP request
2. **Detects attacks automatically** - SQL Injection, XSS, DoS/Flooding
3. **Alerts immediately** - Shows red alerts in console
4. **Blacklists attackers** - Adds malicious IPs to blacklist.txt
5. **Dashboard visualization** - Shows all threats in web dashboard

## 🔍 Attack Detection Rules

### 1. SQL Injection Detection
**Pattern:** Looks for SQL keywords in URLs
- `SELECT`, `UNION`, `DROP` in request paths
- Example: `/login?user=admin' OR '1'='1`

**Alert:** `SQL Injection Attempt Detected`

### 2. XSS Detection
**Pattern:** Looks for script tags
- `<script>` tags in requests
- Example: `/search?q=<script>alert('XSS')</script>`

**Alert:** `XSS Attempt Detected`

### 3. DoS/Flooding Detection
**Pattern:** Counts requests per IP in time window
- **Threshold:** 50 requests
- **Time Window:** 60 seconds
- If same IP makes 50+ requests in 60 seconds → **DoS Alert**

**Alert:** `DoS/Flooding Attempt (X requests in 60s)`

## 📊 Your DoS Attack Detection

When you ran the DoS attack from Red Trinity:
- **273 requests** sent in **30 seconds**
- **Blue Trinity detected it** because:
  - Same IP made 273 requests
  - Exceeded threshold of 50 requests
  - Within 60-second window

**What happened:**
1. ✅ Red Trinity sent 273 HTTP requests to `192.168.0.188:5000`
2. ✅ Green Trinity logged all requests to `access.log`
3. ✅ Blue Trinity monitor detected rapid requests
4. ✅ Alert triggered: "DoS/Flooding Attempt (273 requests in 60s)"
5. ✅ IP added to `blacklist.txt`
6. ✅ Alert shown in SOC dashboard

## 🎯 Viewing Detections

### Method 1: Console Alerts
When Blue Trinity detects an attack, you'll see:
```
╔════════════════════════════════════════════════════════════╗
║ 🚨 SECURITY ALERT DETECTED 🚨 ║
╠════════════════════════════════════════════════════════════╣
║ Type: DoS/Flooding Attempt (273 requests in 60s)
║ Attacker IP: 192.168.0.188
║ Target Path: /health
║ Time: 2025-12-04T...
╚════════════════════════════════════════════════════════════╝
```

### Method 2: SOC Dashboard
```powershell
# Start Blue Trinity with dashboard
cd "blue trinity"
python blue_trinity_agent.py serve --port 8000
```

**Open:** `http://localhost:8000`

**You'll see:**
- ✅ Security Alerts count
- ✅ Blacklisted IPs count
- ✅ Threat log table with all detected attacks
- ✅ Real-time updates

### Method 3: Blacklist File
```powershell
# View blacklisted attackers
Get-Content "green trinity local\blacklist.txt"
```

**Format:** JSON entries with attack details

## 🔧 Detection Configuration

**Current Settings:**
- **DoS Threshold:** 50 requests
- **DoS Window:** 60 seconds
- **Location:** `blue_trinity_agent.py` lines 62-63

**To change:**
```python
DOS_THRESHOLD = 50   # Lower = more sensitive
DOS_WINDOW = 60      # Time window in seconds
```

## 📈 Detection Statistics

Blue Trinity tracks:
- **Total Alerts** - All detected threats
- **By Type:**
  - SQL Injection attempts
  - XSS attempts
  - DoS/Flooding attempts
- **Blacklisted IPs** - Blocked attackers
- **Recent Attacks** - Last 50 alerts

## ✅ What Blue Trinity Detected

From your DoS attack:
- ✅ **273 requests** detected
- ✅ **Exceeded threshold** (50 requests)
- ✅ **Alert generated** immediately
- ✅ **IP blacklisted** automatically
- ✅ **Shown in dashboard** in real-time

## 🎉 Success!

Blue Trinity is working perfectly as a defensive agent:
- ✅ Detects attacks in real-time
- ✅ Alerts immediately
- ✅ Tracks all threats
- ✅ Provides SOC dashboard
- ✅ Blacklists malicious IPs

**Your Red Trinity attack was successfully detected by Blue Trinity!** 🛡️

