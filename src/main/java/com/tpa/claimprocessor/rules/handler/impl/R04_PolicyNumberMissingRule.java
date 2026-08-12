package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

@Component
public class R04_PolicyNumberMissingRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R04";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        String policyNo = extractedData != null && extractedData.getPolicyNumber() != null ? extractedData.getPolicyNumber() : claim.getPolicyNumber();

        if (!isValidPolicyNumber(policyNo)) {
            return RuleEvaluationResult.fail("R04", "Policy Number Missing", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Policy Number was not found in the uploaded Claim Form.");
        }

        return RuleEvaluationResult.pass("R04", "Policy Number Missing", "Policy Number extracted successfully (" + policyNo + ").");
    }

    public static boolean isValidPolicyNumber(String policyNo) {
        if (policyNo == null || policyNo.trim().isEmpty()) {
            return false;
        }
        String cleaned = policyNo.trim();
        String upper = cleaned.toUpperCase();

        if (upper.equals("DETAILS") || upper.equals("POLICY") || upper.equals("N/A")
                || upper.equals("UNKNOWN") || upper.equals("NONE") || upper.equals("NULL")
                || upper.startsWith("DETAILS") || upper.startsWith("PID-") || upper.equals("PID")) {
            return false;
        }

        return cleaned.matches("(?i)^(POL-[A-Za-z0-9\\-]+|[A-Za-z0-9\\-]{3,30})$");
    }
}
