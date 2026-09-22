package com.resumeiq.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        return buildErrorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    @ExceptionHandler(ParserException.class)
    public ResponseEntity<Map<String, String>> handleParserException(ParserException ex) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<Map<String, String>> handleAiServiceException(AiServiceException ex) {
        logger.error("AI Service error: ", ex);
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "We couldn't complete the AI analysis right now. Please try again in a moment.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the 10 MB limit.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        logger.error("An unexpected error occurred", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred.");
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(HttpStatus status, String detail) {
        Map<String, String> response = new HashMap<>();
        response.put("detail", detail);
        return new ResponseEntity<>(response, status);
    }
}
