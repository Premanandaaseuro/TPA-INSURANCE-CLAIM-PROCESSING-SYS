package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimDocument;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.DocumentType;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

@Component
public class R01_ClaimFormMissingRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R01";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        boolean hasClaimForm = claim.getDocuments() != null && claim.getDocuments().stream()
                .anyMatch(doc -> doc.getDocumentType() == DocumentType.CLAIM_FORM && doc.getFileSize() != null && doc.getFileSize() > 0);

        if (hasClaimForm) {
            return RuleEvaluationResult.pass("R01", "Claim Form Missing Check", "Claim Form attached and valid.");
        } else {
            return RuleEvaluationResult.fail("R01", "Claim Form Missing Check", RuleSeverity.REJECTED, "Claim Form PDF is missing or empty.");
        }
    }
}
