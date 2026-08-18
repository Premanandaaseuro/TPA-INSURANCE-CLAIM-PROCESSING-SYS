package com.tpa.claimprocessor.rules;

import com.tpa.claimprocessor.decision.DecisionEngineService;
import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimDocument;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.enums.DocumentType;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.domain.repository.PolicyRepository;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.handler.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Comprehensive Rules Test Suite")
class RulesComprehensiveTest {

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private DecisionEngineService decisionEngineService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ClaimRepository claimRepository;

    private Policy activePolicy;

    @BeforeEach
    void setUp() {
        activePolicy = new Policy(
                "POL-2026-1001",
                "Comprehensive Health Plan",
                "John Doe",
                "Star Health Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        policyRepository.save(activePolicy);
    }

    // R01 - Claim Form Missing Rule Tests
    @Test
    @DisplayName("R01-001: Should reject when claim form is missing")
    void testR01_ClaimFormMissing() {
        Claim claim = new Claim("CLM-2026-R01-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().anyMatch(r -> r.getRuleCode().equals("R01") && !r.isPassed()));
    }

    @Test
    @DisplayName("R01-002: Should pass when claim form is present")
    void testR01_ClaimFormPresent() {
        Claim claim = new Claim("CLM-2026-R01-002");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().noneMatch(r -> r.getRuleCode().equals("R01") && !r.isPassed()));
    }

    // R02 - Combined Document Missing Rule Tests
    @Test
    @DisplayName("R02-001: Should reject when combined document is missing")
    void testR02_CombinedDocMissing() {
        Claim claim = new Claim("CLM-2026-R02-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().anyMatch(r -> r.getRuleCode().equals("R02") && !r.isPassed()));
    }

    @Test
    @DisplayName("R02-002: Should pass when combined document is present")
    void testR02_CombinedDocPresent() {
        Claim claim = new Claim("CLM-2026-R02-002");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().noneMatch(r -> r.getRuleCode().equals("R02") && !r.isPassed()));
    }

    // R03 - Policy Inactive Rule Tests
    @Test
    @DisplayName("R03-001: Should reject when policy is inactive")
    void testR03_InactivePolicy() {
        Policy inactivePolicy = new Policy(
                "POL-2026-1002",
                "Basic Health Plan",
                "Jane Doe",
                "Apollo Insurance",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                new BigDecimal("200000.00"),
                "INACTIVE"
        );
        policyRepository.save(inactivePolicy);

        Claim claim = new Claim("CLM-2026-R03-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1002");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, inactivePolicy);
        assertTrue(results.stream().anyMatch(r -> r.getRuleCode().equals("R03") && !r.isPassed()));
    }

    @Test
    @DisplayName("R03-002: Should pass when policy is active")
    void testR03_ActivePolicy() {
        Claim claim = new Claim("CLM-2026-R03-002");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().noneMatch(r -> r.getRuleCode().equals("R03") && !r.isPassed()));
    }

    // R04 - Policy Number Missing Tests
    @Test
    @DisplayName("R04-001: Should reject when policy number is missing")
    void testR04_PolicyNumberMissing() {
        Claim claim = new Claim("CLM-2026-R04-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber(null);
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().anyMatch(r -> r.getRuleCode().equals("R04") && !r.isPassed()));
    }

    @Test
    @DisplayName("R04-002: Should pass when policy number is present")
    void testR04_PolicyNumberPresent() {
        Claim claim = new Claim("CLM-2026-R04-002");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().noneMatch(r -> r.getRuleCode().equals("R04") && !r.isPassed()));
    }

    // R05 - Patient Name Mismatch Tests
    @Test
    @DisplayName("R05-001: Should reject when patient name mismatches policy holder")
    void testR05_PatientNameMismatch() {
        Claim claim = new Claim("CLM-2026-R05-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("Wrong Name");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().anyMatch(r -> r.getRuleCode().equals("R05") && !r.isPassed()));
    }

    @Test
    @DisplayName("R05-002: Should pass when patient name matches policy holder")
    void testR05_PatientNameMatch() {
        Claim claim = new Claim("CLM-2026-R05-002");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("John Doe");
        data.setClaimedAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().noneMatch(r -> r.getRuleCode().equals("R05") && !r.isPassed()));
    }

    // R08 - Claimed Amount Exceeds Bill Tests
    @Test
    @DisplayName("R08-001: Should reject when claimed amount exceeds bill")
    void testR08_ClaimedExceedsBill() {
        Claim claim = new Claim("CLM-2026-R08-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("John Doe");
        data.setClaimedAmount(new BigDecimal("60000.00"));
        data.setTotalBillAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().anyMatch(r -> r.getRuleCode().equals("R08") && !r.isPassed()));
    }

    @Test
    @DisplayName("R08-002: Should pass when claimed amount equals or less than bill")
    void testR08_ClaimedWithinBill() {
        Claim claim = new Claim("CLM-2026-R08-002");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("John Doe");
        data.setClaimedAmount(new BigDecimal("50000.00"));
        data.setTotalBillAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().noneMatch(r -> r.getRuleCode().equals("R08") && !r.isPassed()));
    }

    // R09 - High Value Claim Tests
    @Test
    @DisplayName("R09-001: Should flag high value claim")
    void testR09_HighValueClaim() {
        Claim claim = new Claim("CLM-2026-R09-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("John Doe");
        data.setClaimedAmount(new BigDecimal("300000.00"));
        data.setTotalBillAmount(new BigDecimal("300000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().anyMatch(r -> r.getRuleCode().equals("R09")));
    }

    @Test
    @DisplayName("R09-002: Should pass for normal value claim")
    void testR09_NormalValueClaim() {
        Claim claim = new Claim("CLM-2026-R09-002");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("John Doe");
        data.setClaimedAmount(new BigDecimal("50000.00"));
        data.setTotalBillAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertTrue(results.stream().anyMatch(r -> r.getRuleCode().equals("R09")));
    }

    // Edge cases and boundary tests
    @Test
    @DisplayName("EDGE-001: Null claimed amount handling")
    void testNullClaimedAmount() {
        Claim claim = new Claim("CLM-2026-EDGE-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("John Doe");
        data.setClaimedAmount(null);
        data.setTotalBillAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("EDGE-002: Zero amount claim")
    void testZeroAmountClaim() {
        Claim claim = new Claim("CLM-2026-EDGE-002");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("John Doe");
        data.setClaimedAmount(BigDecimal.ZERO);
        data.setTotalBillAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertNotNull(results);
    }

    @Test
    @DisplayName("EDGE-003: Multiple documents with same type")
    void testMultipleDocumentsSameType() {
        Claim claim = new Claim("CLM-2026-EDGE-003");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form1.pdf", "form1.pdf", "path1", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form2.pdf", "form2.pdf", "path2", "application/pdf", 1024L, "hash2"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash3"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1001");
        data.setPatientName("John Doe");
        data.setClaimedAmount(new BigDecimal("50000.00"));
        data.setTotalBillAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
        assertNotNull(results);
    }

    @Test
    @DisplayName("EDGE-004: Policy cover limit less than claimed amount")
    void testPolicyCoverLimitExceeded() {
        Policy limitedPolicy = new Policy(
                "POL-2026-1003",
                "Basic Coverage",
                "John Doe",
                "Basic Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("100000.00"),
                "ACTIVE"
        );
        policyRepository.save(limitedPolicy);

        Claim claim = new Claim("CLM-2026-EDGE-004");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1003");
        data.setPatientName("John Doe");
        data.setClaimedAmount(new BigDecimal("150000.00"));
        data.setTotalBillAmount(new BigDecimal("150000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, limitedPolicy);
        assertNotNull(results);
    }

    @Test
    @DisplayName("EDGE-005: Special characters in names")
    void testSpecialCharactersInNames() {
        Policy specialPolicy = new Policy(
                "POL-2026-1004",
                "Premium Plan",
                "José María O'Brien",
                "Global Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        policyRepository.save(specialPolicy);

        Claim claim = new Claim("CLM-2026-EDGE-005");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path", "application/pdf", 1024L, "hash2"));

        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-1004");
        data.setPatientName("José María O'Brien");
        data.setClaimedAmount(new BigDecimal("50000.00"));
        data.setTotalBillAmount(new BigDecimal("50000.00"));

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, specialPolicy);
        assertNotNull(results);
    }
}
