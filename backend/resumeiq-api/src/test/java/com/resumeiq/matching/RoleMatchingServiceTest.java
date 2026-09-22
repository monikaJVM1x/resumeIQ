package com.resumeiq.matching;

import com.resumeiq.dto.ResumeProfile;
import com.resumeiq.dto.RoleMatch;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleMatchingServiceTest {

    private final SkillNormalizationService normalizationService = new SkillNormalizationService();
    private final RoleMatchingService roleMatchingService = new RoleMatchingService(normalizationService);

    @Test
    void testMatchRoles_PerfectMatch() {
        ResumeProfile profile = new ResumeProfile();
        // Python Developer required: Python, Git, REST API. preferred: FastAPI, Django, Flask, Docker, PostgreSQL
        profile.setSkills(Arrays.asList("Python", "Git", "REST API", "FastAPI", "Docker", "PostgreSQL", "Flask", "Django"));

        List<RoleMatch> matches = roleMatchingService.matchRoles(profile, 1);
        
        assertFalse(matches.isEmpty());
        RoleMatch topMatch = matches.get(0);
        assertEquals("Python Developer", topMatch.getRole());
        assertEquals(100.0, topMatch.getMatchPercentage());
    }

    @Test
    void testMatchRoles_PartialMatch() {
        ResumeProfile profile = new ResumeProfile();
        profile.setSkills(Arrays.asList("Java", "Spring Boot", "Git")); // Missing SQL for Java Developer

        List<RoleMatch> matches = roleMatchingService.matchRoles(profile, 5);
        
        assertFalse(matches.isEmpty());
        RoleMatch javaDev = matches.stream().filter(m -> m.getRole().equals("Java Developer")).findFirst().orElse(null);
        assertTrue(javaDev != null);
        assertTrue(javaDev.getMissingRequired().contains("SQL"));
        assertTrue(javaDev.getMatchPercentage() < 100.0);
    }
}
