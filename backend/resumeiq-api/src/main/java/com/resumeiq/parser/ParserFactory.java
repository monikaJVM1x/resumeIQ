package com.resumeiq.parser;

import com.resumeiq.exception.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParserFactory {

    private final List<ResumeParser> parsers;

    @Autowired
    public ParserFactory(List<ResumeParser> parsers) {
        this.parsers = parsers;
    }

    public ResumeParser getParser(String filename, String contentType) {
        for (ResumeParser parser : parsers) {
            if (parser.supports(filename, contentType)) {
                return parser;
            }
        }
        throw new ParserException("Unsupported file format. Please upload a PDF or DOCX file. Got: " + filename);
    }
}
