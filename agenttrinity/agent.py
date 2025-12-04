"""AgentTrinity CLI – Natural Language Commands to Actions

This module provides a command-line interface that interprets natural language
and executes actions via authenticated HTTP requests to agent_server.py.

Example usage:
    python agent.py ask "Create a task called 'Finish report' for Alice"
    python agent.py ask "Tell John the deadline is tomorrow"
    python agent.py ask "Mark task 5 as done"
"""
import argparse
import os
from dotenv import load_dotenv
import requests
import json
import sys

load_dotenv()

from ai_brain import parse_command, validate_action

# Configuration
AGENT_SERVER_URL = os.environ.get('AGENT_SERVER_URL', 'http://localhost:8080')
AGENT_API_KEY = os.environ.get('AGENT_API_KEY', '')

if not AGENT_API_KEY:
    print('ERROR: AGENT_API_KEY environment variable is not set.')
    sys.exit(1)

HEADERS = {'X-API-Key': AGENT_API_KEY, 'Content-Type': 'application/json'}


def execute_action(action_dict):
    """Sends the action to agent_server via HTTP."""
    if not action_dict:
        print("ERROR: No action to execute.")
        return False
    
    action_type = action_dict.get('action')
    
    if not action_type:
        print(f"ERROR: {action_dict.get('error', 'Unknown error')}")
        return False
    
    endpoint = f'{AGENT_SERVER_URL}/{action_type}'
    
    try:
        resp = requests.post(endpoint, json=action_dict, headers=HEADERS, timeout=10)
        
        if resp.status_code in (200, 201):
            result = resp.json()
            print(f"✅ Action '{action_type}' completed successfully.")
            print(json.dumps(result, indent=2))
            return True
        else:
            print(f"❌ Error ({resp.status_code}): {resp.text}")
            return False
    except requests.exceptions.ConnectionError:
        print(f"❌ Failed to connect to agent server at {AGENT_SERVER_URL}")
        return False
    except Exception as e:
        print(f"❌ Request failed: {e}")
        return False


def ask(natural_language_command):
    """Parse natural language and execute."""
    print(f"🤖 Trinity: Interpreting command: '{natural_language_command}'")
    
    # Parse using AI
    action = parse_command(natural_language_command)
    
    # Validate
    is_valid, error_msg = validate_action(action)
    if not is_valid:
        print(f"❌ Validation failed: {error_msg}")
        return False
    
    # Execute
    return execute_action(action)


def main():
    parser = argparse.ArgumentParser(description='Trinity Agent CLI - AI Workplace OS')
    sub = parser.add_subparsers(dest='cmd')

    # Natural language command
    ask_parser = sub.add_parser('ask', help='Ask Trinity to do something')
    ask_parser.add_argument('command', nargs='+', help='Natural language command')

    # Manual actions (if needed)
    create_parser = sub.add_parser('create_task', help='Manually create a task')
    create_parser.add_argument('--title', required=True)
    create_parser.add_argument('--description', default='')
    create_parser.add_argument('--priority', default='Medium')
    create_parser.add_argument('--assignees', nargs='*', type=int, default=[])

    notify_parser = sub.add_parser('notify', help='Send a notification')
    notify_parser.add_argument('--user', type=int, required=True)
    notify_parser.add_argument('--message', required=True)

    update_parser = sub.add_parser('update', help='Update status')
    update_parser.add_argument('--entity', required=True)
    update_parser.add_argument('--id', required=True)
    update_parser.add_argument('--field', required=True)
    update_parser.add_argument('--value', required=True)

    args = parser.parse_args()
    
    if args.cmd == 'ask':
        command_str = ' '.join(args.command)
        ask(command_str)
    elif args.cmd == 'create_task':
        action = {
            'action': 'create_task',
            'title': args.title,
            'description': args.description,
            'priority': args.priority,
            'assignees': args.assignees,
        }
        execute_action(action)
    elif args.cmd == 'notify':
        action = {'action': 'send_notification', 'user_id': args.user, 'message': args.message}
        execute_action(action)
    elif args.cmd == 'update':
        action = {'action': 'update_status', 'entity': args.entity, 'id': args.id, 'field': args.field, 'value': args.value}
        execute_action(action)
    else:
        parser.print_help()


if __name__ == '__main__':
    main()


