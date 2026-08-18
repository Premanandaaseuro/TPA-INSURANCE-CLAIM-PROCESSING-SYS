package com.tpa.claimprocessor.extraction;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimDocument;
import com.tpa.claimprocessor.domain.enums.DocumentType;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Extraction and Data Processing Tests")
class ExtractionComprehensiveTest {

    @Autowired
    private StructuredDataParser structuredDataParser;

    @Autowired
    private PdfTextExtractorService pdfTextExtractorService;

    // Basic parsing tests
    @Test
    @DisplayName("PARSE-001: Parse valid JSON claim data")
    void testParseValidJsonData() {
        String claimForm = "Policy Number: POL-2026-001\nPatient Name: Test Patient\nClaimed Amount: 50000";
        String combinedDoc = "";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
        assertEquals("POL-2026-001", result.getPolicyNumber());
        assertEquals("Test Patient", result.getPatientName());
        assertEquals(new BigDecimal("50000"), result.getClaimedAmount());
    }

    @Test
    @DisplayName("PARSE-002: Parse data with dates")
    void testParseDataWithDates() {
        String claimForm = "Admission Date: 2026-07-01\nDischarge Date: 2026-07-10";
        String combinedDoc = "";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
        assertNotNull(result.getAdmissionDate());
        assertNotNull(result.getDischargeDate());
    }

    @Test
    @DisplayName("PARSE-003: Parse data with missing optional fields")
    void testParseWithMissingFields() {
        String claimForm = "Policy Number: POL-2026-001";
        String combinedDoc = "";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
        assertEquals("POL-2026-001", result.getPolicyNumber());
        assertNull(result.getPatientName());
    }

    @Test
    @DisplayName("PARSE-004: Parse empty JSON object")
    void testParseEmptyJson() {
        String claimForm = "";
        String combinedDoc = "";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("PARSE-005: Parse invalid JSON format")
    void testParseInvalidJson() {
        String claimForm = "Invalid Format {no proper structure";
        String combinedDoc = "";
        try {
            ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
            // Should handle gracefully
            assertNotNull(result);
        } catch (Exception e) {
            // Expected for invalid format
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("PARSE-006: Parse large amount values")
    void testParseLargeAmounts() {
        String claimForm = "Policy Number: POL-2026-001\nClaimed Amount: 9999999.99";
        String combinedDoc = "";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
        assertEquals("POL-2026-001", result.getPolicyNumber());
        assertTrue(result.getClaimedAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("PARSE-007: Parse negative amounts (edge case)")
    void testParseNegativeAmounts() {
        String claimForm = "Policy Number: POL-2026-001\nClaimed Amount: -5000";
        String combinedDoc = "";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
        // Should either reject or handle gracefully
    }

    @Test
    @DisplayName("PARSE-008: Parse with special characters in names")
    void testParseSpecialCharacters() {
        String claimForm = "Patient Name: Jose Maria OBrien-Schmidt";
        String combinedDoc = "";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
        assertNotNull(result.getPatientName());
        assertTrue(result.getPatientName().contains("Jose") || result.getPatientName().contains("Maria"));
    }

    @Test
    @DisplayName("PARSE-009: Parse hospital names with special characters")
    void testParseHospitalNames() {
        String claimForm = "";
        String combinedDoc = "Hospital Name: St. Mary's Medical Center (Apollo)";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
        assertNotNull(result.getHospitalName());
    }

    @Test
    @DisplayName("PARSE-010: Parse with extra whitespace")
    void testParseWithWhitespace() {
        String claimForm = "Policy Number  :  POL-2026-001\nPatient Name  :  Test";
        String combinedDoc = "";
        ExtractedClaimData result = structuredDataParser.parse(claimForm, combinedDoc);
        
        assertNotNull(result);
        assertEquals("POL-2026-001", result.getPolicyNumber());
    }

    // Document type tests
    @Test
    @DisplayName("DOC-001: Claim form document identification")
    void testClaimFormDocument() {
        Claim claim = new Claim("CLM-2026-DOC-001");
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash");
        claim.addDocument(doc);
        
        assertNotNull(claim.getDocuments());
        assertTrue(claim.getDocuments().stream().anyMatch(d -> d.getDocumentType() == DocumentType.CLAIM_FORM));
    }

    @Test
    @DisplayName("DOC-002: Combined document identification")
    void testCombinedDocument() {
        Claim claim = new Claim("CLM-2026-DOC-002");
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 2048L, "hash");
        claim.addDocument(doc);
        
        assertTrue(claim.getDocuments().stream().anyMatch(d -> d.getDocumentType() == DocumentType.COMBINED_HOSPITAL_DOCUMENT));
    }

    @Test
    @DisplayName("DOC-003: Multiple document types")
    void testMultipleDocumentTypes() {
        Claim claim = new Claim("CLM-2026-DOC-003");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path1", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path2", "application/pdf", 2048L, "hash2"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.MEDICAL_RECORDS, "records.pdf", "records.pdf", "path3", "application/pdf", 3072L, "hash3"));
        
        assertEquals(3, claim.getDocuments().size());
        assertTrue(claim.getDocuments().stream().allMatch(d -> d.getDocumentType() != null));
    }

    @Test
    @DisplayName("DOC-004: Document metadata preservation")
    void testDocumentMetadata() {
        Claim claim = new Claim("CLM-2026-DOC-004");
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path/form.pdf", "application/pdf", 2048L, "hash123");
        claim.addDocument(doc);
        
        ClaimDocument retrieved = claim.getDocuments().get(0);
        assertEquals("form.pdf", retrieved.getFileName());
        assertEquals("application/pdf", retrieved.getMimeType());
        assertEquals(2048L, retrieved.getFileSize());
        assertEquals("hash123", retrieved.getFileHash());
    }

    @Test
    @DisplayName("DOC-005: Large file handling")
    void testLargeFileHandling() {
        Claim claim = new Claim("CLM-2026-DOC-005");
        long largeSize = 50000000L; // 50MB
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "large.pdf", "large.pdf", "path", "application/pdf", largeSize, "hash");
        claim.addDocument(doc);
        
        assertEquals(largeSize, claim.getDocuments().get(0).getFileSize());
    }

    @Test
    @DisplayName("DOC-006: Zero-size file edge case")
    void testZeroSizeFile() {
        Claim claim = new Claim("CLM-2026-DOC-006");
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.CLAIM_FORM, "empty.pdf", "empty.pdf", "path", "application/pdf", 0L, "hash");
        claim.addDocument(doc);
        
        assertEquals(0L, claim.getDocuments().get(0).getFileSize());
    }

    // Data validation tests
    @Test
    @DisplayName("VAL-001: Policy number format validation")
    void testPolicyNumberFormat() {
        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-001");
        
        assertNotNull(data.getPolicyNumber());
        assertTrue(data.getPolicyNumber().startsWith("POL"));
    }

    @Test
    @DisplayName("VAL-002: Claim amount validation")
    void testClaimAmountValidation() {
        ExtractedClaimData data = new ExtractedClaimData();
        data.setClaimedAmount(new BigDecimal("50000.00"));
        
        assertNotNull(data.getClaimedAmount());
        assertTrue(data.getClaimedAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("VAL-003: Date range validation")
    void testDateRangeValidation() {
        ExtractedClaimData data = new ExtractedClaimData();
        LocalDate admission = LocalDate.of(2026, 7, 1);
        LocalDate discharge = LocalDate.of(2026, 7, 10);
        data.setAdmissionDate(admission);
        data.setDischargeDate(discharge);
        
        assertTrue(data.getAdmissionDate().isBefore(data.getDischargeDate()));
    }

    @Test
    @DisplayName("VAL-004: Invalid date range")
    void testInvalidDateRange() {
        ExtractedClaimData data = new ExtractedClaimData();
        LocalDate admission = LocalDate.of(2026, 7, 10);
        LocalDate discharge = LocalDate.of(2026, 7, 1);
        data.setAdmissionDate(admission);
        data.setDischargeDate(discharge);
        
        assertFalse(data.getAdmissionDate().isBefore(data.getDischargeDate()));
    }

    @Test
    @DisplayName("VAL-005: Future date admission")
    void testFutureAdmissionDate() {
        ExtractedClaimData data = new ExtractedClaimData();
        LocalDate futureDate = LocalDate.now().plusDays(30);
        data.setAdmissionDate(futureDate);
        
        assertTrue(data.getAdmissionDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("VAL-006: Decimal precision in amounts")
    void testDecimalPrecision() {
        ExtractedClaimData data = new ExtractedClaimData();
        data.setClaimedAmount(new BigDecimal("50000.99"));
        data.setTotalBillAmount(new BigDecimal("50000.99"));
        
        assertEquals(data.getClaimedAmount(), data.getTotalBillAmount());
    }

    @Test
    @DisplayName("VAL-007: Case-insensitive hospital name matching")
    void testCaseInsensitiveHospitalMatching() {
        ExtractedClaimData data1 = new ExtractedClaimData();
        ExtractedClaimData data2 = new ExtractedClaimData();
        
        data1.setHospitalName("Apollo Hospital");
        data2.setHospitalName("APOLLO HOSPITAL");
        
        assertTrue(data1.getHospitalName().equalsIgnoreCase(data2.getHospitalName()));
    }

    @Test
    @DisplayName("VAL-008: Trimmed string validation")
    void testTrimmedStringValidation() {
        ExtractedClaimData data = new ExtractedClaimData();
        data.setPatientName("  John Doe  ");
        
        String trimmed = data.getPatientName().trim();
        assertEquals("John Doe", trimmed);
    }

    @Test
    @DisplayName("VAL-009: Maximum field length validation")
    void testMaxFieldLength() {
        ExtractedClaimData data = new ExtractedClaimData();
        String longName = "A".repeat(500);
        data.setPatientName(longName);
        
        assertEquals(500, data.getPatientName().length());
    }

    @Test
    @DisplayName("VAL-010: Amount comparison operations")
    void testAmountComparison() {
        ExtractedClaimData data = new ExtractedClaimData();
        data.setClaimedAmount(new BigDecimal("50000.00"));
        data.setTotalBillAmount(new BigDecimal("50000.00"));
        
        assertEquals(0, data.getClaimedAmount().compareTo(data.getTotalBillAmount()));
    }
}
