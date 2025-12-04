# Red Trinity - Complete Attack App

## 🎯 What's Been Built

### Backend Enhancements (`parrot_backend/server.py`)
- ✅ **Comprehensive Attack Library** with 30+ attack types:
  - Network Scans (Nmap variants)
  - Web Attacks (SQLMap, Nikto, Dirb, Gobuster)
  - Exploitation (Metasploit, Searchsploit)
  - Wireless (Aircrack-ng)
  - Password Attacks (Hydra, John the Ripper)
  - DoS Attacks (Hping3, Slowloris)
  - Reconnaissance (Whois, Dig, Nslookup)
  - Sniffing (Tcpdump, Ettercap)
- ✅ **Dashboard API** (`/api/dashboard/stats`) - Real-time attack statistics
- ✅ **Attack Execution API** (`/api/attacks/execute`) - Execute predefined attacks
- ✅ **Attack List API** (`/api/attacks/list`) - Get all available attacks

### Android App Features

#### 1. **Attack Library Screen**
- Browse attacks organized by category
- Tap any attack to execute
- Enter target IP/domain
- Professional cyberpunk UI

#### 2. **Dashboard Screen**
- Real-time statistics:
  - Total attacks executed
  - Successful vs Failed counts
  - Recent attacks list
- Auto-refreshes every 5 seconds
- Shows attack history with status

#### 3. **Settings Screen**
- Configure workstation IP address
- Configure port (default: 8888)
- Test connection button
- Settings persist across app restarts

## 🚀 How to Use

### 1. Start Backend Server

On your Parrot OS workstation:

```bash
cd red_trinity/parrot_backend
pip3 install -r requirements.txt
python3 server.py
```

Server runs on `0.0.0.0:8888`

### 2. Configure Android App

1. **Open Android Studio**
2. **Open project**: `red_trinity/android_app/`
3. **Build the app** (may need to sync Gradle first)

### 3. First Launch

1. **Open Settings tab** (bottom navigation)
2. **Enter your workstation IP** (e.g., `192.168.1.100`)
3. **Enter port** (default: `8888`)
4. **Tap "TEST CONNECTION"** to verify
5. **Tap "SAVE SETTINGS"**

### 4. Launch Attacks

1. **Go to Attacks tab**
2. **Browse attack categories**:
   - Network Scans
   - Web Attacks
   - Exploitation
   - Wireless
   - Password Attacks
   - DoS Attacks
   - Reconnaissance
   - Sniffing
3. **Tap an attack** → Enter target → Execute
4. **View results** in the output

### 5. Monitor Dashboard

- **Go to Dashboard tab**
- **See real-time stats**:
  - Total attacks
  - Success rate
  - Recent attack history
- **Auto-updates** every 5 seconds

## 📱 App Structure

```
MainActivity (Bottom Navigation)
├── AttackListFragment
│   ├── AttackCategoryAdapter
│   └── AttackAdapter
├── DashboardFragment
│   └── RecentAttacksAdapter
└── SettingsFragment
    └── PreferenceHelper (saves IP/Port)
```

## 🎨 UI Design

- **Cyberpunk Theme**: Dark background (#0A0A0F)
- **Neon Colors**: Cyan (#00FFFF), Magenta (#FF0080), Green (#00FF80)
- **Professional Cards**: Elevated cards with rounded corners
- **Bottom Navigation**: Easy switching between screens

## 🔧 Technical Details

### Backend APIs

- `GET /api/attacks/list` - List all attacks
- `POST /api/attacks/execute` - Execute attack
- `GET /api/dashboard/stats` - Get dashboard stats
- `GET /api/health` - Health check

### Android Components

- **Retrofit** - HTTP client (dynamic base URL from settings)
- **Coroutines** - Async operations
- **SharedPreferences** - Settings storage
- **RecyclerView** - Attack lists
- **Material Design** - Modern UI components

## ⚠️ Security Notes

- Change `SECRET_KEY` in `server.py` for production
- Use HTTPS in production
- Restrict network access
- Only use on authorized networks/systems

## 🐛 Troubleshooting

**Can't connect?**
- Check workstation IP is correct
- Verify server is running: `ps aux | grep server.py`
- Check firewall: `sudo ufw status`
- Ensure phone and workstation on same network

**Attacks not executing?**
- Check tool is installed: `which nmap`
- Check server logs: `tail -f red_trinity.log`
- Verify target format (IP or domain)

**App crashes?**
- Sync Gradle: File → Sync Project with Gradle Files
- Clean build: Build → Clean Project
- Rebuild: Build → Rebuild Project

## 📊 Attack Categories

1. **Network Scans** - 5 attacks (Nmap variants)
2. **Web Attacks** - 5 attacks (SQLMap, Nikto, etc.)
3. **Exploitation** - 2 attacks (Metasploit)
4. **Wireless** - 2 attacks (Aircrack-ng)
5. **Password Attacks** - 3 attacks (Hydra, John)
6. **DoS Attacks** - 2 attacks (Hping3, Slowloris)
7. **Reconnaissance** - 3 attacks (Whois, Dig, etc.)
8. **Sniffing** - 2 attacks (Tcpdump, Ettercap)

**Total: 24+ predefined attacks ready to use!**

## 🎯 Next Steps

- Add more attack types
- Implement attack templates
- Add attack scheduling
- Export attack results
- Add attack presets

Happy Hacking! 🔴⚔️

