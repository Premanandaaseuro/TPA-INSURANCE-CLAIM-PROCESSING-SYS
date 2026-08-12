package com.tpa.claimprocessor.rules;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimRuleResult;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RuleEngineServiceImpl implements RuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineServiceImpl.class);

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

        RuleHandler r01 = getHandler("R01");
        RuleHandler r02 = getHandler("R02");
        RuleHandler r03 = getHandler("R03");
        RuleHandler r04 = getHandler("R04");
        RuleHandler r05 = getHandler("R05");
        RuleHandler r06 = getHandler("R06");
        RuleHandler r07 = getHandler("R07");
        RuleHandler r08 = getHandler("R08");
        RuleHandler r09 = getHandler("R09");
        RuleHandler r10 = getHandler("R10");

        if (r01 != null) executeAndRecord(r01, claim, extractedData, policy, results);
        if (r02 != null) executeAndRecord(r02, claim, extractedData, policy, results);

        RuleEvaluationResult r04Result = null;
        if (r04 != null) {
            r04Result = executeAndRecord(r04, claim, extractedData, policy, results);
            log.info("R04 CHECK = {}", r04Result.isPassed() ? "PASS" : "FAIL");
        }

        boolean executingR03 = (r04Result != null && r04Result.isPassed() && r03 != null);
        log.info("EXECUTING R03 = {}", executingR03);

        if (executingR03) {
            executeAndRecord(r03, claim, extractedData, policy, results);
        }

        if (r05 != null) executeAndRecord(r05, claim, extractedData, policy, results);
        if (r06 != null) executeAndRecord(r06, claim, extractedData, policy, results);
        if (r07 != null) executeAndRecord(r07, claim, extractedData, policy, results);
        if (r08 != null) executeAndRecord(r08, claim, extractedData, policy, results);
        if (r09 != null) executeAndRecord(r09, claim, extractedData, policy, results);
        if (r10 != null) executeAndRecord(r10, claim, extractedData, policy, results);

        return results;
    }

    private RuleHandler getHandler(String ruleCode) {
        return ruleHandlers.stream()
                .filter(h -> ruleCode.equals(h.getRuleCode()))
                .findFirst()
                .orElse(null);
    }

    private RuleEvaluationResult executeAndRecord(RuleHandler handler, Claim claim, ExtractedClaimData extractedData, Policy policy, List<RuleEvaluationResult> results) {
        RuleEvaluationResult result = handler.evaluate(claim, extractedData, policy);
        results.add(result);

        ClaimRuleResult entityResult = new ClaimRuleResult(
                claim,
                result.getRuleCode(),
                result.getRuleName(),
                result.isPassed(),
                result.getSeverity(),
                result.getDetails()
        );
        claim.addRuleResult(entityResult);
        return result;
    }
}
