package com.resumeiq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

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

    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, Object>>> getRoles() {
        return ResponseEntity.ok(ROLE_CATALOG);
    }
}
