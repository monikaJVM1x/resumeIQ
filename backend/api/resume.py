"""
Resume API router — handles file upload and complete analysis pipeline.
"""

import logging

from fastapi import APIRouter, HTTPException, UploadFile, Depends
from fastapi.responses import JSONResponse

from backend.models.analysis import AnalysisResponse
from backend.services import ai_service, job_service, matching_service, scoring_service
from backend.services.resume_parser import ParserError, parse_resume
from backend.utils.text_cleaner import clean_resume_text, is_text_meaningful
from backend.dependencies.auth import get_current_user

logger = logging.getLogger(__name__)

router = APIRouter()


@router.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume(file: UploadFile, current_user: dict = Depends(get_current_user)) -> AnalysisResponse:
    """
    Accept a PDF or DOCX resume, run the full analysis pipeline, and return results.

    Pipeline:
      1. Parse file → raw text
      2. Clean text
      3. Groq extraction → ResumeProfile
      4. Score calculation (Python)
      5. Role matching (Python)
      6. Job matching (Python)
      7. Skill gap analysis (Python)
      8. Groq role explanations
      9. Groq improvement suggestions
    """
    # --- Step 1: Parse ---
    try:
        file_bytes = await file.read()
        parse_result = parse_resume(file_bytes, file.filename or "upload")
    except ParserError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    except Exception as exc:
        logger.error("Unexpected parse error: %s", exc)
        raise HTTPException(status_code=500, detail="Failed to process the uploaded file.")

    # --- Step 2: Clean ---
    raw_text: str = parse_result["text"]
    clean_text = clean_resume_text(raw_text)

    if not is_text_meaningful(clean_text, min_words=30):
        raise HTTPException(
            status_code=422,
            detail="The resume appears to be too short or empty. "
                   "Please upload a resume with more content.",
        )

    # --- Step 3: Groq extraction ---
    try:
        profile = ai_service.extract_resume_profile(clean_text)
    except EnvironmentError as exc:
        raise HTTPException(status_code=503, detail=str(exc))
    except Exception as exc:
        logger.error("AI extraction failed: %s", exc)
        raise HTTPException(
            status_code=503,
            detail="We couldn't complete the AI analysis right now. Please try again in a moment.",
        )

    # --- Step 4: Score (deterministic Python) ---
    score_result = scoring_service.calculate_score(profile)
    missing_sections = scoring_service.get_missing_sections(profile)

    # --- Step 5: Role matching (deterministic Python) ---
    role_matches = matching_service.match_roles(profile, top_n=5)

    # --- Step 6: Job matching (deterministic Python) ---
    jobs = job_service.load_jobs()
    job_matches = matching_service.match_jobs(profile, jobs, top_n=5)

    # --- Step 7: Skill gap analysis ---
    skill_gap = None
    if role_matches:
        skill_gap = matching_service.analyse_skill_gaps(profile, role_matches[0])

    # --- Step 8: Groq role explanations (optional enrichment) ---
    try:
        role_matches = ai_service.generate_role_explanations(profile, role_matches)
    except Exception as exc:
        logger.warning("Role explanation generation skipped: %s", exc)

    # --- Step 9: Groq improvement suggestions ---
    try:
        suggestions = ai_service.generate_improvement_suggestions(
            profile,
            score_result.weaknesses,
            missing_sections,
        )
    except Exception as exc:
        logger.warning("Suggestion generation skipped: %s", exc)
        from backend.models.analysis import ImprovementSuggestions
        suggestions = ImprovementSuggestions(
            general_suggestions=["Could not generate suggestions. Please try again."]
        )

    from backend.models.analysis import SkillGap, SkillGapPriority
    return AnalysisResponse(
        resume_profile=profile.model_dump(),
        score=score_result,
        role_matches=role_matches,
        job_matches=job_matches,
        skill_gaps=skill_gap or SkillGap(),
        suggestions=suggestions,
    )
