package com.resumeiq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeiq.dto.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    private List<Job> cachedJobs = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadJobs();
    }

    public List<Job> loadJobs() {
        if (!cachedJobs.isEmpty()) {
            return cachedJobs;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("jobs.json").getInputStream();
            cachedJobs = mapper.readValue(is, new TypeReference<List<Job>>() {});
            logger.info("Loaded {} jobs from jobs.json", cachedJobs.size());
        } catch (IOException e) {
            logger.error("Failed to load jobs.json", e);
        }
        return cachedJobs;
    }
}
