package com.resumeiq.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextCleanerTest {

    @Test
    void testCleanResumeText_RemovesNullBytes() {
        String dirty = "Hello\u0000World";
        assertEquals("Hello World", TextCleaner.cleanResumeText(dirty));
    }

    @Test
    void testCleanResumeText_CollapsesWhitespace() {
        String dirty = "This   is \t a   test";
        assertEquals("This is a test", TextCleaner.cleanResumeText(dirty));
    }

    @Test
    void testCleanResumeText_RemovesNoiseLines() {
        String dirty = "Valid Line\n" +
                "------------\n" +
                "***\n" +
                "Another Valid Line";
        assertEquals("Valid Line\nAnother Valid Line", TextCleaner.cleanResumeText(dirty));
    }

    @Test
    void testIsTextMeaningful() {
        assertTrue(TextCleaner.isTextMeaningful("One two three four five", 5));
        assertFalse(TextCleaner.isTextMeaningful("One two three", 5));
    }
}
