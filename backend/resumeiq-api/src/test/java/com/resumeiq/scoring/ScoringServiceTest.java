package com.resumeiq.scoring;

import com.resumeiq.dto.ResumeProfile;
import com.resumeiq.dto.ScoreResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService();

    @Test
    void testCalculateScore_EmptyProfile() {
        ResumeProfile emptyProfile = new ResumeProfile();
        ScoreResult result = scoringService.calculateScore(emptyProfile);

        assertEquals(0.0, result.getOverallScore());
        assertEquals(0.0, result.getCategories().getSkills());
        assertTrue(result.getWeaknesses().contains("No education information found"));
    }

    @Test
    void testCalculateScore_StrongProfile() {
        ResumeProfile profile = new ResumeProfile();
        profile.setCandidateName("John Doe");
        profile.setProfessionalSummary("A very strong software engineer with 10 years of experience.");
        profile.setEducation(Collections.singletonList("B.S. in Computer Science, University of Technology, 2010"));
        profile.setSkills(Arrays.asList("Java", "Spring Boot", "Docker", "Kubernetes", "SQL", "Git", "AWS"));
        profile.setProgrammingLanguages(Arrays.asList("Python", "TypeScript"));
        profile.setExperience(Arrays.asList(
                "Senior Engineer at TechCorp: Increased performance by 50% and managed 10 engineers.",
                "Software Engineer at Startup: Built REST APIs and deployed to AWS."
        ));
        profile.setProjects(Arrays.asList(
                "E-commerce Platform: Designed and developed a scalable microservices architecture using Spring Boot and React.",
                "Analytics Dashboard: Built a real-time data visualization tool using Vue.js and D3.js."
        ));
        profile.setCertifications(Arrays.asList("AWS Certified Solutions Architect", "Certified Kubernetes Administrator"));
        profile.setAchievements(Arrays.asList("Employee of the Year 2021", "Hackathon Winner"));

        ScoreResult result = scoringService.calculateScore(profile);

        assertTrue(result.getOverallScore() > 50.0);
        assertTrue(result.getCategories().getSkills() > 10.0);
        assertTrue(result.getCategories().getExperience() > 10.0);
    }
}
