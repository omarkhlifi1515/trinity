#!/usr/bin/env python3
"""
Red Trinity - Windows Attack Server
Simple server that executes Windows PowerShell attack scripts
"""

from flask import Flask, request, jsonify, render_template_string
from flask_cors import CORS
import subprocess
import os
import json
from datetime import datetime
import logging
from functools import wraps
import platform

app = Flask(__name__)
# Enable CORS for Android app and web dashboard from other devices
CORS(app, resources={r"/*": {"origins": "*"}}, supports_credentials=True)

# Configuration
SECRET_KEY = os.environ.get('SECRET_KEY', 'red-trinity-secret-key-change-in-production')
PORT = int(os.environ.get('PORT', 8888))
SCRIPTS_DIR = os.path.join(os.path.dirname(__file__), 'scripts')

# Logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('red_trinity.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Attack statistics
attack_stats = {
    'total_attacks': 0,
    'successful': 0,
    'failed': 0,
    'by_type': {},
    'recent_attacks': []
}

# Attack library - Windows PowerShell scripts
ATTACK_LIBRARY = {
    'network_scans': [
        {'id': 'nmap_scan', 'name': 'Nmap Network Scan', 'script': 'nmap_scan.ps1', 'description': 'Quick network scan using Nmap'},
        {'id': 'nmap_full', 'name': 'Nmap Full Port Scan', 'script': 'nmap_full_scan.ps1', 'description': 'Scan all ports'},
        {'id': 'ping_sweep', 'name': 'Ping Sweep', 'script': 'ping_sweep.ps1', 'description': 'Discover active hosts on network'},
        {'id': 'port_scan', 'name': 'Port Scanner', 'script': 'port_scan.ps1', 'description': 'Scan common ports'},
    ],
    'reconnaissance': [
        {'id': 'dns_lookup', 'name': 'DNS Lookup', 'script': 'dns_lookup.ps1', 'description': 'DNS record enumeration'},
        {'id': 'whois', 'name': 'Whois Lookup', 'script': 'whois_lookup.ps1', 'description': 'Domain information lookup'},
    ],
    'web_attacks': [
        {'id': 'http_enum', 'name': 'HTTP Enumeration', 'script': 'http_enum.ps1', 'description': 'Enumerate HTTP services and paths'},
    ],
    'windows_attacks': [
        {'id': 'smb_enum', 'name': 'SMB Enumeration', 'script': 'smb_enum.ps1', 'description': 'Enumerate SMB shares'},
        {'id': 'rdp_check', 'name': 'RDP Check', 'script': 'rdp_check.ps1', 'description': 'Check RDP accessibility'},
        {'id': 'service_enum', 'name': 'Service Enumeration', 'script': 'service_enum.ps1', 'description': 'Enumerate Windows services'},
        {'id': 'registry_scan', 'name': 'Registry Scan', 'script': 'registry_scan.ps1', 'description': 'Scan Windows registry (local)'},
    ],
    'dos_attacks': [
        {'id': 'dos_attack', 'name': 'DoS Attack', 'script': 'dos_attack.ps1', 'description': 'Basic DoS attack (HTTP flood, 30 seconds)'},
        {'id': 'ddos_attack', 'name': 'DDoS Attack', 'script': 'ddos_attack.ps1', 'description': 'Distributed DoS attack (multi-threaded, 60 seconds)'},
        {'id': 'slowloris', 'name': 'Slowloris Attack', 'script': 'slowloris.ps1', 'description': 'Slow HTTP DoS attack (keeps connections open)'},
    ],
}

# Security decorator
def require_auth(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token or token != f'Bearer {SECRET_KEY}':
            return jsonify({'error': 'Unauthorized'}), 401
        return f(*args, **kwargs)
    return decorated_function

def execute_script(script_name, target):
    """Execute a Windows PowerShell attack script"""
    script_path = os.path.join(SCRIPTS_DIR, script_name)
    
    # Convert to absolute path and normalize
    script_path = os.path.abspath(script_path)
    
    # Check if script exists
    if not os.path.exists(script_path):
        return {
            'success': False,
            'error': f'Script not found: {script_name} at {script_path}',
            'output': '',
            'command': f'powershell -ExecutionPolicy Bypass -File "{script_path}" {target}'
        }
    
    try:
        logger.info(f"Executing Windows script: {script_path} with target: {target}")
        
        # Execute PowerShell script with proper path quoting
        # -ExecutionPolicy Bypass allows running scripts
        # -File executes the script file
        # Use absolute path with quotes to handle spaces
        if target:
            # Build command with proper quoting
            cmd = ['powershell', '-ExecutionPolicy', 'Bypass', '-File', script_path, target]
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=600,  # 10 minute timeout
                cwd=SCRIPTS_DIR  # Set working directory to scripts folder
            )
        else:
            cmd = ['powershell', '-ExecutionPolicy', 'Bypass', '-File', script_path]
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=600,
                cwd=SCRIPTS_DIR
            )
        
        # Build command string for logging
        cmd_str = ' '.join([f'"{arg}"' if ' ' in arg else arg for arg in cmd])
        
        return {
            'success': result.returncode == 0,
            'output': result.stdout,
            'error': result.stderr,
            'command': cmd_str,
            'exit_code': result.returncode
        }
    except subprocess.TimeoutExpired:
        return {
            'success': False,
            'error': 'Script execution timeout (exceeded 10 minutes)',
            'output': '',
            'command': f'powershell -ExecutionPolicy Bypass -File "{script_path}" {target}'
        }
    except Exception as e:
        logger.error(f"Script execution error: {e}")
        return {
            'success': False,
            'error': str(e),
            'output': '',
            'command': f'powershell -ExecutionPolicy Bypass -File "{script_path}" {target}'
        }

# API Routes
@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint - no auth required"""
    return jsonify({
        'status': 'online',
        'timestamp': datetime.now().isoformat(),
        'system': platform.system(),
        'version': '2.0.0',
        'type': 'windows-script-based'
    })

@app.route('/api/attacks/list', methods=['GET'])
@require_auth
def list_attacks():
    """Get list of available attacks"""
    return jsonify({
        'attacks': ATTACK_LIBRARY,
        'total': sum(len(v) for v in ATTACK_LIBRARY.values())
    })

@app.route('/api/attacks/execute', methods=['POST'])
@require_auth
def execute_attack():
    """Execute an attack script"""
    data = request.json
    attack_id = data.get('attack_id')
    category = data.get('category')
    target = data.get('target', '')
    
    if not attack_id or not category:
        return jsonify({'error': 'Attack ID and category required'}), 400
    
    # Find attack in library
    attack = None
    if category in ATTACK_LIBRARY:
        attack = next((a for a in ATTACK_LIBRARY[category] if a['id'] == attack_id), None)
    
    if not attack:
        return jsonify({'error': 'Attack not found'}), 404
    
    # For registry scan, target is not needed (local only)
    if attack_id == 'registry_scan':
        target = ''
    
    # Execute script
    script_name = attack['script']
    result = execute_script(script_name, target)
    
    # Update stats
    attack_stats['total_attacks'] += 1
    if result.get('success'):
        attack_stats['successful'] += 1
    else:
        attack_stats['failed'] += 1
    
    attack_stats['by_type'][category] = attack_stats['by_type'].get(category, 0) + 1
    
    attack_stats['recent_attacks'].append({
        'timestamp': datetime.now().isoformat(),
        'attack_id': attack_id,
        'attack_name': attack['name'],
        'category': category,
        'target': target,
        'success': result.get('success', False)
    })
    
    # Keep only last 100 attacks
    if len(attack_stats['recent_attacks']) > 100:
        attack_stats['recent_attacks'] = attack_stats['recent_attacks'][-100:]
    
    return jsonify({
        **result,
        'attack_name': attack['name'],
        'attack_id': attack_id
    })

@app.route('/api/dashboard/stats', methods=['GET'])
@require_auth
def get_dashboard_stats():
    """Get dashboard statistics"""
    return jsonify(attack_stats)

@app.route('/', methods=['GET'])
def web_dashboard():
    """Web dashboard for launching attacks"""
    return render_template_string(WEB_DASHBOARD_HTML)

@app.route('/execute', methods=['POST', 'OPTIONS'])
def web_execute_attack():
    """Execute attack from web interface - accessible from any device"""
    # Handle CORS preflight
    if request.method == 'OPTIONS':
        response = jsonify({})
        response.headers.add('Access-Control-Allow-Origin', '*')
        response.headers.add('Access-Control-Allow-Headers', 'Content-Type')
        response.headers.add('Access-Control-Allow-Methods', 'POST')
        return response
    
    # Accept both form data and JSON
    if request.is_json:
        data = request.json
    else:
        data = request.form
    attack_id = data.get('attack_id')
    category = data.get('category')
    target = data.get('target', '')
    
    if not attack_id or not category:
        return jsonify({'error': 'Attack ID and category required'}), 400
    
    # Find attack in library
    attack = None
    if category in ATTACK_LIBRARY:
        attack = next((a for a in ATTACK_LIBRARY[category] if a['id'] == attack_id), None)
    
    if not attack:
        return jsonify({'error': 'Attack not found'}), 404
    
    # For registry scan, target is not needed
    if attack_id == 'registry_scan':
        target = ''
    
    # Execute script
    script_name = attack['script']
    result = execute_script(script_name, target)
    
    # Update stats
    attack_stats['total_attacks'] += 1
    if result.get('success'):
        attack_stats['successful'] += 1
    else:
        attack_stats['failed'] += 1
    
    attack_stats['by_type'][category] = attack_stats['by_type'].get(category, 0) + 1
    
    attack_stats['recent_attacks'].append({
        'timestamp': datetime.now().isoformat(),
        'attack_id': attack_id,
        'attack_name': attack['name'],
        'category': category,
        'target': target,
        'success': result.get('success', False)
    })
    
    # Keep only last 100 attacks
    if len(attack_stats['recent_attacks']) > 100:
        attack_stats['recent_attacks'] = attack_stats['recent_attacks'][-100:]
    
    response = jsonify({
        **result,
        'attack_name': attack['name'],
        'attack_id': attack_id
    })
    # Add CORS headers for cross-origin requests from other devices
    response.headers.add('Access-Control-Allow-Origin', '*')
    return response

# Load web dashboard HTML
WEB_DASHBOARD_HTML = ''
try:
    dashboard_path = os.path.join(os.path.dirname(__file__), 'web_dashboard.html')
    if os.path.exists(dashboard_path):
        with open(dashboard_path, 'r', encoding='utf-8') as f:
            WEB_DASHBOARD_HTML = f.read()
    else:
        # Fallback minimal HTML if file not found
        WEB_DASHBOARD_HTML = '''
        <!DOCTYPE html>
        <html>
        <head><title>Red Trinity</title></head>
        <body>
            <h1>Red Trinity Attack Server</h1>
            <p>Web dashboard HTML file not found. Please ensure web_dashboard.html exists.</p>
            <p>API endpoints available at /api/*</p>
        </body>
        </html>
        '''
except Exception as e:
    logger.error(f"Error loading web dashboard: {e}")
    WEB_DASHBOARD_HTML = '<html><body><h1>Error loading dashboard</h1></body></html>'

if __name__ == '__main__':
    # Ensure scripts directory exists
    os.makedirs(SCRIPTS_DIR, exist_ok=True)
    
    # Check if running on Windows
    if platform.system() != 'Windows':
        logger.warning("This server is designed for Windows. Some scripts may not work on other systems.")
    
    # Get local IP address for network access
    try:
        import socket
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
    except Exception:
        local_ip = "YOUR_IP"
    
    logger.info("=" * 60)
    logger.info("Red Trinity - Windows Attack Server")
    logger.info("=" * 60)
    logger.info(f"Server running on 0.0.0.0:{PORT}")
    logger.info(f"Scripts directory: {SCRIPTS_DIR}")
    logger.info(f"Platform: {platform.system()}")
    logger.info("")
    logger.info("Available attack categories:")
    for category, attacks in ATTACK_LIBRARY.items():
        logger.info(f"  - {category}: {len(attacks)} attacks")
    logger.info("")
    logger.info("=" * 60)
    logger.info("ACCESS FROM THIS COMPUTER:")
    logger.info(f"  Web Dashboard: http://localhost:{PORT}")
    logger.info(f"  API Endpoints: http://localhost:{PORT}/api/*")
    logger.info("")
    logger.info("ACCESS FROM OTHER DEVICES (Same Network):")
    logger.info(f"  Web Dashboard: http://{local_ip}:{PORT}")
    logger.info(f"  API Endpoints: http://{local_ip}:{PORT}/api/*")
    logger.info("")
    logger.info("IMPORTANT: Make sure Windows Firewall allows port 8888")
    logger.info("  Run: netsh advfirewall firewall add rule name=\"Red Trinity\" dir=in action=allow protocol=TCP localport=8888")
    logger.info("=" * 60)
    
    app.run(
        host='0.0.0.0',
        port=PORT,
        debug=True,
        threaded=True
    )
