package com.tpa.claimprocessor.rules;

import com.tpa.claimprocessor.decision.DecisionEngineService;
import com.tpa.claimprocessor.decision.DecisionEngineServiceImpl;
import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimDocument;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.enums.DocumentType;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.handler.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class R06R07RectificationTest {

    private RuleEngineService ruleEngineService;
    private DecisionEngineService decisionEngineService;
    private ClaimRepository claimRepository;

    @BeforeEach
    void setUp() {
        claimRepository = Mockito.mock(ClaimRepository.class);
        Mockito.when(claimRepository.findAll()).thenReturn(List.of());

        List<RuleHandler> handlers = List.of(
                new R01_ClaimFormMissingRule(),
                new R02_CombinedDocMissingRule(),
                new R03_PolicyInactiveRule(),
                new R04_PolicyNumberMissingRule(),
                new R05_PatientNameMismatchRule(),
                new R06_HospitalNameMismatchRule(),
                new R07_DateMismatchRule(),
                new R08_ClaimedAmountExceedsBillRule(),
                new R09_HighValueClaimRule(),
                new R10_PossibleDuplicateClaimRule(claimRepository)
        );
        ruleEngineService = new RuleEngineServiceImpl(handlers);
        decisionEngineService = new DecisionEngineServiceImpl();
    }

    private Claim createValidBaseClaim() {
        Claim claim = new Claim();
        claim.setClaimId("CLM-TEST-001");
        claim.setPolicyNumber("POL-10001");
        claim.setPatientName("Ananya Das");
        claim.setHospitalName("Manipal Hospital Bengaluru");
        claim.setAdmissionDate(LocalDate.of(2026, 4, 10));
        claim.setDischargeDate(LocalDate.of(2026, 4, 14));
        claim.setClaimedAmount(new BigDecimal("25000.00"));

        ClaimDocument cfDoc = new ClaimDocument();
        cfDoc.setDocumentType(DocumentType.CLAIM_FORM);
        cfDoc.setFileSize(1024L);
        cfDoc.setOriginalFilename("ClaimForm.pdf");
        claim.addDocument(cfDoc);

        ClaimDocument dsDoc = new ClaimDocument();
        dsDoc.setDocumentType(DocumentType.COMBINED_HOSPITAL_DOCUMENT);
        dsDoc.setFileSize(2048L);
        dsDoc.setOriginalFilename("CombinedDoc.pdf");
        claim.addDocument(dsDoc);

        return claim;
    }

    private ExtractedClaimData createValidExtractedData() {
        ExtractedClaimData data = new ExtractedClaimData();
        data.setClaimFormRawText("Claim Form Text with Policy Number: POL-10001");
        data.setCombinedDocRawText("Discharge Summary and Final Hospital Bill");
        data.setPolicyNumber("POL-10001");
        data.setClaimFormPatientName("Ananya Das");
        data.setDischargeSummaryPatientName("Ananya Das");
        data.setHospitalBillPatientName("Ananya Das");
        data.setPatientName("Ananya Das");

        data.setClaimFormHospitalName("Manipal Hospital Bengaluru");
        data.setDischargeSummaryHospitalName("Manipal Hospital Bengaluru");
        data.setHospitalBillHospitalName("Manipal Hospital Bengaluru");
        data.setHospitalName("Manipal Hospital Bengaluru");

        data.setClaimFormAdmissionDate(LocalDate.of(2026, 4, 10));
        data.setDischargeSummaryAdmissionDate(LocalDate.of(2026, 4, 10));
        data.setAdmissionDate(LocalDate.of(2026, 4, 10));

        data.setClaimFormDischargeDate(LocalDate.of(2026, 4, 14));
        data.setDischargeSummaryDischargeDate(LocalDate.of(2026, 4, 14));
        data.setDischargeDate(LocalDate.of(2026, 4, 14));

        data.setClaimedAmount(new BigDecimal("25000.00"));
        data.setTotalBillAmount(new BigDecimal("25000.00"));
        return data;
    }

    private Policy createActivePolicy() {
        Policy policy = new Policy();
        policy.setPolicyNumber("POL-10001");
        policy.setCustomerName("Ananya Das");
        policy.setStatus("ACTIVE");
        policy.setStartDate(LocalDate.of(2025, 1, 1));
        policy.setEndDate(LocalDate.of(2027, 12, 31));
        return policy;
    }

    @Test
    @DisplayName("TEST 1: Hospital names identical -> R06 PASS")
    void test1_identicalHospitalNames() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r06 = results.stream().filter(r -> "R06".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertTrue(r06.isPassed());
        assertEquals("PASS", String.valueOf(r06.getStatus()));
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
    }

    @Test
    @DisplayName("TEST 2: Manipal Hospital Bengaluru vs Apollo Hospital Bengaluru -> R06 FAIL")
    void test2_hospitalNameMismatch_ManipalVsApollo() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        data.setDischargeSummaryHospitalName("Apollo Hospital Bengaluru");
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r06 = results.stream().filter(r -> "R06".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertFalse(r06.isPassed());
        assertEquals("FAIL", String.valueOf(r06.getStatus()));
        assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
    }

    @Test
    @DisplayName("TEST 3: MediTrust Hospital vs MediTrust Medical Center -> R06 FAIL")
    void test3_hospitalNameMismatch_MediTrustHospitalVsMedicalCenter() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        data.setClaimFormHospitalName("MediTrust Hospital");
        data.setDischargeSummaryHospitalName("MediTrust Medical Center");
        data.setHospitalBillHospitalName("MediTrust Medical Center");
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r06 = results.stream().filter(r -> "R06".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertFalse(r06.isPassed());
        assertEquals("FAIL", String.valueOf(r06.getStatus()));
        assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
    }

    @Test
    @DisplayName("TEST 4: Same dates, different formats -> R07 PASS")
    void test4_sameDatesDifferentFormats() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        data.setClaimFormAdmissionDate(LocalDate.of(2026, 4, 10));
        data.setDischargeSummaryAdmissionDate(LocalDate.of(2026, 4, 10));
        data.setClaimFormDischargeDate(LocalDate.of(2026, 4, 14));
        data.setDischargeSummaryDischargeDate(LocalDate.of(2026, 4, 14));
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r07 = results.stream().filter(r -> "R07".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertTrue(r07.isPassed());
        assertEquals("PASS", String.valueOf(r07.getStatus()));
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
    }

    @Test
    @DisplayName("TEST 5: Different admission date -> R07 FAIL")
    void test5_differentAdmissionDate() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        data.setClaimFormAdmissionDate(LocalDate.of(2026, 4, 10));
        data.setDischargeSummaryAdmissionDate(LocalDate.of(2026, 4, 11));
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r07 = results.stream().filter(r -> "R07".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertFalse(r07.isPassed());
        assertEquals("FAIL", String.valueOf(r07.getStatus()));
        assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
        assertTrue(r07.getDetails().contains("2026-04-10") && r07.getDetails().contains("2026-04-11"));
    }

    @Test
    @DisplayName("TEST 6: Different discharge date -> R07 FAIL")
    void test6_differentDischargeDate() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        data.setClaimFormDischargeDate(LocalDate.of(2026, 4, 14));
        data.setDischargeSummaryDischargeDate(LocalDate.of(2026, 4, 15));
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r07 = results.stream().filter(r -> "R07".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertFalse(r07.isPassed());
        assertEquals("FAIL", String.valueOf(r07.getStatus()));
        assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
        assertTrue(r07.getDetails().contains("2026-04-14") && r07.getDetails().contains("2026-04-15"));
    }

    @Test
    @DisplayName("TEST 7: R06 fails -> R07-R10 NOT_EVALUATED")
    void test7_r06FailsShortCircuitsSubsequentRules() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        data.setClaimFormHospitalName("MediTrust Hospital");
        data.setDischargeSummaryHospitalName("MediTrust Medical Center");
        data.setClaimFormAdmissionDate(LocalDate.of(2026, 4, 10));
        data.setDischargeSummaryAdmissionDate(LocalDate.of(2026, 4, 11));
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r06 = results.stream().filter(r -> "R06".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertFalse(r06.isPassed());

        RuleEvaluationResult r07 = results.stream().filter(r -> "R07".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertEquals("NOT_EVALUATED", String.valueOf(r07.getStatus()));

        RuleEvaluationResult r08 = results.stream().filter(r -> "R08".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertEquals("NOT_EVALUATED", String.valueOf(r08.getStatus()));

        RuleEvaluationResult r09 = results.stream().filter(r -> "R09".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertEquals("NOT_EVALUATED", String.valueOf(r09.getStatus()));

        RuleEvaluationResult r10 = results.stream().filter(r -> "R10".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertEquals("NOT_EVALUATED", String.valueOf(r10.getStatus()));

        assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
    }

    @Test
    @DisplayName("TEST 8: R07 fails -> R08-R10 NOT_EVALUATED")
    void test8_r07FailsShortCircuitsSubsequentRules() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        data.setClaimFormAdmissionDate(LocalDate.of(2026, 4, 10));
        data.setDischargeSummaryAdmissionDate(LocalDate.of(2026, 4, 11));
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r07 = results.stream().filter(r -> "R07".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertFalse(r07.isPassed());

        RuleEvaluationResult r08 = results.stream().filter(r -> "R08".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertEquals("NOT_EVALUATED", String.valueOf(r08.getStatus()));

        RuleEvaluationResult r09 = results.stream().filter(r -> "R09".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertEquals("NOT_EVALUATED", String.valueOf(r09.getStatus()));

        RuleEvaluationResult r10 = results.stream().filter(r -> "R10".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertEquals("NOT_EVALUATED", String.valueOf(r10.getStatus()));

        assertEquals(ClaimStatus.NEEDS_MANUAL_REVIEW, claim.getStatus());
    }

    @Test
    @DisplayName("TEST 9: All rules pass -> APPROVED")
    void test9_allRulesPass() {
        Claim claim = createValidBaseClaim();
        ExtractedClaimData data = createValidExtractedData();
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
    }

    @Test
    @DisplayName("TEST 10: R01 fails -> REJECTED, R02 PASS (if doc uploaded), R03-R10 NOT_EVALUATED")
    void test10_r01FailsShortCircuitsAll() {
        Claim claim = createValidBaseClaim();
        claim.getDocuments().removeIf(d -> d.getDocumentType() == DocumentType.CLAIM_FORM);
        ExtractedClaimData data = createValidExtractedData();
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r01 = results.stream().filter(r -> "R01".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertFalse(r01.isPassed());
        assertEquals("FAIL", String.valueOf(r01.getStatus()));

        RuleEvaluationResult r02 = results.stream().filter(r -> "R02".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertTrue(r02.isPassed());
        assertEquals("PASS", String.valueOf(r02.getStatus()));

        for (int i = 3; i <= 10; i++) {
            String code = String.format("R%02d", i);
            RuleEvaluationResult r = results.stream().filter(res -> code.equals(res.getRuleCode())).findFirst().orElseThrow();
            assertEquals("NOT_EVALUATED", String.valueOf(r.getStatus()));
        }

        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
    }

    @Test
    @DisplayName("TEST 11: R02 fails -> REJECTED and later rules NOT_EVALUATED")
    void test11_r02FailsShortCircuitsAll() {
        Claim claim = createValidBaseClaim();
        claim.getDocuments().removeIf(d -> d.getDocumentType() == DocumentType.COMBINED_HOSPITAL_DOCUMENT);
        ExtractedClaimData data = createValidExtractedData();
        Policy policy = createActivePolicy();

        List<RuleEvaluationResult> results = ruleEngineService.evaluateAllRules(claim, data, policy);
        decisionEngineService.applyDecision(claim, results);

        RuleEvaluationResult r02 = results.stream().filter(r -> "R02".equals(r.getRuleCode())).findFirst().orElseThrow();
        assertFalse(r02.isPassed());
        assertEquals("FAIL", String.valueOf(r02.getStatus()));

        for (int i = 3; i <= 10; i++) {
            String code = String.format("R%02d", i);
            RuleEvaluationResult r = results.stream().filter(res -> code.equals(res.getRuleCode())).findFirst().orElseThrow();
            assertEquals("NOT_EVALUATED", String.valueOf(r.getStatus()));
        }

        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
    }
}
