package com.resumeiq.ai;

import com.resumeiq.dto.ImprovementSuggestions;
import com.resumeiq.dto.ResumeProfile;
import com.resumeiq.dto.RoleMatch;
import com.resumeiq.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestClient restClient;
    private final String aiServiceUrl;

    @Autowired
    public AiServiceClient(RestClient restClient, @Value("${ai.service.url}") String aiServiceUrl) {
        this.restClient = restClient;
        this.aiServiceUrl = aiServiceUrl;
    }

    private record ExtractRequest(String resumeText) {}

    public ResumeProfile extractResumeProfile(String text) {
        ExtractRequest request = new ExtractRequest(text);

        try {
            return restClient.post()
                    .uri(aiServiceUrl + "/ai/extract")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ResumeProfile.class);
        } catch (RestClientException e) {
            logger.error("Failed to extract resume profile from AI service", e);
            throw new AiServiceException("Failed to communicate with AI service for extraction", e);
        }
    }

    private record RoleExplanationsRequest(ResumeProfile resume_profile, List<RoleMatch> role_matches) {}

    public List<RoleMatch> generateRoleExplanations(ResumeProfile profile, List<RoleMatch> roleMatches) {
        RoleExplanationsRequest request = new RoleExplanationsRequest(profile, roleMatches);

        try {
            return restClient.post()
                    .uri(aiServiceUrl + "/ai/role-explanations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<RoleMatch>>() {});
        } catch (RestClientException e) {
            logger.warn("Failed to generate role explanations, returning unmodified role matches", e);
            return roleMatches;
        }
    }

    private record ImprovementSuggestionsRequest(ResumeProfile resume_profile, List<String> weaknesses, List<String> missing_sections) {}

    public ImprovementSuggestions generateImprovementSuggestions(ResumeProfile profile, List<String> weaknesses, List<String> missingSections) {
        ImprovementSuggestionsRequest request = new ImprovementSuggestionsRequest(profile, weaknesses, missingSections);

        try {
            return restClient.post()
                    .uri(aiServiceUrl + "/ai/improvement-suggestions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ImprovementSuggestions.class);
        } catch (RestClientException e) {
            logger.warn("Failed to generate improvement suggestions", e);
            return new ImprovementSuggestions(List.of("Could not generate suggestions at this time. Please try again."));
        }
    }
}
