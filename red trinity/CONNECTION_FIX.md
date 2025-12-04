# Red Trinity Connection Fix Guide

## ✅ What's Been Fixed

### 1. Backend CORS Configuration
- ✅ Updated CORS to allow all origins for development
- ✅ Health endpoint now accessible without auth
- ✅ Proper error handling

### 2. Android App Connection
- ✅ Fixed Retrofit base URL handling (ensures trailing slash)
- ✅ Better error messages in all fragments
- ✅ Connection status indicator in dashboard
- ✅ Proper UI thread handling for all network calls

### 3. Blue Trinity ↔ Green Trinity Sync
- ✅ Blue Trinity reads from same database as Green Trinity Local
- ✅ Path: `green trinity local/local_data.db`
- ✅ Real-time presence tracking from `presence` table
- ✅ HR status from `employee` table

## 🔧 How to Test Connection

### Step 1: Start Backend Server

On your Parrot OS workstation:

```bash
cd red_trinity/parrot_backend
pip3 install -r requirements.txt
python3 server.py
```

**Expected output:**
```
Starting Red Trinity Backend Server...
Server will run on 0.0.0.0:8888
```

### Step 2: Get Workstation IP

```bash
hostname -I
# or
ip addr show
```

Copy the IP address (e.g., `192.168.1.100`)

### Step 3: Configure Android App

1. **Open app** → Go to **Settings** tab
2. **Enter IP**: Your workstation IP (e.g., `192.168.1.100`)
3. **Enter Port**: `8888`
4. **Tap "TEST CONNECTION"**
   - Should show: "✓ Connected! Status: online"
5. **Tap "SAVE SETTINGS"**

### Step 4: Test Features

**Attacks Tab:**
- Should load list of attacks
- Tap an attack → Enter target → Execute
- Should show success/failure message

**Dashboard Tab:**
- Should show stats (Total, Success, Failed)
- Connection indicator (green dot = connected)
- Recent attacks list
- Auto-refreshes every 5 seconds

## 🐛 Troubleshooting

### "Connection error" in Android app

**Check:**
1. ✅ Backend server is running
2. ✅ IP address is correct (no spaces, correct format)
3. ✅ Port is `8888`
4. ✅ Phone and workstation on same network
5. ✅ Firewall allows port 8888:
   ```bash
   sudo ufw allow 8888
   ```

### "Failed to load attacks" 

**Check:**
1. ✅ Auth token matches in `Config.kt` and `server.py`
2. ✅ Backend logs show request received
3. ✅ Check backend: `tail -f red_trinity.log`

### Dashboard shows 0 attacks

**This is normal if:**
- No attacks executed yet
- Execute an attack from Attacks tab
- Dashboard will update automatically

### Blue Trinity not syncing with Green Trinity

**Check:**
1. ✅ Green Trinity Local is using SQLite (not Postgres)
   - Don't set `POSTGRES_URL` env var
2. ✅ Database file exists: `green trinity local/local_data.db`
3. ✅ Blue Trinity path is correct (line 46 in `blue_trinity_agent.py`)
4. ✅ Both apps can access the database file

## 📱 Android App Features

### Connection Status Indicator
- **Green dot (●)** = Connected
- **Red dot (●)** = Disconnected
- Shows in Dashboard header

### Error Handling
- Clear error messages
- Connection retry on auto-refresh
- Toast notifications for user feedback

### Auto-Refresh
- Dashboard refreshes every 5 seconds
- Shows latest attack stats
- Updates recent attacks list

## 🔗 API Endpoints

All endpoints require auth except `/api/health`:

- `GET /api/health` - No auth (for connection test)
- `GET /api/attacks/list` - List all attacks
- `POST /api/attacks/execute` - Execute attack
- `GET /api/dashboard/stats` - Get dashboard stats

**Auth Header:**
```
Authorization: Bearer red-trinity-secret-key-change-in-production
```

## ✅ Verification Checklist

- [ ] Backend server starts without errors
- [ ] Health endpoint returns `{"status": "online"}`
- [ ] Android app connects successfully
- [ ] Attacks list loads
- [ ] Dashboard shows stats
- [ ] Attacks can be executed
- [ ] Dashboard updates after attack
- [ ] Blue Trinity reads from Green Trinity database

## 🎯 Next Steps

1. **Test connection** from Android app
2. **Execute a test attack** (e.g., Quick Nmap Scan)
3. **Check dashboard** updates automatically
4. **Verify Blue Trinity** reads same data as Green Trinity

All fixed and ready to use! 🚀

