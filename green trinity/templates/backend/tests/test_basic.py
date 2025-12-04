def test_index_redirect(client):
    response = client.get('/')
    assert response.status_code == 302
    assert b'Redirecting...' in response.data

def test_login_page(client):
    response = client.get('/auth/login')
    assert response.status_code == 200
    assert b"Login" in response.data

def test_register_page(client):
    response = client.get('/auth/register')
    assert response.status_code == 200
    assert b"Register" in response.data
