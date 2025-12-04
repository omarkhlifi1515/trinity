# Android App Connection Fix Guide

## 🔧 Step-by-Step Connection Setup

### Step 1: Start Backend Server

On your **Parrot OS workstation** (or Windows if running locally):

```bash
cd red_trinity/parrot_backend
python3 server.py
```

**Expected output:**
```
============================================================
Starting Red Trinity Backend Server...
============================================================
Server will run on 0.0.0.0:8888
Health endpoint: http://0.0.0.0:8888/api/health
...
```

### Step 2: Get Workstation IP Address

**On Linux/Parrot OS:**
```bash
hostname -I
# or
ip addr show | grep "inet "
```

**On Windows:**
```powershell
ipconfig | findstr IPv4
```

**Copy the IP address** (e.g., `192.168.1.100`)

### Step 3: Check Firewall

**On Linux:**
```bash
sudo ufw allow 8888
sudo ufw status
```

**On Windows:**
- Open Windows Defender Firewall
- Allow port 8888 for Python

### Step 4: Test from Computer First

**Test if server is accessible:**

**On Windows (PowerShell):**
```powershell
Invoke-WebRequest -Uri "http://YOUR_IP:8888/api/health"
```

**On Linux:**
```bash
curl http://YOUR_IP:8888/api/health
```

**Expected response:**
```json
{
  "status": "online",
  "timestamp": "...",
  "system": "Parrot OS",
  "version": "1.0.0"
}
```

### Step 5: Configure Android App

1. **Open Red Trinity app**
2. **Go to Settings tab** (bottom navigation)
3. **Enter IP address:**
   - Example: `192.168.1.100`
   - **NOT** `http://192.168.1.100` (no http://)
   - **NOT** `192.168.1.100:8888` (no port in IP field)
4. **Enter Port:** `8888`
5. **Tap "TEST CONNECTION"**

### Step 6: Troubleshoot Connection Errors

#### Error: "Cannot resolve host"

**Cause:** Wrong IP address or hostname

**Fix:**
- Double-check IP address
- Make sure no spaces before/after
- Try `ping YOUR_IP` from phone's network

#### Error: "Cannot connect"

**Cause:** Server not running or network issue

**Fix:**
1. ✅ Verify server is running: `ps aux | grep server.py`
2. ✅ Check IP is correct
3. ✅ Ensure phone and workstation on **same network**
4. ✅ Check firewall: `sudo ufw status`
5. ✅ Try from computer browser: `http://YOUR_IP:8888/api/health`

#### Error: "Connection timeout"

**Cause:** Firewall blocking or server not responding

**Fix:**
1. **Allow port in firewall:**
   ```bash
   sudo ufw allow 8888
   ```
2. **Check server logs** for errors
3. **Verify server is listening:**
   ```bash
   netstat -tuln | grep 8888
   ```

#### Error: "Server error" (500)

**Cause:** Backend error

**Fix:**
1. Check server logs: `tail -f red_trinity.log`
2. Verify Python dependencies installed
3. Check for errors in server output

### Step 7: Verify Connection Works

**After successful connection test:**

1. **Go to Attacks tab**
   - Should load list of attacks
   - If empty, check server logs

2. **Go to Dashboard tab**
   - Should show stats (may be 0 if no attacks yet)
   - Connection indicator should be green

3. **Execute a test attack:**
   - Go to Attacks tab
   - Tap "Quick Nmap Scan"
   - Enter target: `127.0.0.1` (localhost)
   - Execute
   - Check Dashboard updates

## 🔍 Advanced Debugging

### Check Network Connectivity

**From Android phone (using ADB or terminal app):**
```bash
ping YOUR_WORKSTATION_IP
```

**From workstation:**
```bash
# Check if port is open
nc -zv YOUR_WORKSTATION_IP 8888
```

### Check Server Logs

**Watch server output:**
```bash
# Server should show incoming requests
# Look for lines like:
# 127.0.0.1 - - [04/Dec/2025 16:30:45] "GET /api/health HTTP/1.1" 200 -
```

### Test Health Endpoint Manually

**From Android phone browser:**
```
http://YOUR_IP:8888/api/health
```

Should show JSON response.

### Common Issues

#### Issue: "Network is unreachable"

**Solution:**
- Phone and workstation must be on same WiFi network
- Check WiFi settings on both devices

#### Issue: "Connection refused"

**Solution:**
- Server not running
- Wrong port number
- Firewall blocking

#### Issue: Works on computer but not phone

**Solution:**
- Different networks (phone on mobile data?)
- Firewall only allows localhost
- Check `host='0.0.0.0'` in server.py (should be 0.0.0.0, not 127.0.0.1)

## ✅ Success Checklist

- [ ] Backend server starts without errors
- [ ] Server shows "Running on 0.0.0.0:8888"
- [ ] Can access `/api/health` from computer browser
- [ ] Phone and workstation on same network
- [ ] Firewall allows port 8888
- [ ] Android app Settings shows correct IP
- [ ] "TEST CONNECTION" shows "✓ Connected!"
- [ ] Attacks tab loads attack list
- [ ] Dashboard shows connection indicator (green)

## 🚀 Quick Test

1. **Start server:** `python3 server.py`
2. **Get IP:** `hostname -I`
3. **Test from computer:** `curl http://IP:8888/api/health`
4. **Configure Android:** Settings → IP → Port → Test
5. **Should work!** ✅

If still not working, check the detailed error message in the Android app - it now shows specific error types!

