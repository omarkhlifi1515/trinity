"""Trinity Agent AI Brain - Natural Language to Action Parser

Uses OpenAI GPT to interpret natural language commands and convert them to executable actions.
"""
import json
import os
from dotenv import load_dotenv

load_dotenv()

try:
    import openai
except ImportError:
    raise ImportError("openai library not installed. Run: pip install openai")

OPENAI_API_KEY = os.environ.get('OPENAI_API_KEY')
if not OPENAI_API_KEY:
    raise ValueError('OPENAI_API_KEY environment variable is required.')

openai.api_key = OPENAI_API_KEY

SYSTEM_PROMPT = """
You are Trinity, the AI Workplace Operating System. You interpret natural language commands and convert them to executable actions.

Available actions:
1. create_task: Create a new task
   - Fields: title (string, required), description (string, optional), priority (string: Low/Medium/High), assignees (list of user IDs), due_date (ISO date string)
   
2. send_notification: Send a notification to a user
   - Fields: user_id (integer, required), message (string, required)
   
3. update_status: Update task or user status
   - Fields: entity (string: 'task' or 'user'), id (integer), field (string), value (string)

When given a command, respond with ONLY valid JSON (no markdown, no text before/after).

Example interpretations:
- "Create a task called 'Finish report' for Alice" → {"action":"create_task","title":"Finish report","description":"","assignees":[<alice_id>]}
- "Tell John the deadline is tomorrow" → {"action":"send_notification","user_id":<john_id>,"message":"The deadline is tomorrow"}
- "Mark task 5 as done" → {"action":"update_status","entity":"task","id":5,"field":"status","value":"Done"}

If the command is ambiguous or you cannot parse it, respond with:
{"error": "Could not parse command", "suggestion": "Please be more specific"}
"""


def parse_command(natural_language_command: str) -> dict:
    """
    Converts a natural language command to an action JSON object using OpenAI GPT.
    
    Args:
        natural_language_command: User's spoken or typed command
        
    Returns:
        dict: Action JSON (action, parameters, or error)
    """
    try:
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",  # Use gpt-4 for better accuracy if available
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": natural_language_command}
            ],
            temperature=0.3,  # Lower temperature for consistency
            max_tokens=500
        )
        
        response_text = response.choices[0].message.content.strip()
        
        # Try to parse as JSON
        action = json.loads(response_text)
        return action
        
    except json.JSONDecodeError:
        return {
            "error": "AI response was not valid JSON",
            "raw_response": response_text
        }
    except openai.error.AuthenticationError:
        return {
            "error": "OpenAI API authentication failed. Check OPENAI_API_KEY."
        }
    except Exception as e:
        return {
            "error": str(e)
        }


def validate_action(action: dict) -> tuple[bool, str]:
    """
    Validates that the action has required fields.
    
    Args:
        action: Action JSON object
        
    Returns:
        tuple: (is_valid: bool, error_message: str)
    """
    if "error" in action:
        return False, action.get("error", "Unknown error")
    
    action_type = action.get("action")
    
    if action_type == "create_task":
        if not action.get("title"):
            return False, "create_task requires 'title'"
        return True, ""
    
    elif action_type == "send_notification":
        if not action.get("user_id"):
            return False, "send_notification requires 'user_id'"
        if not action.get("message"):
            return False, "send_notification requires 'message'"
        return True, ""
    
    elif action_type == "update_status":
        if not action.get("entity"):
            return False, "update_status requires 'entity' (task or user)"
        if not action.get("id"):
            return False, "update_status requires 'id'"
        if not action.get("field"):
            return False, "update_status requires 'field'"
        if not action.get("value"):
            return False, "update_status requires 'value'"
        return True, ""
    
    else:
        return False, f"Unknown action: {action_type}"
