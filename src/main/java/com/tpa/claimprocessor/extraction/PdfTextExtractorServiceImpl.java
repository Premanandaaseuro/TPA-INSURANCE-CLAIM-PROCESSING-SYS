package com.tpa.claimprocessor.extraction;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Service
public class PdfTextExtractorServiceImpl implements PdfTextExtractorService {

    private static final Logger log = LoggerFactory.getLogger(PdfTextExtractorServiceImpl.class);

    @Override
    public String extractText(File pdfFile) {
        try {
            byte[] bytes = Files.readAllBytes(pdfFile.toPath());
            return extractText(bytes);
        } catch (IOException e) {
            log.error("Failed to read PDF file: {}", pdfFile.getAbsolutePath(), e);
            return "";
        }
    }

    @Override
    public String extractText(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            return extractText(bytes);
        } catch (IOException e) {
            log.error("Failed to read PDF input stream", e);
            return "";
        }
    }

    @Override
    public String extractText(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return "";
        }

        StringBuilder extractedText = new StringBuilder();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text != null && text.trim().length() >= 20) {
                return text.trim();
            }

            log.info("Digital text layer empty or insufficient. Triggering Tess4J OCR fallback...");
            String ocrResult = performOcrFallback(document);

            if (ocrResult != null && !ocrResult.trim().isEmpty()) {
                return ocrResult.trim();
            }

            return text != null ? text.trim() : "";

        } catch (IOException e) {
            log.error("Error extracting text layer with PDFBox", e);
            return "";
        }
    }

    private String performOcrFallback(PDDocument document) {
        StringBuilder ocrText = new StringBuilder();
        try {
            ITesseract tesseract = new Tesseract();
            // Set datapath if TESSDATA_PREFIX environment variable is available
            String tessDataPath = System.getenv("TESSDATA_PREFIX");
            if (tessDataPath != null) {
                tesseract.setDatapath(tessDataPath);
            }

            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int page = 0; page < pageCount; page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 150);
                String pageText = tesseract.doOCR(image);
                if (pageText != null) {
                    ocrText.append(pageText).append("\n");
                }
            }
        } catch (UnsatisfiedLinkError e) {
            log.warn("OCR native library unavailable: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("OCR fallback failed: {}", e.getMessage());
        }
        return ocrText.toString();
    }
}

