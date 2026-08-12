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
import org.junit.jupiter.api.Nested;
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
class RuleEngineAuditTest {

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private DecisionEngineService decisionEngineService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private R01_ClaimFormMissingRule r01Rule;
    @Autowired
    private R02_CombinedDocMissingRule r02Rule;
    @Autowired
    private R03_PolicyInactiveRule r03Rule;
    @Autowired
    private R04_PolicyNumberMissingRule r04Rule;
    @Autowired
    private R05_PatientNameMismatchRule r05Rule;
    @Autowired
    private R06_HospitalNameMismatchRule r06Rule;
    @Autowired
    private R07_DateMismatchRule r07Rule;
    @Autowired
    private R08_ClaimedAmountExceedsBillRule r08Rule;
    @Autowired
    private R09_HighValueClaimRule r09Rule;
    @Autowired
    private R10_PossibleDuplicateClaimRule r10Rule;

    private Policy activePolicy;

    @BeforeEach
    void setUp() {
        activePolicy = new Policy(
                "POL-2026-8899",
                "Comprehensive Health Plan",
                "Rahul Sharma",
                "Star Health Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        policyRepository.save(activePolicy);
    }

    private Claim createBaseClaim(String claimId) {
        Claim claim = new Claim(claimId);
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path/form.pdf", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path/combined.pdf", "application/pdf", 2048L, "hash2"));
        return claim;
    }

    private ExtractedClaimData createCleanExtractedData() {
        ExtractedClaimData data = new ExtractedClaimData();
        data.setPolicyNumber("POL-2026-8899");
        data.setPatientName("Rahul Sharma");
        data.setCustomerName("Rahul Sharma");
        data.setHospitalName("Apollo Hospital");
        data.setAdmissionDate(LocalDate.of(2026, 7, 10));
        data.setDischargeDate(LocalDate.of(2026, 7, 15));
        data.setClaimedAmount(new BigDecimal("45000.00"));
        data.setTotalBillAmount(new BigDecimal("45000.00"));
        return data;
    }

    // ==========================================
    // 1. INDIVIDUAL RULE AUDITS (R01 THROUGH R10)
    // ==========================================
    @Nested
    @DisplayName("Individual Rule Audits (R01 - R10)")
    class IndividualRuleAudits {

        @Test
        @DisplayName("R01 - Claim Form Missing")
        void testR01() {
            Claim claimWithDoc = createBaseClaim("CLM-TEST-001");
            RuleEvaluationResult passResult = r01Rule.evaluate(claimWithDoc, createCleanExtractedData(), activePolicy);
            assertTrue(passResult.isPassed());

            Claim claimNoDoc = new Claim("CLM-TEST-002"); // missing docs
            RuleEvaluationResult failResult = r01Rule.evaluate(claimNoDoc, createCleanExtractedData(), activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.REJECTED, failResult.getSeverity());
            assertTrue(failResult.getDetails().contains("Claim Form is missing"));
        }

        @Test
        @DisplayName("R02 - Combined Document Missing")
        void testR02() {
            Claim claimWithDoc = createBaseClaim("CLM-TEST-003");
            RuleEvaluationResult passResult = r02Rule.evaluate(claimWithDoc, createCleanExtractedData(), activePolicy);
            assertTrue(passResult.isPassed());

            Claim claimNoCombined = new Claim("CLM-TEST-004");
            claimNoCombined.addDocument(new ClaimDocument(claimNoCombined, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 100L, "h"));
            RuleEvaluationResult failResult = r02Rule.evaluate(claimNoCombined, createCleanExtractedData(), activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.REJECTED, failResult.getSeverity());
            assertTrue(failResult.getDetails().contains("Combined Hospital Document is missing"));
        }

        @Test
        @DisplayName("R03 - Policy Inactive Check")
        void testR03() {
            Claim claim = createBaseClaim("CLM-TEST-005");
            ExtractedClaimData cleanData = createCleanExtractedData();
            RuleEvaluationResult passResult = r03Rule.evaluate(claim, cleanData, activePolicy);
            assertTrue(passResult.isPassed());

            // Inactive Policy (POL-10002) test case
            Policy inactivePolicy = new Policy(
                    "POL-10002",
                    "Family Care",
                    "Priya Sharma",
                    "Aseuro Insurance",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 3, 31),
                    new BigDecimal("300000.00"),
                    "INACTIVE"
            );

            ExtractedClaimData inactiveData = createCleanExtractedData();
            inactiveData.setPolicyNumber("POL-10002");
            inactiveData.setPatientName("Priya Sharma");
            inactiveData.setAdmissionDate(LocalDate.of(2026, 4, 10));

            RuleEvaluationResult failInactive = r03Rule.evaluate(claim, inactiveData, inactivePolicy);
            assertFalse(failInactive.isPassed());
            assertEquals(RuleSeverity.REJECTED, failInactive.getSeverity());
            assertTrue(failInactive.getDetails().contains("inactive on admission date"));

            // Null policy (policy does not exist in database)
            RuleEvaluationResult failResult = r03Rule.evaluate(claim, cleanData, null);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.REJECTED, failResult.getSeverity());
            assertTrue(failResult.getDetails().contains("not found"));
        }

        @Test
        @DisplayName("R04 - Policy Number Missing")
        void testR04() {
            Claim claim = createBaseClaim("CLM-TEST-006");
            RuleEvaluationResult passResult = r04Rule.evaluate(claim, createCleanExtractedData(), activePolicy);
            assertTrue(passResult.isPassed());

            ExtractedClaimData noPolicyData = createCleanExtractedData();
            noPolicyData.setPolicyNumber(null);
            RuleEvaluationResult failResult = r04Rule.evaluate(claim, noPolicyData, activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.NEEDS_MANUAL_REVIEW, failResult.getSeverity());
        }

        @Test
        @DisplayName("R05 - Patient Name Mismatch")
        void testR05() {
            Claim claim = createBaseClaim("CLM-TEST-007");
            RuleEvaluationResult passResult = r05Rule.evaluate(claim, createCleanExtractedData(), activePolicy);
            assertTrue(passResult.isPassed());

            ExtractedClaimData mismatchData = createCleanExtractedData();
            mismatchData.setPatientName("Vikram Singh");
            RuleEvaluationResult failResult = r05Rule.evaluate(claim, mismatchData, activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.NEEDS_MANUAL_REVIEW, failResult.getSeverity());
        }

        @Test
        @DisplayName("R06 - Hospital Name Mismatch")
        void testR06() {
            Claim claim = createBaseClaim("CLM-TEST-008");
            RuleEvaluationResult passResult = r06Rule.evaluate(claim, createCleanExtractedData(), activePolicy);
            assertTrue(passResult.isPassed());

            ExtractedClaimData noHospitalData = createCleanExtractedData();
            noHospitalData.setHospitalName(null);
            RuleEvaluationResult failResult = r06Rule.evaluate(claim, noHospitalData, activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.NEEDS_MANUAL_REVIEW, failResult.getSeverity());
        }

        @Test
        @DisplayName("R07 - Admission/Discharge Date Mismatch")
        void testR07() {
            Claim claim = createBaseClaim("CLM-TEST-009");
            RuleEvaluationResult passResult = r07Rule.evaluate(claim, createCleanExtractedData(), activePolicy);
            assertTrue(passResult.isPassed());

            ExtractedClaimData invalidDateData = createCleanExtractedData();
            invalidDateData.setAdmissionDate(LocalDate.of(2026, 7, 20));
            invalidDateData.setDischargeDate(LocalDate.of(2026, 7, 15)); // Admission > Discharge
            RuleEvaluationResult failResult = r07Rule.evaluate(claim, invalidDateData, activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.NEEDS_MANUAL_REVIEW, failResult.getSeverity());
        }

        @Test
        @DisplayName("R08 - Claimed Amount Exceeds Bill Amount")
        void testR08() {
            Claim claim = createBaseClaim("CLM-TEST-010");
            RuleEvaluationResult passResult = r08Rule.evaluate(claim, createCleanExtractedData(), activePolicy);
            assertTrue(passResult.isPassed());

            ExtractedClaimData excessData = createCleanExtractedData();
            excessData.setClaimedAmount(new BigDecimal("60000.00"));
            excessData.setTotalBillAmount(new BigDecimal("45000.00")); // Claimed > Bill
            RuleEvaluationResult failResult = r08Rule.evaluate(claim, excessData, activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.NEEDS_MANUAL_REVIEW, failResult.getSeverity());
        }

        @Test
        @DisplayName("R09 - High Value Claim (> ₹50,000)")
        void testR09() {
            Claim claim = createBaseClaim("CLM-TEST-011");
            RuleEvaluationResult passResult = r09Rule.evaluate(claim, createCleanExtractedData(), activePolicy); // ₹45,000
            assertTrue(passResult.isPassed());

            ExtractedClaimData highValueData = createCleanExtractedData();
            highValueData.setClaimedAmount(new BigDecimal("75000.00")); // > ₹50k
            RuleEvaluationResult failResult = r09Rule.evaluate(claim, highValueData, activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.NEEDS_MANUAL_REVIEW, failResult.getSeverity());
        }

        @Test
        @DisplayName("R10 - Possible Duplicate Claim")
        void testR10() {
            Claim claim1 = createBaseClaim("CLM-2026-100001");
            claim1.setPolicyNumber("POL-2026-8899");
            claim1.setPatientName("Rahul Sharma");
            claim1.setAdmissionDate(LocalDate.of(2026, 7, 10));
            claimRepository.save(claim1);

            Claim newClaim = createBaseClaim("CLM-2026-100002");
            ExtractedClaimData data = createCleanExtractedData();
            RuleEvaluationResult failResult = r10Rule.evaluate(newClaim, data, activePolicy);
            assertFalse(failResult.isPassed());
            assertEquals(RuleSeverity.NEEDS_MANUAL_REVIEW, failResult.getSeverity());
        }
    }

    // ====================================================
    // 2. COMBINATION TESTS & PRIORITY MATRIX VERIFICATION
    // ====================================================
    @Nested
    @DisplayName("Rule Combinations & Priority Resolution Matrix")
    class RuleCombinationsAndPriority {

        @Test
        @DisplayName("Clean Claim -> APPROVED")
        void testCleanClaim_Approved() {
            Claim claim = createBaseClaim("CLM-CLEAN-001");
            ExtractedClaimData data = createCleanExtractedData();

            List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
            decisionEngineService.applyDecision(claim, results);

            assertEquals(ClaimStatus.APPROVED, claim.getStatus());
            assertTrue(claim.getDecisionReason().toLowerCase().contains("approved"));
            assertEquals(10, claim.getRuleResults().size());
            assertTrue(claim.getRuleResults().stream().allMatch(r -> r.isPassed()));
        }

        @Test
        @DisplayName("Combination R03 + R08 -> REJECTED Priority Over MANUAL_REVIEW")
        void testCombination_R03_R08_RejectedPriority() {
            Claim claim = createBaseClaim("CLM-COMB-001");
            ExtractedClaimData data = createCleanExtractedData();
            data.setClaimedAmount(new BigDecimal("90000.00"));
            data.setTotalBillAmount(new BigDecimal("45000.00")); // Triggers R08 (MANUAL_REVIEW)

            // Null policy triggers R03 (REJECTED)
            List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, null);
            decisionEngineService.applyDecision(claim, results);

            // MUST be REJECTED because REJECTED > NEEDS_MANUAL_REVIEW
            assertEquals(ClaimStatus.REJECTED, claim.getStatus());
            assertTrue(claim.getDecisionReason().contains("R03"));
        }

        @Test
        @DisplayName("Combination R05 + R09 -> NEEDS_MANUAL_REVIEW")
        void testCombination_R05_R09_ManualReview() {
            Claim claim = createBaseClaim("CLM-COMB-002");
            ExtractedClaimData data = createCleanExtractedData();
            data.setPatientName("Suresh Kumar"); // Mismatch with policy (Triggers R05)
            data.setClaimedAmount(new BigDecimal("80000.00")); // > ₹50k (Triggers R09)

            List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
            decisionEngineService.applyDecision(claim, results);

            assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
            assertTrue(claim.getDecisionReason().contains("R05"));
            assertTrue(claim.getDecisionReason().contains("R09"));
        }

        @Test
        @DisplayName("Combination R04 + R08 + R10 -> NEEDS_MANUAL_REVIEW with Multi-Rule Audit Trail")
        void testCombination_R04_R08_R10_MultiRuleAuditTrail() {
            // Seed duplicate claim for R10
            Claim dup = createBaseClaim("CLM-DUP-99");
            dup.setPatientName("Rahul Sharma");
            dup.setAdmissionDate(LocalDate.of(2026, 7, 10));
            claimRepository.saveAndFlush(dup);

            Claim claim = createBaseClaim("CLM-COMB-003");
            claim.setPolicyNumber(null);
            ExtractedClaimData data = createCleanExtractedData();
            data.setPolicyNumber(null); // Extracted is null, triggering R04
            data.setClaimedAmount(new BigDecimal("99000.00"));
            data.setTotalBillAmount(new BigDecimal("10000.00")); // Triggers R08
            // Duplicate patient/admission triggers R10


            List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
            decisionEngineService.applyDecision(claim, results);

            assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
            assertTrue(claim.getDecisionReason().contains("R04"), "Expected R04 in decision reason, but was: " + claim.getDecisionReason());
            assertTrue(claim.getDecisionReason().contains("R08"), "Expected R08 in decision reason, but was: " + claim.getDecisionReason());
            assertTrue(claim.getDecisionReason().contains("R10"), "Expected R10 in decision reason, but was: " + claim.getDecisionReason());
            // All 10 rules recorded
            assertEquals(10, claim.getRuleResults().size());
            assertTrue(claim.getRuleResults().stream().anyMatch(r -> "R03".equals(r.getRuleCode()) && r.getStatus() == com.tpa.claimprocessor.domain.enums.RuleStatus.NOT_EVALUATED));
        }

        @Test
        @DisplayName("TEST 1 - Missing Policy Number -> R04 FAIL, R03 NOT_EVALUATED, Status NEEDS_MANUAL_REVIEW")
        void testMissingPolicyNumber_SkipsR03() {
            Claim claim = createBaseClaim("CLM-R04-001");
            claim.setPolicyNumber(null);
            ExtractedClaimData data = createCleanExtractedData();
            data.setPolicyNumber(null);

            List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, activePolicy);
            decisionEngineService.applyDecision(claim, results);

            assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
            assertTrue(claim.getRuleResults().stream().anyMatch(r -> "R04".equals(r.getRuleCode()) && !r.isPassed()));
            assertTrue(claim.getRuleResults().stream().anyMatch(r -> "R03".equals(r.getRuleCode()) && r.getStatus() == com.tpa.claimprocessor.domain.enums.RuleStatus.NOT_EVALUATED));
        }

        @Test
        @DisplayName("TEST 2 - Inactive Policy -> R04 PASS, R03 FAIL, Status REJECTED")
        void testInactivePolicy_TriggersR03Fail() {
            Claim claim = createBaseClaim("CLM-R03-001");
            claim.setPolicyNumber("POL-10002");
            ExtractedClaimData data = createCleanExtractedData();
            data.setPolicyNumber("POL-10002");
            data.setAdmissionDate(LocalDate.of(2026, 4, 10));

            Policy inactivePolicy = new Policy(
                    "POL-10002",
                    "Health Secure",
                    "Test User",
                    "Star Insurance",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 3, 31),
                    new BigDecimal("100000.00"),
                    "INACTIVE"
            );

            List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, inactivePolicy);
            decisionEngineService.applyDecision(claim, results);

            assertEquals(ClaimStatus.REJECTED, claim.getStatus());
            assertTrue(claim.getRuleResults().stream().anyMatch(r -> "R04".equals(r.getRuleCode()) && r.isPassed()));
            assertTrue(claim.getRuleResults().stream().anyMatch(r -> "R03".equals(r.getRuleCode()) && !r.isPassed()));
        }
    }
}
