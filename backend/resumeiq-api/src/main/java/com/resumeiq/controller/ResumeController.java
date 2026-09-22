package com.resumeiq.controller;

import com.resumeiq.dto.AnalysisResponse;
import com.resumeiq.exception.ParserException;
import com.resumeiq.parser.ParserFactory;
import com.resumeiq.parser.ResumeParser;
import com.resumeiq.service.AnalysisService;
import com.resumeiq.util.TextCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ParserFactory parserFactory;
    private final AnalysisService analysisService;

    @Autowired
    public ResumeController(ParserFactory parserFactory, AnalysisService analysisService) {
        this.parserFactory = parserFactory;
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            // Step 1: Parse
            ResumeParser parser = parserFactory.getParser(file.getOriginalFilename(), file.getContentType());
            String rawText = parser.parse(file.getBytes());
            
            if (rawText == null || rawText.trim().isEmpty()) {
                throw new ParserException("The document appears to be empty or could not be read.");
            }

            // Step 2: Clean
            String cleanText = TextCleaner.cleanResumeText(rawText);

            if (!TextCleaner.isTextMeaningful(cleanText, 30)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, 
                        "The resume appears to be too short or empty. Please upload a resume with more content.");
            }

            // Execute Pipeline
            AnalysisResponse response = analysisService.analyze(cleanText);
            return ResponseEntity.ok(response);

        } catch (ParserException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process the uploaded file.", e);
        }
    }
}
