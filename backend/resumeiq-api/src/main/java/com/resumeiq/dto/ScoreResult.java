package com.resumeiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ScoreResult {
    @JsonProperty("overall_score")
    private double overallScore = 0;
    
    private CategoryScores categories = new CategoryScores();
    private List<String> strengths = new ArrayList<>();
    private List<String> weaknesses = new ArrayList<>();
    
    @JsonProperty("improvement_priorities")
    private List<String> improvementPriorities = new ArrayList<>();

    public ScoreResult() {}

    // Getters and Setters
    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public CategoryScores getCategories() { return categories; }
    public void setCategories(CategoryScores categories) { this.categories = categories; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }

    public List<String> getImprovementPriorities() { return improvementPriorities; }
    public void setImprovementPriorities(List<String> improvementPriorities) { this.improvementPriorities = improvementPriorities; }
}
