package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class RoleMatch {
    private String role;
    
    @JsonProperty("match_percentage")
    private double matchPercentage;
    
    @JsonProperty("matched_skills")
    private List<String> matchedSkills = new ArrayList<>();
    
    @JsonProperty("missing_required")
    private List<String> missingRequired = new ArrayList<>();
    
    @JsonProperty("missing_preferred")
    private List<String> missingPreferred = new ArrayList<>();
    
    private String explanation = "";

    public RoleMatch() {}

    public RoleMatch(String role, double matchPercentage, List<String> matchedSkills, List<String> missingRequired, List<String> missingPreferred) {
        this.role = role;
        this.matchPercentage = matchPercentage;
        this.matchedSkills = matchedSkills;
        this.missingRequired = missingRequired;
        this.missingPreferred = missingPreferred;
    }

    // Getters and Setters
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(double matchPercentage) { this.matchPercentage = matchPercentage; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingRequired() { return missingRequired; }
    public void setMissingRequired(List<String> missingRequired) { this.missingRequired = missingRequired; }

    public List<String> getMissingPreferred() { return missingPreferred; }
    public void setMissingPreferred(List<String> missingPreferred) { this.missingPreferred = missingPreferred; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
