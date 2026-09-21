"""
Pydantic models for analysis results returned by the scoring,
matching, and suggestion services.
"""

from pydantic import BaseModel


class CategoryScores(BaseModel):
    skills: float = 0
    experience: float = 0
    projects: float = 0
    education: float = 0
    structure: float = 0
    ats_readiness: float = 0
    certifications: float = 0
    achievements: float = 0


class ScoreResult(BaseModel):
    overall_score: float = 0
    categories: CategoryScores = CategoryScores()
    strengths: list[str] = []
    weaknesses: list[str] = []
    improvement_priorities: list[str] = []


class RoleMatch(BaseModel):
    role: str
    match_percentage: float
    matched_skills: list[str] = []
    missing_required: list[str] = []
    missing_preferred: list[str] = []
    explanation: str = ""


class JobMatch(BaseModel):
    job_id: int
    title: str
    company: str
    location: str
    employment_type: str
    experience: str
    match_percentage: float
    matched_skills: list[str] = []
    missing_skills: list[str] = []


class SkillGapPriority(BaseModel):
    high: list[str] = []
    medium: list[str] = []
    low: list[str] = []


class SkillGap(BaseModel):
    target_role: str = ""
    existing_skills: list[str] = []
    gaps: SkillGapPriority = SkillGapPriority()


class ImprovementSuggestions(BaseModel):
    summary_suggestion: str = ""
    bullet_point_suggestions: list[str] = []
    missing_sections: list[str] = []
    keyword_suggestions: list[str] = []
    general_suggestions: list[str] = []


class AnalysisResponse(BaseModel):
    resume_profile: dict = {}
    score: ScoreResult = ScoreResult()
    role_matches: list[RoleMatch] = []
    job_matches: list[JobMatch] = []
    skill_gaps: SkillGap = SkillGap()
    suggestions: ImprovementSuggestions = ImprovementSuggestions()
