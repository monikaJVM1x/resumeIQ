"""
Analysis router — exposes role catalog for reference.
"""

from fastapi import APIRouter

from backend.services.matching_service import ROLE_CATALOG

router = APIRouter()


@router.get("/roles")
def get_roles() -> list[dict]:
    """Return the predefined role catalog."""
    return ROLE_CATALOG
