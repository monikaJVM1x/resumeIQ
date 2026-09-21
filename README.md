# ResumeIQ

ResumeIQ is an AI-powered Resume Analysis and Job Matching platform that helps candidates understand how their resume aligns with common technical roles and predefined job requirements. 

It combines Groq-based natural language understanding with deterministic Python scoring and matching algorithms to produce an explainable, accurate, and predictable resume analysis.

---

## Problem Statement

Traditional resume review processes suffer from several significant flaws:
- **Time-Consuming & Subjective:** Manually analyzing resumes against job descriptions takes time and is highly dependent on the reviewer's personal bias.
- **Difficult for Candidates to Interpret:** Students and freshers often struggle to understand exactly *why* their resume is being rejected or how it quantifies against industry standards.
- **Inconsistent AI Results:** Simply uploading a resume to an LLM for a "score out of 100" often yields hallucinated, inconsistent, and unpredictable numbers that fluctuate between runs.
- **Lack of Actionable Skill Gaps:** Candidates need to clearly see their missing required and preferred skills, not just generic advice.

This necessitates a hybrid architecture that uses AI purely for *understanding* unstructured text, while relying on predictable, rule-based algorithms for *scoring* and *matching*.

---

## Solution

ResumeIQ solves this problem by strictly separating natural language extraction from numerical analysis. 

The application works by taking a raw PDF/DOCX resume and passing it through the following pipeline:

1. **Extraction:** The document text is extracted and cleaned.
2. **AI Structured Extraction:** Groq (Llama 3) converts the unstructured text into a highly structured JSON format.
3. **Validated Resume Profile:** Pydantic strictly validates this JSON into a robust Python data model.
4. **Deterministic Analysis:** Python algorithms process the profile to generate:
   - A Resume Readiness Score
   - Role Matching Percentages
   - Job Matching Percentages
   - Skill Gap Analysis
5. **AI Explanations & Suggestions:** Groq generates natural-language explanations of the role matches and actionable suggestions for improvement.
6. **Interactive Dashboard:** The results are presented in a responsive, authenticated React dashboard.

---

## Key Features

**Authentication**
- Google Sign-In
- Firebase Authentication
- Protected React routes
- Firebase ID token verification
- FastAPI protected endpoints
- Profile page & Logout

**Resume Processing**
- PDF and DOCX upload
- Text extraction and cleaning
- Robust file validation (size, type)

**AI Analysis**
- Structured resume extraction
- Candidate profile generation
- Natural-language role explanations
- Targeted improvement suggestions

**Deterministic Analysis**
- Resume readiness score & breakdown
- Predefined role matching
- Predefined job matching
- Required and preferred skill gap detection

**Dashboard**
- Extracted resume profile display
- Interactive score visualization
- Role and Job match cards
- Skill gap alerts

---

## System Architecture

```mermaid
flowchart TD
    subgraph Frontend [React + Vite Frontend]
        UI[React Dashboard]
        Auth[Firebase Auth (Google Sign-In)]
        UI --> Auth
    end

    subgraph Backend [FastAPI Backend]
        API[Protected API Endpoints]
        Parse[PyMuPDF / python-docx Parser]
        Val[Pydantic Validation]
        Score[Deterministic Scoring & Matching Engine]
    end

    subgraph AI [External AI Services]
        Groq[Groq Llama 3 70B]
    end

    UI -- "JWT + PDF/DOCX" --> API
    API -- "Verify Token" --> Auth
    API --> Parse
    Parse -- "Raw Text" --> Groq
    Groq -- "Structured JSON" --> Val
    Val -- "ResumeProfile" --> Score
    Score -- "Matches & Gaps" --> Groq
    Groq -- "Explanations & Suggestions" --> Score
    Score -- "Final AnalysisResponse" --> API
    API --> UI
```

---

## Tech Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Frontend** | React | Responsive, component-based user interface |
| **Build Tool** | Vite | Extremely fast frontend development and production build |
| **Authentication** | Firebase Auth | Secure Google Sign-In and JWT generation |
| **API Client** | Axios | Frontend/backend communication with interceptors |
| **Backend** | FastAPI | High-performance async REST API |
| **Language** | Python | Backend logic, parsing, and analysis |
| **LLM** | Groq API | Lightning-fast resume extraction and text generation |
| **Validation** | Pydantic | Strict structured data validation and normalization |
| **PDF Parsing** | PyMuPDF | Robust PDF text extraction |
| **DOCX Parsing** | python-docx | Word document extraction |
| **Testing** | Pytest | Comprehensive backend unit tests |

---

## Why Groq + Deterministic Python?

One of the core architectural decisions in ResumeIQ is the strict separation of concerns between the LLM and the application logic.

**Groq / LLM handles what it is good at:**
- Understanding the unstructured, messy natural language of different resume formats.
- Generating human-readable explanations of data.
- Writing constructive, specific improvement suggestions based on identified weaknesses.

**Python handles what it is good at:**
- Performing exact numerical scoring.
- Matching normalized skills using deterministic rules.
- Calculating exact skill gaps and match percentages.
- Providing predictable and 100% testable results.

We explicitly avoid asking the LLM to *"give this resume a score out of 100"*. Instead, the LLM extracts the raw data, and Python calculates the score. This guarantees that if you upload the exact same resume twice, you will receive the exact same score and match percentages.

---

## Resume Analysis Pipeline

Every resume uploaded to the system goes through a rigorous 14-step pipeline:

1. **Upload:** React sends the selected PDF/DOCX to FastAPI via an Axios multipart request.
2. **Authentication:** The Axios interceptor attaches a fresh Firebase ID token to the Authorization header.
3. **Token Verification:** FastAPI verifies the token cryptographically using the Firebase Admin SDK.
4. **Parsing:** PyMuPDF (or python-docx) extracts the raw string text from the binary file.
5. **Text Cleaning:** Obvious junk characters and excessive whitespace are stripped.
6. **AI Extraction:** Groq converts the unstructured text into a flat, structured JSON object representing the resume.
7. **Normalization:** The backend normalizes variations in AI output (e.g., lowercasing skills, flattening nested arrays).
8. **Validation:** Pydantic validates the JSON payload into a strict `ResumeProfile` Python object.
9. **Deterministic Scoring:** Python calculates the resume readiness score using predefined heuristic weights.
10. **Role Matching:** The candidate's technical skills are compared against predefined role templates (e.g., Frontend Developer, Data Scientist).
11. **Job Matching:** The candidate's skills are compared against a local JSON catalog of predefined sample jobs.
12. **Skill Gaps:** Missing required and preferred skills are strictly calculated based on the top matched roles.
13. **AI Explanation & Suggestions:** Groq performs a final pass to generate one-sentence natural-language explanations for the matches and targeted improvement suggestions based on the identified weaknesses.
14. **Response:** FastAPI returns the complete `AnalysisResponse` to React for rendering.

---

## Scoring System

The **Resume Readiness Score** (out of 100) is an application-generated heuristic. It is NOT an "ATS probability" or a guarantee of hiring. It simply measures the structural completeness and depth of the resume based on these criteria:

- **Skills (Max 30 pts):** Based on the total number of normalized technical skills listed.
- **Experience (Max 30 pts):** Evaluates the number of experience entries, granting bonuses if the entries contain quantifiable metrics (e.g., %, $, numbers).
- **Projects (Max 15 pts):** Evaluates the number of projects listed.
- **Certifications (Max 10 pts):** Awards points for relevant certifications.
- **Education (Max 5 pts):** Awards points for the presence of educational background.
- **Achievements/Summary (Max 10 pts):** Awards points for having a professional summary and listed achievements.

---

## Role & Job Matching

Matching is performed using exact and synonym-based string matching against skill dictionaries.

- **Skill Normalization:** Skills are lowercased and stripped of special characters (e.g., `Node.js` -> `nodejs`, `Spring Boot` -> `springboot`).
- **Required vs Preferred:** Roles and jobs define both *required* and *preferred* skills. 
- **Calculation:** The match percentage is heavily weighted toward possessing the required skills. Missing required skills drastically lowers the match percentage, even if all preferred skills are present.
- **Skill Gaps:** Any required skill missing from the candidate's profile is flagged as a High Priority gap. Missing preferred skills are flagged as Medium Priority gaps.

*(Note: The current job catalog is a sample JSON dataset, not live job-board data).*

---

## Data Models

ResumeIQ relies heavily on Pydantic to enforce data contracts. The primary models include:

- `ResumeProfile`: The core representation of the candidate (name, summary, skills, experience, projects).
- `RoleMatch`: Represents the overlap between the candidate and a predefined technical role.
- `JobMatch`: Represents the overlap between the candidate and a specific job posting.
- `SkillGap`: Flags missing skills with a priority level.
- `AnalysisResponse`: The master JSON payload returned to the frontend.

---

## Project Structure

```text
ResumeIQ/
├── backend/
│   ├── api/                 # FastAPI router endpoints
│   ├── dependencies/        # Token verification and auth injections
│   ├── models/              # Pydantic data models
│   ├── services/            # Core business logic (AI, scoring, matching, parsing)
│   ├── utils/               # Helper functions
│   └── main.py              # FastAPI application entrypoint
│
├── frontend/
│   ├── public/              # Static assets
│   ├── src/
│   │   ├── components/      # Reusable React components (ScoreCard, Matches, etc.)
│   │   ├── context/         # React Context providers (AuthContext)
│   │   ├── pages/           # Main views (Dashboard, Login, Profile)
│   │   ├── services/        # Axios API configurations
│   │   ├── utils/           # Frontend normalization helpers
│   │   ├── App.jsx          # React Router configuration
│   │   └── main.jsx         # React DOM entrypoint
│   ├── package.json         # Node dependencies
│   └── vite.config.js       # Vite configuration
│
├── tests/                   # Pytest suite for backend services
├── data/                    # Sample JSON datasets (jobs)
├── .env.example             # Backend environment template
├── frontend/.env.example    # Frontend environment template
├── requirements.txt         # Python dependencies
├── .gitignore               # Git ignore rules
└── README.md                # Project documentation
```

---

## Installation

This guide assumes you have Python (3.10+), Node.js (18+), npm, and Git installed.

**1. Clone the repository**
```bash
git clone <your-repository-url>
cd ResumeIQ
```

**2. Setup the Backend Environment**
```bash
python -m venv .venv
source .venv/bin/activate  # On Windows use: .venv\Scripts\activate
pip install -r requirements.txt
```

**3. Setup the Frontend Environment**
```bash
cd frontend
npm install
```

---

## Firebase Setup

ResumeIQ uses Firebase for Google Sign-In and authentication.

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Navigate to **Authentication** -> **Sign-in method** and enable **Google**.
3. Navigate to **Project Settings** -> **General** and add a new **Web App**.
4. Copy the resulting Firebase config keys for your frontend environment variables.
5. *(Optional but recommended for production)*: Navigate to **Service Accounts** and generate a new private key JSON file. Save this securely on your backend machine.

---

## Environment Configuration

Never commit real secrets to the repository. Use the provided `.env.example` files to create your local configurations.

**Backend (`.env`)**
```env
GROQ_API_KEY=your_groq_api_key
GROQ_MODEL=llama-3.3-70b-versatile
FIREBASE_PROJECT_ID=your_firebase_project_id
# Optional: Only needed if you aren't using Application Default Credentials (ADC) or MockCredentials
# FIREBASE_CREDENTIALS_PATH=/absolute/path/to/serviceAccountKey.json 
```

**Frontend (`frontend/.env`)**
```env
VITE_FIREBASE_API_KEY=your_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
VITE_FIREBASE_APP_ID=your_app_id
VITE_API_BASE_URL=http://localhost:8000
```

---

## Running the Application

You will need two terminal windows to run both servers simultaneously.

**Terminal 1 (Backend)**
```bash
# From the project root
source .venv/bin/activate
uvicorn backend.main:app --reload
```
The FastAPI backend will be available at `http://localhost:8000`.

**Terminal 2 (Frontend)**
```bash
# From the frontend directory
cd frontend
npm run dev
```
The React frontend will be available at `http://localhost:5173`.

---

## Testing

**Backend Tests**
The repository includes a comprehensive `pytest` suite that tests scoring, matching, parsing, and API authentication.
```bash
# From the project root
source .venv/bin/activate
pytest tests/ -v
```

**Frontend Build**
Verify that the frontend successfully compiles for production.
```bash
cd frontend
npm run build
```

---

## API Documentation

FastAPI provides an automatic interactive Swagger documentation interface. Once the backend is running, visit:
`http://localhost:8000/docs`

### Primary Endpoint: Resume Analysis

**Method:** `POST`
**Path:** `/api/resume/analyze`
**Authentication:** Required (Bearer Token containing a valid Firebase JWT)
**Request Body:** `multipart/form-data` containing a `file` (PDF or DOCX, max 10MB).
**Response:** Complete `AnalysisResponse` JSON containing the parsed profile, score, role matches, job matches, and suggestions.

---

## Security Considerations

- **Authentication:** All secure endpoints require a valid Firebase ID token.
- **Backend Verification:** The backend uses the Firebase Admin SDK to cryptographically verify the JWT signature before processing files.
- **Secrets Management:** Environment variables are strictly ignored in `.gitignore`. No private keys or service accounts are committed.
- **File Validation:** Uploads are strictly validated for MIME type (`application/pdf`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`) and size limits (< 10MB) to prevent malicious payloads or memory exhaustion.

---

## Error Handling

ResumeIQ fails gracefully under various conditions:
- **Authentication Failure / Expiration:** Returns a 401 with a specific message. The frontend catches this interceptor and redirects the user to log in.
- **Unsupported File Type:** Returns a 400 Bad Request if the file is not a PDF or DOCX.
- **File Too Large:** Returns a 400 Bad Request if the file exceeds the 10MB limit.
- **AI Extraction Failure:** Automatically retries on initial JSON/Pydantic validation failure. If it still fails, returns a 503 Service Unavailable, asking the user to try again later.
- **Corrupted PDF:** Returns a 400 Bad Request if PyMuPDF cannot extract text.

---

## Limitations

- **Predefined Catalog:** The current job match dataset and role templates are predefined samples. They do not query a live job board API (e.g., LinkedIn or Indeed).
- **Heuristic Scoring:** The resume score is a deterministic application heuristic based on structural completeness. It is not an official "ATS Pass Rate".
- **AI Variability:** While highly constrained, Groq's LLM generation for suggestions and explanations can still exhibit minor variability between runs depending on token constraints.
- **Skill Extraction Limitations:** The deterministic matching relies heavily on the quality of the skills extracted by the AI. Highly obscure or newly branded technologies might occasionally be mis-categorized.

---

## Future Improvements

- **Live Job Board Integration:** Connect the job matcher to live APIs (Greenhouse, Lever) instead of static JSON.
- **Vector-Based Semantic Matching:** Upgrade the string-based skill matching to a vector embedding model (e.g., OpenAI embeddings) to understand that "React.js" and "React Framework" are identical conceptually without relying on hardcoded synonym maps.
- **Historical Analysis & Tracking:** Allow users to save their resume versions in a database (e.g., Firestore) to track score improvements over time.
- **Resume-to-JD Matching:** Allow users to paste a specific Job Description and receive a tailored score against that exact JD.

---

## Project Design Philosophy

ResumeIQ was built with a few core principles in mind:
1. **Deterministic Where Possible, AI Where Necessary:** Don't use a hammer for a screw. AI is used for natural language parsing and generation; standard Python logic is used for math and rules.
2. **Modular:** The backend services (parsing, AI, scoring, matching) are strictly decoupled.
3. **Validated Contracts:** Pydantic models define the absolute source of truth between the AI, the backend logic, and the React frontend.
4. **Secure:** Authentication is pushed to the edge (Firebase), and the backend operates on a zero-trust model by verifying every token.

---

## Typical User Flow

1. The user navigates to the application and signs in securely using **Google**.
2. Upon redirecting to the **Dashboard**, the user uploads their PDF or DOCX resume.
3. The frontend sends the file to the backend with their secure Firebase JWT.
4. The backend verifies the token and parses the text from the document.
5. **Groq** analyzes the text and returns a highly structured candidate profile.
6. **Pydantic** validates this profile, rejecting hallucinations.
7. The deterministic engine calculates a **Resume Readiness Score** and matches the skills against roles and jobs.
8. Missing required and preferred skills are flagged as **Skill Gaps**.
9. **Groq** performs a final pass to generate targeted, actionable improvement suggestions.
10. The user reviews their complete analysis on an interactive, responsive dashboard, and can visit their **Profile** page to manage their session.

---

## Project Goal

ResumeIQ was built to demonstrate a fully functional, end-to-end full-stack application. 

It highlights the integration of modern web architecture (**React**, **Vite**), high-performance backend APIs (**FastAPI**), secure cloud authentication (**Firebase**), and practical, structured, production-ready AI integration (**Groq**, **Pydantic**). It proves that LLMs can be harnessed predictably within a modular software ecosystem when combined with deterministic business logic.
