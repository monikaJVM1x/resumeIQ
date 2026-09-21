"""
Pydantic models for resume data extracted by the AI service.
"""

from pydantic import BaseModel


class ResumeProfile(BaseModel):
    """Structured representation of a candidate's resume."""

    candidate_name: str = ""
    professional_summary: str = ""
    education: list[str] = []
    skills: list[str] = []
    programming_languages: list[str] = []
    frameworks: list[str] = []
    databases: list[str] = []
    cloud_tools: list[str] = []
    experience: list[str] = []
    projects: list[str] = []
    certifications: list[str] = []
    achievements: list[str] = []
    soft_skills: list[str] = []

    def all_technical_skills(self) -> list[str]:
        """Return a deduplicated flat list of every technical skill."""
        combined = (
            self.skills
            + self.programming_languages
            + self.frameworks
            + self.databases
            + self.cloud_tools
        )
        seen: set[str] = set()
        unique: list[str] = []
        for skill in combined:
            normalised = skill.lower().strip()
            if normalised not in seen:
                seen.add(normalised)
                unique.append(skill)
        return unique
