package com.tpa.claimprocessor.extraction;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.dto.ClaimResponseDto;
import com.tpa.claimprocessor.service.ClaimService;
import com.tpa.claimprocessor.util.PdfFixtureGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DocumentProcessingPipelineTest {

    @Autowired
    private PdfTextExtractorService pdfTextExtractorService;

    @Autowired
    private StructuredDataParser structuredDataParser;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ClaimRepository claimRepository;

    @Test
    void testPdfTextExtractionAndStructuredParsingPipeline() throws Exception {
        // 1. Generate Realistic Sample PDFs
        byte[] formBytes = PdfFixtureGenerator.generateSampleClaimFormPdf();
        byte[] combinedBytes = PdfFixtureGenerator.generateSampleCombinedHospitalDocPdf();

        // 2. Extract Raw Text using PDFBox
        String formText = pdfTextExtractorService.extractText(formBytes);
        String combinedText = pdfTextExtractorService.extractText(combinedBytes);

        assertNotNull(formText);
        assertTrue(formText.contains("POL-2026-8899"));
        assertNotNull(combinedText);
        assertTrue(combinedText.contains("BILL-9901"));

        // 3. Structured Data Parsing
        ExtractedClaimData extracted = structuredDataParser.parse(formText, combinedText);

        // Inspect and Verify Extracted Values
        assertEquals("POL-2026-8899", extracted.getPolicyNumber());
        assertEquals("Rahul Sharma", extracted.getPatientName());
        assertEquals("Rahul Sharma", extracted.getCustomerName());
        assertEquals("Apollo Hospital", extracted.getHospitalName());
        assertEquals(LocalDate.of(2026, 7, 10), extracted.getAdmissionDate());
        assertEquals(LocalDate.of(2026, 7, 15), extracted.getDischargeDate());
        assertEquals(new BigDecimal("45000.00"), extracted.getClaimedAmount());
        assertEquals(new BigDecimal("45000.00"), extracted.getTotalBillAmount());
        assertEquals("BILL-9901", extracted.getBillNumber());
        assertEquals("Acute Appendicitis", extracted.getPrimaryDiagnosis());
        assertEquals("Dr. A. K. Gupta", extracted.getTreatingDoctor());

        System.out.println("=== EXTRACTED CLAIM DATA INSPECTION ===");
        System.out.println("Policy Number   : " + extracted.getPolicyNumber());
        System.out.println("Patient Name    : " + extracted.getPatientName());
        System.out.println("Hospital Name   : " + extracted.getHospitalName());
        System.out.println("Admission Date  : " + extracted.getAdmissionDate());
        System.out.println("Discharge Date  : " + extracted.getDischargeDate());
        System.out.println("Claimed Amount  : " + extracted.getClaimedAmount());
        System.out.println("Total Bill Amt  : " + extracted.getTotalBillAmount());
        System.out.println("=========================================");
    }

    @Test
    @Transactional
    void testEndToEndDocumentProcessingAndDatabasePersistence() throws Exception {
        byte[] formBytes = PdfFixtureGenerator.generateSampleClaimFormPdf();
        byte[] combinedBytes = PdfFixtureGenerator.generateSampleCombinedHospitalDocPdf();

        MockMultipartFile claimForm = new MockMultipartFile(
                "claimForm", "Claim_Form_Sample.pdf", "application/pdf", formBytes
        );
        MockMultipartFile combinedDoc = new MockMultipartFile(
                "combinedHospitalDocument", "Combined_Hospital_Document_Sample.pdf", "application/pdf", combinedBytes
        );

        // Process Claim via Service
        ClaimResponseDto response = claimService.createClaim(claimForm, combinedDoc);

        assertNotNull(response.getClaimId());
        assertEquals("POL-2026-8899", response.getPolicyNumber());
        assertEquals("Rahul Sharma", response.getPatientName());
        assertEquals("Apollo Hospital", response.getHospitalName());
        assertEquals(LocalDate.of(2026, 7, 10), response.getAdmissionDate());
        assertEquals(LocalDate.of(2026, 7, 15), response.getDischargeDate());
        assertEquals(new BigDecimal("45000.00"), response.getClaimedAmount());

        // Verify Database Persistence
        Claim dbClaim = claimRepository.findByClaimId(response.getClaimId()).orElse(null);
        assertNotNull(dbClaim);
        assertNotNull(dbClaim.getDischargeDetails());
        assertEquals("Acute Appendicitis", dbClaim.getDischargeDetails().getPrimaryDiagnosis());
        assertNotNull(dbClaim.getHospitalBillDetails());
        assertEquals("BILL-9901", dbClaim.getHospitalBillDetails().getBillNumber());
        assertNotNull(dbClaim.getClaimJson());
        assertTrue(dbClaim.getClaimJson().getExtractedPayload().contains("POL-2026-8899"));
    }
}
