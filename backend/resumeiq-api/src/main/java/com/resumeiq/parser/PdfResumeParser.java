package com.resumeiq.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class PdfResumeParser implements ResumeParser {

    @Override
    public String parse(byte[] fileBytes) throws Exception {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    @Override
    public boolean supports(String filename, String contentType) {
        if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
            return true;
        }
        return "application/pdf".equals(contentType);
    }
}
