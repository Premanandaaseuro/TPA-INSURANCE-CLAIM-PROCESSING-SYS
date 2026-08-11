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
        String policyCustomerName = policy != null ? policy.getCustomerName() : null;

        if (patientName == null || patientName.trim().isEmpty()) {
            return RuleEvaluationResult.fail("R05", "Patient Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW, "Patient Name is missing from documents.");
        }

        if (policyCustomerName != null && !policyCustomerName.trim().isEmpty()) {
            String normPatient = patientName.toLowerCase().replaceAll("[^a-z]", "");
            String normCustomer = policyCustomerName.toLowerCase().replaceAll("[^a-z]", "");

            if (!normPatient.contains(normCustomer) && !normCustomer.contains(normPatient)) {
                return RuleEvaluationResult.fail("R05", "Patient Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                        "Patient Name ('" + patientName + "') mismatches Policy record ('" + policyCustomerName + "').");
            }
        }

        return RuleEvaluationResult.pass("R05", "Patient Name Mismatch Check", "Patient name matches policy records.");
    }
}
