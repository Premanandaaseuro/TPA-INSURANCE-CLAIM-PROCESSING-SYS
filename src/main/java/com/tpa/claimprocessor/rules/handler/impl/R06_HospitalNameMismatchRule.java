package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

@Component
public class R06_HospitalNameMismatchRule implements RuleHandler {

    @Override
    public String getRuleCode() {
        return "R06";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        String hospitalName = extractedData.getHospitalName() != null ? extractedData.getHospitalName() : claim.getHospitalName();

        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            return RuleEvaluationResult.fail("R06", "Hospital Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW, "Hospital Name is missing from documents.");
        }

        String cfHospital = extractedData.getClaimFormHospitalName();
        String dsHospital = extractedData.getDischargeSummaryHospitalName();
        String hbHospital = extractedData.getHospitalBillHospitalName();

        if (cfHospital != null && dsHospital != null && !isNormalizedMatch(cfHospital, dsHospital)) {
            return RuleEvaluationResult.fail("R06", "Hospital Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Hospital Name mismatch between Claim Form ('" + cfHospital + "') and Discharge Summary ('" + dsHospital + "').");
        }

        if (cfHospital != null && hbHospital != null && !isNormalizedMatch(cfHospital, hbHospital)) {
            return RuleEvaluationResult.fail("R06", "Hospital Name Mismatch Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                    "Hospital Name mismatch between Claim Form ('" + cfHospital + "') and Hospital Bill ('" + hbHospital + "').");
        }

        return RuleEvaluationResult.pass("R06", "Hospital Name Mismatch Check", "Hospital name matches across claim form, discharge summary and hospital bill.");
    }

    private boolean isNormalizedMatch(String s1, String s2) {
        if (s1 == null || s2 == null) return true;
        String n1 = s1.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String n2 = s2.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        if (n1.isEmpty() || n2.isEmpty()) return true;
        return n1.equals(n2) || n1.contains(n2) || n2.contains(n1);
    }
}
