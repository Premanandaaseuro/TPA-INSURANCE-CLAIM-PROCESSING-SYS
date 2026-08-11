package com.tpa.claimprocessor.rules;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;

import java.util.List;

public interface RuleEngineService {

    List<RuleEvaluationResult> evaluateAllRules(Claim claim, ExtractedClaimData extractedData, Policy policy);
}
