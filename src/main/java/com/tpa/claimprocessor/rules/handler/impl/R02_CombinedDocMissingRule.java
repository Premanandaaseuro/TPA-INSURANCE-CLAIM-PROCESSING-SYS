package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.DocumentType;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

@Component
public class R02_CombinedDocMissingRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R02";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        boolean hasCombinedDoc = claim.getDocuments() != null && claim.getDocuments().stream()
                .anyMatch(doc -> doc.getDocumentType() == DocumentType.COMBINED_HOSPITAL_DOCUMENT && doc.getFileSize() != null && doc.getFileSize() > 0);

        if (hasCombinedDoc) {
            return RuleEvaluationResult.pass("R02", "Combined Hospital Document Missing Check", "Combined Hospital Document attached and valid.");
        } else {
            return RuleEvaluationResult.fail("R02", "Combined Hospital Document Missing Check", RuleSeverity.REJECTED, "Combined Discharge Summary and Hospital Bill PDF is missing or empty.");
        }
    }
}
