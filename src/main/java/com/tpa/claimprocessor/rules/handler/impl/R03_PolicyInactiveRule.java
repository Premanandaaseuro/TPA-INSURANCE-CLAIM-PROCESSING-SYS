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
            String polNum = extractedData != null && extractedData.getPolicyNumber() != null ? extractedData.getPolicyNumber() : "unknown";
            return RuleEvaluationResult.fail("R03", "Policy Inactive Check", RuleSeverity.REJECTED,
                    "Policy " + polNum + " not found in database.");
        }

        LocalDate admissionDate = extractedData != null ? extractedData.getAdmissionDate() : null;
        String admStr = admissionDate != null ? admissionDate.toString() : "unknown";
        String endStr = policy.getEndDate() != null ? policy.getEndDate().toString() : "N/A";
        String startStr = policy.getStartDate() != null ? policy.getStartDate().toString() : "N/A";

        // 1. Status Check
        if (policy.getStatus() == null || !"ACTIVE".equalsIgnoreCase(policy.getStatus().trim())) {
            return RuleEvaluationResult.fail("R03", "Policy Inactive Check", RuleSeverity.REJECTED,
                    "Policy " + policy.getPolicyNumber() + " was inactive on admission date " + admStr + ". Policy coverage ended on " + endStr + ".");
        }

        // 2. Admission Date Coverage Window Check
        if (admissionDate != null) {
            if (policy.getStartDate() != null && admissionDate.isBefore(policy.getStartDate())) {
                return RuleEvaluationResult.fail("R03", "Policy Inactive Check", RuleSeverity.REJECTED,
                        "Policy " + policy.getPolicyNumber() + " was not active on admission date " + admStr + ". Policy start date is " + startStr + ".");
            }
            if (policy.getEndDate() != null && admissionDate.isAfter(policy.getEndDate())) {
                return RuleEvaluationResult.fail("R03", "Policy Inactive Check", RuleSeverity.REJECTED,
                        "Policy " + policy.getPolicyNumber() + " was inactive on admission date " + admStr + ". Policy coverage ended on " + endStr + ".");
            }
        }

        return RuleEvaluationResult.pass("R03", "Policy Inactive Check",
                "Policy " + policy.getPolicyNumber() + " is ACTIVE and valid for admission date.");
    }
}
