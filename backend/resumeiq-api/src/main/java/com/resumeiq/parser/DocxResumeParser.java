package com.resumeiq.parser;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
public class DocxResumeParser implements ResumeParser {

    @Override
    public String parse(byte[] fileBytes) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
             XWPFDocument document = new XWPFDocument(bais);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    @Override
    public boolean supports(String filename, String contentType) {
        if (filename != null && filename.toLowerCase().endsWith(".docx")) {
            return true;
        }
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType);
    }
}
