"""
Deterministic resume scoring service.

Score breakdown (total = 100):
  Skills             20
  Experience         20
  Projects           15
  Education          10
  Structure          10
  ATS Readiness      15
  Certifications      5
  Achievements        5

All calculations are pure Python — the LLM is NOT involved.
"""

import re

from backend.models.analysis import CategoryScores, ScoreResult
from backend.models.resume import ResumeProfile

# ---------------------------------------------------------------------------
# Weight constants
# ---------------------------------------------------------------------------
WEIGHT_SKILLS = 20
WEIGHT_EXPERIENCE = 20
WEIGHT_PROJECTS = 15
WEIGHT_EDUCATION = 10
WEIGHT_STRUCTURE = 10
WEIGHT_ATS = 15
WEIGHT_CERTIFICATIONS = 5
WEIGHT_ACHIEVEMENTS = 5

# ATS keywords commonly found in strong resumes
ATS_ACTION_VERBS = {
    "developed", "built", "designed", "implemented", "led", "managed",
    "improved", "optimised", "optimized", "created", "delivered", "reduced",
    "increased", "automated", "deployed", "architected", "collaborated",
    "maintained", "migrated", "integrated", "analysed", "analyzed", "tested",
    "reviewed", "mentored", "launched", "engineered", "streamlined",
}

STANDARD_SECTIONS = [
    "summary", "objective", "profile",
    "education", "skills", "experience", "work experience",
    "projects", "certifications", "achievements", "accomplishments",
]

TECH_KEYWORDS = {
    "python", "java", "javascript", "typescript", "react", "node",
    "spring", "django", "fastapi", "flask", "sql", "nosql", "mongodb",
    "postgresql", "mysql", "docker", "kubernetes", "aws", "azure", "gcp",
    "git", "linux", "api", "rest", "graphql", "ci/cd", "agile", "scrum",
    "machine learning", "ml", "deep learning", "tensorflow", "pytorch",
    "data", "analytics", "excel", "tableau", "power bi",
}


def calculate_score(profile: ResumeProfile) -> ScoreResult:
    """
    Calculate the resume readiness score from a validated ResumeProfile.
    Returns a ScoreResult with category breakdown, strengths, and weaknesses.
    """
    skills_score = _score_skills(profile)
    experience_score = _score_experience(profile)
    projects_score = _score_projects(profile)
    education_score = _score_education(profile)
    structure_score = _score_structure(profile)
    ats_score = _score_ats(profile)
    cert_score = _score_certifications(profile)
    achievements_score = _score_achievements(profile)

    overall = round(
        skills_score
        + experience_score
        + projects_score
        + education_score
        + structure_score
        + ats_score
        + cert_score
        + achievements_score,
        1,
    )

    categories = CategoryScores(
        skills=round(skills_score, 1),
        experience=round(experience_score, 1),
        projects=round(projects_score, 1),
        education=round(education_score, 1),
        structure=round(structure_score, 1),
        ats_readiness=round(ats_score, 1),
        certifications=round(cert_score, 1),
        achievements=round(achievements_score, 1),
    )

    strengths, weaknesses = _derive_strengths_weaknesses(categories, profile)
    priorities = _derive_priorities(categories)

    return ScoreResult(
        overall_score=overall,
        categories=categories,
        strengths=strengths,
        weaknesses=weaknesses,
        improvement_priorities=priorities,
    )


# ---------------------------------------------------------------------------
# Category scorers
# ---------------------------------------------------------------------------

def _score_skills(profile: ResumeProfile) -> float:
    """Award up to 20 points based on number and diversity of skills."""
    all_skills = profile.all_technical_skills()
    count = len(all_skills)
    # Buckets: 0 skills = 0, 1-4 = 8, 5-9 = 13, 10-14 = 16, 15+ = 20
    if count == 0:
        return 0.0
    if count < 5:
        return 8.0
    if count < 10:
        return 13.0
    if count < 15:
        return 16.0
    return 20.0


def _score_experience(profile: ResumeProfile) -> float:
    """Award up to 20 points for experience quantity and quality."""
    entries = profile.experience
    if not entries:
        return 0.0
    count = len(entries)
    # Base score from number of entries
    if count == 1:
        base = 10.0
    elif count == 2:
        base = 14.0
    elif count == 3:
        base = 17.0
    else:
        base = 19.0
    # Bonus for entries containing numbers/metrics
    has_metrics = any(re.search(r"\d", entry) for entry in entries)
    bonus = 1.0 if has_metrics else 0.0
    return min(base + bonus, WEIGHT_EXPERIENCE)


def _score_projects(profile: ResumeProfile) -> float:
    """Award up to 15 points for projects with meaningful descriptions."""
    projects = profile.projects
    if not projects:
        return 0.0
    count = len(projects)
    # Average description length as a quality proxy
    avg_len = sum(len(p) for p in projects) / count
    if count == 1:
        base = 7.0
    elif count == 2:
        base = 10.0
    else:
        base = 12.0
    # Bonus for quality descriptions (> 60 chars average)
    bonus = 3.0 if avg_len > 60 else (1.5 if avg_len > 30 else 0.0)
    return min(base + bonus, WEIGHT_PROJECTS)


def _score_education(profile: ResumeProfile) -> float:
    """Award up to 10 points for education information."""
    education = profile.education
    if not education:
        return 0.0
    # Any education entry = 7 pts; detailed entry (> 40 chars) = 10 pts
    avg_len = sum(len(e) for e in education) / len(education)
    return 10.0 if avg_len > 40 else 7.0


def _score_structure(profile: ResumeProfile) -> float:
    """Award up to 10 points for presence of standard resume sections."""
    # Build a combined text block to check for section headings
    text_blocks = [
        profile.professional_summary,
        " ".join(profile.education),
        " ".join(profile.skills),
        " ".join(profile.experience),
        " ".join(profile.projects),
        " ".join(profile.certifications),
    ]
    combined = " ".join(text_blocks).lower()

    present_sections = 0
    if profile.professional_summary:
        present_sections += 1
    if profile.education:
        present_sections += 1
    if profile.skills or profile.programming_languages:
        present_sections += 1
    if profile.experience:
        present_sections += 1
    if profile.projects:
        present_sections += 1
    if profile.certifications:
        present_sections += 1

    # 6 sections = 10, 5 = 9, 4 = 7, 3 = 5, 2 = 3, 1 = 1
    mapping = {6: 10, 5: 9, 4: 7, 3: 5, 2: 3, 1: 1, 0: 0}
    return float(mapping.get(present_sections, 0))


def _score_ats(profile: ResumeProfile) -> float:
    """Award up to 15 points for ATS/keyword readiness."""
    all_text = " ".join([
        profile.professional_summary,
        " ".join(profile.experience),
        " ".join(profile.projects),
        " ".join(profile.skills),
    ]).lower()

    # Check for action verbs
    verb_count = sum(1 for verb in ATS_ACTION_VERBS if verb in all_text)
    verb_score = min(verb_count / 5 * 6, 6.0)  # max 6 pts

    # Check for tech keywords
    tech_count = sum(1 for kw in TECH_KEYWORDS if kw in all_text)
    tech_score = min(tech_count / 5 * 5, 5.0)  # max 5 pts

    # Check for numbers/metrics
    has_numbers = bool(re.search(r"\b\d+\s*(%|x|k|\+|years?|months?|users?|requests?)\b", all_text))
    metric_score = 4.0 if has_numbers else 0.0

    return min(verb_score + tech_score + metric_score, WEIGHT_ATS)


def _score_certifications(profile: ResumeProfile) -> float:
    """Award up to 5 points for certifications."""
    count = len(profile.certifications)
    if count == 0:
        return 0.0
    if count == 1:
        return 3.0
    if count == 2:
        return 4.0
    return 5.0


def _score_achievements(profile: ResumeProfile) -> float:
    """Award up to 5 points for notable achievements."""
    count = len(profile.achievements)
    if count == 0:
        return 0.0
    if count == 1:
        return 3.0
    if count == 2:
        return 4.0
    return 5.0


# ---------------------------------------------------------------------------
# Strengths / weaknesses derivation
# ---------------------------------------------------------------------------

def _derive_strengths_weaknesses(
    categories: CategoryScores,
    profile: ResumeProfile,
) -> tuple[list[str], list[str]]:
    """Return strengths and weaknesses based on category scores."""
    strengths: list[str] = []
    weaknesses: list[str] = []

    if categories.skills >= 16:
        strengths.append("Strong and diverse technical skill set")
    elif categories.skills < 10:
        weaknesses.append("Limited technical skills listed")

    if categories.experience >= 16:
        strengths.append("Solid professional experience")
    elif categories.experience < 8:
        weaknesses.append("Work experience section needs more detail")

    if categories.projects >= 12:
        strengths.append("Good portfolio of projects")
    elif categories.projects < 6:
        weaknesses.append("Few or brief project descriptions")

    if categories.education >= 8:
        strengths.append("Education section is well documented")
    elif categories.education == 0:
        weaknesses.append("No education information found")

    if categories.ats_readiness >= 12:
        strengths.append("Good use of action verbs and measurable results")
    elif categories.ats_readiness < 7:
        weaknesses.append("Resume lacks action verbs and quantified results")

    if categories.certifications >= 4:
        strengths.append("Relevant certifications listed")
    elif categories.certifications == 0:
        weaknesses.append("No certifications found")

    if categories.achievements >= 4:
        strengths.append("Notable achievements highlighted")
    elif categories.achievements == 0:
        weaknesses.append("No achievements section found")

    if categories.structure >= 8:
        strengths.append("Resume has a clear and complete structure")
    elif categories.structure < 5:
        weaknesses.append("Resume is missing standard sections")

    return strengths, weaknesses


def _derive_priorities(categories: CategoryScores) -> list[str]:
    """Return improvement priorities ranked by impact opportunity."""
    gaps: list[tuple[float, str]] = [
        (WEIGHT_EXPERIENCE - categories.experience, "Expand work experience with quantified achievements"),
        (WEIGHT_SKILLS - categories.skills, "Add more relevant technical skills"),
        (WEIGHT_ATS - categories.ats_readiness, "Use more action verbs and add measurable metrics"),
        (WEIGHT_PROJECTS - categories.projects, "Add detailed project descriptions"),
        (WEIGHT_STRUCTURE - categories.structure, "Ensure all standard sections are present"),
        (WEIGHT_EDUCATION - categories.education, "Complete the education section"),
        (WEIGHT_CERTIFICATIONS - categories.certifications, "Consider adding industry certifications"),
        (WEIGHT_ACHIEVEMENTS - categories.achievements, "Highlight key achievements"),
    ]
    # Sort by largest gap first
    gaps.sort(key=lambda x: x[0], reverse=True)
    return [item[1] for item in gaps if item[0] > 0][:5]


def get_missing_sections(profile: ResumeProfile) -> list[str]:
    """Return a list of standard sections that appear to be missing."""
    missing: list[str] = []
    if not profile.professional_summary:
        missing.append("Professional Summary / Objective")
    if not profile.education:
        missing.append("Education")
    if not profile.skills and not profile.programming_languages:
        missing.append("Skills")
    if not profile.experience:
        missing.append("Work Experience")
    if not profile.projects:
        missing.append("Projects")
    if not profile.certifications:
        missing.append("Certifications")
    if not profile.achievements:
        missing.append("Achievements")
    return missing
