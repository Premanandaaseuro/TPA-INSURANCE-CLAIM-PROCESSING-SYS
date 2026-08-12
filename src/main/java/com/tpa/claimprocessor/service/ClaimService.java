package com.tpa.claimprocessor.service;

import com.tpa.claimprocessor.dto.ClaimResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClaimService {

    ClaimResponseDto createClaim(MultipartFile claimForm, MultipartFile combinedHospitalDocument);

    List<ClaimResponseDto> getAllClaims();

    ClaimResponseDto getClaimByClaimId(String claimId);

    void clearAllClaimData();
}
