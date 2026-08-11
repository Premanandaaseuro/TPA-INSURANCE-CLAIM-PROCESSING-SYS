package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class R09_HighValueClaimRule implements RuleHandler {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("50000.00");

    @Override
    public String getRuleCode() {
        return "R09";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        BigDecimal amount = extractedData.getClaimedAmount() != null ? extractedData.getClaimedAmount() : claim.getClaimedAmount();
        if (amount == null && extractedData.getTotalBillAmount() != null) {
            amount = extractedData.getTotalBillAmount();
        }

        if (amount != null && amount.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            return RuleEvaluationResult.fail("R09", "High Value Claim Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Claim amount (₹" + amount + ") exceeds automated approval limit of ₹50,000.");
        }

        return RuleEvaluationResult.pass("R09", "High Value Claim Check", "Claim amount is within automated threshold (<= ₹50,000).");
    }
}
