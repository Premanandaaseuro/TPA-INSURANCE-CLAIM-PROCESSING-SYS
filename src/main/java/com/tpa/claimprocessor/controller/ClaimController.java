package com.tpa.claimprocessor.controller;

import com.tpa.claimprocessor.dto.ClaimResponseDto;
import com.tpa.claimprocessor.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*")
public class ClaimController {


    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClaimResponseDto> createClaim(
            @RequestPart("claimForm") MultipartFile claimForm,
            @RequestPart("combinedHospitalDocument") MultipartFile combinedHospitalDocument) {

        ClaimResponseDto createdClaim = claimService.createClaim(claimForm, combinedHospitalDocument);
        return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClaimResponseDto>> getAllClaims() {
        List<ClaimResponseDto> claims = claimService.getAllClaims();
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponseDto> getClaimByClaimId(@PathVariable("claimId") String claimId) {
        ClaimResponseDto claim = claimService.getClaimByClaimId(claimId);
        return ResponseEntity.ok(claim);
    }

    @PostMapping("/clear-test-data")
    public ResponseEntity<java.util.Map<String, String>> clearAllClaimDataPost() {
        claimService.clearAllClaimData();
        return ResponseEntity.ok(java.util.Map.of("message", "All claim test data cleared successfully."));
    }

    @DeleteMapping("/clear-test-data")
    public ResponseEntity<java.util.Map<String, String>> clearAllClaimDataDelete() {
        claimService.clearAllClaimData();
        return ResponseEntity.ok(java.util.Map.of("message", "All claim test data cleared successfully."));
    }

    @GetMapping("/{claimId}/debug")
    public ResponseEntity<java.util.Map<String, Object>> getClaimDebugDetails(@PathVariable("claimId") String claimId) {
        ClaimResponseDto claim = claimService.getClaimByClaimId(claimId);
        java.util.Map<String, Object> debugInfo = new java.util.LinkedHashMap<>();
        debugInfo.put("claimId", claim.getClaimId());
        debugInfo.put("claimNumber", claim.getClaimNumber());
        debugInfo.put("status", claim.getStatus());
        debugInfo.put("decisionReason", claim.getDecisionReason());

        java.util.Map<String, Object> extractedFields = new java.util.LinkedHashMap<>();
        extractedFields.put("patientName", claim.getPatientName());
        extractedFields.put("hospitalName", claim.getHospitalName());
        extractedFields.put("policyNumber", claim.getPolicyNumber());
        extractedFields.put("admissionDate", claim.getAdmissionDate());
        extractedFields.put("dischargeDate", claim.getDischargeDate());
        extractedFields.put("claimedAmount", claim.getClaimedAmount());
        debugInfo.put("extractedFields", extractedFields);

        java.util.Map<String, Object> normalizedFields = new java.util.LinkedHashMap<>();
        normalizedFields.put("patientNameNormalized", claim.getPatientName() != null ? claim.getPatientName().trim().toLowerCase() : null);
        normalizedFields.put("hospitalNameNormalized", claim.getHospitalName() != null ? claim.getHospitalName().trim().toLowerCase() : null);
        debugInfo.put("normalizedFields", normalizedFields);

        debugInfo.put("ruleResults", claim.getRuleResults());
        return ResponseEntity.ok(debugInfo);
    }
}
