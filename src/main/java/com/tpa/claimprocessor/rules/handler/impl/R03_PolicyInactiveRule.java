package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

@Component
public class R03_PolicyInactiveRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R03";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        if (policy == null) {
            return RuleEvaluationResult.fail("R03", "Policy Exists Check", RuleSeverity.REJECTED, "Policy not found in policy registry database.");
        }

        return RuleEvaluationResult.pass("R03", "Policy Exists Check", "Policy " + policy.getPolicyNumber() + " found in policy registry database.");
    }
}
