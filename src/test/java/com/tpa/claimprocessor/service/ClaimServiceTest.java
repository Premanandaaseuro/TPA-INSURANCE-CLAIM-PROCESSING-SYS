package com.tpa.claimprocessor.service;

import com.tpa.claimprocessor.decision.DecisionEngineService;
import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.enums.DocumentType;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.domain.repository.PolicyRepository;
import com.tpa.claimprocessor.dto.ClaimResponseDto;
import com.tpa.claimprocessor.exception.InvalidDocumentException;
import com.tpa.claimprocessor.extraction.ExtractedClaimData;
import com.tpa.claimprocessor.extraction.PdfTextExtractorService;
import com.tpa.claimprocessor.extraction.StructuredDataParser;
import com.tpa.claimprocessor.rules.RuleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClaimServiceTest {

    @MockBean
    private ClaimRepository claimRepository;

    @MockBean
    private PolicyRepository policyRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private PdfTextExtractorService pdfTextExtractorService;

    @MockBean
    private StructuredDataParser structuredDataParser;

    @MockBean
    private RuleEngineService ruleEngineService;

    @MockBean
    private DecisionEngineService decisionEngineService;

    @Autowired
    private ClaimServiceImpl claimService;
    
    @Autowired
    private ClaimIdGeneratorService claimIdGeneratorService;

    private MockMultipartFile validClaimForm;
    private MockMultipartFile validCombinedDoc;

    @BeforeEach
    void setUp() {
        validClaimForm = new MockMultipartFile("claimForm", "claim_form.pdf", "application/pdf", "Dummy Claim Form Data".getBytes());
        validCombinedDoc = new MockMultipartFile("combinedHospitalDocument", "combined.pdf", "application/pdf", "Dummy Combined Document Data".getBytes());
    }

    @Test
    void createClaim_Success() {
        String nextClaimId = claimIdGeneratorService.generateNextClaimId();

        FileStorageService.StoredFileMetaData formMeta = new FileStorageService.StoredFileMetaData(
                "claim_form.pdf", "claim_form.pdf", "target/test-storage/claim_form.pdf", "application/pdf", 100L, "hash1"
        );
        FileStorageService.StoredFileMetaData combinedMeta = new FileStorageService.StoredFileMetaData(
                "combined.pdf", "combined_document.pdf", "target/test-storage/combined_document.pdf", "application/pdf", 200L, "hash2"
        );

        when(fileStorageService.storeFile(eq(nextClaimId), eq(DocumentType.CLAIM_FORM), any(MultipartFile.class)))
                .thenReturn(formMeta);
        when(fileStorageService.storeFile(eq(nextClaimId), eq(DocumentType.COMBINED_HOSPITAL_DOCUMENT), any(MultipartFile.class)))
                .thenReturn(combinedMeta);

        when(pdfTextExtractorService.extractText(any(File.class))).thenReturn("Sample Raw Text");
        when(structuredDataParser.parse(anyString(), anyString())).thenReturn(new ExtractedClaimData());
        when(ruleEngineService.evaluateAllRules(any(), any(), any())).thenReturn(Collections.emptyList());

        doAnswer(invocation -> {
            Claim c = invocation.getArgument(0);
            c.setStatus(ClaimStatus.APPROVED);
            return null;
        }).when(decisionEngineService).applyDecision(any(), any());

        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim claim = invocation.getArgument(0);
            claim.setId(1L);
            return claim;
        });

        when(claimRepository.findByClaimIdWithDetails(anyString())).thenAnswer(invocation -> {
            Claim claim = new Claim();
            claim.setId(1L);
            claim.setClaimId(nextClaimId);
            claim.setStatus(ClaimStatus.APPROVED);
            com.tpa.claimprocessor.domain.entity.ClaimDocument doc1 = new com.tpa.claimprocessor.domain.entity.ClaimDocument();
            doc1.setDocumentType(DocumentType.CLAIM_FORM);
            com.tpa.claimprocessor.domain.entity.ClaimDocument doc2 = new com.tpa.claimprocessor.domain.entity.ClaimDocument();
            doc2.setDocumentType(DocumentType.COMBINED_HOSPITAL_DOCUMENT);
            claim.setDocuments(java.util.List.of(doc1, doc2));
            return java.util.Optional.of(claim);
        });

        ClaimResponseDto result = claimService.createClaim(validClaimForm, validCombinedDoc);

        assertNotNull(result);
        assertEquals(nextClaimId, result.getClaimId());
        assertEquals(ClaimStatus.APPROVED, result.getStatus());
        assertEquals(2, result.getDocuments().size());

        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    void createClaim_MissingClaimForm_TriggersR01Rejected() {
        String nextClaimId = claimIdGeneratorService.generateNextClaimId();
        FileStorageService.StoredFileMetaData combinedMeta = new FileStorageService.StoredFileMetaData(
                "combined.pdf", "combined_document.pdf", "target/test-storage/combined_document.pdf", "application/pdf", 200L, "hash2"
        );
        when(fileStorageService.storeFile(eq(nextClaimId), eq(DocumentType.COMBINED_HOSPITAL_DOCUMENT), any(MultipartFile.class)))
                .thenReturn(combinedMeta);
        when(pdfTextExtractorService.extractText(any(File.class))).thenReturn("Sample Raw Text");
        when(structuredDataParser.parse(anyString(), anyString())).thenReturn(new ExtractedClaimData());

        com.tpa.claimprocessor.rules.RuleEvaluationResult r01Fail = com.tpa.claimprocessor.rules.RuleEvaluationResult.fail(
                "R01", "Claim Form Missing Check", com.tpa.claimprocessor.domain.enums.RuleSeverity.REJECTED, "R01 – Claim Form is missing."
        );
        when(ruleEngineService.evaluateAllRules(any(), any(), any())).thenReturn(java.util.List.of(r01Fail));

        doAnswer(invocation -> {
            Claim c = invocation.getArgument(0);
            c.setStatus(ClaimStatus.REJECTED);
            c.setDecisionReason("R01 – Claim Form is missing.");
            return null;
        }).when(decisionEngineService).applyDecision(any(), any());

        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(claimRepository.findByClaimIdWithDetails(anyString())).thenAnswer(invocation -> {
            Claim c = new Claim();
            c.setClaimId(nextClaimId);
            c.setStatus(ClaimStatus.REJECTED);
            c.setDecisionReason("R01 – Claim Form is missing.");
            c.setDocuments(java.util.List.of());
            return java.util.Optional.of(c);
        });

        ClaimResponseDto result = claimService.createClaim(null, validCombinedDoc);
        assertNotNull(result);
        assertEquals(ClaimStatus.REJECTED, result.getStatus());
    }

    @Test
    void createClaim_MissingCombinedDoc_TriggersR02Rejected() {
        String nextClaimId = claimIdGeneratorService.generateNextClaimId();
        FileStorageService.StoredFileMetaData formMeta = new FileStorageService.StoredFileMetaData(
                "claim_form.pdf", "claim_form.pdf", "target/test-storage/claim_form.pdf", "application/pdf", 100L, "hash1"
        );
        when(fileStorageService.storeFile(eq(nextClaimId), eq(DocumentType.CLAIM_FORM), any(MultipartFile.class)))
                .thenReturn(formMeta);
        when(pdfTextExtractorService.extractText(any(File.class))).thenReturn("Sample Raw Text");
        when(structuredDataParser.parse(anyString(), anyString())).thenReturn(new ExtractedClaimData());

        com.tpa.claimprocessor.rules.RuleEvaluationResult r02Fail = com.tpa.claimprocessor.rules.RuleEvaluationResult.fail(
                "R02", "Combined Hospital Document Missing Check", com.tpa.claimprocessor.domain.enums.RuleSeverity.REJECTED, "R02 – Combined Hospital Document is missing."
        );
        when(ruleEngineService.evaluateAllRules(any(), any(), any())).thenReturn(java.util.List.of(r02Fail));

        doAnswer(invocation -> {
            Claim c = invocation.getArgument(0);
            c.setStatus(ClaimStatus.REJECTED);
            c.setDecisionReason("R02 – Combined Hospital Document is missing.");
            return null;
        }).when(decisionEngineService).applyDecision(any(), any());

        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(claimRepository.findByClaimIdWithDetails(anyString())).thenAnswer(invocation -> {
            Claim c = new Claim();
            c.setClaimId(nextClaimId);
            c.setStatus(ClaimStatus.REJECTED);
            c.setDecisionReason("R02 – Combined Hospital Document is missing.");
            c.setDocuments(java.util.List.of());
            return java.util.Optional.of(c);
        });

        ClaimResponseDto result = claimService.createClaim(validClaimForm, null);
        assertNotNull(result);
        assertEquals(ClaimStatus.REJECTED, result.getStatus());
    }

    @Test
    void createClaim_BothMissing_ThrowsException() {
        assertThrows(InvalidDocumentException.class, () -> claimService.createClaim(null, null));
    }
}
