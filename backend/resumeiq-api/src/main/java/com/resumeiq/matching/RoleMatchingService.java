package com.resumeiq.matching;

import com.resumeiq.dto.RoleMatch;
import com.resumeiq.dto.ResumeProfile;
import com.resumeiq.dto.SkillGap;
import com.resumeiq.dto.SkillGapPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleMatchingService {

    private final SkillNormalizationService normalizationService;

    // Hardcoded roles for deterministic catalog based matching
    private static final List<Map<String, Object>> ROLE_CATALOG = new ArrayList<>();

    static {
        ROLE_CATALOG.add(createRole("Software Engineer", Arrays.asList("Python", "Git", "SQL", "REST API"), Arrays.asList("Docker", "AWS", "CI/CD", "Linux")));
        ROLE_CATALOG.add(createRole("Backend Developer", Arrays.asList("Python", "SQL", "REST API", "Git"), Arrays.asList("Docker", "Redis", "Kubernetes", "FastAPI", "Django")));
        ROLE_CATALOG.add(createRole("Java Developer", Arrays.asList("Java", "Spring Boot", "SQL"), Arrays.asList("Docker", "AWS", "MongoDB", "Maven", "JUnit")));
        ROLE_CATALOG.add(createRole("Python Developer", Arrays.asList("Python", "Git", "REST API"), Arrays.asList("FastAPI", "Django", "Flask", "Docker", "PostgreSQL")));
        ROLE_CATALOG.add(createRole("Full Stack Developer", Arrays.asList("JavaScript", "HTML", "CSS", "REST API"), Arrays.asList("React", "Node.js", "SQL", "Docker", "TypeScript")));
        ROLE_CATALOG.add(createRole("Data Analyst", Arrays.asList("SQL", "Python", "Excel"), Arrays.asList("Tableau", "Power BI", "Pandas", "NumPy", "Statistics")));
        ROLE_CATALOG.add(createRole("Data Engineer", Arrays.asList("Python", "SQL", "ETL"), Arrays.asList("Spark", "Airflow", "AWS", "Kafka", "Databricks")));
        ROLE_CATALOG.add(createRole("Machine Learning Engineer", Arrays.asList("Python", "Machine Learning", "SQL"), Arrays.asList("TensorFlow", "PyTorch", "Scikit-learn", "Docker", "AWS")));
        ROLE_CATALOG.add(createRole("DevOps Engineer", Arrays.asList("Docker", "Linux", "CI/CD", "Git"), Arrays.asList("Kubernetes", "AWS", "Terraform", "Ansible", "Jenkins")));
        ROLE_CATALOG.add(createRole("QA Engineer", Arrays.asList("Testing", "SQL", "Git"), Arrays.asList("Selenium", "Jest", "Postman", "CI/CD", "Python")));
    }

    private static Map<String, Object> createRole(String name, List<String> req, List<String> pref) {
        Map<String, Object> map = new HashMap<>();
        map.put("role", name);
        map.put("required_skills", req);
        map.put("preferred_skills", pref);
        return map;
    }

    @Autowired
    public RoleMatchingService(SkillNormalizationService normalizationService) {
        this.normalizationService = normalizationService;
    }

    public List<RoleMatch> matchRoles(ResumeProfile profile, int topN) {
        List<String> candidateSkills = profile.allTechnicalSkills();
        List<RoleMatch> results = new ArrayList<>();

        for (Map<String, Object> roleDef : ROLE_CATALOG) {
            String roleName = (String) roleDef.get("role");
            @SuppressWarnings("unchecked")
            List<String> required = (List<String>) roleDef.get("required_skills");
            @SuppressWarnings("unchecked")
            List<String> preferred = (List<String>) roleDef.get("preferred_skills");

            SplitResult reqResult = splitMatchedMissing(candidateSkills, required);
            SplitResult prefResult = splitMatchedMissing(candidateSkills, preferred);

            double pct = calculateMatchPercentage(
                    reqResult.matched.size(), required.size(),
                    prefResult.matched.size(), preferred.size()
            );

            List<String> allMatched = new ArrayList<>(reqResult.matched);
            allMatched.addAll(prefResult.matched);

            results.add(new RoleMatch(roleName, Math.round(pct * 10.0) / 10.0, allMatched, reqResult.missing, prefResult.missing));
        }

        return results.stream()
                .sorted((r1, r2) -> Double.compare(r2.getMatchPercentage(), r1.getMatchPercentage()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    public SkillGap analyseSkillGaps(ResumeProfile profile, RoleMatch topRole) {
        SkillGapPriority gaps = new SkillGapPriority(
                topRole.getMissingRequired(),
                topRole.getMissingPreferred(),
                new ArrayList<>()
        );
        return new SkillGap(topRole.getRole(), topRole.getMatchedSkills(), gaps);
    }

    private SplitResult splitMatchedMissing(List<String> candidateSkills, List<String> targetSkills) {
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String skill : targetSkills) {
            boolean isMatched = candidateSkills.stream()
                    .anyMatch(cs -> normalizationService.skillsMatch(cs, skill));
            if (isMatched) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }
        return new SplitResult(matched, missing);
    }

    public double calculateMatchPercentage(int matchedReq, int totalReq, int matchedPref, int totalPref) {
        double reqRatio = totalReq > 0 ? (double) matchedReq / totalReq : 0.0;
        double prefRatio = totalPref > 0 ? (double) matchedPref / totalPref : 0.0;
        return (reqRatio * 0.70 + prefRatio * 0.30) * 100;
    }

    private static class SplitResult {
        List<String> matched;
        List<String> missing;
        SplitResult(List<String> matched, List<String> missing) {
            this.matched = matched;
            this.missing = missing;
        }
    }
}
