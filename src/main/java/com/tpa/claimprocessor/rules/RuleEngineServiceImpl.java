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

        // 1. Evaluate R01 (Claim Form Presence)
        RuleEvaluationResult r01Result = null;
        if (r01 != null) {
            r01Result = executeAndRecord(r01, claim, extractedData, policy, results);
        }
        boolean hasClaimForm = r01Result != null && r01Result.isPassed();

        // 2. Evaluate R02 (Combined Hospital Document Presence)
        RuleEvaluationResult r02Result = null;
        if (r02 != null) {
            r02Result = executeAndRecord(r02, claim, extractedData, policy, results);
        }
        boolean hasCombinedDoc = r02Result != null && r02Result.isPassed();

        // 3. Evaluate Downstream Rules based on Document Availability
        if (!hasClaimForm) {
            // Claim Form is missing -> R03..R10 cannot be evaluated!
            recordNotEvaluated("R03", "Policy Inactive Check", "Policy inactive check was not evaluated because the Claim Form was missing.", claim, results);
            recordNotEvaluated("R04", "Policy Number Missing", "Policy number missing check was not evaluated because the Claim Form was missing.", claim, results);
            recordNotEvaluated("R05", "Patient Name Mismatch Check", "Patient name mismatch check was not evaluated because the Claim Form was missing.", claim, results);
            recordNotEvaluated("R06", "Hospital Name Mismatch Check", "Hospital name mismatch check was not evaluated because the Claim Form was missing.", claim, results);
            recordNotEvaluated("R07", "Admission/Discharge Date Check", "Admission/discharge date check was not evaluated because the Claim Form was missing.", claim, results);
            recordNotEvaluated("R08", "Claimed Amount Exceeds Bill Check", "Claimed amount check was not evaluated because the Claim Form was missing.", claim, results);
            recordNotEvaluated("R09", "High Value Claim Check", "High value claim check was not evaluated because the Claim Form was missing.", claim, results);
            recordNotEvaluated("R10", "Possible Duplicate Claim Check", "Possible duplicate claim check was not evaluated because the Claim Form was missing.", claim, results);

        } else {
            // Claim Form is present -> Evaluate R04 (Policy Number Missing Check inside Claim Form)
            RuleEvaluationResult r04Result = null;
            if (r04 != null) {
                r04Result = executeAndRecord(r04, claim, extractedData, policy, results);
                log.info("R04 CHECK = {}", r04Result.isPassed() ? "PASS" : "FAIL");
            }

            // R03 (Active Policy Check) ONLY runs if Policy Number was present & extracted (R04 PASS)
            boolean executingR03 = (r04Result != null && r04Result.isPassed() && r03 != null);
            log.info("EXECUTING R03 = {}", executingR03);

            if (executingR03) {
                executeAndRecord(r03, claim, extractedData, policy, results);
            } else {
                recordNotEvaluated("R03", "Policy Inactive Check", "Policy inactive check was not evaluated because the Policy Number was missing from the uploaded Claim Form.", claim, results);
            }

            // Evaluate R05..R10 if Combined Hospital Document is present
            if (!hasCombinedDoc) {
                recordNotEvaluated("R05", "Patient Name Mismatch Check", "Patient name mismatch check was not evaluated because the Combined Hospital Document was missing.", claim, results);
                recordNotEvaluated("R06", "Hospital Name Mismatch Check", "Hospital name mismatch check was not evaluated because the Combined Hospital Document was missing.", claim, results);
                recordNotEvaluated("R07", "Admission/Discharge Date Check", "Admission/discharge date check was not evaluated because the Combined Hospital Document was missing.", claim, results);
                recordNotEvaluated("R08", "Claimed Amount Exceeds Bill Check", "Claimed amount check was not evaluated because the Combined Hospital Document was missing.", claim, results);
                recordNotEvaluated("R09", "High Value Claim Check", "High value claim check was not evaluated because the Combined Hospital Document was missing.", claim, results);
                recordNotEvaluated("R10", "Possible Duplicate Claim Check", "Possible duplicate claim check was not evaluated because the Combined Hospital Document was missing.", claim, results);
            } else {
                if (r05 != null) executeAndRecord(r05, claim, extractedData, policy, results);
                if (r06 != null) executeAndRecord(r06, claim, extractedData, policy, results);
                if (r07 != null) executeAndRecord(r07, claim, extractedData, policy, results);
                if (r08 != null) executeAndRecord(r08, claim, extractedData, policy, results);
                if (r09 != null) executeAndRecord(r09, claim, extractedData, policy, results);
                if (r10 != null) executeAndRecord(r10, claim, extractedData, policy, results);
            }
        }

        // Always sort R01 through R10 in exact sequential order
        results.sort(Comparator.comparing(RuleEvaluationResult::getRuleCode));
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
                result.getStatus(),
                result.getSeverity(),
                result.getDetails()
        );
        claim.addRuleResult(entityResult);
        return result;
    }

    private void recordNotEvaluated(String ruleCode, String ruleName, String details, Claim claim, List<RuleEvaluationResult> results) {
        RuleEvaluationResult notEval = RuleEvaluationResult.notEvaluated(ruleCode, ruleName, details);
        results.add(notEval);

        ClaimRuleResult entityResult = new ClaimRuleResult(
                claim,
                notEval.getRuleCode(),
                notEval.getRuleName(),
                notEval.isPassed(),
                notEval.getStatus(),
                notEval.getSeverity(),
                notEval.getDetails()
        );
        claim.addRuleResult(entityResult);
    }
}
