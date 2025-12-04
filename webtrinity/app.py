from flask import Flask, jsonify, request, render_template
from flask_sqlalchemy import SQLAlchemy
import os
from dotenv import load_dotenv

# Load .env when present (local development)
load_dotenv()

app = Flask(__name__, template_folder='templates')
# Default DATABASE_URL: use provided Render Postgres URL if env not set
DEFAULT_DATABASE_URL = 'postgresql://trinity_5mxn_user:6q8XxiTlIiEN7NS9ehPiOGENlWeU4Pqr@dpg-d4o6t22dbo4c73ab07t0-a.oregon-postgres.render.com/trinity_5mxn'
DATABASE_URL = os.environ.get('DATABASE_URL', DEFAULT_DATABASE_URL)
app.config['SQLALCHEMY_DATABASE_URI'] = DATABASE_URL
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

from models import User, Department, Task, Message


@app.route('/')
def index():
    return render_template('index.html')


@app.route('/api/users')
def list_users():
    users = User.query.all()
    return jsonify([u.to_dict() for u in users])


@app.route('/api/stats')
def stats():
    total_tasks = Task.query.count()
    done = Task.query.filter_by(status='Done').count()
    absent = User.query.filter_by(status='Absent').count()
    return jsonify({
        'total_tasks': total_tasks,
        'done': done,
        'absent_users': absent
    })


if __name__ == '__main__':
    # Local development default
    app.run(debug=True, host='127.0.0.1', port=int(os.environ.get('PORT', 5000)))
