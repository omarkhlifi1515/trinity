# Quick Network Access Setup

## 🚀 3 Steps to Access from Phone

### Step 1: Allow Firewall (Run as Administrator)

```powershell
cd "red trinity\parrot_backend"
powershell -ExecutionPolicy Bypass -File .\allow_firewall.ps1
```

**OR manually:**
```powershell
netsh advfirewall firewall add rule name="Red Trinity Server" dir=in action=allow protocol=TCP localport=8888
```

### Step 2: Start Server

```powershell
python server.py
```

**Look for:**
```
ACCESS FROM OTHER DEVICES (Same Network):
  Web Dashboard: http://192.168.1.50:8888
```

### Step 3: Open on Phone

1. Make sure phone is on **same WiFi**
2. Open browser
3. Go to: `http://192.168.1.50:8888` (use IP from Step 2)
4. **Done!** 🎉

## 📱 That's It!

You can now launch attacks from your phone browser!

