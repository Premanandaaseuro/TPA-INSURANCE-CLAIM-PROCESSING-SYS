package com.tpa.claimprocessor.extraction;

import java.io.File;
import java.io.InputStream;

public interface PdfTextExtractorService {

    String extractText(File pdfFile);

    String extractText(InputStream inputStream);

    String extractText(byte[] pdfBytes);
}
