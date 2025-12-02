# Red Trinity Setup Guide

## Overview
Red Trinity is an Android application that connects to a Parrot OS workstation to remotely control penetration testing tools.

## Part 1: Parrot OS Backend Setup

### 1. Install Dependencies
```bash
cd parrot_backend
pip3 install -r requirements.txt
```

### 2. Configure Firewall
Allow incoming connections on port 8888:
```bash
sudo ufw allow 8888/tcp
# Or for iptables:
sudo iptables -A INPUT -p tcp --dport 8888 -j ACCEPT
```

### 3. Get Your Parrot OS IP Address
```bash
hostname -I
# Or
ip addr show
```

### 4. Start the Server
```bash
chmod +x start_server.sh
./start_server.sh
# Or directly:
python3 server.py
```

The server will run on `0.0.0.0:8888`

### 5. Test the Server
From another machine or your Android device:
```bash
curl http://YOUR_PARROT_OS_IP:8888/api/health
```

## Part 2: Android App Setup

### 1. Open in Android Studio
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to `red_trinity/android_app/`
4. Wait for Gradle sync to complete

### 2. Configure Server IP
Edit `app/src/main/java/com/redtrinity/pentest/Config.kt`:
```kotlin
const val SERVER_BASE_URL = "http://YOUR_PARROT_OS_IP:8888"
```
Replace `YOUR_PARROT_OS_IP` with your actual Parrot OS IP address.

### 3. Update Authentication Token
In `Config.kt`, update the AUTH_TOKEN to match the SECRET_KEY in `parrot_backend/server.py`:
```kotlin
const val AUTH_TOKEN = "Bearer your-secret-key-here"
```

### 4. Build and Run
1. Connect your Android device or start an emulator
2. Click "Run" in Android Studio
3. The app will install and launch

## Part 3: Usage

### From Android App:
1. **Check Connection**: Tap "Check Connection" to verify connectivity
2. **List Tools**: Tap "List Tools" to see available penetration testing tools
3. **Nmap Scan**: Enter a target IP/domain and tap "Nmap Scan"
4. **System Info**: Get information about the Parrot OS workstation
5. **History**: View command execution history

### Supported Tools:
- **Nmap**: Network scanning
- **Metasploit**: Penetration testing framework
- **SQLMap**: SQL injection testing
- **Aircrack-ng**: WiFi security auditing
- **John the Ripper**: Password cracking
- **Burp Suite**: Web application security testing

## Security Considerations

⚠️ **IMPORTANT**: This is for authorized penetration testing only!

1. **Change Default Secret Key**: Update SECRET_KEY in both server and app
2. **Use HTTPS**: In production, use HTTPS with SSL certificates
3. **Network Security**: Only allow connections from trusted IPs
4. **Firewall Rules**: Restrict access to port 8888
5. **Authentication**: Implement proper user authentication
6. **Encryption**: Use encrypted communication channels

## Troubleshooting

### Connection Issues
- Verify Parrot OS IP address is correct
- Check firewall rules allow port 8888
- Ensure server is running: `ps aux | grep server.py`
- Check server logs: `tail -f red_trinity.log`

### Android App Issues
- Verify internet permission in AndroidManifest.xml
- Check cleartext traffic is allowed (for HTTP)
- Ensure server IP is accessible from your network
- Check Android Studio logcat for errors

### Tool Execution Issues
- Verify tools are installed on Parrot OS
- Check tool availability: `which nmap`
- Review server logs for command errors
- Ensure proper permissions for tool execution

## Advanced Configuration

### Custom Tools
Add custom tool execution in `parrot_backend/server.py`:
```python
def execute_custom_tool(command):
    # Your custom tool logic
    pass
```

### SSH Integration
For more secure communication, consider SSH integration:
```python
import paramiko
# SSH connection setup
```

## License
For authorized security testing only. Use responsibly and legally.

