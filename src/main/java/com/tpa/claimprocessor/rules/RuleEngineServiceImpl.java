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

        String stopReasonRuleCode = null;

        // 1. R01 - Claim Form Presence
        RuleEvaluationResult r01Res = null;
        if (r01 != null) {
            r01Res = executeAndRecord(r01, claim, extractedData, policy, results);
            if (!r01Res.isPassed()) {
                stopReasonRuleCode = "R01";
            }
        }

        // 2. R02 - Combined Hospital Document Presence
        RuleEvaluationResult r02Res = null;
        if (r02 != null) {
            r02Res = executeAndRecord(r02, claim, extractedData, policy, results);
            if (!r02Res.isPassed() && stopReasonRuleCode == null) {
                stopReasonRuleCode = "R02";
            }
        }

        // 3. R03 & R04 - Policy Number & Inactive Check
        if (stopReasonRuleCode != null) {
            recordNotEvaluated("R03", "Policy Inactive Check",
                    "Policy inactive check was not evaluated because a prior rule (" + stopReasonRuleCode + ") failed.", claim, results);
            recordNotEvaluated("R04", "Policy Number Missing",
                    "Policy number missing check was not evaluated because a prior rule (" + stopReasonRuleCode + ") failed.", claim, results);
        } else {
            RuleEvaluationResult r04Res = r04 != null ? r04.evaluate(claim, extractedData, policy) : null;
            boolean hasPolicyNum = r04Res != null && r04Res.isPassed();

            if (hasPolicyNum) {
                if (r03 != null) {
                    RuleEvaluationResult r03Res = executeAndRecord(r03, claim, extractedData, policy, results);
                    if (!r03Res.isPassed()) {
                        stopReasonRuleCode = "R03";
                    }
                }
                if (r04Res != null) {
                    recordResult(r04Res, claim, results);
                }
            } else {
                recordNotEvaluated("R03", "Policy Inactive Check",
                        "Policy inactive check was not evaluated because the Policy Number was missing from the uploaded Claim Form.", claim, results);
                if (r04Res != null) {
                    recordResult(r04Res, claim, results);
                    stopReasonRuleCode = "R04";
                }
            }
        }

        // 4. R05 - Patient Name Mismatch Check
        if (stopReasonRuleCode != null) {
            recordNotEvaluated("R05", "Patient Name Mismatch Check",
                    "Patient name mismatch check was not evaluated because a prior rule (" + stopReasonRuleCode + ") failed.", claim, results);
        } else if (r05 != null) {
            RuleEvaluationResult res = executeAndRecord(r05, claim, extractedData, policy, results);
            if (!res.isPassed()) {
                stopReasonRuleCode = "R05";
            }
        }

        // 5. R06 - Hospital Name Mismatch Check
        if (stopReasonRuleCode != null) {
            recordNotEvaluated("R06", "Hospital Name Mismatch Check",
                    "Hospital name mismatch check was not evaluated because a prior rule (" + stopReasonRuleCode + ") failed.", claim, results);
        } else if (r06 != null) {
            RuleEvaluationResult res = executeAndRecord(r06, claim, extractedData, policy, results);
            if (!res.isPassed()) {
                stopReasonRuleCode = "R06";
            }
        }

        // 6. R07 - Admission/Discharge Date Check
        if (stopReasonRuleCode != null) {
            recordNotEvaluated("R07", "Admission/Discharge Date Check",
                    "Admission/discharge date check was not evaluated because a prior rule (" + stopReasonRuleCode + ") failed.", claim, results);
        } else if (r07 != null) {
            RuleEvaluationResult res = executeAndRecord(r07, claim, extractedData, policy, results);
            if (!res.isPassed()) {
                stopReasonRuleCode = "R07";
            }
        }

        // 7. R08 - Claimed Amount Exceeds Bill Check
        if (stopReasonRuleCode != null) {
            recordNotEvaluated("R08", "Claimed Amount Exceeds Bill Check",
                    "Claimed amount check was not evaluated because a prior rule (" + stopReasonRuleCode + ") failed.", claim, results);
        } else if (r08 != null) {
            RuleEvaluationResult res = executeAndRecord(r08, claim, extractedData, policy, results);
            if (!res.isPassed()) {
                stopReasonRuleCode = "R08";
            }
        }

        // 8. R09 - High Value Claim Check
        if (stopReasonRuleCode != null) {
            recordNotEvaluated("R09", "High Value Claim Check",
                    "High value claim check was not evaluated because a prior rule (" + stopReasonRuleCode + ") failed.", claim, results);
        } else if (r09 != null) {
            RuleEvaluationResult res = executeAndRecord(r09, claim, extractedData, policy, results);
            if (!res.isPassed()) {
                stopReasonRuleCode = "R09";
            }
        }

        // 9. R10 - Possible Duplicate Claim Check
        if (stopReasonRuleCode != null) {
            recordNotEvaluated("R10", "Possible Duplicate Claim Check",
                    "Possible duplicate claim check was not evaluated because a prior rule (" + stopReasonRuleCode + ") failed.", claim, results);
        } else if (r10 != null) {
            RuleEvaluationResult res = executeAndRecord(r10, claim, extractedData, policy, results);
            if (!res.isPassed()) {
                stopReasonRuleCode = "R10";
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
        return recordResult(result, claim, results);
    }

    private RuleEvaluationResult recordResult(RuleEvaluationResult result, Claim claim, List<RuleEvaluationResult> results) {
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
        recordResult(notEval, claim, results);
    }
}
