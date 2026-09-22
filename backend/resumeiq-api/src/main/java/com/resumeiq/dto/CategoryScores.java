package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryScores {
    private double skills = 0;
    private double experience = 0;
    private double projects = 0;
    private double education = 0;
    private double structure = 0;
    
    @JsonProperty("ats_readiness")
    private double atsReadiness = 0;
    
    private double certifications = 0;
    private double achievements = 0;

    public CategoryScores() {}

    public CategoryScores(double skills, double experience, double projects, double education, double structure, double atsReadiness, double certifications, double achievements) {
        this.skills = skills;
        this.experience = experience;
        this.projects = projects;
        this.education = education;
        this.structure = structure;
        this.atsReadiness = atsReadiness;
        this.certifications = certifications;
        this.achievements = achievements;
    }

    // Getters and Setters
    public double getSkills() { return skills; }
    public void setSkills(double skills) { this.skills = skills; }

    public double getExperience() { return experience; }
    public void setExperience(double experience) { this.experience = experience; }

    public double getProjects() { return projects; }
    public void setProjects(double projects) { this.projects = projects; }

    public double getEducation() { return education; }
    public void setEducation(double education) { this.education = education; }

    public double getStructure() { return structure; }
    public void setStructure(double structure) { this.structure = structure; }

    public double getAtsReadiness() { return atsReadiness; }
    public void setAtsReadiness(double atsReadiness) { this.atsReadiness = atsReadiness; }

    public double getCertifications() { return certifications; }
    public void setCertifications(double certifications) { this.certifications = certifications; }

    public double getAchievements() { return achievements; }
    public void setAchievements(double achievements) { this.achievements = achievements; }
}
