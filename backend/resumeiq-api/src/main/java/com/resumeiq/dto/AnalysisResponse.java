package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AnalysisResponse {
    @JsonProperty("resume_profile")
    private ResumeProfile resumeProfile;
    
    private ScoreResult score;
    
    @JsonProperty("role_matches")
    private List<RoleMatch> roleMatches = new ArrayList<>();
    
    @JsonProperty("job_matches")
    private List<JobMatch> jobMatches = new ArrayList<>();
    
    @JsonProperty("skill_gaps")
    private SkillGap skillGaps;
    
    private ImprovementSuggestions suggestions;

    public AnalysisResponse() {}

    // Getters and Setters
    public ResumeProfile getResumeProfile() { return resumeProfile; }
    public void setResumeProfile(ResumeProfile resumeProfile) { this.resumeProfile = resumeProfile; }

    public ScoreResult getScore() { return score; }
    public void setScore(ScoreResult score) { this.score = score; }

    public List<RoleMatch> getRoleMatches() { return roleMatches; }
    public void setRoleMatches(List<RoleMatch> roleMatches) { this.roleMatches = roleMatches; }

    public List<JobMatch> getJobMatches() { return jobMatches; }
    public void setJobMatches(List<JobMatch> jobMatches) { this.jobMatches = jobMatches; }

    public SkillGap getSkillGaps() { return skillGaps; }
    public void setSkillGaps(SkillGap skillGaps) { this.skillGaps = skillGaps; }

    public ImprovementSuggestions getSuggestions() { return suggestions; }
    public void setSuggestions(ImprovementSuggestions suggestions) { this.suggestions = suggestions; }
}
