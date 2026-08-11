package com.tpa.claimprocessor.scripts;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

/**
 * Standalone script to test all three decision scenarios against the live API.
 * Run with: mvn exec:java -Dexec.mainClass=com.tpa.claimprocessor.scripts.DecisionUIVerificationScript
 */
public class DecisionUIVerificationScript {

    static final String API = "http://localhost:7002/api/claims";

    public static void main(String[] args) throws Exception {
        System.out.println("=== DECISION UI VERIFICATION TEST ===\n");

        System.out.println("--- Test 1: APPROVED (POL-10001, INR 40000, valid policy) ---");
        testClaim(
            buildClaimForm("POL-10001", "Rahul Kumar", "Rahul Kumar", "Aseuro Health Insurance", "Health Secure Plus",
                           "Apollo Hospital Bengaluru", "2026-04-10", "2026-04-15", "INR 40000", "REIMBURSEMENT"),
            buildHospitalDoc("Rahul Kumar", "Apollo Hospital Bengaluru", "2026-04-10", "2026-04-15",
                             "Acute Gastroenteritis", "Dr. S. K. Roy", "BILL-V1-001", "2026-04-15", "INR 40000"),
            "Expected: APPROVED"
        );

        Thread.sleep(2000);

        System.out.println("\n--- Test 2: REJECTED (POL-INACTIVE-9999, inactive/missing policy) ---");
        testClaim(
            buildClaimForm("POL-INACTIVE-9999", "Test Patient", "Test Customer", "Test Insurer", "Test Plan",
                           "Test Hospital", "2025-01-01", "2025-01-05", "INR 100000", "REIMBURSEMENT"),
            buildHospitalDoc("Test Patient", "Test Hospital", "2025-01-01", "2025-01-05",
                             "Test Diagnosis", "Dr. Test", "BILL-V1-002", "2025-01-05", "INR 100000"),
            "Expected: REJECTED or NEEDS_MANUAL_REVIEW (missing policy)"
        );

        Thread.sleep(2000);

        System.out.println("\n--- Test 3: NEEDS_MANUAL_REVIEW (amount exceeds 50000 limit) ---");
        testClaim(
            buildClaimForm("POL-2026-8899", "Rahul Sharma", "Rahul Sharma", "Star Health Insurance", "Comprehensive Health Care",
                           "Apollo Hospital", "2026-07-20", "2026-07-25", "INR 75000", "REIMBURSEMENT"),
            buildHospitalDoc("Rahul Sharma", "Apollo Hospital", "2026-07-20", "2026-07-25",
                             "Complex Surgery", "Dr. B. K. Sharma", "BILL-V1-003", "2026-07-25", "INR 75000"),
            "Expected: NEEDS_MANUAL_REVIEW (exceeds 50000 auto-approval limit)"
        );

        System.out.println("\n=== TEST COMPLETE ===");
    }

    static void testClaim(byte[] formPdf, byte[] hospitalPdf, String expectation) throws Exception {
        System.out.println("  Expectation: " + expectation);

        String boundary = "----FormBoundary" + UUID.randomUUID().toString().replace("-", "");
        URL url = new URL(API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = conn.getOutputStream()) {
            writePart(os, boundary, "claimForm", "claim_form.pdf", formPdf);
            writePart(os, boundary, "combinedHospitalDocument", "combined_doc.pdf", hospitalPdf);
            os.write(("--" + boundary + "--\r\n").getBytes());
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String response = new String(is.readAllBytes());

        String status = extractField(response, "\"status\"");
        String reason = extractField(response, "\"decisionReason\"");
        String claimId = extractField(response, "\"claimId\"");

        System.out.println("  HTTP: " + code);
        System.out.println("  Claim ID: " + claimId);
        System.out.println("  Status: " + status);
        System.out.println("  Decision Reason: " + (reason.length() > 100 ? reason.substring(0, 100) + "..." : reason));
    }

    static void writePart(OutputStream os, String boundary, String fieldName, String filename, byte[] data) throws IOException {
        String header = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n"
            + "Content-Type: application/pdf\r\n\r\n";
        os.write(header.getBytes());
        os.write(data);
        os.write("\r\n".getBytes());
    }

    static String extractField(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return "N/A";
        int start = json.indexOf(":", idx) + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        int end = json.indexOf('"', start);
        if (end < 0) end = json.indexOf(',', start);
        if (end < 0) end = Math.min(start + 200, json.length());
        return json.substring(start, end).replace("\\n", " ").replace("\\", "").trim();
    }

    static byte[] buildClaimForm(String policyNo, String custName, String patientName,
                                  String carrier, String policyName, String hospital,
                                  String admDate, String disDate, String amount, String claimType) throws IOException {
        return buildPdf(new String[]{
            "HEALTH INSURANCE CLAIM FORM",
            "Policy Details",
            "Policy Number: " + policyNo,
            "Customer Name: " + custName,
            "Patient Name: " + patientName,
            "Carrier Name: " + carrier,
            "Policy Name: " + policyName,
            "Hospital Name: " + hospital,
            "Admission Date: " + admDate,
            "Discharge Date: " + disDate,
            "Claimed Amount: " + amount,
            "Claim Type: " + claimType
        });
    }

    static byte[] buildHospitalDoc(String patientName, String hospital, String admDate,
                                    String disDate, String diagnosis, String doctor,
                                    String billNo, String billDate, String totalBill) throws IOException {
        return buildPdf(new String[]{
            "DISCHARGE SUMMARY & FINAL HOSPITAL BILL",
            "Patient Name: " + patientName,
            "Hospital Name: " + hospital,
            "Admission Date: " + admDate,
            "Discharge Date: " + disDate,
            "Primary Diagnosis: " + diagnosis,
            "Treating Doctor: " + doctor,
            "Bill Number: " + billNo,
            "Bill Date: " + billDate,
            "Total Bill Amount: " + totalBill
        });
    }

    static byte[] buildPdf(String[] lines) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                int y = 740;
                for (String line : lines) {
                    cs.beginText();
                    cs.newLineAtOffset(50, y);
                    cs.showText(line);
                    cs.endText();
                    y -= 22;
                    if (y < 50) break;
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }
}
