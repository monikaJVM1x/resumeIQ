package com.resumeiq.service;

import com.resumeiq.ai.AiServiceClient;
import com.resumeiq.dto.*;
import com.resumeiq.matching.JobMatchingService;
import com.resumeiq.matching.RoleMatchingService;
import com.resumeiq.scoring.ScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalysisService {

    private final AiServiceClient aiServiceClient;
    private final ScoringService scoringService;
    private final RoleMatchingService roleMatchingService;
    private final JobMatchingService jobMatchingService;
    private final JobService jobService;

    @Autowired
    public AnalysisService(AiServiceClient aiServiceClient, ScoringService scoringService,
                           RoleMatchingService roleMatchingService, JobMatchingService jobMatchingService,
                           JobService jobService) {
        this.aiServiceClient = aiServiceClient;
        this.scoringService = scoringService;
        this.roleMatchingService = roleMatchingService;
        this.jobMatchingService = jobMatchingService;
        this.jobService = jobService;
    }

    public AnalysisResponse analyze(String cleanText) {
        // Step 3: Groq extraction
        ResumeProfile profile = aiServiceClient.extractResumeProfile(cleanText);

        // Step 4: Score (deterministic)
        ScoreResult scoreResult = scoringService.calculateScore(profile);
        List<String> missingSections = scoringService.getMissingSections(profile);

        // Step 5: Role matching
        List<RoleMatch> roleMatches = roleMatchingService.matchRoles(profile, 5);

        // Step 6: Job matching
        List<Job> jobs = jobService.loadJobs();
        List<JobMatch> jobMatches = jobMatchingService.matchJobs(profile, jobs, 5);

        // Step 7: Skill gap analysis
        SkillGap skillGap = new SkillGap();
        if (roleMatches != null && !roleMatches.isEmpty()) {
            skillGap = roleMatchingService.analyseSkillGaps(profile, roleMatches.get(0));
        }

        // Step 8: Groq role explanations
        roleMatches = aiServiceClient.generateRoleExplanations(profile, roleMatches);

        // Step 9: Groq improvement suggestions
        ImprovementSuggestions suggestions = aiServiceClient.generateImprovementSuggestions(
                profile, scoreResult.getWeaknesses(), missingSections);

        AnalysisResponse response = new AnalysisResponse();
        response.setResumeProfile(profile);
        response.setScore(scoreResult);
        response.setRoleMatches(roleMatches);
        response.setJobMatches(jobMatches);
        response.setSkillGaps(skillGap);
        response.setSuggestions(suggestions);

        return response;
    }
}
