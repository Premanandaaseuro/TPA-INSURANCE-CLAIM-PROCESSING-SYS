package com.tpa.claimprocessor.decision;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DecisionEngineServiceImpl implements DecisionEngineService {

    @Override
    public void applyDecision(Claim claim, List<RuleEvaluationResult> ruleResults) {
        if (ruleResults == null || ruleResults.isEmpty()) {
            claim.setStatus(ClaimStatus.NEEDS_MANUAL_REVIEW);
            claim.setDecisionReason("No business rule evaluations recorded.");
            claim.setProcessedAt(LocalDateTime.now());
            return;
        }

        List<RuleEvaluationResult> rejectedFailures = ruleResults.stream()
                .filter(r -> !r.isPassed() && r.getSeverity() == RuleSeverity.REJECTED)
                .collect(Collectors.toList());

        List<RuleEvaluationResult> manualReviewFailures = ruleResults.stream()
                .filter(r -> !r.isPassed() && r.getSeverity() == RuleSeverity.NEEDS_MANUAL_REVIEW)
                .collect(Collectors.toList());

        // Decision Priority: REJECTED > NEEDS_MANUAL_REVIEW > APPROVED
        if (!rejectedFailures.isEmpty()) {
            claim.setStatus(ClaimStatus.REJECTED);
            String reasons = rejectedFailures.stream()
                    .map(r -> r.getRuleCode() + ": " + r.getDetails())
                    .collect(Collectors.joining(" | "));
            claim.setDecisionReason("Claim REJECTED: " + reasons);

        } else if (!manualReviewFailures.isEmpty()) {
            claim.setStatus(ClaimStatus.NEEDS_MANUAL_REVIEW);
            String reasons = manualReviewFailures.stream()
                    .map(r -> r.getRuleCode() + ": " + r.getDetails())
                    .collect(Collectors.joining(" | "));
            claim.setDecisionReason("Claim flagged for MANUAL REVIEW: " + reasons);

        } else {
            claim.setStatus(ClaimStatus.APPROVED);
            claim.setDecisionReason("Claim auto-APPROVED. All 10 business validation rules passed clean.");
        }

        claim.setProcessedAt(LocalDateTime.now());
    }
}
