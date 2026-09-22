package com.resumeiq.matching;

import com.resumeiq.dto.Job;
import com.resumeiq.dto.JobMatch;
import com.resumeiq.dto.ResumeProfile;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JobMatchingServiceTest {

    private final SkillNormalizationService normalizationService = new SkillNormalizationService();
    private final RoleMatchingService roleMatchingService = new RoleMatchingService(normalizationService);
    private final JobMatchingService jobMatchingService = new JobMatchingService(normalizationService, roleMatchingService);

    @Test
    void testMatchJobs() {
        ResumeProfile profile = new ResumeProfile();
        profile.setSkills(Arrays.asList("Java", "Spring Boot", "SQL"));

        Job job = new Job();
        job.setId(1);
        job.setTitle("Java Backend Engineer");
        job.setRequiredSkills(Arrays.asList("Java", "Spring Boot"));
        job.setPreferredSkills(Arrays.asList("AWS", "Docker"));

        List<JobMatch> matches = jobMatchingService.matchJobs(profile, Arrays.asList(job), 1);
        
        assertFalse(matches.isEmpty());
        JobMatch match = matches.get(0);
        assertEquals(1, match.getJobId());
        assertEquals(70.0, match.getMatchPercentage()); // 100% required (70), 0% preferred (0) -> 70%
    }
}
