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
}
