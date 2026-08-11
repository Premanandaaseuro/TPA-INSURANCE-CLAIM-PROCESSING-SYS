package com.tpa.claimprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tpa.claimprocessor.decision.DecisionEngineService;
import com.tpa.claimprocessor.domain.entity.*;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.enums.ClaimType;
import com.tpa.claimprocessor.domain.enums.DocumentType;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.domain.repository.PolicyRepository;
import com.tpa.claimprocessor.dto.ClaimDocumentDto;
import com.tpa.claimprocessor.dto.ClaimResponseDto;
import com.tpa.claimprocessor.dto.ClaimRuleResultDto;
import com.tpa.claimprocessor.exception.ClaimNotFoundException;
import com.tpa.claimprocessor.exception.InvalidDocumentException;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.extraction.PdfTextExtractorService;
import com.tpa.claimprocessor.extraction.StructuredDataParser;
import com.tpa.claimprocessor.rules.RuleEngineService;
import com.tpa.claimprocessor.rules.RuleEvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClaimServiceImpl implements ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimServiceImpl.class);

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final ClaimIdGeneratorService claimIdGeneratorService;
    private final FileStorageService fileStorageService;
    private final PdfTextExtractorService pdfTextExtractorService;
    private final StructuredDataParser structuredDataParser;
    private final RuleEngineService ruleEngineService;
    private final DecisionEngineService decisionEngineService;
    private final ObjectMapper objectMapper;

    public ClaimServiceImpl(ClaimRepository claimRepository,
                            PolicyRepository policyRepository,
                            ClaimIdGeneratorService claimIdGeneratorService,
                            FileStorageService fileStorageService,
                            PdfTextExtractorService pdfTextExtractorService,
                            StructuredDataParser structuredDataParser,
                            RuleEngineService ruleEngineService,
                            DecisionEngineService decisionEngineService) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.claimIdGeneratorService = claimIdGeneratorService;
        this.fileStorageService = fileStorageService;
        this.pdfTextExtractorService = pdfTextExtractorService;
        this.structuredDataParser = structuredDataParser;
        this.ruleEngineService = ruleEngineService;
        this.decisionEngineService = decisionEngineService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    @Transactional
    public ClaimResponseDto createClaim(MultipartFile claimForm, MultipartFile combinedHospitalDocument) {
        // 1. Validate presence of both required documents
        if (claimForm == null || claimForm.isEmpty()) {
            throw new InvalidDocumentException("Claim Form PDF is missing or empty (R01 violation)");
        }
        if (combinedHospitalDocument == null || combinedHospitalDocument.isEmpty()) {
            throw new InvalidDocumentException("Combined Hospital Document PDF is missing or empty (R02 violation)");
        }

        // 2. Generate unique Claim ID (CLM-YYYY-XXXXXX)
        String claimId = claimIdGeneratorService.generateNextClaimId();

        // 3. Create Claim aggregate
        Claim claim = new Claim(claimId);
        claim.setStatus(ClaimStatus.PENDING);
        claim.setClaimType(ClaimType.REIMBURSEMENT);

        // 4. Store Files in storage/claims/{claimId}/
        FileStorageService.StoredFileMetaData formMeta = fileStorageService.storeFile(
                claimId, DocumentType.CLAIM_FORM, claimForm
        );
        ClaimDocument formDoc = new ClaimDocument(
                claim,
                DocumentType.CLAIM_FORM,
                formMeta.originalFilename(),
                formMeta.storedFilename(),
                formMeta.filePath(),
                formMeta.contentType(),
                formMeta.fileSize(),
                formMeta.checksumSha256()
        );
        claim.addDocument(formDoc);

        FileStorageService.StoredFileMetaData combinedMeta = fileStorageService.storeFile(
                claimId, DocumentType.COMBINED_HOSPITAL_DOCUMENT, combinedHospitalDocument
        );
        ClaimDocument combinedDoc = new ClaimDocument(
                claim,
                DocumentType.COMBINED_HOSPITAL_DOCUMENT,
                combinedMeta.originalFilename(),
                combinedMeta.storedFilename(),
                combinedMeta.filePath(),
                combinedMeta.contentType(),
                combinedMeta.fileSize(),
                combinedMeta.checksumSha256()
        );
        claim.addDocument(combinedDoc);

        // 5. Extract Text using PDFBox / Tess4J OCR
        String formRawText = pdfTextExtractorService.extractText(new File(formMeta.filePath()));
        String combinedRawText = pdfTextExtractorService.extractText(new File(combinedMeta.filePath()));

        // 6. Structured Data Extraction
        ExtractedClaimData extractedData = structuredDataParser.parse(formRawText, combinedRawText);

        // 7. Update Claim Entity with Structured Data
        claim.setPolicyNumber(extractedData.getPolicyNumber());
        claim.setPolicyId(extractedData.getPolicyId());
        claim.setCustomerName(extractedData.getCustomerName());
        claim.setCarrierName(extractedData.getCarrierName());
        claim.setPolicyName(extractedData.getPolicyName());
        claim.setPatientName(extractedData.getPatientName());
        claim.setHospitalName(extractedData.getHospitalName());
        claim.setAdmissionDate(extractedData.getAdmissionDate());
        claim.setDischargeDate(extractedData.getDischargeDate());
        claim.setClaimedAmount(extractedData.getClaimedAmount());
        if (extractedData.getClaimType() != null) {
            try {
                claim.setClaimType(com.tpa.claimprocessor.domain.enums.ClaimType.valueOf(extractedData.getClaimType().toUpperCase()));
            } catch (Exception ignored) {
            }
        }


        // 8. Populate DischargeDetails Entity
        if (extractedData.getAdmissionDate() != null || extractedData.getDischargeDate() != null || extractedData.getPrimaryDiagnosis() != null) {
            DischargeDetails discharge = new DischargeDetails();
            discharge.setPatientName(extractedData.getPatientName());
            discharge.setHospitalName(extractedData.getHospitalName());
            discharge.setAdmissionDate(extractedData.getAdmissionDate());
            discharge.setDischargeDate(extractedData.getDischargeDate());
            discharge.setPrimaryDiagnosis(extractedData.getPrimaryDiagnosis());
            discharge.setTreatingDoctor(extractedData.getTreatingDoctor());
            claim.setDischargeDetails(discharge);
        }

        // 9. Populate HospitalBillDetails Entity
        if (extractedData.getTotalBillAmount() != null || extractedData.getBillNumber() != null) {
            HospitalBillDetails bill = new HospitalBillDetails();
            bill.setBillNumber(extractedData.getBillNumber());
            bill.setBillDate(extractedData.getBillDate());
            bill.setHospitalName(extractedData.getHospitalName());
            bill.setPatientName(extractedData.getPatientName());
            bill.setTotalAmount(extractedData.getTotalBillAmount());
            bill.setRoomCharges(extractedData.getRoomCharges());
            bill.setPharmacyCharges(extractedData.getPharmacyCharges());
            claim.setHospitalBillDetails(bill);
        }

        // 10. Store JSON Extracted Payload
        try {
            String jsonPayload = objectMapper.writeValueAsString(extractedData);
            ClaimJson claimJson = new ClaimJson(claim, jsonPayload);
            claim.setClaimJson(claimJson);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize extracted data to JSON for claim {}", claimId, e);
        }

        // 11. Query Policy Database
        Policy policy = null;
        if (extractedData.getPolicyNumber() != null) {
            Optional<Policy> policyOpt = policyRepository.findByPolicyNumber(extractedData.getPolicyNumber());
            if (policyOpt.isPresent()) {
                policy = policyOpt.get();
                claim.setPolicyId(String.valueOf(policy.getId()));
                if (claim.getCustomerName() == null) claim.setCustomerName(policy.getCustomerName());
                if (claim.getCarrierName() == null) claim.setCarrierName(policy.getCarrierName());
                if (claim.getPolicyName() == null) claim.setPolicyName(policy.getPolicyName());
            }
        }

        // 12. Evaluate Rule Engine (R01 to R10)
        List<RuleEvaluationResult> ruleResults = ruleEngineService.evaluateAllRules(claim, extractedData, policy);

        // 13. Apply Decision Engine Matrix (REJECTED > NEEDS_MANUAL_REVIEW > APPROVED)
        decisionEngineService.applyDecision(claim, ruleResults);

        // 14. Save Finalized Claim to PostgreSQL Database
        claimRepository.save(claim);

        // 15. Re-fetch with all associations eagerly loaded for the response DTO
        Claim savedClaim = claimRepository.findByClaimIdWithDetails(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found after save: " + claimId));

        return mapToResponseDto(savedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponseDto> getAllClaims() {
        return claimRepository.findAllWithDetails().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponseDto getClaimByClaimId(String claimId) {
        Claim claim = claimRepository.findByClaimIdWithDetails(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));
        return mapToResponseDto(claim);
    }

    private ClaimResponseDto mapToResponseDto(Claim claim) {
        ClaimResponseDto dto = new ClaimResponseDto();
        dto.setId(claim.getId());
        dto.setClaimId(claim.getClaimId());
        dto.setPolicyNumber(claim.getPolicyNumber());
        dto.setPolicyId(claim.getPolicyId());
        dto.setCustomerName(claim.getCustomerName());
        dto.setCarrierName(claim.getCarrierName());
        dto.setPolicyName(claim.getPolicyName());
        dto.setPatientName(claim.getPatientName());
        dto.setHospitalName(claim.getHospitalName());
        dto.setAdmissionDate(claim.getAdmissionDate());
        dto.setDischargeDate(claim.getDischargeDate());
        dto.setClaimedAmount(claim.getClaimedAmount());
        dto.setClaimType(claim.getClaimType());
        dto.setStatus(claim.getStatus());
        dto.setDecisionReason(claim.getDecisionReason());
        dto.setCreatedAt(claim.getCreatedAt());
        dto.setProcessedAt(claim.getProcessedAt());

        if (claim.getDocuments() != null) {
            List<ClaimDocumentDto> docDtos = claim.getDocuments().stream()
                    .map(doc -> new ClaimDocumentDto(
                            doc.getId(),
                            doc.getDocumentType(),
                            doc.getOriginalFilename(),
                            doc.getStoredFilename(),
                            doc.getFilePath(),
                            doc.getContentType(),
                            doc.getFileSize(),
                            doc.getChecksumSha256(),
                            doc.getUploadedAt()
                    ))
                    .collect(Collectors.toList());
            dto.setDocuments(docDtos);
        }

        if (claim.getRuleResults() != null) {
            List<ClaimRuleResultDto> ruleDtos = claim.getRuleResults().stream()
                    .map(r -> new ClaimRuleResultDto(
                            r.getId(),
                            r.getRuleCode(),
                            r.getRuleName(),
                            r.isPassed(),
                            r.getSeverity(),
                            r.getDetails(),
                            r.getEvaluatedAt()
                    ))
                    .sorted(java.util.Comparator.comparing(ClaimRuleResultDto::getRuleCode))
                    .collect(Collectors.toList());
            dto.setRuleResults(ruleDtos);
        }

        return dto;
    }
}
