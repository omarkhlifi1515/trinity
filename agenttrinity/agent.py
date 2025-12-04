"""AgentTrinity - minimal agent CLI with placeholder executable actions.

This module exposes three functions expected by the Trinity spec:
- create_task
- send_notification
- update_status

The functions are dummies that print JSON to stdout; replace with real API/db calls when wiring into your backend.
"""
import json
import argparse

def create_task(title, description, assignees):
    payload = {
        'action': 'create_task',
        'title': title,
        'description': description,
        'assignees': assignees
    }
    print(json.dumps(payload))
    return payload

def send_notification(user_id, message):
    payload = {'action': 'send_notification', 'user_id': user_id, 'message': message}
    print(json.dumps(payload))
    return payload

def update_status(entity, entity_id, field, value):
    payload = {'action': 'update_status', 'entity': entity, 'id': entity_id, 'field': field, 'value': value}
    print(json.dumps(payload))
    return payload

def main():
    parser = argparse.ArgumentParser(description='AgentTrinity CLI (starter)')
    sub = parser.add_subparsers(dest='cmd')

    p1 = sub.add_parser('create_task')
    p1.add_argument('--title', required=True)
    p1.add_argument('--description', default='')
    p1.add_argument('--assignees', nargs='*', type=int, default=[])

    p2 = sub.add_parser('notify')
    p2.add_argument('--user', type=int, required=True)
    p2.add_argument('--message', required=True)

    p3 = sub.add_parser('update')
    p3.add_argument('--entity', required=True)
    p3.add_argument('--id', required=True)
    p3.add_argument('--field', required=True)
    p3.add_argument('--value', required=True)

    args = parser.parse_args()
    if args.cmd == 'create_task':
        create_task(args.title, args.description, args.assignees)
    elif args.cmd == 'notify':
        send_notification(args.user, args.message)
    elif args.cmd == 'update':
        update_status(args.entity, args.id, args.field, args.value)
    else:
        parser.print_help()

if __name__ == '__main__':
    main()
