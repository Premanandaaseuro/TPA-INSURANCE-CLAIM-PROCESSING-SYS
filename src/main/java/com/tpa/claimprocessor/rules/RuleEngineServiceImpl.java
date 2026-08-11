package com.tpa.claimprocessor.rules;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimRuleResult;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RuleEngineServiceImpl implements RuleEngineService {

    private final List<RuleHandler> ruleHandlers;

    public RuleEngineServiceImpl(List<RuleHandler> ruleHandlers) {
        // Sort handlers sequentially R01..R10
        List<RuleHandler> sorted = new ArrayList<>(ruleHandlers);
        sorted.sort(Comparator.comparing(RuleHandler::getRuleCode));
        this.ruleHandlers = sorted;
    }

    @Override
    public List<RuleEvaluationResult> evaluateAllRules(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        List<RuleEvaluationResult> results = new ArrayList<>();

        for (RuleHandler handler : ruleHandlers) {
            RuleEvaluationResult result = handler.evaluate(claim, extractedData, policy);
            results.add(result);

            // Create and persist entity result attached to Claim
            ClaimRuleResult entityResult = new ClaimRuleResult(
                    claim,
                    result.getRuleCode(),
                    result.getRuleName(),
                    result.isPassed(),
                    result.getSeverity(),
                    result.getDetails()
            );
            claim.addRuleResult(entityResult);
        }

        return results;
    }
}
