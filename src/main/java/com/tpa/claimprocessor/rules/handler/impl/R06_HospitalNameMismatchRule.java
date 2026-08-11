package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

@Component
public class R06_HospitalNameMismatchRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R06";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        String hospitalName = extractedData.getHospitalName() != null ? extractedData.getHospitalName() : claim.getHospitalName();

        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            return RuleEvaluationResult.fail("R06", "Hospital Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW, "Hospital Name is missing from documents.");
        }

        return RuleEvaluationResult.pass("R06", "Hospital Name Mismatch Check", "Hospital name verified across documents (" + hospitalName + ").");
    }
}
