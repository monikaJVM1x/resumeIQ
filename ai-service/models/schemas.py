from pydantic import BaseModel
from typing import List, Optional

class ResumeProfile(BaseModel):
    candidate_name: str = ""
    professional_summary: str = ""
    education: List[str] = []
    skills: List[str] = []
    programming_languages: List[str] = []
    frameworks: List[str] = []
    databases: List[str] = []
    cloud_tools: List[str] = []
    experience: List[str] = []
    projects: List[str] = []
    certifications: List[str] = []
    achievements: List[str] = []
    soft_skills: List[str] = []
    
    def all_technical_skills(self) -> List[str]:
        combined = (
            self.skills
            + self.programming_languages
            + self.frameworks
            + self.databases
            + self.cloud_tools
        )
        seen: set[str] = set()
        unique: List[str] = []
        for skill in combined:
            normalised = skill.lower().strip()
            if normalised not in seen:
                seen.add(normalised)
                unique.append(skill)
        return unique

class ExtractRequest(BaseModel):
    resumeText: str

class RoleMatch(BaseModel):
    role: str
    match_percentage: float
    matched_skills: List[str] = []
    missing_required: List[str] = []
    missing_preferred: List[str] = []
    explanation: str = ""

class RoleExplanationsRequest(BaseModel):
    resume_profile: ResumeProfile
    role_matches: List[RoleMatch]

class ImprovementSuggestionsRequest(BaseModel):
    resume_profile: ResumeProfile
    weaknesses: List[str]
    missing_sections: List[str]

class ImprovementSuggestions(BaseModel):
    summary_suggestion: str = ""
    bullet_point_suggestions: List[str] = []
    missing_sections: List[str] = []
    keyword_suggestions: List[str] = []
    general_suggestions: List[str] = []
