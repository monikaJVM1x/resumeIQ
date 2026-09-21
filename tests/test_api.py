import pytest
from fastapi.testclient import TestClient
from backend.main import app
from backend.dependencies.auth import get_current_user

client = TestClient(app)

@pytest.fixture
def mock_verify_id_token(monkeypatch):
    def _mock_verify(token):
        if token == "valid_token":
            return {"uid": "12345", "email": "test@example.com"}
        elif token == "expired_token":
            raise ValueError("Token expired")
        return None
    
    # We patch verify_id_token in the auth dependency module where it's imported
    monkeypatch.setattr("backend.dependencies.auth.verify_id_token", _mock_verify)

def test_analyze_resume_missing_auth():
    """Test missing Authorization header."""
    response = client.post("/api/resume/analyze", files={"file": ("test.pdf", b"dummy")})
    assert response.status_code == 401

def test_analyze_resume_invalid_format():
    """Test invalid Authorization format (not Bearer)."""
    response = client.post(
        "/api/resume/analyze", 
        files={"file": ("test.pdf", b"dummy")},
        headers={"Authorization": "Basic dXNlcjpwYXNz"}
    )
    assert response.status_code == 401

def test_analyze_resume_missing_bearer_token():
    """Test missing token after Bearer."""
    response = client.post(
        "/api/resume/analyze", 
        files={"file": ("test.pdf", b"dummy")},
        headers={"Authorization": "Bearer "}
    )
    # HTTPBearer in FastAPI expects a non-empty token string
    assert response.status_code == 401

def test_analyze_resume_invalid_token(mock_verify_id_token):
    """Test invalid Firebase token."""
    response = client.post(
        "/api/resume/analyze", 
        files={"file": ("test.pdf", b"dummy")},
        headers={"Authorization": "Bearer invalid_token"}
    )
    assert response.status_code == 401
    assert "Invalid or expired authentication token" in response.json()["detail"]

def test_analyze_resume_expired_token(mock_verify_id_token, monkeypatch):
    """Test expired Firebase token."""
    # We mock verify_id_token to return None for expired as well, mimicking firebase_service behavior
    # since firebase_service.verify_id_token catches exceptions and returns None
    def _mock_verify_expired(token):
        return None
    monkeypatch.setattr("backend.dependencies.auth.verify_id_token", _mock_verify_expired)

    response = client.post(
        "/api/resume/analyze", 
        files={"file": ("test.pdf", b"dummy")},
        headers={"Authorization": "Bearer expired_token"}
    )
    assert response.status_code == 401
    assert "Invalid or expired authentication token" in response.json()["detail"]

def test_analyze_resume_valid_token_mocked_dependency():
    """Test the endpoint using dependency override for a valid user."""
    # Override the dependency to bypass Firebase check completely for endpoint testing
    async def mock_get_current_user():
        return {"uid": "12345", "email": "override@example.com"}
    
    app.dependency_overrides[get_current_user] = mock_get_current_user
    
    # We send an unparsable file to ensure we get past Auth and hit the Parser logic
    response = client.post(
        "/api/resume/analyze", 
        files={"file": ("test.txt", b"dummy text content that is not pdf or docx")}
    )
    
    # Clean up override
    app.dependency_overrides = {}
    
    # 422 means it passed auth and hit the parse error (since test.txt isn't supported)
    assert response.status_code == 422
    assert "Unsupported file format" in response.json()["detail"] or "Unsupported file type" in response.json()["detail"]
