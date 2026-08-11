package com.tpa.claimprocessor.export;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimRuleResult;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.exception.ClaimNotFoundException;
import com.tpa.claimprocessor.exception.FileStorageException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class PdfExportServiceImpl implements PdfExportService {

    private static final Logger log = LoggerFactory.getLogger(PdfExportServiceImpl.class);

    private final ClaimRepository claimRepository;

    public PdfExportServiceImpl(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public byte[] generateClaimSummaryPdf(String claimId) {
        Claim claim = claimRepository.findByClaimIdWithDetails(claimId)
                .orElseGet(() -> claimRepository.findByClaimId(claimId)
                        .orElseThrow(() -> new ClaimNotFoundException("Claim not found: " + claimId)));

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDFont fontRegular;
            PDFont fontBold;

            try (InputStream regStream = getClass().getResourceAsStream("/fonts/arial.ttf");
                 InputStream boldStream = getClass().getResourceAsStream("/fonts/arialbd.ttf")) {
                if (regStream != null && boldStream != null) {
                    fontRegular = PDType0Font.load(document, regStream);
                    fontBold = PDType0Font.load(document, boldStream);
                } else if (regStream != null) {
                    fontRegular = PDType0Font.load(document, regStream);
                    fontBold = fontRegular;
                } else {
                    log.warn("Bundled TTF font not found on classpath. Falling back to standard Helvetica.");
                    fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                }
            } catch (Exception e) {
                log.warn("Failed to load Unicode font: {}. Falling back to Helvetica.", e.getMessage());
                fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            }

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                // ── HEADER ──────────────────────────────────────────────
                cs.setFont(fontBold, 15);
                cs.beginText();
                cs.newLineAtOffset(50, 760);
                cs.showText(sanitizeText("TPA HEALTH INSURANCE CLAIM ADJUDICATION REPORT", fontBold));
                cs.endText();

                cs.setFont(fontRegular, 9);
                cs.beginText();
                cs.newLineAtOffset(50, 744);
                cs.showText(sanitizeText("Claim ID: " + claim.getClaimId()
                        + "   |   Generated: " + (claim.getProcessedAt() != null
                            ? claim.getProcessedAt().toString()
                            : claim.getCreatedAt().toString()), fontRegular));
                cs.endText();

                cs.setLineWidth(1.2f);
                cs.moveTo(50, 736);
                cs.lineTo(550, 736);
                cs.stroke();

                // ── DECISION BANNER ───────────────────────────────────────
                int y = 718;
                String statusStr = claim.getStatus() != null ? claim.getStatus().name() : "PENDING";
                String decisionLabel = switch (statusStr) {
                    case "APPROVED"           -> "APPROVED";
                    case "REJECTED"           -> "REJECTED";
                    case "NEEDS_MANUAL_REVIEW"-> "NEEDS MANUAL REVIEW";
                    default                   -> "PENDING";
                };

                cs.setFont(fontBold, 13);
                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText(sanitizeText("DECISION: [ " + decisionLabel + " ]", fontBold));
                cs.endText();

                y -= 18;
                String reason = claim.getDecisionReason() != null && !claim.getDecisionReason().isBlank()
                        ? claim.getDecisionReason()
                        : fallbackReason(statusStr);

                cs.setFont(fontRegular, 9.5f);
                List<String> reasonLines = wrapText(reason, 85);
                for (String line : reasonLines) {
                    cs.beginText();
                    cs.newLineAtOffset(60, y);
                    cs.showText(sanitizeText(line, fontRegular));
                    cs.endText();
                    y -= 13;
                }

                y -= 8;
                cs.setLineWidth(0.6f);
                cs.moveTo(50, y);
                cs.lineTo(550, y);
                cs.stroke();

                // ── SECTION 1: EXTRACTED CLAIM DETAILS ─────────────────────
                y -= 18;
                cs.setFont(fontBold, 11);
                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText(sanitizeText("1. EXTRACTED CLAIM DETAILS", fontBold));
                cs.endText();

                y -= 16;
                String amountDisplay = claim.getClaimedAmount() != null
                        ? "Rs. " + claim.getClaimedAmount().toPlainString()
                        : "N/A";

                String[][] details = {
                        {"Policy Number",   claim.getPolicyNumber() != null ? claim.getPolicyNumber() : "N/A"},
                        {"Policy Name",     claim.getPolicyName() != null ? claim.getPolicyName() : "N/A"},
                        {"Carrier",         claim.getCarrierName() != null ? claim.getCarrierName() : "N/A"},
                        {"Patient Name",    claim.getPatientName() != null ? claim.getPatientName() : "N/A"},
                        {"Customer Name",   claim.getCustomerName() != null ? claim.getCustomerName() : "N/A"},
                        {"Hospital Name",   claim.getHospitalName() != null ? claim.getHospitalName() : "N/A"},
                        {"Admission Date",  claim.getAdmissionDate() != null ? claim.getAdmissionDate().toString() : "N/A"},
                        {"Discharge Date",  claim.getDischargeDate() != null ? claim.getDischargeDate().toString() : "N/A"},
                        {"Claimed Amount",  amountDisplay},
                        {"Claim Type",      claim.getClaimType() != null ? claim.getClaimType().name() : "N/A"},
                };

                for (String[] pair : details) {
                    cs.setFont(fontBold, 9);
                    cs.beginText();
                    cs.newLineAtOffset(55, y);
                    cs.showText(sanitizeText(pair[0] + ":", fontBold));
                    cs.endText();

                    cs.setFont(fontRegular, 9);
                    cs.beginText();
                    cs.newLineAtOffset(160, y);
                    cs.showText(sanitizeText(pair[1], fontRegular));
                    cs.endText();

                    y -= 13;
                    if (y < 80) break;
                }

                // ── SECTION 2: RULE AUDIT LOG ────────────────────────────
                y -= 10;
                cs.setLineWidth(0.5f);
                cs.moveTo(50, y);
                cs.lineTo(550, y);
                cs.stroke();

                y -= 16;
                cs.setFont(fontBold, 11);
                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText(sanitizeText("2. BUSINESS RULES AUDIT LOG (R01 - R10)", fontBold));
                cs.endText();

                y -= 14;
                cs.setFont(fontBold, 8);
                cs.beginText();
                cs.newLineAtOffset(50, y);
                cs.showText(sanitizeText(String.format("%-7s  %-26s  %-6s  %-16s  %s",
                        "Code", "Rule Name", "Result", "Severity", "Audit Details"), fontBold));
                cs.endText();

                y -= 5;
                cs.moveTo(50, y);
                cs.lineTo(550, y);
                cs.stroke();

                y -= 13;
                cs.setFont(fontRegular, 8);

                List<ClaimRuleResult> results = claim.getRuleResults();
                if (results != null && !results.isEmpty()) {
                    for (ClaimRuleResult res : results) {
                        if (y < 60) break;

                        String passStr = res.isPassed() ? "PASS" : "FAIL";
                        String sevStr = res.getSeverity() != null
                                ? res.getSeverity().name().replace("NEEDS_MANUAL_REVIEW", "MANUAL_REVIEW")
                                : "-";
                        String msg = res.getDetails() != null ? res.getDetails() : "Evaluated successfully.";
                        if (msg.length() > 50) msg = msg.substring(0, 47) + "...";
                        String ruleName = res.getRuleName() != null && res.getRuleName().length() > 26
                                ? res.getRuleName().substring(0, 23) + "..."
                                : (res.getRuleName() != null ? res.getRuleName() : "");

                        String row = String.format("%-7s  %-26s  %-6s  %-16s  %s",
                                res.getRuleCode(), ruleName, passStr, sevStr, msg);

                        cs.beginText();
                        cs.newLineAtOffset(50, y);
                        cs.showText(sanitizeText(row, fontRegular));
                        cs.endText();

                        y -= 12;
                    }
                }

                // ── FOOTER ────────────────────────────────────────────────
                if (y > 60) {
                    cs.setLineWidth(0.5f);
                    cs.moveTo(50, 55);
                    cs.lineTo(550, 55);
                    cs.stroke();

                    cs.setFont(fontRegular, 7);
                    cs.beginText();
                    cs.newLineAtOffset(50, 45);
                    cs.showText(sanitizeText("This is a system-generated adjudication report. Claim ID: " + claim.getClaimId()
                            + "   Final Decision: " + statusStr, fontRegular));
                    cs.endText();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error building PDF export for claim {}", claimId, e);
            throw new FileStorageException("Failed to generate claim summary PDF for " + claimId, e);
        }
    }

    private String sanitizeText(String text, PDFont font) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            try {
                boolean hasGlyph = true;
                if (font instanceof PDType0Font t0Font) {
                    hasGlyph = t0Font.hasGlyph(codePoint);
                }
                if (hasGlyph) {
                    sb.appendCodePoint(codePoint);
                } else {
                    if (codePoint == 0x20B9) { // Rupee symbol ₹
                        sb.append("Rs.");
                    } else if (codePoint == 0x2713) { // ✓
                        sb.append("PASS");
                    } else if (codePoint == 0x2717) { // ✕
                        sb.append("FAIL");
                    } else if (codePoint == 0x26A0) { // ⚠
                        sb.append("WARN");
                    } else {
                        sb.append("?");
                    }
                }
            } catch (Exception e) {
                sb.append("?");
            }
            i += Character.charCount(codePoint);
        }
        return sb.toString();
    }

    private String fallbackReason(String status) {
        return switch (status) {
            case "APPROVED"            -> "All mandatory validation rules passed.";
            case "REJECTED"            -> "Claim rejected based on mandatory validation rules.";
            case "NEEDS_MANUAL_REVIEW" -> "Claim requires manual review.";
            default                    -> "Claim is pending processing.";
        };
    }

    private List<String> wrapText(String text, int maxChars) {
        List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isBlank()) return lines;
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() + word.length() + 1 > maxChars) {
                lines.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(word).append(" ");
        }
        if (!current.toString().isBlank()) {
            lines.add(current.toString().trim());
        }
        return lines;
    }
}
