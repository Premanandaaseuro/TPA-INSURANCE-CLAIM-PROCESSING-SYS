package com.tpa.claimprocessor.extraction;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.domain.repository.PolicyRepository;
import com.tpa.claimprocessor.dto.ClaimResponseDto;
import com.tpa.claimprocessor.service.ClaimService;
import com.tpa.claimprocessor.util.PdfFixtureGenerator;
import org.junit.jupiter.api.BeforeEach;
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
@Transactional
class DocumentProcessingPipelineTest {


    @Autowired
    private PdfTextExtractorService pdfTextExtractorService;

    @Autowired
    private StructuredDataParser structuredDataParser;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @BeforeEach
    void setUpPolicies() {
        if (policyRepository.findByPolicyNumber("POL-10001").isEmpty()) {
            Policy p1 = new Policy("POL-10001", "Health Secure Plus", "Rahul Kumar", "Aseuro Health Insurance",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), new BigDecimal("500000.00"), "ACTIVE");
            p1.setPolicyId("PID-10001");
            policyRepository.save(p1);
        }

        if (policyRepository.findByPolicyNumber("POL-2026-8899").isEmpty()) {
            Policy p2 = new Policy("POL-2026-8899", "Comprehensive Health Care", "Rahul Sharma", "Star Health Insurance",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), new BigDecimal("300000.00"), "ACTIVE");
            p2.setPolicyId("PID-8899");
            policyRepository.save(p2);
        }
    }

    @Test
    void testPdfTextExtractionAndStructuredParsingPipeline() throws Exception {
        byte[] formBytes = PdfFixtureGenerator.generateSampleClaimFormPdf();
        byte[] combinedBytes = PdfFixtureGenerator.generateSampleCombinedHospitalDocPdf();

        String formText = pdfTextExtractorService.extractText(formBytes);
        String combinedText = pdfTextExtractorService.extractText(combinedBytes);

        assertNotNull(formText);
        assertTrue(formText.contains("POL-2026-8899"));
        assertNotNull(combinedText);
        assertTrue(combinedText.contains("BILL-9901"));

        ExtractedClaimData extracted = structuredDataParser.parse(formText, combinedText);

        assertEquals("POL-2026-8899", extracted.getPolicyNumber());
        assertEquals("Rahul Sharma", extracted.getPatientName());
        assertEquals("Rahul Sharma", extracted.getCustomerName());
        assertEquals("Apollo Hospital", extracted.getHospitalName());
        assertEquals(LocalDate.of(2026, 7, 10), extracted.getAdmissionDate());
        assertEquals(LocalDate.of(2026, 7, 15), extracted.getDischargeDate());
        assertEquals(0, new BigDecimal("45000.00").compareTo(extracted.getClaimedAmount()));
        assertEquals(0, new BigDecimal("45000.00").compareTo(extracted.getTotalBillAmount()));
        assertEquals("BILL-9901", extracted.getBillNumber());
    }

    @Test
    @Transactional
    void testCase01CleanFlow_ReturnsApproved() throws Exception {
        byte[] formBytes = PdfFixtureGenerator.generateCase01ClaimFormPdf();
        byte[] combinedBytes = PdfFixtureGenerator.generateCase01CombinedDocPdf();

        MockMultipartFile claimForm = new MockMultipartFile(
                "claimForm", "Case01_Claim_Form.pdf", "application/pdf", formBytes
        );
        MockMultipartFile combinedDoc = new MockMultipartFile(
                "combinedHospitalDocument", "Case01_Combined_Doc.pdf", "application/pdf", combinedBytes
        );

        // Process Case 01 Claim
        ClaimResponseDto response = claimService.createClaim(claimForm, combinedDoc);

        assertNotNull(response.getClaimId());
        assertEquals("POL-10001", response.getPolicyNumber());
        assertEquals("Rahul Kumar", response.getPatientName());
        assertEquals("Apollo Hospital Bengaluru", response.getHospitalName());
        assertEquals(LocalDate.of(2026, 4, 10), response.getAdmissionDate());
        assertEquals(LocalDate.of(2026, 4, 15), response.getDischargeDate());
        assertEquals(0, new BigDecimal("40000.00").compareTo(response.getClaimedAmount()));

        // Verify Decision Status is APPROVED
        assertEquals(ClaimStatus.APPROVED, response.getStatus());

        // Verify Database Record & Rule Results (R01 to R10 all passed)
        Claim dbClaim = claimRepository.findByClaimId(response.getClaimId()).orElse(null);
        assertNotNull(dbClaim);
        assertNotNull(dbClaim.getRuleResults());
        assertEquals(10, dbClaim.getRuleResults().size());
        assertTrue(dbClaim.getRuleResults().stream().allMatch(r -> Boolean.TRUE.equals(r.isPassed())));
    }
}
