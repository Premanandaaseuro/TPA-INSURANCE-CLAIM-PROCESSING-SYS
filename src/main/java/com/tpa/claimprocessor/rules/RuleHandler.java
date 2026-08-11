package com.tpa.claimprocessor.rules;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;

public interface RuleHandler {

    String getRuleCode();

    RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy);
}
