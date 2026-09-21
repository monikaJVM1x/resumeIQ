"""
Jobs router — exposes the sample job dataset.
"""

from fastapi import APIRouter

from backend.models.job import Job
from backend.services.job_service import load_jobs

router = APIRouter()


@router.get("/jobs", response_model=list[Job])
def get_jobs() -> list[Job]:
    """Return all sample job listings."""
    return load_jobs()
