package com.tpa.claimprocessor.decision;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;

import java.util.List;

public interface DecisionEngineService {

    void applyDecision(Claim claim, List<RuleEvaluationResult> ruleResults);
}
