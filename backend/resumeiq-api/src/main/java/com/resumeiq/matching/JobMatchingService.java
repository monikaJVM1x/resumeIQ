package com.resumeiq.matching;

import com.resumeiq.dto.Job;
import com.resumeiq.dto.JobMatch;
import com.resumeiq.dto.ResumeProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private final SkillNormalizationService normalizationService;
    private final RoleMatchingService roleMatchingService;

    @Autowired
    public JobMatchingService(SkillNormalizationService normalizationService, RoleMatchingService roleMatchingService) {
        this.normalizationService = normalizationService;
        this.roleMatchingService = roleMatchingService;
    }

    public List<JobMatch> matchJobs(ResumeProfile profile, List<Job> jobs, int topN) {
        List<String> candidateSkills = profile.allTechnicalSkills();
        List<JobMatch> results = new ArrayList<>();

        for (Job job : jobs) {
            SplitResult reqResult = splitMatchedMissing(candidateSkills, job.getRequiredSkills());
            SplitResult prefResult = splitMatchedMissing(candidateSkills, job.getPreferredSkills());

            double pct = roleMatchingService.calculateMatchPercentage(
                    reqResult.matched.size(), job.getRequiredSkills().size(),
                    prefResult.matched.size(), job.getPreferredSkills().size()
            );

            List<String> allMatched = new ArrayList<>(reqResult.matched);
            allMatched.addAll(prefResult.matched);
            
            List<String> allMissing = new ArrayList<>(reqResult.missing);
            allMissing.addAll(prefResult.missing);

            JobMatch match = new JobMatch();
            match.setJobId(job.getId());
            match.setTitle(job.getTitle());
            match.setCompany(job.getCompany());
            match.setLocation(job.getLocation());
            match.setEmploymentType(job.getEmploymentType());
            match.setExperience(job.getExperience());
            match.setMatchPercentage(Math.round(pct * 10.0) / 10.0);
            match.setMatchedSkills(allMatched);
            match.setMissingSkills(allMissing);

            results.add(match);
        }

        return results.stream()
                .sorted((j1, j2) -> Double.compare(j2.getMatchPercentage(), j1.getMatchPercentage()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    private SplitResult splitMatchedMissing(List<String> candidateSkills, List<String> targetSkills) {
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        if (targetSkills == null) return new SplitResult(matched, missing);
        
        for (String skill : targetSkills) {
            boolean isMatched = candidateSkills.stream()
                    .anyMatch(cs -> normalizationService.skillsMatch(cs, skill));
            if (isMatched) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }
        return new SplitResult(matched, missing);
    }

    private static class SplitResult {
        List<String> matched;
        List<String> missing;
        SplitResult(List<String> matched, List<String> missing) {
            this.matched = matched;
            this.missing = missing;
        }
    }
}
