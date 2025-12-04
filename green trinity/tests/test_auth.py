import pytest
from app.models import User

def test_auth_flow(client):
    # 1. Test Registration
    response = client.post('/auth/register', data={
        'username': 'testuser',
        'password': 'password',
        'confirm_password': 'password',
        'submit': 'Register'
    }, follow_redirects=True)
    assert response.status_code == 200
    assert b"Your account has been created!" in response.data
    user = User.query.filter_by(username='testuser').first()
    assert user is not None

    # 2. Test Login
    response = client.post('/auth/login', data={
        'username': 'testuser',
        'password': 'password',
        'submit': 'Login'
    }, follow_redirects=True)
    assert response.status_code == 200
    assert b"User Dashboard" in response.data

    # 3. Test Logout
    response = client.get('/auth/logout', follow_redirects=True)
    assert response.status_code == 200
    assert b"Login" in response.data