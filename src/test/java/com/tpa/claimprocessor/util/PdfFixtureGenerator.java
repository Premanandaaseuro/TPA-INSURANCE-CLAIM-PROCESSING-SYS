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
                        "Policy Number: POL-2026-8899",
                        "Policy Holder Name: Rahul Sharma",
                        "Patient Name: Rahul Sharma",
                        "Insurance Company: Star Health Insurance",
                        "Policy Name: Comprehensive Health Care Plan",
                        "Hospital Name: Apollo Hospital",
                        "Admission Date: 2026-07-10",
                        "Discharge Date: 2026-07-15",
                        "Claimed Amount: 45000.00"
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
                        "Patient Name: Rahul Sharma",
                        "Hospital Name: Apollo Hospital",
                        "Admission Date: 2026-07-10",
                        "Discharge Date: 2026-07-15",
                        "Primary Diagnosis: Acute Appendicitis",
                        "Treating Doctor: Dr. A. K. Gupta",
                        "-----------------------------------------",
                        "Bill Number: BILL-9901",
                        "Bill Date: 2026-07-15",
                        "Room Charges: 15000.00",
                        "Pharmacy Charges: 10000.00",
                        "Total Bill Amount: 45000.00"
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
