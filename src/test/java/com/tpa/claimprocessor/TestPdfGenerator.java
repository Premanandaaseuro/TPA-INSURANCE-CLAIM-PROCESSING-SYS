package com.tpa.claimprocessor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class TestPdfGenerator {

    @Test
    public void generateTestPdfs() throws IOException {
        File dir = new File("sample_test_documents");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // PDF 1: Claim Form WITHOUT Policy Number (Only Policy ID) -> For testing R04
        File claimFormFile = new File(dir, "ClaimForm_MissingPolicyNumber_R04.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("HEALTH INSURANCE CLAIM FORM");
                cs.newLineAtOffset(0, -30);

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Policy ID: PID-10008");
                cs.newLineAtOffset(0, -20);
                cs.showText("Customer Name: Ananya Das");
                cs.newLineAtOffset(0, -20);
                cs.showText("Patient Name: Ananya Das");
                cs.newLineAtOffset(0, -20);
                cs.showText("Hospital Name: Apollo Hospital Bengaluru");
                cs.newLineAtOffset(0, -20);
                cs.showText("Admission Date: 2026-04-10");
                cs.newLineAtOffset(0, -20);
                cs.showText("Discharge Date: 2026-04-14");
                cs.newLineAtOffset(0, -20);
                cs.showText("Claimed Amount: 30000.00");
                cs.newLineAtOffset(0, -20);
                cs.showText("Claim Type: REIMBURSEMENT");
                cs.endText();
            }
            doc.save(claimFormFile);
        }

        // PDF 2: Claim Form WITH Valid Policy Number (POL-10008) -> For testing Passing R04 & R03
        File claimFormValidFile = new File(dir, "ClaimForm_WithPolicyNumber_POL10008.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("HEALTH INSURANCE CLAIM FORM");
                cs.newLineAtOffset(0, -30);

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Policy Number: POL-10008");
                cs.newLineAtOffset(0, -20);
                cs.showText("Policy ID: PID-10008");
                cs.newLineAtOffset(0, -20);
                cs.showText("Customer Name: Ananya Das");
                cs.newLineAtOffset(0, -20);
                cs.showText("Patient Name: Ananya Das");
                cs.newLineAtOffset(0, -20);
                cs.showText("Hospital Name: Apollo Hospital Bengaluru");
                cs.newLineAtOffset(0, -20);
                cs.showText("Admission Date: 2026-04-10");
                cs.newLineAtOffset(0, -20);
                cs.showText("Discharge Date: 2026-04-14");
                cs.newLineAtOffset(0, -20);
                cs.showText("Claimed Amount: 30000.00");
                cs.newLineAtOffset(0, -20);
                cs.showText("Claim Type: REIMBURSEMENT");
                cs.endText();
            }
            doc.save(claimFormValidFile);
        }

        // PDF 2: Combined Hospital Document (Discharge Summary + Final Bill)
        File combinedDocFile = new File(dir, "CombinedHospitalDoc_AnanyaDas.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("DISCHARGE SUMMARY");
                cs.newLineAtOffset(0, -30);

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Hospital Name: Apollo Hospital Bengaluru");
                cs.newLineAtOffset(0, -20);
                cs.showText("Patient Name: Ananya Das");
                cs.newLineAtOffset(0, -20);
                cs.showText("Admission Date: 2026-04-10");
                cs.newLineAtOffset(0, -20);
                cs.showText("Discharge Date: 2026-04-14");
                cs.newLineAtOffset(0, -20);
                cs.showText("Diagnosis: Acute Appendicitis");
                cs.newLineAtOffset(0, -20);
                cs.showText("Treating Doctor: Dr. Ramesh Gupta");
                cs.newLineAtOffset(0, -40);

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.showText("FINAL HOSPITAL BILL");
                cs.newLineAtOffset(0, -30);

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.showText("Bill Number: HB-2026-9901");
                cs.newLineAtOffset(0, -20);
                cs.showText("Bill Date: 2026-04-14");
                cs.newLineAtOffset(0, -20);
                cs.showText("Hospital Name: Apollo Hospital Bengaluru");
                cs.newLineAtOffset(0, -20);
                cs.showText("Patient Name: Ananya Das");
                cs.newLineAtOffset(0, -20);
                cs.showText("Room Charges: 12000.00");
                cs.newLineAtOffset(0, -20);
                cs.showText("Pharmacy Charges: 8000.00");
                cs.newLineAtOffset(0, -20);
                cs.showText("Total Bill Amount: 30000.00");
                cs.endText();
            }
            doc.save(combinedDocFile);
        }

        System.out.println("Generated Test PDFs successfully in: " + dir.getAbsolutePath());
    }
}
