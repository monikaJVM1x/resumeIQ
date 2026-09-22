package com.resumeiq.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextCleaner {

    private static final Pattern NULL_BYTES_PATTERN = Pattern.compile("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[ \\t]+");
    private static final Pattern PUNCTUATION_REPEATED_PATTERN = Pattern.compile("[^\\w\\s]{3,}");
    private static final Pattern HORIZONTAL_RULE_PATTERN = Pattern.compile("[-_=*#~]{3,}");

    public static String cleanResumeText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 1. Normalise line endings
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        // 2. Remove NULL bytes and other control characters (keep tab, newline)
        text = NULL_BYTES_PATTERN.matcher(text).replaceAll(" ");

        // 3. Collapse horizontal whitespace (spaces/tabs) within a line
        String[] lines = text.split("\n");
        List<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            line = WHITESPACE_PATTERN.matcher(line).replaceAll(" ").trim();
            cleanedLines.add(line);
        }

        // 4. Remove lines that are purely repeated punctuation / decoration
        List<String> filteredLines = new ArrayList<>();
        for (String line : cleanedLines) {
            if (PUNCTUATION_REPEATED_PATTERN.matcher(line).matches()) {
                continue;
            }
            if (HORIZONTAL_RULE_PATTERN.matcher(line).matches()) {
                continue;
            }
            filteredLines.add(line);
        }

        // 5. Collapse more than two consecutive blank lines
        List<String> resultLines = new ArrayList<>();
        int blankCount = 0;
        for (String line : filteredLines) {
            if (line.isEmpty()) {
                blankCount++;
                if (blankCount <= 2) {
                    resultLines.add(line);
                }
            } else {
                blankCount = 0;
                resultLines.add(line);
            }
        }

        return String.join("\n", resultLines).trim();
    }

    public static boolean isTextMeaningful(String text, int minWords) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String[] words = text.split("\\s+");
        return words.length >= minWords;
    }
}
