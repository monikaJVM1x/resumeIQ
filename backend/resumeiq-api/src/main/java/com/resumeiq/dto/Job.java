package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Job {
    private int id;
    private String title;
    private String company;
    private String location;
    
    @JsonProperty("employment_type")
    private String employmentType;
    
    private String experience;
    
    @JsonProperty("required_skills")
    private List<String> requiredSkills = new ArrayList<>();
    
    @JsonProperty("preferred_skills")
    private List<String> preferredSkills = new ArrayList<>();
    
    private String description = "";

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
