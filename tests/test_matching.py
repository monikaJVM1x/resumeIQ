"""
Unit tests for matching_service.py

All tests are deterministic — no external API calls.
"""

import pytest

from backend.models.job import Job
from backend.models.resume import ResumeProfile
from backend.services.matching_service import (
    _normalise_skill,
    _skills_match,
    analyse_skill_gaps,
    match_jobs,
    match_roles,
)


class TestSkillNormalisation:
    def test_springboot_maps_to_spring_boot(self):
        assert _normalise_skill("springboot") == "spring boot"

    def test_spring_hyphen_boot_maps_to_spring_boot(self):
        assert _normalise_skill("spring-boot") == "spring boot"

    def test_nodejs_maps_to_node_js(self):
        assert _normalise_skill("nodejs") == "node.js"

    def test_k8s_maps_to_kubernetes(self):
        assert _normalise_skill("k8s") == "kubernetes"

    def test_ml_maps_to_machine_learning(self):
        assert _normalise_skill("ml") == "machine learning"

    def test_unknown_skill_returned_lowercased(self):
        assert _normalise_skill("FastAPI") == "fastapi"


class TestSkillsMatch:
    def test_exact_match(self):
        assert _skills_match("Python", "Python") is True

    def test_case_insensitive_match(self):
        assert _skills_match("python", "Python") is True

    def test_synonym_match_springboot(self):
        assert _skills_match("springboot", "Spring Boot") is True

    def test_no_match(self):
        assert _skills_match("Java", "Python") is False

    def test_partial_containment(self):
        # "rest api" is contained in "rest api design"
        assert _skills_match("REST API Design", "REST API") is True


class TestRoleMatchingJavaDeveloper:
    def _java_profile(self) -> ResumeProfile:
        return ResumeProfile(
            skills=["REST API", "Git"],
            programming_languages=["Java", "SQL"],
            frameworks=["Spring Boot"],
        )

    def test_java_developer_is_top_match(self):
        profile = self._java_profile()
        matches = match_roles(profile, top_n=3)
        top_roles = [m.role for m in matches]
        assert "Java Developer" in top_roles, f"Java Developer not in {top_roles}"

    def test_java_developer_has_high_match_percentage(self):
        profile = self._java_profile()
        matches = match_roles(profile, top_n=10)
        java_match = next(m for m in matches if m.role == "Java Developer")
        assert java_match.match_percentage >= 70, (
            f"Expected >= 70%, got {java_match.match_percentage}%"
        )

    def test_matched_skills_not_empty(self):
        profile = self._java_profile()
        matches = match_roles(profile, top_n=5)
        java_match = next((m for m in matches if m.role == "Java Developer"), None)
        assert java_match is not None
        assert len(java_match.matched_skills) > 0


class TestRoleMatchingMissingSkills:
    def _python_only_profile(self) -> ResumeProfile:
        return ResumeProfile(programming_languages=["Python"])

    def test_java_developer_is_not_top_match_for_python_only(self):
        profile = self._python_only_profile()
        matches = match_roles(profile, top_n=3)
        top_roles = [m.role for m in matches]
        assert "Java Developer" not in top_roles

    def test_java_developer_has_missing_required_skills(self):
        profile = self._python_only_profile()
        matches = match_roles(profile, top_n=10)
        java_match = next(m for m in matches if m.role == "Java Developer")
        # Java Developer requires Java, Spring Boot, SQL — Python profile has none
        assert "Java" in java_match.missing_required
        assert "Spring Boot" in java_match.missing_required


class TestJobMatching:
    def _sample_jobs(self) -> list[Job]:
        return [
            Job(
                id=1,
                title="Python Backend Developer",
                company="Acme",
                location="Bangalore",
                employment_type="Full-time",
                experience="1-3 years",
                required_skills=["Python", "SQL", "REST API"],
                preferred_skills=["Docker", "FastAPI"],
            ),
            Job(
                id=2,
                title="Java Developer",
                company="TechCorp",
                location="Mumbai",
                employment_type="Full-time",
                experience="0-2 years",
                required_skills=["Java", "Spring Boot", "SQL"],
                preferred_skills=["Docker"],
            ),
        ]

    def test_python_profile_matches_python_job_higher(self):
        profile = ResumeProfile(
            programming_languages=["Python"],
            skills=["SQL", "REST API"],
            frameworks=["FastAPI"],
        )
        jobs = self._sample_jobs()
        matches = match_jobs(profile, jobs, top_n=2)
        assert matches[0].title == "Python Backend Developer"

    def test_all_required_matched_gives_high_percentage(self):
        profile = ResumeProfile(
            programming_languages=["Python"],
            skills=["SQL", "REST API"],
        )
        jobs = self._sample_jobs()
        matches = match_jobs(profile, jobs, top_n=2)
        python_match = next(m for m in matches if "Python" in m.title)
        # All 3 required skills matched, 70% weight → at least 70%
        assert python_match.match_percentage >= 70

    def test_returns_top_n(self):
        profile = ResumeProfile(programming_languages=["Python"])
        jobs = self._sample_jobs()
        matches = match_jobs(profile, jobs, top_n=1)
        assert len(matches) == 1


class TestSkillGapAnalysis:
    def test_missing_required_goes_to_high_priority(self):
        profile = ResumeProfile(programming_languages=["Python"])
        matches = match_roles(profile, top_n=5)
        # Find a role where some required skills are missing
        role_with_gaps = next(
            (m for m in matches if m.missing_required),
            None,
        )
        if role_with_gaps is None:
            pytest.skip("No role with missing required skills for this profile")
        gap = analyse_skill_gaps(profile, role_with_gaps)
        assert set(gap.gaps.high) == set(role_with_gaps.missing_required)

    def test_missing_preferred_goes_to_medium_priority(self):
        profile = ResumeProfile(programming_languages=["Python"])
        matches = match_roles(profile, top_n=5)
        role_with_gaps = next(
            (m for m in matches if m.missing_preferred),
            None,
        )
        if role_with_gaps is None:
            pytest.skip("No role with missing preferred skills for this profile")
        gap = analyse_skill_gaps(profile, role_with_gaps)
        assert set(gap.gaps.medium) == set(role_with_gaps.missing_preferred)
