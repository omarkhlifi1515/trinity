# Red Trinity - Network Access Guide

## 🌐 Access from Phone/Other Devices

Red Trinity web dashboard can be accessed from any device on the same network!

## 🔧 Step 1: Get Your Computer's IP Address

**Windows PowerShell:**
```powershell
ipconfig | findstr IPv4
```

**Example output:**
```
IPv4 Address. . . . . . . . . . . : 192.168.1.50
```

**Copy the IP address** (e.g., `192.168.1.50`)

## 🔥 Step 2: Allow Firewall Access

### Option A: Run PowerShell Script (Easiest)

```powershell
cd "red trinity\parrot_backend"
# Run as Administrator
powershell -ExecutionPolicy Bypass -File .\allow_firewall.ps1
```

### Option B: Manual Firewall Rule

**Run PowerShell as Administrator:**
```powershell
netsh advfirewall firewall add rule name="Red Trinity Server" dir=in action=allow protocol=TCP localport=8888
```

### Option C: Windows Firewall GUI

1. Open **Windows Defender Firewall**
2. Click **Advanced Settings**
3. Click **Inbound Rules** → **New Rule**
4. Select **Port** → **TCP** → **Specific local ports: 8888**
5. Select **Allow the connection**
6. Apply to all profiles
7. Name: "Red Trinity Server"

## 🚀 Step 3: Start Server

```powershell
cd "red trinity\parrot_backend"
python server.py
```

**Look for this output:**
```
ACCESS FROM OTHER DEVICES (Same Network):
  Web Dashboard: http://192.168.1.50:8888
```

## 📱 Step 4: Access from Phone/Other Device

1. **Make sure device is on same WiFi network**
2. **Open browser on phone/device**
3. **Enter:** `http://YOUR_IP:8888`
   - Example: `http://192.168.1.50:8888`
4. **You should see the Red Trinity dashboard!**

## ✅ Testing Network Access

### From Phone Browser:
```
http://192.168.1.50:8888
```

### From Another Computer:
```
http://192.168.1.50:8888
```

### Test Health Endpoint:
```
http://192.168.1.50:8888/api/health
```

## 🔍 Troubleshooting

### Issue: "Can't connect" from phone

**Check 1: Same Network?**
- Phone and computer must be on same WiFi
- Check WiFi network names match

**Check 2: Firewall?**
```powershell
# Verify firewall rule exists
netsh advfirewall firewall show rule name="Red Trinity Server"
```

**Check 3: Server Running?**
- Check server console shows "Running on 0.0.0.0:8888"
- No errors in console

**Check 4: IP Address Correct?**
```powershell
ipconfig | findstr IPv4
```
- Use the IP shown, not `localhost` or `127.0.0.1`

### Issue: "Connection refused"

**Solution:**
1. Check Windows Firewall allows port 8888
2. Verify server is running
3. Try accessing from computer first: `http://localhost:8888`

### Issue: Dashboard loads but attacks don't work

**Solution:**
- Check browser console for errors (F12)
- Verify API endpoints are accessible
- Check server logs for errors

## 📋 Quick Checklist

- [ ] Server started: `python server.py`
- [ ] Firewall rule added (port 8888 allowed)
- [ ] Got IP address: `ipconfig | findstr IPv4`
- [ ] Phone/device on same WiFi network
- [ ] Accessing: `http://YOUR_IP:8888`
- [ ] Dashboard loads successfully

## 🎯 Access URLs

**From Computer:**
- Dashboard: `http://localhost:8888`
- API: `http://localhost:8888/api/*`

**From Phone/Other Device:**
- Dashboard: `http://192.168.1.50:8888` (use your IP)
- API: `http://192.168.1.50:8888/api/*`

## 🎉 Success!

Once configured, you can:
- ✅ Access dashboard from any device on network
- ✅ Launch attacks from phone browser
- ✅ View results in real-time
- ✅ Check stats and recent attacks

**Red Trinity is now accessible from your entire network!** 🚀

