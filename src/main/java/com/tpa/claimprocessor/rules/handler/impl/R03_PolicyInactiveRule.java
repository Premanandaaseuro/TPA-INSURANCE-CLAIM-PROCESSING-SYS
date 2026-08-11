package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class R03_PolicyInactiveRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R03";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        if (policy == null) {
            return RuleEvaluationResult.fail("R03", "Policy Inactive Check", RuleSeverity.REJECTED, "Policy not found in policy registry database.");
        }

        if (!"ACTIVE".equalsIgnoreCase(policy.getStatus())) {
            return RuleEvaluationResult.fail("R03", "Policy Inactive Check", RuleSeverity.REJECTED, "Policy status is " + policy.getStatus() + " (Inactive).");
        }

        LocalDate admissionDate = extractedData.getAdmissionDate() != null ? extractedData.getAdmissionDate() : claim.getAdmissionDate();
        if (admissionDate != null) {
            if (policy.getStartDate() != null && admissionDate.isBefore(policy.getStartDate())) {
                return RuleEvaluationResult.fail("R03", "Policy Inactive Check", RuleSeverity.REJECTED, "Admission date (" + admissionDate + ") is prior to policy start date (" + policy.getStartDate() + ").");
            }
            if (policy.getEndDate() != null && admissionDate.isAfter(policy.getEndDate())) {
                return RuleEvaluationResult.fail("R03", "Policy Inactive Check", RuleSeverity.REJECTED, "Admission date (" + admissionDate + ") is after policy expiration date (" + policy.getEndDate() + ").");
            }
        }

        return RuleEvaluationResult.pass("R03", "Policy Inactive Check", "Policy is active for admission date.");
    }
}
