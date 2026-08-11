package com.tpa.claimprocessor.rules.handler.impl;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.RuleSeverity;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import com.tpa.claimprocessor.rules.RuleHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class R10_PossibleDuplicateClaimRule implements RuleHandler {

    private final ClaimRepository claimRepository;

    public R10_PossibleDuplicateClaimRule(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public String getRuleCode() {
        return "R10";
    }

    @Override
    public RuleEvaluationResult evaluate(Claim claim, ExtractedClaimData extractedData, Policy policy) {
        String policyNo = extractedData.getPolicyNumber() != null ? extractedData.getPolicyNumber() : claim.getPolicyNumber();
        String patientName = extractedData.getPatientName() != null ? extractedData.getPatientName() : claim.getPatientName();
        String hospitalName = extractedData.getHospitalName() != null ? extractedData.getHospitalName() : claim.getHospitalName();
        LocalDate admissionDate = extractedData.getAdmissionDate() != null ? extractedData.getAdmissionDate() : claim.getAdmissionDate();

        if (patientName != null && admissionDate != null) {
            List<Claim> existingClaims = claimRepository.findAll();
            boolean duplicateExists = existingClaims.stream().anyMatch(existing -> {
                if (existing.getId() != null && claim.getId() != null && existing.getId().equals(claim.getId())) {
                    return false;
                }
                if (existing.getClaimId() != null && claim.getClaimId() != null && existing.getClaimId().equals(claim.getClaimId())) {
                    return false;
                }

                boolean patientMatch = existing.getPatientName() != null && existing.getPatientName().equalsIgnoreCase(patientName);
                boolean admissionMatch = existing.getAdmissionDate() != null && existing.getAdmissionDate().equals(admissionDate);

                boolean policyMatch = true;
                if (policyNo != null && existing.getPolicyNumber() != null) {
                    policyMatch = existing.getPolicyNumber().equalsIgnoreCase(policyNo);
                }

                boolean hospitalMatch = true;
                if (hospitalName != null && existing.getHospitalName() != null) {
                    hospitalMatch = existing.getHospitalName().equalsIgnoreCase(hospitalName);
                }

                return patientMatch && admissionMatch && policyMatch && hospitalMatch;
            });

            if (duplicateExists) {
                return RuleEvaluationResult.fail("R10", "Possible Duplicate Claim Check", RuleSeverity.NEEDS_MANUAL_REVIEW,
                        "Possible duplicate claim found for Patient " + patientName + ", Admission Date " + admissionDate);
            }
        }

        return RuleEvaluationResult.pass("R10", "Possible Duplicate Claim Check", "No duplicate claim detected.");
    }
}
