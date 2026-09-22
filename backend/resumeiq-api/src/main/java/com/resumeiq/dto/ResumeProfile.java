package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResumeProfile {
    @JsonProperty("candidate_name")
    private String candidateName = "";

    @JsonProperty("professional_summary")
    private String professionalSummary = "";

    private List<String> education = new ArrayList<>();
    private List<String> skills = new ArrayList<>();

    @JsonProperty("programming_languages")
    private List<String> programmingLanguages = new ArrayList<>();

    private List<String> frameworks = new ArrayList<>();
    private List<String> databases = new ArrayList<>();

    @JsonProperty("cloud_tools")
    private List<String> cloudTools = new ArrayList<>();

    private List<String> experience = new ArrayList<>();
    private List<String> projects = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();
    private List<String> achievements = new ArrayList<>();

    @JsonProperty("soft_skills")
    private List<String> softSkills = new ArrayList<>();

    public List<String> allTechnicalSkills() {
        List<String> combined = new ArrayList<>();
        if (skills != null) combined.addAll(skills);
        if (programmingLanguages != null) combined.addAll(programmingLanguages);
        if (frameworks != null) combined.addAll(frameworks);
        if (databases != null) combined.addAll(databases);
        if (cloudTools != null) combined.addAll(cloudTools);

        Set<String> seen = new HashSet<>();
        List<String> unique = new ArrayList<>();
        for (String skill : combined) {
            String normalised = skill.toLowerCase().trim();
            if (!seen.contains(normalised)) {
                seen.add(normalised);
                unique.add(skill);
            }
        }
        return unique;
    }

    // Getters and Setters
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getProfessionalSummary() { return professionalSummary; }
    public void setProfessionalSummary(String professionalSummary) { this.professionalSummary = professionalSummary; }

    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getProgrammingLanguages() { return programmingLanguages; }
    public void setProgrammingLanguages(List<String> programmingLanguages) { this.programmingLanguages = programmingLanguages; }

    public List<String> getFrameworks() { return frameworks; }
    public void setFrameworks(List<String> frameworks) { this.frameworks = frameworks; }

    public List<String> getDatabases() { return databases; }
    public void setDatabases(List<String> databases) { this.databases = databases; }

    public List<String> getCloudTools() { return cloudTools; }
    public void setCloudTools(List<String> cloudTools) { this.cloudTools = cloudTools; }

    public List<String> getExperience() { return experience; }
    public void setExperience(List<String> experience) { this.experience = experience; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public List<String> getAchievements() { return achievements; }
    public void setAchievements(List<String> achievements) { this.achievements = achievements; }

    public List<String> getSoftSkills() { return softSkills; }
    public void setSoftSkills(List<String> softSkills) { this.softSkills = softSkills; }
}
