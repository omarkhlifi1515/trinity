import time
from socketio import Client

# Connect to the WebTrinity Socket Server as a "Super Admin" client
sio = Client()

def start_scheduler():
    try:
        sio.connect('http://localhost:5000')
        print("Agent connected to Neural Network...")
        
        while True:
            # TODO: replace with real DB checks for overdue tasks
            # Simulated: Found a task assigned to User 2 that is overdue
            target_user_id = 2 
            task_title = "Submit Daily Report"
            
            sio.emit('agent_push_notification', {
                'user_id': target_user_id,
                'message': f"Reminder: '{task_title}' is overdue. Need help?"
            })
            
            time.sleep(60) # Wait 1 minute
            
    except Exception as e:
        print(f"Connection lost: {e}")

if __name__ == '__main__':
    start_scheduler()
