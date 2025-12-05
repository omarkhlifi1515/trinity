#!/bin/bash
# Installation script for Red Trinity Backend on Parrot OS

echo "========================================="
echo "Red Trinity Backend Installation"
echo "========================================="

# Check if running on Parrot OS
if [ ! -f /etc/os-release ]; then
    echo "Warning: Cannot detect OS. Continuing anyway..."
else
    source /etc/os-release
    if [[ "$ID" != "parrot" ]]; then
        echo "Warning: This script is designed for Parrot OS"
        read -p "Continue anyway? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
fi

# Update package list
echo "Updating package list..."
sudo apt update

# Install Python dependencies
echo "Installing Python and pip..."
sudo apt install -y python3 python3-pip

# Install required Python packages
echo "Installing Python packages..."
pip3 install -r requirements.txt

# Install penetration testing tools (if not already installed)
echo "Checking for penetration testing tools..."

tools=("nmap" "metasploit-framework" "sqlmap" "aircrack-ng" "john")

for tool in "${tools[@]}"; do
    if ! command -v $tool &> /dev/null; then
        echo "Installing $tool..."
        sudo apt install -y $tool
    else
        echo "$tool is already installed"
    fi
done

# Create systemd service (optional)
read -p "Create systemd service for auto-start? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Creating systemd service..."
    sudo tee /etc/systemd/system/red-trinity.service > /dev/null <<EOF
[Unit]
Description=Red Trinity Backend Server
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$(pwd)
Environment="SECRET_KEY=red-trinity-secret-key-change-in-production"
ExecStart=/usr/bin/python3 $(pwd)/server.py
Restart=always

[Install]
WantedBy=multi-user.target
EOF
    sudo systemctl daemon-reload
    sudo systemctl enable red-trinity.service
    echo "Service created. Start with: sudo systemctl start red-trinity"
fi

# Configure firewall
read -p "Configure firewall to allow port 8888? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if command -v ufw &> /dev/null; then
        sudo ufw allow 8888/tcp
        echo "Firewall rule added"
    elif command -v iptables &> /dev/null; then
        sudo iptables -A INPUT -p tcp --dport 8888 -j ACCEPT
        echo "iptables rule added (may need to save rules)"
    else
        echo "No firewall manager found. Please configure manually."
    fi
fi

echo ""
echo "========================================="
echo "Installation Complete!"
echo "========================================="
echo "To start the server:"
echo "  python3 server.py"
echo "  OR"
echo "  ./start_server.sh"
echo ""
echo "Get your IP address:"
echo "  hostname -I"
echo ""
echo "Update the Android app Config.kt with your IP address"

