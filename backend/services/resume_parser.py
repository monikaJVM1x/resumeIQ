"""
Resume parser — extracts plain text from PDF and DOCX files.
Uses PyMuPDF for PDF and python-docx for DOCX.
"""

import io
import logging

import pymupdf as fitz  # PyMuPDF (fitz alias kept for API compatibility)
from docx import Document

logger = logging.getLogger(__name__)

SUPPORTED_TYPES = {
    "application/pdf": "pdf",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "docx",
}

MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024  # 10 MB


class ParserError(Exception):
    """Raised when the resume cannot be parsed."""


def parse_resume(file_bytes: bytes, filename: str) -> dict[str, str]:
    """
    Parse a resume file and return extracted text.

    Args:
        file_bytes: Raw bytes of the uploaded file.
        filename: Original filename (used to detect format).

    Returns:
        {"filename": ..., "text": ...}

    Raises:
        ParserError: On unsupported format, size violation, or parse failure.
    """
    if len(file_bytes) > MAX_FILE_SIZE_BYTES:
        raise ParserError("File size exceeds the 10 MB limit.")

    lower_name = filename.lower()
    if lower_name.endswith(".pdf"):
        text = _extract_pdf(file_bytes, filename)
    elif lower_name.endswith(".docx"):
        text = _extract_docx(file_bytes, filename)
    else:
        raise ParserError(
            f"Unsupported file format. Please upload a PDF or DOCX file. Got: {filename}"
        )

    if not text or not text.strip():
        raise ParserError(
            "The document appears to be empty or could not be read. "
            "Please check the file and try again."
        )

    return {"filename": filename, "text": text}


def _extract_pdf(file_bytes: bytes, filename: str) -> str:
    """Extract text from a PDF using PyMuPDF."""
    try:
        pdf_stream = io.BytesIO(file_bytes)
        doc = fitz.open(stream=pdf_stream, filetype="pdf")
        pages: list[str] = []
        for page in doc:
            pages.append(page.get_text())  # type: ignore[attr-defined]
        doc.close()
        return "\n".join(pages)
    except Exception as exc:
        logger.error("PDF extraction failed for %s: %s", filename, exc)
        raise ParserError(f"Failed to read PDF file: {exc}") from exc


def _extract_docx(file_bytes: bytes, filename: str) -> str:
    """Extract text from a DOCX using python-docx."""
    try:
        docx_stream = io.BytesIO(file_bytes)
        doc = Document(docx_stream)
        paragraphs = [para.text for para in doc.paragraphs]
        return "\n".join(paragraphs)
    except Exception as exc:
        logger.error("DOCX extraction failed for %s: %s", filename, exc)
        raise ParserError(f"Failed to read DOCX file: {exc}") from exc
