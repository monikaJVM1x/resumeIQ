"""
Pydantic model for a job listing loaded from data/jobs.json.
"""

from pydantic import BaseModel


class Job(BaseModel):
    id: int
    title: str
    company: str
    location: str
    employment_type: str
    experience: str
    required_skills: list[str] = []
    preferred_skills: list[str] = []
    description: str = ""
