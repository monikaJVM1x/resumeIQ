import pytest
from fastapi import HTTPException
from fastapi.security import HTTPAuthorizationCredentials
from backend.dependencies.auth import get_current_user

@pytest.fixture
def anyio_backend():
    return 'asyncio'

@pytest.mark.anyio
async def test_get_current_user_valid(monkeypatch):
    # Mock verify_id_token to return a valid user payload
    def mock_verify_id_token(token):
        if token == "valid_token":
            return {"uid": "12345", "email": "test@example.com"}
        return None

    monkeypatch.setattr("backend.dependencies.auth.verify_id_token", mock_verify_id_token)

    credentials = HTTPAuthorizationCredentials(scheme="Bearer", credentials="valid_token")
    user = await get_current_user(credentials)
    
    assert user["uid"] == "12345"
    assert user["email"] == "test@example.com"

@pytest.mark.anyio
async def test_get_current_user_invalid(monkeypatch):
    # Mock verify_id_token to return None for invalid token
    def mock_verify_id_token(token):
        return None

    monkeypatch.setattr("backend.dependencies.auth.verify_id_token", mock_verify_id_token)

    credentials = HTTPAuthorizationCredentials(scheme="Bearer", credentials="invalid_token")
    
    with pytest.raises(HTTPException) as excinfo:
        await get_current_user(credentials)
        
    assert excinfo.value.status_code == 401
    assert "Invalid or expired authentication token" in str(excinfo.value.detail)

@pytest.mark.anyio
async def test_get_current_user_missing(monkeypatch):
    credentials = HTTPAuthorizationCredentials(scheme="Bearer", credentials="")
    
    with pytest.raises(HTTPException) as excinfo:
        await get_current_user(credentials)
        
    assert excinfo.value.status_code == 401
    assert "Missing authentication token" in str(excinfo.value.detail)
