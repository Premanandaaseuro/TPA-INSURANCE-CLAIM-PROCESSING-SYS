package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class R07_DateMismatchRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R07";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        LocalDate admissionDate = extractedData.getAdmissionDate() != null ? extractedData.getAdmissionDate() : claim.getAdmissionDate();
        LocalDate dischargeDate = extractedData.getDischargeDate() != null ? extractedData.getDischargeDate() : claim.getDischargeDate();

        if (admissionDate == null || dischargeDate == null) {
            return RuleEvaluationResult.fail("R07", "Admission/Discharge Date Check", RuleSeverity.NEEDS_MANUAL_REVIEW, "Admission or Discharge date is missing.");
        }

        if (admissionDate.isAfter(dischargeDate)) {
            return RuleEvaluationResult.fail("R07", "Admission/Discharge Date Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Admission date (" + admissionDate + ") cannot be after discharge date (" + dischargeDate + ").");
        }

        return RuleEvaluationResult.pass("R07", "Admission/Discharge Date Check", "Admission and discharge dates match and are logical.");
    }
}
