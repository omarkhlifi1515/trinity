# Red Trinity - Quick Start Guide

## 🚀 Quick Setup (5 minutes)

### On Parrot OS Workstation:

1. **Navigate to backend folder:**
   ```bash
   cd red_trinity/parrot_backend
   ```

2. **Install dependencies:**
   ```bash
   pip3 install -r requirements.txt
   ```

3. **Get your IP address:**
   ```bash
   hostname -I
   ```
   Copy this IP address - you'll need it for the Android app!

4. **Start the server:**
   ```bash
   python3 server.py
   ```
   Server will run on port 8888

### On Android Device:

1. **Open Android Studio:**
   - Open `red_trinity/android_app/` as a project

2. **Configure IP address:**
   - Edit `app/src/main/java/com/redtrinity/pentest/Config.kt`
   - Replace `YOUR_PARROT_OS_IP` with the IP from step 3 above
   ```kotlin
   const val SERVER_BASE_URL = "http://192.168.1.100:8888"  // Your IP here
   ```

3. **Build and Run:**
   - Connect Android device or start emulator
   - Click Run in Android Studio
   - App will install and launch

4. **Test Connection:**
   - Tap "Check Connection" button
   - Should show "Connected: online"

## ✅ Verify Installation

### Test from command line:
```bash
curl http://YOUR_IP:8888/api/health
```

Should return:
```json
{
  "status": "online",
  "timestamp": "...",
  "system": "Parrot OS"
}
```

## 🎯 First Commands

1. **List Available Tools:**
   - Tap "List Tools" in the app
   - See which penetration testing tools are available

2. **Run Nmap Scan:**
   - Enter a target IP (e.g., `192.168.1.1`)
   - Tap "Nmap Scan"
   - View results in output area

3. **Get System Info:**
   - Tap "System Info"
   - See Parrot OS workstation details

## 🔧 Troubleshooting

**Can't connect?**
- Check Parrot OS IP is correct
- Verify server is running: `ps aux | grep server.py`
- Check firewall: `sudo ufw status`
- Ensure both devices on same network

**Tools not available?**
- Install tools: `sudo apt install nmap metasploit-framework`
- Check installation: `which nmap`

**Android build errors?**
- Sync Gradle: File → Sync Project with Gradle Files
- Clean build: Build → Clean Project
- Rebuild: Build → Rebuild Project

## 📱 App Features

- ✅ Real-time command execution
- ✅ Tool availability checking
- ✅ Command history
- ✅ System information
- ✅ Secure authentication
- ✅ Dark theme UI

## 🔒 Security Note

⚠️ **For authorized testing only!**

- Change default SECRET_KEY
- Use HTTPS in production
- Restrict network access
- Implement proper authentication

## 📚 Next Steps

- Read `SETUP_GUIDE.md` for detailed configuration
- Check `README.md` for project overview
- Customize tools in `parrot_backend/server.py`

Happy Hacking! 🎯

