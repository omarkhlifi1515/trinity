#!/usr/bin/env python3
"""
Red Trinity - Parrot OS Backend Server
Handles commands from Android app and executes penetration testing tools
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import subprocess
import json
import os
import threading
import time
from datetime import datetime
import logging
from functools import wraps

app = Flask(__name__)
CORS(app)  # Enable CORS for Android app

# Configuration
SECRET_KEY = os.environ.get('SECRET_KEY', 'red-trinity-secret-key-change-in-production')
ALLOWED_IPS = ['127.0.0.1', 'localhost']  # Add your network IPs
PORT = 8888

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

# Command history
command_history = []
active_sessions = {}

# Security decorator
def require_auth(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token or token != f'Bearer {SECRET_KEY}':
            return jsonify({'error': 'Unauthorized'}), 401
        return f(*args, **kwargs)
    return decorated_function

# Tool execution functions
def execute_nmap(target, options='-sV'):
    """Execute Nmap scan"""
    try:
        cmd = f'nmap {options} {target}'
        result = subprocess.run(
            cmd,
            shell=True,
            capture_output=True,
            text=True,
            timeout=300
        )
        return {
            'success': True,
            'output': result.stdout,
            'error': result.stderr,
            'command': cmd
        }
    except subprocess.TimeoutExpired:
        return {'success': False, 'error': 'Command timeout'}
    except Exception as e:
        return {'success': False, 'error': str(e)}

def execute_metasploit(command):
    """Execute Metasploit command"""
    try:
        # Use msfconsole with resource script
        script_file = f'/tmp/msf_cmd_{int(time.time())}.rc'
        with open(script_file, 'w') as f:
            f.write(f'{command}\nexit\n')
        
        result = subprocess.run(
            f'msfconsole -r {script_file}',
            shell=True,
            capture_output=True,
            text=True,
            timeout=60
        )
        
        os.remove(script_file)
        return {
            'success': True,
            'output': result.stdout,
            'error': result.stderr,
            'command': command
        }
    except Exception as e:
        return {'success': False, 'error': str(e)}

def execute_generic_tool(tool, command):
    """Execute generic penetration testing tool"""
    try:
        result = subprocess.run(
            command,
            shell=True,
            capture_output=True,
            text=True,
            timeout=300
        )
        return {
            'success': True,
            'output': result.stdout,
            'error': result.stderr,
            'command': command,
            'tool': tool
        }
    except subprocess.TimeoutExpired:
        return {'success': False, 'error': 'Command timeout'}
    except Exception as e:
        return {'success': False, 'error': str(e)}

# API Routes
@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'online',
        'timestamp': datetime.now().isoformat(),
        'system': 'Parrot OS'
    })

@app.route('/api/tools/list', methods=['GET'])
@require_auth
def list_tools():
    """List available penetration testing tools"""
    tools = {
        'nmap': {
            'name': 'Nmap',
            'description': 'Network scanner',
            'available': check_tool_available('nmap')
        },
        'metasploit': {
            'name': 'Metasploit Framework',
            'description': 'Penetration testing framework',
            'available': check_tool_available('msfconsole')
        },
        'burp': {
            'name': 'Burp Suite',
            'description': 'Web application security testing',
            'available': check_tool_available('burpsuite')
        },
        'sqlmap': {
            'name': 'SQLMap',
            'description': 'SQL injection tool',
            'available': check_tool_available('sqlmap')
        },
        'aircrack': {
            'name': 'Aircrack-ng',
            'description': 'WiFi security auditing',
            'available': check_tool_available('aircrack-ng')
        },
        'john': {
            'name': 'John the Ripper',
            'description': 'Password cracker',
            'available': check_tool_available('john')
        }
    }
    return jsonify({'tools': tools})

def check_tool_available(tool):
    """Check if a tool is available in the system"""
    try:
        result = subprocess.run(
            f'which {tool}',
            shell=True,
            capture_output=True,
            text=True
        )
        return result.returncode == 0
    except:
        return False

@app.route('/api/execute', methods=['POST'])
@require_auth
def execute_command():
    """Execute a penetration testing command"""
    data = request.json
    tool = data.get('tool')
    command = data.get('command')
    target = data.get('target')
    options = data.get('options', '')
    
    if not tool:
        return jsonify({'error': 'Tool not specified'}), 400
    
    logger.info(f"Executing {tool} command: {command}")
    
    # Execute based on tool type
    if tool == 'nmap':
        result = execute_nmap(target, options)
    elif tool == 'metasploit':
        result = execute_metasploit(command)
    else:
        # Generic tool execution
        full_command = f"{tool} {command} {options}".strip()
        result = execute_generic_tool(tool, full_command)
    
    # Save to history
    command_history.append({
        'timestamp': datetime.now().isoformat(),
        'tool': tool,
        'command': command,
        'result': result
    })
    
    return jsonify(result)

@app.route('/api/history', methods=['GET'])
@require_auth
def get_history():
    """Get command execution history"""
    limit = request.args.get('limit', 50, type=int)
    return jsonify({
        'history': command_history[-limit:],
        'total': len(command_history)
    })

@app.route('/api/session/create', methods=['POST'])
@require_auth
def create_session():
    """Create a new session"""
    session_id = f"session_{int(time.time())}"
    active_sessions[session_id] = {
        'created': datetime.now().isoformat(),
        'commands': []
    }
    return jsonify({'session_id': session_id})

@app.route('/api/system/info', methods=['GET'])
@require_auth
def system_info():
    """Get system information"""
    try:
        hostname = subprocess.check_output(['hostname'], text=True).strip()
        uptime = subprocess.check_output(['uptime'], text=True).strip()
        ip = subprocess.check_output(['hostname', '-I'], text=True).strip()
        
        return jsonify({
            'hostname': hostname,
            'uptime': uptime,
            'ip': ip,
            'os': 'Parrot OS',
            'timestamp': datetime.now().isoformat()
        })
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    logger.info("Starting Red Trinity Backend Server...")
    logger.info(f"Server will run on 0.0.0.0:{PORT}")
    logger.info("Make sure to configure firewall rules to allow connections")
    
    app.run(
        host='0.0.0.0',
        port=PORT,
        debug=True,
        threaded=True
    )

