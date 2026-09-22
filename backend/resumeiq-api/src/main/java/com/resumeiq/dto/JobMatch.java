package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class JobMatch {
    @JsonProperty("job_id")
    private int jobId;
    
    private String title;
    private String company;
    private String location;
    
    @JsonProperty("employment_type")
    private String employmentType;
    
    private String experience;
    
    @JsonProperty("match_percentage")
    private double matchPercentage;
    
    @JsonProperty("matched_skills")
    private List<String> matchedSkills = new ArrayList<>();
    
    @JsonProperty("missing_skills")
    private List<String> missingSkills = new ArrayList<>();

    public JobMatch() {}

    // Getters and Setters
    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public double getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(double matchPercentage) { this.matchPercentage = matchPercentage; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
}
