package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ImprovementSuggestions {
    @JsonProperty("summary_suggestion")
    private String summarySuggestion = "";
    
    @JsonProperty("bullet_point_suggestions")
    private List<String> bulletPointSuggestions = new ArrayList<>();
    
    @JsonProperty("missing_sections")
    private List<String> missingSections = new ArrayList<>();
    
    @JsonProperty("keyword_suggestions")
    private List<String> keywordSuggestions = new ArrayList<>();
    
    @JsonProperty("general_suggestions")
    private List<String> generalSuggestions = new ArrayList<>();

    public ImprovementSuggestions() {}

    public ImprovementSuggestions(List<String> generalSuggestions) {
        this.generalSuggestions = generalSuggestions;
    }

    // Getters and Setters
    public String getSummarySuggestion() { return summarySuggestion; }
    public void setSummarySuggestion(String summarySuggestion) { this.summarySuggestion = summarySuggestion; }

    public List<String> getBulletPointSuggestions() { return bulletPointSuggestions; }
    public void setBulletPointSuggestions(List<String> bulletPointSuggestions) { this.bulletPointSuggestions = bulletPointSuggestions; }

    public List<String> getMissingSections() { return missingSections; }
    public void setMissingSections(List<String> missingSections) { this.missingSections = missingSections; }

    public List<String> getKeywordSuggestions() { return keywordSuggestions; }
    public void setKeywordSuggestions(List<String> keywordSuggestions) { this.keywordSuggestions = keywordSuggestions; }

    public List<String> getGeneralSuggestions() { return generalSuggestions; }
    public void setGeneralSuggestions(List<String> generalSuggestions) { this.generalSuggestions = generalSuggestions; }
}
