import logging
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv

from models.schemas import (
    ExtractRequest,
    ResumeProfile,
    RoleExplanationsRequest,
    RoleMatch,
    ImprovementSuggestionsRequest,
    ImprovementSuggestions
)
from services import groq_service

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)s  %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)

app = FastAPI(title="ResumeIQ AI Microservice")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def health_check():
    return {"status": "ok", "service": "ResumeIQ AI Service"}

@app.post("/ai/extract", response_model=ResumeProfile)
def extract(request: ExtractRequest):
    try:
        return groq_service.extract_resume_profile(request.resumeText)
    except EnvironmentError as e:
        raise HTTPException(status_code=503, detail=str(e))
    except Exception as e:
        logger.error(f"Extraction error: {e}")
        raise HTTPException(status_code=500, detail="Internal extraction error")

@app.post("/ai/role-explanations", response_model=list[RoleMatch])
def role_explanations(request: RoleExplanationsRequest):
    try:
        return groq_service.generate_role_explanations(request.resume_profile, request.role_matches)
    except EnvironmentError as e:
        raise HTTPException(status_code=503, detail=str(e))
    except Exception as e:
        logger.error(f"Role explanation error: {e}")
        raise HTTPException(status_code=500, detail="Internal error")

@app.post("/ai/improvement-suggestions", response_model=ImprovementSuggestions)
def improvement_suggestions(request: ImprovementSuggestionsRequest):
    try:
        return groq_service.generate_improvement_suggestions(
            request.resume_profile, 
            request.weaknesses, 
            request.missing_sections
        )
    except EnvironmentError as e:
        raise HTTPException(status_code=503, detail=str(e))
    except Exception as e:
        logger.error(f"Suggestion error: {e}")
        raise HTTPException(status_code=500, detail="Internal error")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=9000)
