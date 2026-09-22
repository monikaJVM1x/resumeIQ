package com.resumeiq.scoring;

import com.resumeiq.dto.CategoryScores;
import com.resumeiq.dto.ResumeProfile;
import com.resumeiq.dto.ScoreResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ScoringService {

    private static final double WEIGHT_SKILLS = 20;
    private static final double WEIGHT_EXPERIENCE = 20;
    private static final double WEIGHT_PROJECTS = 15;
    private static final double WEIGHT_EDUCATION = 10;
    private static final double WEIGHT_STRUCTURE = 10;
    private static final double WEIGHT_ATS = 15;
    private static final double WEIGHT_CERTIFICATIONS = 5;
    private static final double WEIGHT_ACHIEVEMENTS = 5;

    private static final Set<String> ATS_ACTION_VERBS = Set.of(
            "developed", "built", "designed", "implemented", "led", "managed",
            "improved", "optimised", "optimized", "created", "delivered", "reduced",
            "increased", "automated", "deployed", "architected", "collaborated",
            "maintained", "migrated", "integrated", "analysed", "analyzed", "tested",
            "reviewed", "mentored", "launched", "engineered", "streamlined"
    );

    private static final Set<String> TECH_KEYWORDS = Set.of(
            "python", "java", "javascript", "typescript", "react", "node",
            "spring", "django", "fastapi", "flask", "sql", "nosql", "mongodb",
            "postgresql", "mysql", "docker", "kubernetes", "aws", "azure", "gcp",
            "git", "linux", "api", "rest", "graphql", "ci/cd", "agile", "scrum",
            "machine learning", "ml", "deep learning", "tensorflow", "pytorch",
            "data", "analytics", "excel", "tableau", "power bi"
    );

    private static final Pattern METRIC_PATTERN = Pattern.compile("\\b\\d+\\s*(%|x|k|\\+|years?|months?|users?|requests?)\\b");

    public ScoreResult calculateScore(ResumeProfile profile) {
        double skillsScore = scoreSkills(profile);
        double experienceScore = scoreExperience(profile);
        double projectsScore = scoreProjects(profile);
        double educationScore = scoreEducation(profile);
        double structureScore = scoreStructure(profile);
        double atsScore = scoreAts(profile);
        double certScore = scoreCertifications(profile);
        double achievementsScore = scoreAchievements(profile);

        double overall = Math.round((skillsScore + experienceScore + projectsScore + educationScore +
                structureScore + atsScore + certScore + achievementsScore) * 10.0) / 10.0;

        CategoryScores categories = new CategoryScores(
                Math.round(skillsScore * 10.0) / 10.0,
                Math.round(experienceScore * 10.0) / 10.0,
                Math.round(projectsScore * 10.0) / 10.0,
                Math.round(educationScore * 10.0) / 10.0,
                Math.round(structureScore * 10.0) / 10.0,
                Math.round(atsScore * 10.0) / 10.0,
                Math.round(certScore * 10.0) / 10.0,
                Math.round(achievementsScore * 10.0) / 10.0
        );

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        deriveStrengthsWeaknesses(categories, strengths, weaknesses);

        List<String> priorities = derivePriorities(categories);

        ScoreResult result = new ScoreResult();
        result.setOverallScore(overall);
        result.setCategories(categories);
        result.setStrengths(strengths);
        result.setWeaknesses(weaknesses);
        result.setImprovementPriorities(priorities);

        return result;
    }

    private double scoreSkills(ResumeProfile profile) {
        int count = profile.allTechnicalSkills().size();
        if (count == 0) return 0.0;
        if (count < 5) return 8.0;
        if (count < 10) return 13.0;
        if (count < 15) return 16.0;
        return 20.0;
    }

    private double scoreExperience(ResumeProfile profile) {
        List<String> entries = profile.getExperience();
        if (entries == null || entries.isEmpty()) return 0.0;
        int count = entries.size();
        double base;
        if (count == 1) base = 10.0;
        else if (count == 2) base = 14.0;
        else if (count == 3) base = 17.0;
        else base = 19.0;

        boolean hasMetrics = entries.stream().anyMatch(e -> e.matches(".*\\d.*"));
        double bonus = hasMetrics ? 1.0 : 0.0;
        return Math.min(base + bonus, WEIGHT_EXPERIENCE);
    }

    private double scoreProjects(ResumeProfile profile) {
        List<String> projects = profile.getProjects();
        if (projects == null || projects.isEmpty()) return 0.0;
        int count = projects.size();
        double avgLen = projects.stream().mapToInt(String::length).average().orElse(0.0);
        
        double base;
        if (count == 1) base = 7.0;
        else if (count == 2) base = 10.0;
        else base = 12.0;

        double bonus = avgLen > 60 ? 3.0 : (avgLen > 30 ? 1.5 : 0.0);
        return Math.min(base + bonus, WEIGHT_PROJECTS);
    }

    private double scoreEducation(ResumeProfile profile) {
        List<String> education = profile.getEducation();
        if (education == null || education.isEmpty()) return 0.0;
        double avgLen = education.stream().mapToInt(String::length).average().orElse(0.0);
        return avgLen > 40 ? 10.0 : 7.0;
    }

    private double scoreStructure(ResumeProfile profile) {
        int presentSections = 0;
        if (profile.getProfessionalSummary() != null && !profile.getProfessionalSummary().isEmpty()) presentSections++;
        if (profile.getEducation() != null && !profile.getEducation().isEmpty()) presentSections++;
        if ((profile.getSkills() != null && !profile.getSkills().isEmpty()) || 
            (profile.getProgrammingLanguages() != null && !profile.getProgrammingLanguages().isEmpty())) presentSections++;
        if (profile.getExperience() != null && !profile.getExperience().isEmpty()) presentSections++;
        if (profile.getProjects() != null && !profile.getProjects().isEmpty()) presentSections++;
        if (profile.getCertifications() != null && !profile.getCertifications().isEmpty()) presentSections++;

        return switch (presentSections) {
            case 6, 7, 8 -> 10.0;
            case 5 -> 9.0;
            case 4 -> 7.0;
            case 3 -> 5.0;
            case 2 -> 3.0;
            case 1 -> 1.0;
            default -> 0.0;
        };
    }

    private double scoreAts(ResumeProfile profile) {
        String allText = String.join(" ", 
                profile.getProfessionalSummary(),
                String.join(" ", profile.getExperience()),
                String.join(" ", profile.getProjects()),
                String.join(" ", profile.getSkills())
        ).toLowerCase();

        long verbCount = ATS_ACTION_VERBS.stream().filter(allText::contains).count();
        double verbScore = Math.min((verbCount / 5.0) * 6.0, 6.0);

        long techCount = TECH_KEYWORDS.stream().filter(allText::contains).count();
        double techScore = Math.min((techCount / 5.0) * 5.0, 5.0);

        boolean hasNumbers = METRIC_PATTERN.matcher(allText).find();
        double metricScore = hasNumbers ? 4.0 : 0.0;

        return Math.min(verbScore + techScore + metricScore, WEIGHT_ATS);
    }

    private double scoreCertifications(ResumeProfile profile) {
        int count = profile.getCertifications() != null ? profile.getCertifications().size() : 0;
        if (count == 0) return 0.0;
        if (count == 1) return 3.0;
        if (count == 2) return 4.0;
        return 5.0;
    }

    private double scoreAchievements(ResumeProfile profile) {
        int count = profile.getAchievements() != null ? profile.getAchievements().size() : 0;
        if (count == 0) return 0.0;
        if (count == 1) return 3.0;
        if (count == 2) return 4.0;
        return 5.0;
    }

    private void deriveStrengthsWeaknesses(CategoryScores categories, List<String> strengths, List<String> weaknesses) {
        if (categories.getSkills() >= 16) strengths.add("Strong and diverse technical skill set");
        else if (categories.getSkills() < 10) weaknesses.add("Limited technical skills listed");

        if (categories.getExperience() >= 16) strengths.add("Solid professional experience");
        else if (categories.getExperience() < 8) weaknesses.add("Work experience section needs more detail");

        if (categories.getProjects() >= 12) strengths.add("Good portfolio of projects");
        else if (categories.getProjects() < 6) weaknesses.add("Few or brief project descriptions");

        if (categories.getEducation() >= 8) strengths.add("Education section is well documented");
        else if (categories.getEducation() == 0) weaknesses.add("No education information found");

        if (categories.getAtsReadiness() >= 12) strengths.add("Good use of action verbs and measurable results");
        else if (categories.getAtsReadiness() < 7) weaknesses.add("Resume lacks action verbs and quantified results");

        if (categories.getCertifications() >= 4) strengths.add("Relevant certifications listed");
        else if (categories.getCertifications() == 0) weaknesses.add("No certifications found");

        if (categories.getAchievements() >= 4) strengths.add("Notable achievements highlighted");
        else if (categories.getAchievements() == 0) weaknesses.add("No achievements section found");

        if (categories.getStructure() >= 8) strengths.add("Resume has a clear and complete structure");
        else if (categories.getStructure() < 5) weaknesses.add("Resume is missing standard sections");
    }

    private List<String> derivePriorities(CategoryScores categories) {
        class Gap {
            final double gap;
            final String message;
            Gap(double gap, String message) { this.gap = gap; this.message = message; }
        }

        List<Gap> gaps = Arrays.asList(
                new Gap(WEIGHT_EXPERIENCE - categories.getExperience(), "Expand work experience with quantified achievements"),
                new Gap(WEIGHT_SKILLS - categories.getSkills(), "Add more relevant technical skills"),
                new Gap(WEIGHT_ATS - categories.getAtsReadiness(), "Use more action verbs and add measurable metrics"),
                new Gap(WEIGHT_PROJECTS - categories.getProjects(), "Add detailed project descriptions"),
                new Gap(WEIGHT_STRUCTURE - categories.getStructure(), "Ensure all standard sections are present"),
                new Gap(WEIGHT_EDUCATION - categories.getEducation(), "Complete the education section"),
                new Gap(WEIGHT_CERTIFICATIONS - categories.getCertifications(), "Consider adding industry certifications"),
                new Gap(WEIGHT_ACHIEVEMENTS - categories.getAchievements(), "Highlight key achievements")
        );

        return gaps.stream()
                .filter(g -> g.gap > 0)
                .sorted((g1, g2) -> Double.compare(g2.gap, g1.gap))
                .limit(5)
                .map(g -> g.message)
                .collect(Collectors.toList());
    }

    public List<String> getMissingSections(ResumeProfile profile) {
        List<String> missing = new ArrayList<>();
        if (profile.getProfessionalSummary() == null || profile.getProfessionalSummary().isEmpty())
            missing.add("Professional Summary / Objective");
        if (profile.getEducation() == null || profile.getEducation().isEmpty())
            missing.add("Education");
        if ((profile.getSkills() == null || profile.getSkills().isEmpty()) &&
            (profile.getProgrammingLanguages() == null || profile.getProgrammingLanguages().isEmpty()))
            missing.add("Skills");
        if (profile.getExperience() == null || profile.getExperience().isEmpty())
            missing.add("Work Experience");
        if (profile.getProjects() == null || profile.getProjects().isEmpty())
            missing.add("Projects");
        if (profile.getCertifications() == null || profile.getCertifications().isEmpty())
            missing.add("Certifications");
        if (profile.getAchievements() == null || profile.getAchievements().isEmpty())
            missing.add("Achievements");
        return missing;
    }
}
