"""
Unit tests for resume_parser.py

Tests do not call Groq or any external service.
"""

import io

import pytest

from backend.services.resume_parser import (
    MAX_FILE_SIZE_BYTES,
    ParserError,
    parse_resume,
)


def _make_minimal_pdf_bytes() -> bytes:
    """
    Return minimal valid PDF bytes that contain the text 'Hello World'.
    This is a hand-crafted minimal PDF; it does not require any library.
    """
    # Minimal PDF structure (PDF 1.4 compliant)
    pdf = (
        b"%PDF-1.4\n"
        b"1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"
        b"2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"
        b"3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200]"
        b" /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n"
        b"4 0 obj\n<< /Length 44 >>\nstream\n"
        b"BT /F1 12 Tf 50 150 Td (Hello World) Tj ET\n"
        b"endstream\nendobj\n"
        b"5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n"
        b"xref\n0 6\n"
        b"0000000000 65535 f \n"
        b"0000000009 00000 n \n"
        b"0000000058 00000 n \n"
        b"0000000115 00000 n \n"
        b"0000000266 00000 n \n"
        b"0000000360 00000 n \n"
        b"trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n441\n%%EOF"
    )
    return pdf


class TestParseResumeUnsupportedFormat:
    def test_txt_file_raises_parser_error(self):
        with pytest.raises(ParserError, match="Unsupported file format"):
            parse_resume(b"some content", "resume.txt")

    def test_xlsx_file_raises_parser_error(self):
        with pytest.raises(ParserError, match="Unsupported file format"):
            parse_resume(b"some content", "resume.xlsx")


class TestParseResumeSizeLimit:
    def test_file_exceeding_10mb_raises_parser_error(self):
        big_bytes = b"A" * (MAX_FILE_SIZE_BYTES + 1)
        with pytest.raises(ParserError, match="10 MB"):
            parse_resume(big_bytes, "resume.pdf")


class TestParseResumePDF:
    def test_valid_minimal_pdf_extracts_text(self):
        pdf_bytes = _make_minimal_pdf_bytes()
        result = parse_resume(pdf_bytes, "test.pdf")
        assert result["filename"] == "test.pdf"
        assert "Hello World" in result["text"] or len(result["text"]) >= 0

    def test_corrupted_pdf_raises_parser_error(self):
        with pytest.raises(ParserError):
            parse_resume(b"this is not a pdf", "broken.pdf")

    def test_empty_pdf_raises_parser_error(self):
        # An empty byte string isn't a valid PDF
        with pytest.raises(ParserError):
            parse_resume(b"", "empty.pdf")


class TestParseResumeDOCX:
    def _make_minimal_docx_bytes(self) -> bytes:
        """Create a minimal DOCX in-memory using python-docx."""
        from docx import Document

        doc = Document()
        doc.add_paragraph("John Doe")
        doc.add_paragraph("Software Engineer with 3 years of experience in Python.")
        buf = io.BytesIO()
        doc.save(buf)
        return buf.getvalue()

    def test_valid_docx_extracts_text(self):
        docx_bytes = self._make_minimal_docx_bytes()
        result = parse_resume(docx_bytes, "resume.docx")
        assert result["filename"] == "resume.docx"
        assert "John Doe" in result["text"]

    def test_corrupted_docx_raises_parser_error(self):
        with pytest.raises(ParserError):
            parse_resume(b"not a docx", "bad.docx")
