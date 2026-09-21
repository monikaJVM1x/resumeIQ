"""
Job service — loads and caches job listings from data/jobs.json.
"""

import json
import logging
import os
from functools import lru_cache

from backend.models.job import Job

logger = logging.getLogger(__name__)

# Path relative to the project root (where uvicorn is run from)
JOBS_FILE_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "data", "jobs.json")


@lru_cache(maxsize=1)
def load_jobs() -> list[Job]:
    """
    Load and parse jobs from data/jobs.json.
    Results are cached after first load.
    Returns an empty list if the file is missing or malformed.
    """
    path = os.path.abspath(JOBS_FILE_PATH)
    if not os.path.exists(path):
        logger.error("jobs.json not found at %s", path)
        return []

    try:
        with open(path, "r", encoding="utf-8") as f:
            raw = json.load(f)
        jobs = [Job(**item) for item in raw]
        logger.info("Loaded %d jobs from %s", len(jobs), path)
        return jobs
    except (json.JSONDecodeError, Exception) as exc:
        logger.error("Failed to load jobs.json: %s", exc)
        return []
