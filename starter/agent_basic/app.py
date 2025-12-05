import os
from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy

BASE_DIR = os.path.dirname(__file__)
DB_PATH = os.path.join(BASE_DIR, 'agent_basic.db')

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///' + DB_PATH
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

class Task(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(255))
    description = db.Column(db.Text)
    assignee = db.Column(db.String(150))  # store username
    status = db.Column(db.String(50), default='Pending')

with app.app_context():
    db.create_all()

@app.route('/health')
def health():
    return jsonify({'status': 'running'})

@app.route('/tasks', methods=['GET'])
def get_tasks():
    tasks = Task.query.order_by(Task.id.desc()).all()
    return jsonify([{'id':t.id,'title':t.title,'description':t.description,'assignee':t.assignee,'status':t.status} for t in tasks])

@app.route('/create_task', methods=['POST'])
def create_task():
    data = request.json or {}
    title = data.get('title')
    description = data.get('description')
    assignee = data.get('assignee')  # expect username
    if not title:
        return jsonify({'error':'title required'}), 400
    t = Task(title=title, description=description, assignee=assignee)
    db.session.add(t)
    db.session.commit()
    return jsonify({'id': t.id, 'status': 'created'})

if __name__ == '__main__':
    app.run(port=5002, debug=True)
