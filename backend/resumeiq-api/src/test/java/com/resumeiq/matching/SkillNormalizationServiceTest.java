package com.resumeiq.matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillNormalizationServiceTest {

    private final SkillNormalizationService normalizationService = new SkillNormalizationService();

    @Test
    void testNormalizeSkill() {
        assertEquals("spring boot", normalizationService.normalizeSkill("SpringBoot"));
        assertEquals("spring boot", normalizationService.normalizeSkill("spring-boot"));
        assertEquals("node.js", normalizationService.normalizeSkill("Node JS"));
        assertEquals("python", normalizationService.normalizeSkill("Python..."));
        assertEquals("java", normalizationService.normalizeSkill("Java"));
    }

    @Test
    void testSkillsMatch() {
        assertTrue(normalizationService.skillsMatch("SpringBoot", "Spring Boot"));
        assertTrue(normalizationService.skillsMatch("Python 3", "Python"));
        assertTrue(normalizationService.skillsMatch("React JS", "React"));
        assertFalse(normalizationService.skillsMatch("Java", "JavaScript"));
    }
}
