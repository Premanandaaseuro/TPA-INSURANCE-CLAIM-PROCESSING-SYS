package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

@Component
public class R05_PatientNameMismatchRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R05";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        String patientName = extractedData.getPatientName() != null ? extractedData.getPatientName() : claim.getPatientName();

        if (patientName == null || patientName.trim().isEmpty()) {
            return RuleEvaluationResult.fail("R05", "Patient Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW, "Patient Name is missing from documents.");
        }

        String cfPatient = extractedData.getClaimFormPatientName();
        String dsPatient = extractedData.getDischargeSummaryPatientName();
        String hbPatient = extractedData.getHospitalBillPatientName();

        if (cfPatient != null && dsPatient != null && !isNormalizedMatch(cfPatient, dsPatient)) {
            return RuleEvaluationResult.fail("R05", "Patient Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Patient Name mismatch between Claim Form ('" + cfPatient + "') and Discharge Summary ('" + dsPatient + "').");
        }

        if (cfPatient != null && hbPatient != null && !isNormalizedMatch(cfPatient, hbPatient)) {
            return RuleEvaluationResult.fail("R05", "Patient Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Patient Name mismatch between Claim Form ('" + cfPatient + "') and Hospital Bill ('" + hbPatient + "').");
        }

        String policyCustomerName = policy != null ? policy.getCustomerName() : null;
        if (policyCustomerName != null && !policyCustomerName.trim().isEmpty() && !isNormalizedMatch(patientName, policyCustomerName)) {
            return RuleEvaluationResult.fail("R05", "Patient Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Patient Name ('" + patientName + "') mismatches Policy record ('" + policyCustomerName + "').");
        }

        return RuleEvaluationResult.pass("R05", "Patient Name Mismatch Check", "Patient name matches across claim form, discharge summary and hospital bill.");
    }

    private boolean isNormalizedMatch(String s1, String s2) {
        if (s1 == null || s2 == null) return true;
        String n1 = s1.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String n2 = s2.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        if (n1.isEmpty() || n2.isEmpty()) return true;
        return n1.equals(n2) || n1.contains(n2) || n2.contains(n1);
    }
}
