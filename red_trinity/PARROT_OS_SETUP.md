# Parrot OS Setup Guide for Red Trinity

## Option 1: VMware Setup (Recommended)

### Step 1: Download Parrot OS
1. Go to: https://parrotsec.org/download/
2. Download **Parrot Security OS** (Home Edition is fine)
3. Choose ISO format for VMware

### Step 2: Create VMware Virtual Machine

1. **Open VMware Workstation/Player**
2. **Create New Virtual Machine:**
   - Select "Typical" installation
   - Choose "I will install the operating system later"
   - Select "Linux" → "Debian 11.x 64-bit" (Parrot is Debian-based)
   
3. **Configure VM Settings:**
   - **Name:** Parrot OS - Red Trinity
   - **Location:** Choose your preferred location
   - **Disk Size:** 40 GB (minimum 20 GB)
   - **Memory:** 4096 MB (4 GB) - minimum 2048 MB
   - **Processors:** 2 cores (minimum 1)

4. **Network Configuration:**
   - **Bridged Networking** (Recommended) - VM gets its own IP on your network
   - OR **NAT** - VM shares your host IP (may need port forwarding)

5. **Mount ISO:**
   - Right-click VM → Settings
   - CD/DVD → Use ISO image file
   - Select downloaded Parrot OS ISO

### Step 3: Install Parrot OS

1. **Start the VM**
2. **Boot from ISO:**
   - Select "Install" or "Graphical Install"
   - Follow installation wizard:
     - Language: English
     - Location: Your timezone
     - Keyboard: Your layout
     - Hostname: `parrot-redtrinity` (or your choice)
     - Username: Create a user account
     - Password: Set a strong password
     - Disk partitioning: Use entire disk (or manual if preferred)
     - Software selection: Standard system + Desktop environment
     - Install GRUB: Yes

3. **After Installation:**
   - Reboot the VM
   - Login with your credentials

### Step 4: Get VM IP Address

**In Parrot OS terminal, run:**
```bash
hostname -I
# OR
ip addr show
# OR
ifconfig
```

**You'll see something like:**
```
192.168.1.105
```

**This is the IP you'll use in the Android app Config.kt!**

### Step 5: Install Red Trinity Backend

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Navigate to backend (if you copied files)
cd red_trinity/parrot_backend

# Install Python dependencies
pip3 install -r requirements.txt

# Make scripts executable
chmod +x start_server.sh install.sh

# Run installation script (optional)
./install.sh
```

### Step 6: Configure Firewall

```bash
# Check if ufw is installed
sudo apt install ufw -y

# Allow port 8888
sudo ufw allow 8888/tcp

# Enable firewall
sudo ufw enable

# Check status
sudo ufw status
```

### Step 7: Start the Server

```bash
cd red_trinity/parrot_backend
python3 server.py
```

**You should see:**
```
Starting Red Trinity Backend Server...
Server will run on 0.0.0.0:8888
 * Running on http://0.0.0.0:8888
```

### Step 8: Test from Host Machine

**From your Windows host (or Android device on same network):**
```bash
# Replace with your VM's IP
curl http://192.168.1.105:8888/api/health
```

**Should return:**
```json
{
  "status": "online",
  "timestamp": "...",
  "system": "Parrot OS"
}
```

## Option 2: VirtualBox (Alternative)

### Similar steps but:
1. Download VirtualBox: https://www.virtualbox.org/
2. Create VM:
   - Type: Linux
   - Version: Debian (64-bit)
   - RAM: 4096 MB
   - Create virtual hard disk: 40 GB
3. **Network Settings:**
   - Adapter 1 → Bridged Adapter (to get own IP)
   - OR NAT with port forwarding (8888 → 8888)
4. Mount Parrot OS ISO and install
5. Follow same steps as VMware

## Option 3: Physical Machine

If you have a spare computer:
1. Download Parrot OS ISO
2. Create bootable USB (using Rufus or similar)
3. Boot from USB and install
4. Follow installation steps above

## Network Configuration Tips

### Bridged Network (Recommended)
- VM gets its own IP on your network
- Can access from any device on same network
- Example: If your network is 192.168.1.x, VM gets 192.168.1.105

### NAT Network
- VM shares host IP
- May need port forwarding
- Less accessible from other devices

### Host-Only Network
- Only accessible from host machine
- Not recommended for Android app

## Troubleshooting

### Can't find VM IP?
```bash
# In Parrot OS
ip addr show
# Look for inet address under your network interface
```

### Can't connect from host?
1. **Check VM network adapter is enabled**
2. **Verify firewall allows port 8888**
3. **Ensure both devices on same network**
4. **Ping test:**
   ```bash
   # From host
   ping 192.168.1.105  # Your VM IP
   ```

### Server not starting?
```bash
# Check if port is in use
sudo netstat -tulpn | grep 8888

# Check Python version
python3 --version  # Should be 3.8+

# Check dependencies
pip3 list | grep -i flask
```

### Permission denied?
```bash
# Make scripts executable
chmod +x *.sh

# Run with sudo if needed (not recommended for server)
```

## Security Notes

⚠️ **Important:**
- Change default SECRET_KEY in server.py
- Use strong passwords
- Keep Parrot OS updated
- Only allow connections from trusted networks
- Consider using VPN for remote access

## Next Steps

1. ✅ VM is running Parrot OS
2. ✅ Backend server is running
3. ✅ IP address is known
4. ✅ Firewall configured
5. ✅ Test connection works
6. → **Update Android app Config.kt with VM IP**
7. → **Build and run Android app**

## Quick Verification

**Run this in Parrot OS:**
```bash
# Get IP
IP=$(hostname -I | awk '{print $1}')
echo "Your Parrot OS IP: $IP"

# Test server
python3 server.py &
sleep 2
curl http://localhost:8888/api/health
```

**Copy the IP address and use it in Android app!**

