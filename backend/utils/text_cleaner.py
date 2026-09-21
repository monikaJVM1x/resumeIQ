"""
Text cleaning utilities for resume content.
Normalises whitespace and removes extraction noise
without destroying meaningful formatting.
"""

import re


def clean_resume_text(text: str) -> str:
    """
    Clean extracted resume text.

    Steps:
    1. Normalise Windows line endings to Unix.
    2. Remove NULL bytes and control characters.
    3. Collapse runs of whitespace that are not newlines.
    4. Remove lines that are purely noise (long repeated punctuation).
    5. Collapse more than two consecutive blank lines into two.
    6. Strip leading/trailing whitespace from the whole document.
    """
    if not text:
        return ""

    # 1. Normalise line endings
    text = text.replace("\r\n", "\n").replace("\r", "\n")

    # 2. Remove NULL bytes and other control characters (keep tab, newline)
    text = re.sub(r"[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]", " ", text)

    # 3. Collapse horizontal whitespace (spaces/tabs) within a line
    lines = text.split("\n")
    cleaned_lines = []
    for line in lines:
        line = re.sub(r"[ \t]+", " ", line).strip()
        cleaned_lines.append(line)

    # 4. Remove lines that are purely repeated punctuation / decoration
    filtered_lines = []
    for line in cleaned_lines:
        # Skip lines that are only punctuation/symbols repeated 3+ times
        if re.fullmatch(r"[^\w\s]{3,}", line):
            continue
        # Skip lines that are only underscores, dashes, equals (horizontal rules)
        if re.fullmatch(r"[-_=*#~]{3,}", line):
            continue
        filtered_lines.append(line)

    # 5. Collapse more than two consecutive blank lines
    result_lines: list[str] = []
    blank_count = 0
    for line in filtered_lines:
        if line == "":
            blank_count += 1
            if blank_count <= 2:
                result_lines.append(line)
        else:
            blank_count = 0
            result_lines.append(line)

    return "\n".join(result_lines).strip()


def is_text_meaningful(text: str, min_words: int = 50) -> bool:
    """
    Return True if the text contains enough content to be analysed.
    A resume with fewer than `min_words` words is considered too short.
    """
    words = text.split()
    return len(words) >= min_words
