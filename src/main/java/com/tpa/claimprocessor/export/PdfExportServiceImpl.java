package com.tpa.claimprocessor.export;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimRuleResult;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.exception.ClaimNotFoundException;
import com.tpa.claimprocessor.exception.FileStorageException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfExportServiceImpl implements PdfExportService {

    private final ClaimRepository claimRepository;

    public PdfExportServiceImpl(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public byte[] generateClaimSummaryPdf(String claimId) {
        Claim claim = claimRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found: " + claimId));

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                // Header Title
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.beginText();
                cs.newLineAtOffset(50, 750);
                cs.showText("TPA HEALTH INSURANCE CLAIM ADJUDICATION REPORT");
                cs.endText();

                // Subheader
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.beginText();
                cs.newLineAtOffset(50, 730);
                cs.showText("Claim ID: " + claim.getClaimId() + " | Status: " + claim.getStatus());
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(50, 715);
                cs.showText("Generated Date: " + (claim.getProcessedAt() != null ? claim.getProcessedAt().toString() : claim.getCreatedAt().toString()));
                cs.endText();

                // Line separator
                cs.setLineWidth(1.0f);
                cs.moveTo(50, 705);
                cs.lineTo(550, 705);
                cs.stroke();

                // Section 1: Structured Claim Summary
                int y = 685;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText("1. EXTRACTED CLAIM DETAILS");
                cs.endText();

                y -= 20;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

                String[] details = {
                        "Policy Number: " + (claim.getPolicyNumber() != null ? claim.getPolicyNumber() : "N/A"),
                        "Patient Name: " + (claim.getPatientName() != null ? claim.getPatientName() : "N/A"),
                        "Customer Name: " + (claim.getCustomerName() != null ? claim.getCustomerName() : "N/A"),
                        "Hospital Name: " + (claim.getHospitalName() != null ? claim.getHospitalName() : "N/A"),
                        "Admission Date: " + (claim.getAdmissionDate() != null ? claim.getAdmissionDate().toString() : "N/A"),
                        "Discharge Date: " + (claim.getDischargeDate() != null ? claim.getDischargeDate().toString() : "N/A"),
                        "Claimed Amount: Rs. " + (claim.getClaimedAmount() != null ? claim.getClaimedAmount().toString() : "N/A")
                };

                for (String line : details) {
                    cs.beginText();
                    cs.newLineAtOffset(50, y);
                    cs.showText(line);
                    cs.endText();
                    y -= 15;
                }

                // Decision Summary
                y -= 10;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText("Decision Summary: ");
                cs.endText();

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.beginText();
                cs.newLineAtOffset(150, y);
                String reason = claim.getDecisionReason() != null ? claim.getDecisionReason() : "Pending evaluation.";
                if (reason.length() > 65) reason = reason.substring(0, 62) + "...";
                cs.showText(reason);
                cs.endText();

                // Section 2: Rule Engine Audit Results
                y -= 25;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText("2. BUSINESS RULES AUDIT LOG (R01 - R10)");
                cs.endText();

                y -= 20;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText("Rule Code | Rule Name | Result | Severity | Audit Message");
                cs.endText();

                y -= 5;
                cs.moveTo(50, y);
                cs.lineTo(550, y);
                cs.stroke();

                y -= 15;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);

                List<ClaimRuleResult> results = claim.getRuleResults();
                if (results != null && !results.isEmpty()) {
                    for (ClaimRuleResult res : results) {
                        String statusStr = res.isPassed() ? "PASS" : "FAIL";
                        String sevStr = res.getSeverity() != null ? res.getSeverity().name() : "-";
                        String msg = res.getDetails() != null ? res.getDetails() : "";
                        if (msg.length() > 50) msg = msg.substring(0, 47) + "...";

                        String row = String.format("%-8s | %-22s | %-5s | %-12s | %s",
                                res.getRuleCode(),
                                res.getRuleName().length() > 22 ? res.getRuleName().substring(0, 19) + "..." : res.getRuleName(),
                                statusStr,
                                sevStr,
                                msg
                        );

                        cs.beginText();
                        cs.newLineAtOffset(50, y);
                        cs.showText(row);
                        cs.endText();

                        y -= 14;
                        if (y < 50) break; // page boundary guard
                    }
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new FileStorageException("Failed to generate claim summary PDF for " + claimId, e);
        }
    }
}
