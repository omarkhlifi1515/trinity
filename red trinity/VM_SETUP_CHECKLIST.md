# VMware + Parrot OS Setup Checklist

## ✅ Pre-Setup
- [ ] VMware Workstation/Player installed
- [ ] Parrot OS ISO downloaded from https://parrotsec.org/download/
- [ ] At least 40 GB free disk space
- [ ] 4 GB RAM available for VM

## ✅ Create VM
- [ ] New Virtual Machine created
- [ ] OS Type: Linux → Debian 11.x 64-bit
- [ ] RAM: 4096 MB (4 GB)
- [ ] Disk: 40 GB
- [ ] Network: **Bridged** (important!)
- [ ] ISO mounted to CD/DVD

## ✅ Install Parrot OS
- [ ] Booted from ISO
- [ ] Installation completed
- [ ] User account created
- [ ] System rebooted
- [ ] Can login successfully

## ✅ Get IP Address
- [ ] Opened terminal in Parrot OS
- [ ] Ran: `hostname -I`
- [ ] **Copied IP address** (e.g., 192.168.1.105)
- [ ] Tested ping from host: `ping [VM_IP]`

## ✅ Install Red Trinity Backend
- [ ] Copied `red_trinity` folder to VM (or cloned)
- [ ] Navigated to `parrot_backend/`
- [ ] Installed dependencies: `pip3 install -r requirements.txt`
- [ ] Made scripts executable: `chmod +x *.sh`

## ✅ Configure Firewall
- [ ] Installed ufw: `sudo apt install ufw -y`
- [ ] Allowed port 8888: `sudo ufw allow 8888/tcp`
- [ ] Enabled firewall: `sudo ufw enable`
- [ ] Verified: `sudo ufw status`

## ✅ Start Server
- [ ] Started server: `python3 server.py`
- [ ] Server shows: "Running on http://0.0.0.0:8888"
- [ ] No errors in terminal

## ✅ Test Connection
- [ ] From host machine, tested: `curl http://[VM_IP]:8888/api/health`
- [ ] Received JSON response with "status": "online"
- [ ] Connection successful!

## ✅ Android App Configuration
- [ ] Opened Android Studio
- [ ] Opened `red_trinity/android_app/` project
- [ ] Edited `Config.kt`
- [ ] Updated `SERVER_BASE_URL` with VM IP
- [ ] Updated `AUTH_TOKEN` to match server
- [ ] Built and ran app
- [ ] Tapped "Check Connection" - Success!

## 🎯 You're Ready!
- [ ] Can execute commands from Android app
- [ ] Tools are available on Parrot OS
- [ ] Command history working
- [ ] System info displaying

---

## Quick Commands Reference

```bash
# Get IP
hostname -I

# Install backend
cd red_trinity/parrot_backend
pip3 install -r requirements.txt

# Configure firewall
sudo ufw allow 8888/tcp
sudo ufw enable

# Start server
python3 server.py

# Test from host
curl http://[VM_IP]:8888/api/health
```

---

## Common Issues

**❌ Can't connect from host?**
- Check VM network is Bridged
- Verify firewall allows port 8888
- Ensure both on same network

**❌ Server won't start?**
- Check Python: `python3 --version`
- Install dependencies: `pip3 install -r requirements.txt`
- Check port: `sudo netstat -tulpn | grep 8888`

**❌ Android app can't connect?**
- Verify IP in Config.kt matches VM IP
- Check server is running
- Test with curl first

---

**Need help?** Check `PARROT_OS_SETUP.md` for detailed instructions!

