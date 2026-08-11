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
public class R08_ClaimedAmountExceedsBillRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R08";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        BigDecimal claimedAmount = extractedData.getClaimedAmount() != null ? extractedData.getClaimedAmount() : claim.getClaimedAmount();
        BigDecimal totalBillAmount = extractedData.getTotalBillAmount();

        if (claimedAmount == null || totalBillAmount == null) {
            return RuleEvaluationResult.fail("R08", "Claimed vs Bill Amount Check", RuleSeverity.NEEDS_MANUAL_REVIEW, "Claimed amount or Total Bill amount is unparseable.");
        }

        if (claimedAmount.compareTo(totalBillAmount) > 0) {
            return RuleEvaluationResult.fail("R08", "Claimed vs Bill Amount Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Claimed amount (₹" + claimedAmount + ") exceeds total bill amount (₹" + totalBillAmount + ").");
        }

        return RuleEvaluationResult.pass("R08", "Claimed vs Bill Amount Check", "Claimed amount does not exceed total bill amount.");
    }
}
