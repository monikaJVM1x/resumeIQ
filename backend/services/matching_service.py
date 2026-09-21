"""
Role and job matching service.

All match percentages are calculated deterministically in Python.
The LLM is NOT involved in any numerical computation here.
"""

import re

from backend.models.analysis import JobMatch, RoleMatch, SkillGap, SkillGapPriority
from backend.models.job import Job
from backend.models.resume import ResumeProfile

# ---------------------------------------------------------------------------
# Predefined role catalog
# ---------------------------------------------------------------------------

ROLE_CATALOG: list[dict] = [
    {
        "role": "Software Engineer",
        "required_skills": ["Python", "Git", "SQL", "REST API"],
        "preferred_skills": ["Docker", "AWS", "CI/CD", "Linux"],
    },
    {
        "role": "Backend Developer",
        "required_skills": ["Python", "SQL", "REST API", "Git"],
        "preferred_skills": ["Docker", "Redis", "Kubernetes", "FastAPI", "Django"],
    },
    {
        "role": "Java Developer",
        "required_skills": ["Java", "Spring Boot", "SQL"],
        "preferred_skills": ["Docker", "AWS", "MongoDB", "Maven", "JUnit"],
    },
    {
        "role": "Python Developer",
        "required_skills": ["Python", "Git", "REST API"],
        "preferred_skills": ["FastAPI", "Django", "Flask", "Docker", "PostgreSQL"],
    },
    {
        "role": "Full Stack Developer",
        "required_skills": ["JavaScript", "HTML", "CSS", "REST API"],
        "preferred_skills": ["React", "Node.js", "SQL", "Docker", "TypeScript"],
    },
    {
        "role": "Data Analyst",
        "required_skills": ["SQL", "Python", "Excel"],
        "preferred_skills": ["Tableau", "Power BI", "Pandas", "NumPy", "Statistics"],
    },
    {
        "role": "Data Engineer",
        "required_skills": ["Python", "SQL", "ETL"],
        "preferred_skills": ["Spark", "Airflow", "AWS", "Kafka", "Databricks"],
    },
    {
        "role": "Machine Learning Engineer",
        "required_skills": ["Python", "Machine Learning", "SQL"],
        "preferred_skills": ["TensorFlow", "PyTorch", "Scikit-learn", "Docker", "AWS"],
    },
    {
        "role": "DevOps Engineer",
        "required_skills": ["Docker", "Linux", "CI/CD", "Git"],
        "preferred_skills": ["Kubernetes", "AWS", "Terraform", "Ansible", "Jenkins"],
    },
    {
        "role": "QA Engineer",
        "required_skills": ["Testing", "SQL", "Git"],
        "preferred_skills": ["Selenium", "Jest", "Postman", "CI/CD", "Python"],
    },
]

# ---------------------------------------------------------------------------
# Skill normalisation
# ---------------------------------------------------------------------------

# Simple synonym map — maps alternative spellings/forms to canonical form
SKILL_SYNONYMS: dict[str, str] = {
    "springboot": "spring boot",
    "spring-boot": "spring boot",
    "nodejs": "node.js",
    "node js": "node.js",
    "reactjs": "react",
    "react.js": "react",
    "vuejs": "vue.js",
    "vue js": "vue.js",
    "angularjs": "angular",
    "postgres": "postgresql",
    "postgressql": "postgresql",
    "mongo": "mongodb",
    "k8s": "kubernetes",
    "gke": "kubernetes",
    "py": "python",
    "js": "javascript",
    "ts": "typescript",
    "ml": "machine learning",
    "dl": "deep learning",
    "tf": "tensorflow",
    "sklearn": "scikit-learn",
    "scikit learn": "scikit-learn",
    "ci cd": "ci/cd",
    "cicd": "ci/cd",
    "rest": "rest api",
    "restful": "rest api",
    "restful api": "rest api",
    "amazon web services": "aws",
    "google cloud": "gcp",
    "google cloud platform": "gcp",
    "azure cloud": "azure",
    "microsoft azure": "azure",
    "powerbi": "power bi",
    "power-bi": "power bi",
}


def _normalise_skill(skill: str) -> str:
    """Lowercase, strip punctuation extremes, and apply synonym map."""
    lower = skill.lower().strip()
    # Remove trailing punctuation
    lower = re.sub(r"[.,;:!?]+$", "", lower)
    return SKILL_SYNONYMS.get(lower, lower)


def _skills_match(candidate_skill: str, required_skill: str) -> bool:
    """Return True if the candidate skill satisfies the required skill."""
    c = _normalise_skill(candidate_skill)
    r = _normalise_skill(required_skill)
    # Exact match or substring containment (e.g., "spring boot" in "spring boot 3")
    return c == r or r in c or c in r


# ---------------------------------------------------------------------------
# Role matching
# ---------------------------------------------------------------------------

def match_roles(profile: ResumeProfile, top_n: int = 5) -> list[RoleMatch]:
    """
    Match the candidate's skills against the predefined role catalog.
    Returns the top_n roles sorted by match percentage descending.
    """
    candidate_skills = profile.all_technical_skills()
    results: list[RoleMatch] = []

    for role_def in ROLE_CATALOG:
        role_name = role_def["role"]
        required = role_def["required_skills"]
        preferred = role_def["preferred_skills"]

        matched_req, missing_req = _split_matched_missing(candidate_skills, required)
        matched_pref, missing_pref = _split_matched_missing(candidate_skills, preferred)

        pct = _calculate_match_percentage(
            matched_req=len(matched_req),
            total_req=len(required),
            matched_pref=len(matched_pref),
            total_pref=len(preferred),
        )

        results.append(
            RoleMatch(
                role=role_name,
                match_percentage=round(pct, 1),
                matched_skills=matched_req + matched_pref,
                missing_required=missing_req,
                missing_preferred=missing_pref,
            )
        )

    results.sort(key=lambda r: r.match_percentage, reverse=True)
    return results[:top_n]


def _split_matched_missing(
    candidate_skills: list[str],
    target_skills: list[str],
) -> tuple[list[str], list[str]]:
    """Return (matched_skills, missing_skills) for a list of target skills."""
    matched: list[str] = []
    missing: list[str] = []
    for skill in target_skills:
        if any(_skills_match(cs, skill) for cs in candidate_skills):
            matched.append(skill)
        else:
            missing.append(skill)
    return matched, missing


def _calculate_match_percentage(
    matched_req: int,
    total_req: int,
    matched_pref: int,
    total_pref: int,
) -> float:
    """Calculate weighted match percentage (required=70%, preferred=30%)."""
    req_ratio = matched_req / total_req if total_req > 0 else 0.0
    pref_ratio = matched_pref / total_pref if total_pref > 0 else 0.0
    return (req_ratio * 0.70 + pref_ratio * 0.30) * 100


# ---------------------------------------------------------------------------
# Job matching
# ---------------------------------------------------------------------------

def match_jobs(profile: ResumeProfile, jobs: list[Job], top_n: int = 5) -> list[JobMatch]:
    """
    Match the candidate against every job in the dataset.
    Returns the top_n jobs sorted by match percentage descending.
    """
    candidate_skills = profile.all_technical_skills()
    results: list[JobMatch] = []

    for job in jobs:
        matched_req, missing_req = _split_matched_missing(candidate_skills, job.required_skills)
        matched_pref, missing_pref = _split_matched_missing(candidate_skills, job.preferred_skills)

        pct = _calculate_match_percentage(
            matched_req=len(matched_req),
            total_req=len(job.required_skills),
            matched_pref=len(matched_pref),
            total_pref=len(job.preferred_skills),
        )

        results.append(
            JobMatch(
                job_id=job.id,
                title=job.title,
                company=job.company,
                location=job.location,
                employment_type=job.employment_type,
                experience=job.experience,
                match_percentage=round(pct, 1),
                matched_skills=matched_req + matched_pref,
                missing_skills=missing_req + missing_pref,
            )
        )

    results.sort(key=lambda j: j.match_percentage, reverse=True)
    return results[:top_n]


# ---------------------------------------------------------------------------
# Skill gap analysis
# ---------------------------------------------------------------------------

def analyse_skill_gaps(
    profile: ResumeProfile,
    top_role: RoleMatch,
) -> SkillGap:
    """
    For the top matching role, categorise missing skills by priority.
    Required missing → High Priority
    Preferred missing → Medium Priority
    """
    high = top_role.missing_required
    medium = top_role.missing_preferred
    low: list[str] = []  # Optional/low-priority — kept empty for simplicity

    return SkillGap(
        target_role=top_role.role,
        existing_skills=top_role.matched_skills,
        gaps=SkillGapPriority(high=high, medium=medium, low=low),
    )
