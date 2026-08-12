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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClaimIdGeneratorService claimIdGeneratorService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PdfTextExtractorService pdfTextExtractorService;

    @Mock
    private StructuredDataParser structuredDataParser;

    @Mock
    private RuleEngineService ruleEngineService;

    @Mock
    private DecisionEngineService decisionEngineService;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private MockMultipartFile validClaimForm;
    private MockMultipartFile validCombinedDoc;

    @BeforeEach
    void setUp() {
        validClaimForm = new MockMultipartFile("claimForm", "claim_form.pdf", "application/pdf", "Dummy Claim Form Data".getBytes());
        validCombinedDoc = new MockMultipartFile("combinedHospitalDocument", "combined.pdf", "application/pdf", "Dummy Combined Document Data".getBytes());
    }

    @Test
    void createClaim_Success() {
        when(claimIdGeneratorService.generateNextClaimId()).thenReturn("CLM-2026-000001");

        FileStorageService.StoredFileMetaData formMeta = new FileStorageService.StoredFileMetaData(
                "claim_form.pdf", "claim_form.pdf", "target/test-storage/claim_form.pdf", "application/pdf", 100L, "hash1"
        );
        FileStorageService.StoredFileMetaData combinedMeta = new FileStorageService.StoredFileMetaData(
                "combined.pdf", "combined_document.pdf", "target/test-storage/combined_document.pdf", "application/pdf", 200L, "hash2"
        );

        when(fileStorageService.storeFile(eq("CLM-2026-000001"), eq(DocumentType.CLAIM_FORM), any(MultipartFile.class)))
                .thenReturn(formMeta);
        when(fileStorageService.storeFile(eq("CLM-2026-000001"), eq(DocumentType.COMBINED_HOSPITAL_DOCUMENT), any(MultipartFile.class)))
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
            claim.setClaimId("CLM-2026-000001");
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
        assertEquals("CLM-2026-000001", result.getClaimId());
        assertEquals(ClaimStatus.APPROVED, result.getStatus());
        assertEquals(2, result.getDocuments().size());

        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    void createClaim_MissingClaimForm_ThrowsException() {
        InvalidDocumentException exception = assertThrows(InvalidDocumentException.class, () ->
                claimService.createClaim(null, validCombinedDoc)
        );
        assertTrue(exception.getMessage().contains("Claim Form PDF is missing"));
    }

    @Test
    void createClaim_MissingCombinedDoc_ThrowsException() {
        InvalidDocumentException exception = assertThrows(InvalidDocumentException.class, () ->
                claimService.createClaim(validClaimForm, null)
        );
        assertTrue(exception.getMessage().contains("Combined Hospital Document PDF is missing"));
    }
}
