#!/bin/bash
# Start Red Trinity Backend Server on Parrot OS

echo "Starting Red Trinity Backend Server..."
echo "Make sure you have installed dependencies: pip install -r requirements.txt"

# Check if running as root (not recommended for security)
if [ "$EUID" -eq 0 ]; then 
    echo "Warning: Running as root. Consider using a non-root user."
fi

# Set environment variables
export SECRET_KEY="${SECRET_KEY:-red-trinity-secret-key-change-in-production}"
export FLASK_APP=server.py
export FLASK_ENV=production

# Start the server
python3 server.py

