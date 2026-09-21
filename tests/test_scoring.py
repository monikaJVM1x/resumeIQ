"""
Unit tests for scoring_service.py

All tests are deterministic — no external API calls.
"""

import pytest

from backend.models.resume import ResumeProfile
from backend.services.scoring_service import (
    WEIGHT_ACHIEVEMENTS,
    WEIGHT_ATS,
    WEIGHT_CERTIFICATIONS,
    WEIGHT_EDUCATION,
    WEIGHT_EXPERIENCE,
    WEIGHT_PROJECTS,
    WEIGHT_SKILLS,
    WEIGHT_STRUCTURE,
    calculate_score,
    get_missing_sections,
)


class TestScoreEmptyProfile:
    """An empty profile should produce a very low but valid score."""

    def test_overall_score_is_numeric(self):
        profile = ResumeProfile()
        result = calculate_score(profile)
        assert isinstance(result.overall_score, float)

    def test_overall_score_is_zero_for_empty_profile(self):
        profile = ResumeProfile()
        result = calculate_score(profile)
        assert result.overall_score == 0.0

    def test_all_categories_are_zero(self):
        profile = ResumeProfile()
        result = calculate_score(profile)
        cats = result.categories
        assert cats.skills == 0
        assert cats.experience == 0
        assert cats.projects == 0
        assert cats.education == 0
        assert cats.structure == 0
        assert cats.ats_readiness == 0
        assert cats.certifications == 0
        assert cats.achievements == 0


class TestScoreCompleteProfile:
    """A well-populated profile should score high."""

    def _make_rich_profile(self) -> ResumeProfile:
        return ResumeProfile(
            candidate_name="Jane Doe",
            professional_summary="Experienced software engineer with 5 years building Python APIs.",
            education=["B.Tech in Computer Science, IIT Delhi, 2019"],
            skills=["REST API", "Git", "Linux", "CI/CD", "Agile"],
            programming_languages=["Python", "Java", "SQL"],
            frameworks=["FastAPI", "Django", "Spring Boot"],
            databases=["PostgreSQL", "MongoDB"],
            cloud_tools=["AWS", "Docker", "Kubernetes"],
            experience=[
                "Senior Python Developer at TechCorp (2022–present): "
                "Led migration of 3 monoliths to microservices, reducing latency by 40%.",
                "Backend Engineer at StartupXYZ (2020–2022): "
                "Built FastAPI services serving 500k daily requests.",
            ],
            projects=[
                "ResumeIQ: AI-powered resume analyser built with FastAPI and Groq.",
                "ETL Pipeline: Automated data pipeline processing 1M rows/day using Airflow.",
            ],
            # 3 entries each → score_certifications returns 5, score_achievements returns 5
            certifications=[
                "AWS Solutions Architect Associate",
                "Docker Certified Associate",
                "Kubernetes Administrator (CKA)",
            ],
            achievements=[
                "Top performer 2023 at TechCorp",
                "Speaker at PyCon India 2022",
                "Open-source maintainer with 500+ GitHub stars",
            ],
            soft_skills=["Leadership", "Communication"],
        )

    def test_overall_score_is_high(self):
        profile = self._make_rich_profile()
        result = calculate_score(profile)
        assert result.overall_score >= 70, f"Expected >= 70, got {result.overall_score}"

    def test_skills_score_at_max(self):
        profile = self._make_rich_profile()
        result = calculate_score(profile)
        assert result.categories.skills == WEIGHT_SKILLS

    def test_certifications_score_at_max(self):
        profile = self._make_rich_profile()
        result = calculate_score(profile)
        assert result.categories.certifications == WEIGHT_CERTIFICATIONS

    def test_achievements_score_at_max(self):
        profile = self._make_rich_profile()
        result = calculate_score(profile)
        assert result.categories.achievements == WEIGHT_ACHIEVEMENTS

    def test_total_score_equals_sum_of_categories(self):
        profile = self._make_rich_profile()
        result = calculate_score(profile)
        cats = result.categories
        expected = round(
            cats.skills
            + cats.experience
            + cats.projects
            + cats.education
            + cats.structure
            + cats.ats_readiness
            + cats.certifications
            + cats.achievements,
            1,
        )
        assert result.overall_score == expected

    def test_score_does_not_exceed_100(self):
        profile = self._make_rich_profile()
        result = calculate_score(profile)
        assert result.overall_score <= 100


class TestScoreCategoryCalculations:
    """Test individual scoring buckets."""

    def test_no_skills_scores_zero(self):
        profile = ResumeProfile()
        result = calculate_score(profile)
        assert result.categories.skills == 0

    def test_15_skills_scores_max(self):
        profile = ResumeProfile(
            skills=["Python", "Java", "SQL", "Docker", "AWS"],
            programming_languages=["JavaScript", "TypeScript", "Go"],
            frameworks=["React", "FastAPI", "Django", "Spring Boot"],
            databases=["PostgreSQL", "MongoDB", "Redis"],
        )
        result = calculate_score(profile)
        assert result.categories.skills == WEIGHT_SKILLS

    def test_no_experience_scores_zero(self):
        profile = ResumeProfile()
        result = calculate_score(profile)
        assert result.categories.experience == 0

    def test_experience_with_metrics_gets_bonus(self):
        profile_no_metrics = ResumeProfile(experience=["Worked at Acme Corp as developer"])
        profile_with_metrics = ResumeProfile(
            experience=["Worked at Acme Corp, improved latency by 30%"]
        )
        no_metrics_score = calculate_score(profile_no_metrics).categories.experience
        with_metrics_score = calculate_score(profile_with_metrics).categories.experience
        assert with_metrics_score >= no_metrics_score

    def test_no_certifications_scores_zero(self):
        profile = ResumeProfile()
        result = calculate_score(profile)
        assert result.categories.certifications == 0

    def test_two_certifications_scores_4(self):
        profile = ResumeProfile(certifications=["AWS SAA", "CKA"])
        result = calculate_score(profile)
        assert result.categories.certifications == 4


class TestMissingSections:
    def test_all_missing_for_empty_profile(self):
        profile = ResumeProfile()
        missing = get_missing_sections(profile)
        assert "Professional Summary / Objective" in missing
        assert "Education" in missing
        assert "Skills" in missing
        assert "Work Experience" in missing

    def test_nothing_missing_for_complete_profile(self):
        profile = ResumeProfile(
            professional_summary="A great developer.",
            education=["B.Tech CS"],
            skills=["Python"],
            experience=["Software Engineer at Acme"],
            projects=["Cool project"],
            certifications=["AWS SAA"],
            achievements=["Best employee 2023"],
        )
        missing = get_missing_sections(profile)
        assert missing == []
