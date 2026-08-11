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
        String policyNo = extractedData.getPolicyNumber() != null ? extractedData.getPolicyNumber() : claim.getPolicyNumber();

        if (!isValidPolicyNumber(policyNo)) {
            return RuleEvaluationResult.fail("R04", "Policy Number Missing Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Extracted Policy Number is missing, unparseable, or invalid (" + (policyNo != null ? policyNo : "N/A") + ").");
        }

        return RuleEvaluationResult.pass("R04", "Policy Number Missing Check", "Policy Number extracted successfully (" + policyNo + ").");
    }

    public static boolean isValidPolicyNumber(String policyNo) {
        if (policyNo == null || policyNo.trim().isEmpty()) {
            return false;
        }
        String cleaned = policyNo.trim();
        String upper = cleaned.toUpperCase();

        if (upper.equals("DETAILS") || upper.equals("POLICY") || upper.equals("N/A")
                || upper.equals("UNKNOWN") || upper.equals("NONE") || upper.equals("NULL")
                || upper.startsWith("DETAILS")) {
            return false;
        }

        return cleaned.matches("(?i)^(POL-[A-Za-z0-9\\-]+|[A-Za-z0-9\\-]{3,30})$");
    }
}
