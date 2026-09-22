package com.resumeiq.matching;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SkillNormalizationService {

    private static final Map<String, String> SKILL_SYNONYMS = new HashMap<>();

    static {
        SKILL_SYNONYMS.put("springboot", "spring boot");
        SKILL_SYNONYMS.put("spring-boot", "spring boot");
        SKILL_SYNONYMS.put("nodejs", "node.js");
        SKILL_SYNONYMS.put("node js", "node.js");
        SKILL_SYNONYMS.put("reactjs", "react");
        SKILL_SYNONYMS.put("react.js", "react");
        SKILL_SYNONYMS.put("vuejs", "vue.js");
        SKILL_SYNONYMS.put("vue js", "vue.js");
        SKILL_SYNONYMS.put("angularjs", "angular");
        SKILL_SYNONYMS.put("postgres", "postgresql");
        SKILL_SYNONYMS.put("postgressql", "postgresql");
        SKILL_SYNONYMS.put("mongo", "mongodb");
        SKILL_SYNONYMS.put("k8s", "kubernetes");
        SKILL_SYNONYMS.put("gke", "kubernetes");
        SKILL_SYNONYMS.put("py", "python");
        SKILL_SYNONYMS.put("js", "javascript");
        SKILL_SYNONYMS.put("ts", "typescript");
        SKILL_SYNONYMS.put("ml", "machine learning");
        SKILL_SYNONYMS.put("dl", "deep learning");
        SKILL_SYNONYMS.put("tf", "tensorflow");
        SKILL_SYNONYMS.put("sklearn", "scikit-learn");
        SKILL_SYNONYMS.put("scikit learn", "scikit-learn");
        SKILL_SYNONYMS.put("ci cd", "ci/cd");
        SKILL_SYNONYMS.put("cicd", "ci/cd");
        SKILL_SYNONYMS.put("rest", "rest api");
        SKILL_SYNONYMS.put("restful", "rest api");
        SKILL_SYNONYMS.put("restful api", "rest api");
        SKILL_SYNONYMS.put("amazon web services", "aws");
        SKILL_SYNONYMS.put("google cloud", "gcp");
        SKILL_SYNONYMS.put("google cloud platform", "gcp");
        SKILL_SYNONYMS.put("azure cloud", "azure");
        SKILL_SYNONYMS.put("microsoft azure", "azure");
        SKILL_SYNONYMS.put("powerbi", "power bi");
        SKILL_SYNONYMS.put("power-bi", "power bi");
    }

    public String normalizeSkill(String skill) {
        if (skill == null) return "";
        String lower = skill.toLowerCase().trim();
        lower = lower.replaceAll("[.,;:!?]+$", "");
        return SKILL_SYNONYMS.getOrDefault(lower, lower);
    }

    public boolean skillsMatch(String candidateSkill, String requiredSkill) {
        String c = normalizeSkill(candidateSkill);
        String r = normalizeSkill(requiredSkill);
        if (c.equals(r)) return true;
        // Check if one is a distinct word in the other
        return c.matches(".*\\b" + java.util.regex.Pattern.quote(r) + "\\b.*") ||
               r.matches(".*\\b" + java.util.regex.Pattern.quote(c) + "\\b.*");
    }
}
