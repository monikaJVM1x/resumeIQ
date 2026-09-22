package com.resumeiq.dto;

import java.util.ArrayList;
import java.util.List;

public class SkillGapPriority {
    private List<String> high = new ArrayList<>();
    private List<String> medium = new ArrayList<>();
    private List<String> low = new ArrayList<>();

    public SkillGapPriority() {}

    public SkillGapPriority(List<String> high, List<String> medium, List<String> low) {
        this.high = high;
        this.medium = medium;
        this.low = low;
    }

    // Getters and Setters
    public List<String> getHigh() { return high; }
    public void setHigh(List<String> high) { this.high = high; }

    public List<String> getMedium() { return medium; }
    public void setMedium(List<String> medium) { this.medium = medium; }

    public List<String> getLow() { return low; }
    public void setLow(List<String> low) { this.low = low; }
}
