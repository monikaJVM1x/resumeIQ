package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class SkillGap {
    @JsonProperty("target_role")
    private String targetRole = "";
    
    @JsonProperty("existing_skills")
    private List<String> existingSkills = new ArrayList<>();
    
    private SkillGapPriority gaps = new SkillGapPriority();

    public SkillGap() {}

    public SkillGap(String targetRole, List<String> existingSkills, SkillGapPriority gaps) {
        this.targetRole = targetRole;
        this.existingSkills = existingSkills;
        this.gaps = gaps;
    }

    // Getters and Setters
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public List<String> getExistingSkills() { return existingSkills; }
    public void setExistingSkills(List<String> existingSkills) { this.existingSkills = existingSkills; }

    public SkillGapPriority getGaps() { return gaps; }
    public void setGaps(SkillGapPriority gaps) { this.gaps = gaps; }
}
