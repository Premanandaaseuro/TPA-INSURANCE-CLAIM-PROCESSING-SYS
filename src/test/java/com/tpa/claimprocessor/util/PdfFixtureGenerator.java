package com.tpa.claimprocessor.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PdfFixtureGenerator {

    public static byte[] generateSampleClaimFormPdf() throws IOException {
        return generateClaimFormPdf(
                "POL-2026-8899", "Rahul Sharma", "Rahul Sharma",
                "Star Health Insurance", "Comprehensive Health Care Plan",
                "Apollo Hospital", "2026-07-10", "2026-07-15", "45000.00"
        );
    }

    public static byte[] generateCase01ClaimFormPdf() throws IOException {
        return generateClaimFormPdf(
                "POL-10001", "Rahul Kumar", "Rahul Kumar",
                "Aseuro Health Insurance", "Health Secure Plus",
                "Apollo Hospital Bengaluru", "2026-04-10", "2026-04-15", "INR 40000"
        );
    }

    public static byte[] generateClaimFormPdf(String policyNo, String customerName, String patientName,
                                              String carrierName, String policyName, String hospitalName,
                                              String admissionDate, String dischargeDate, String claimedAmount) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("HEALTH INSURANCE CLAIM FORM");
                contentStream.endText();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                String[] lines = {
                        "Policy Details",
                        "Policy Number: " + policyNo,
                        "Policy Holder Name: " + customerName,
                        "Patient Name: " + patientName,
                        "Insurance Company: " + carrierName,
                        "Policy Name: " + policyName,
                        "Hospital Name: " + hospitalName,
                        "Admission Date: " + admissionDate,
                        "Discharge Date: " + dischargeDate,
                        "Claimed Amount: " + claimedAmount,
                        "Claim Type: REIMBURSEMENT"
                };

                int y = 710;
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(line);
                    contentStream.endText();
                    y -= 25;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    public static byte[] generateSampleCombinedHospitalDocPdf() throws IOException {
        return generateCombinedHospitalDocPdf("Rahul Sharma", "Apollo Hospital", "2026-07-10", "2026-07-15", "Acute Appendicitis", "Dr. A. K. Gupta", "BILL-9901", "2026-07-15", "15000.00", "10000.00", "45000.00");
    }

    public static byte[] generateCase01CombinedDocPdf() throws IOException {
        return generateCombinedHospitalDocPdf("Rahul Kumar", "Apollo Hospital Bengaluru", "2026-04-10", "2026-04-15", "Acute Gastroenteritis", "Dr. S. K. Roy", "BILL-10001", "2026-04-15", "10000.00", "5000.00", "INR 40000");
    }

    public static byte[] generateCombinedHospitalDocPdf(String patientName, String hospitalName,
                                                         String admissionDate, String dischargeDate,
                                                         String diagnosis, String doctor, String billNo,
                                                         String billDate, String roomCharges,
                                                         String pharmacyCharges, String totalBill) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("DISCHARGE SUMMARY & FINAL HOSPITAL BILL");
                contentStream.endText();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                String[] lines = {
                        "Patient Name: " + patientName,
                        "Hospital Name: " + hospitalName,
                        "Admission Date: " + admissionDate,
                        "Discharge Date: " + dischargeDate,
                        "Primary Diagnosis: " + diagnosis,
                        "Treating Doctor: " + doctor,
                        "FINAL HOSPITAL BILL DETAILS",
                        "Bill Number: " + billNo,
                        "Bill Date: " + billDate,
                        "Room Charges: " + roomCharges,
                        "Pharmacy Charges: " + pharmacyCharges,
                        "Total Bill Amount: " + totalBill
                };

                int y = 710;
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(line);
                    contentStream.endText();
                    y -= 25;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
