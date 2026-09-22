package com.resumeiq.parser;

public interface ResumeParser {
    String parse(byte[] fileBytes) throws Exception;
    boolean supports(String filename, String contentType);
}
