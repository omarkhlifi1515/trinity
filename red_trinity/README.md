# Red Trinity - Android Penetration Testing Control App

Android application that connects to a Parrot OS workstation to remotely control penetration testing tools.

## Project Structure

```
red_trinity/
├── android_app/          # Android application (Kotlin)
├── parrot_backend/       # Python backend service for Parrot OS
└── README.md
```

## Features

- Remote control of penetration testing tools
- Real-time command execution
- Secure communication via SSH/API
- Tool integration: Nmap, Metasploit, Burp Suite, etc.
- Session management
- Command history

## Setup

### Parrot OS Backend
1. Install dependencies: `pip install -r parrot_backend/requirements.txt`
2. Run server: `python parrot_backend/server.py`

### Android App
1. Open project in Android Studio
2. Configure server IP in `Config.kt`
3. Build and run

## Security

- SSH key-based authentication
- Encrypted communication
- Session tokens
- Command validation

