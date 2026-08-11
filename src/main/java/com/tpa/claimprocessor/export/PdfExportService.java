package com.tpa.claimprocessor.export;

public interface PdfExportService {

    byte[] generateClaimSummaryPdf(String claimId);
}
