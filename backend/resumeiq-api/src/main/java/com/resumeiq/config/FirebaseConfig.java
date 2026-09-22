package com.resumeiq.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder();
                
                if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
                    logger.info("Initializing Firebase Admin with explicitly provided credentials at: {}", credentialsPath);
                    FileInputStream serviceAccount = new FileInputStream(credentialsPath);
                    optionsBuilder.setCredentials(GoogleCredentials.fromStream(serviceAccount));
                } else {
                    logger.info("FIREBASE_CREDENTIALS_PATH not set. Attempting to initialize Firebase Admin with Application Default Credentials (ADC)...");
                    try {
                        optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault());
                        logger.info("Successfully loaded Application Default Credentials (ADC).");
                    } catch (IOException e) {
                        logger.error("ADC not found. You must set FIREBASE_CREDENTIALS_PATH or GOOGLE_APPLICATION_CREDENTIALS to a valid service-account.json file.");
                        throw e; // Fail fast instead of proceeding with invalid setup
                    }
                }
                
                if (projectId != null && !projectId.trim().isEmpty()) {
                    logger.info("Explicitly setting Firebase project ID to: {}", projectId);
                    optionsBuilder.setProjectId(projectId);
                }

                FirebaseApp app = FirebaseApp.initializeApp(optionsBuilder.build());
                logger.info("Firebase Admin SDK successfully initialized for project: {}", app.getOptions().getProjectId());
            } catch (Exception e) {
                logger.error("CRITICAL: Failed to initialize Firebase Admin SDK. Authentication will fail.", e);
            }
        }
    }
}
