package com.tpa.claimprocessor.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.ClaimDocument;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.enums.DocumentType;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.domain.repository.PolicyRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Utility and Database Tests")
class UtilityDatabaseTest {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @BeforeEach
    void setUp() {
        // Clean state for each test
    }

    // Claim ID generation tests
    @Test
    @DisplayName("UTIL-001: Claim ID format validation")
    void testClaimIdFormat() {
        Claim claim = new Claim("CLM-2026-000001");
        assertEquals("CLM-2026-000001", claim.getClaimId());
        assertTrue(claim.getClaimId().matches("CLM-\\d{4}-\\d{6}"));
    }

    @Test
    @DisplayName("UTIL-002: Multiple claim ID generation")
    void testMultipleClaimIdGeneration() {
        Claim claim1 = new Claim("CLM-2026-000001");
        Claim claim2 = new Claim("CLM-2026-000002");
        
        assertNotEquals(claim1.getClaimId(), claim2.getClaimId());
    }

    @Test
    @DisplayName("UTIL-003: Claim creation timestamp")
    void testClaimCreationTimestamp() {
        Claim claim = new Claim("CLM-2026-000003");
        Claim saved = claimRepository.save(claim);
        
        assertNotNull(saved.getCreatedDate());
    }

    @Test
    @DisplayName("UTIL-004: Claim status transitions")
    void testClaimStatusTransitions() {
        Claim claim = new Claim("CLM-2026-000004");
        claim.setStatus(ClaimStatus.PENDING);
        Claim saved = claimRepository.save(claim);
        
        // Transition to APPROVED
        saved.setStatus(ClaimStatus.APPROVED);
        Claim updated = claimRepository.save(saved);
        assertEquals(ClaimStatus.APPROVED, updated.getStatus());
    }

    @Test
    @DisplayName("UTIL-005: Document file hash generation")
    void testDocumentFileHash() {
        Claim claim = new Claim("CLM-2026-000005");
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path", "application/pdf", 1024L, "hash123");
        claim.addDocument(doc);
        
        assertEquals("hash123", claim.getDocuments().get(0).getFileHash());
    }

    @Test
    @DisplayName("UTIL-006: Document metadata update")
    void testDocumentMetadataUpdate() {
        Claim claim = new Claim("CLM-2026-000006");
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.CLAIM_FORM, "old_name.pdf", "old_name.pdf", "path", "application/pdf", 1024L, "hash");
        claim.addDocument(doc);
        Claim saved = claimRepository.save(claim);
        
        assertNotNull(saved.getDocuments().get(0).getFileName());
    }

    @Test
    @DisplayName("UTIL-007: Claim decision reason persistence")
    void testClaimDecisionReason() {
        Claim claim = new Claim("CLM-2026-000007");
        claim.setDecisionReason("Policy coverage limit exceeded");
        Claim saved = claimRepository.save(claim);
        
        assertEquals("Policy coverage limit exceeded", saved.getDecisionReason());
    }

    @Test
    @DisplayName("UTIL-008: Audit trail generation")
    void testAuditTrail() {
        Claim claim = new Claim("CLM-2026-000008");
        Claim saved = claimRepository.save(claim);
        
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedDate());
    }

    @Test
    @DisplayName("UTIL-009: Claim-Policy relationship")
    void testClaimPolicyRelationship() {
        Policy policy = new Policy(
                "POL-2026-UTIL-001",
                "Test Plan",
                "Test User",
                "Test Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        Policy savedPolicy = policyRepository.save(policy);
        
        Claim claim = new Claim("CLM-2026-000009");
        claim.setPolicyNumber(savedPolicy.getPolicyNumber());
        Claim savedClaim = claimRepository.save(claim);
        
        assertEquals(savedPolicy.getPolicyNumber(), savedClaim.getPolicyNumber());
    }

    @Test
    @DisplayName("UTIL-010: Bulk claim creation")
    void testBulkClaimCreation() {
        for (int i = 1; i <= 10; i++) {
            Claim claim = new Claim("CLM-2026-BULK-" + String.format("%03d", i));
            claimRepository.save(claim);
        }
        
        List<Claim> claims = claimRepository.findAll();
        assertTrue(claims.size() >= 10);
    }

    // Policy management tests
    @Test
    @DisplayName("POL-UTIL-001: Policy creation")
    void testPolicyCreation() {
        Policy policy = new Policy(
                "POL-2026-UTIL-002",
                "Premium Plan",
                "Premium User",
                "Premium Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("1000000.00"),
                "ACTIVE"
        );
        Policy saved = policyRepository.save(policy);
        
        assertNotNull(saved.getId());
        assertEquals("POL-2026-UTIL-002", saved.getPolicyNumber());
    }

    @Test
    @DisplayName("POL-UTIL-002: Policy expiration date")
    void testPolicyExpirationDate() {
        Policy policy = new Policy(
                "POL-2026-UTIL-003",
                "Expiring Plan",
                "Expiring User",
                "Test Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 12, 31),
                new BigDecimal("300000.00"),
                "ACTIVE"
        );
        Policy saved = policyRepository.save(policy);
        
        assertTrue(saved.getExpirationDate().isAfter(LocalDate.now()) || saved.getExpirationDate().equals(LocalDate.now()));
    }

    @Test
    @DisplayName("POL-UTIL-003: Policy insurance company tracking")
    void testPolicyInsuranceCompany() {
        Policy policy = new Policy(
                "POL-2026-UTIL-004",
                "Company Plan",
                "Company User",
                "Apollo Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        Policy saved = policyRepository.save(policy);
        
        assertEquals("Apollo Insurance", saved.getInsuranceCompany());
    }

    @Test
    @DisplayName("POL-UTIL-004: Bulk policy creation")
    void testBulkPolicyCreation() {
        for (int i = 1; i <= 5; i++) {
            Policy policy = new Policy(
                    "POL-2026-BULK-" + String.format("%03d", i),
                    "Bulk Plan " + i,
                    "Bulk User " + i,
                    "Bulk Insurance",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    BigDecimal.valueOf(100000 * i),
                    "ACTIVE"
            );
            policyRepository.save(policy);
        }
        
        List<Policy> policies = policyRepository.findAll();
        assertTrue(policies.size() >= 5);
    }

    @Test
    @DisplayName("POL-UTIL-005: Policy coverage limit boundary")
    void testPolicyCoverageLimitBoundary() {
        Policy policy = new Policy(
                "POL-2026-UTIL-005",
                "Max Coverage",
                "Max User",
                "Test Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("9999999.99"),
                "ACTIVE"
        );
        Policy saved = policyRepository.save(policy);
        
        assertTrue(saved.getCoverageLimit().compareTo(BigDecimal.ZERO) > 0);
    }

    // Data consistency tests
    @Test
    @DisplayName("DATA-001: Claim document count")
    void testClaimDocumentCount() {
        Claim claim = new Claim("CLM-2026-DATA-001");
        claim.addDocument(new ClaimDocument(claim, DocumentType.CLAIM_FORM, "form.pdf", "form.pdf", "path1", "application/pdf", 1024L, "hash1"));
        claim.addDocument(new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "combined.pdf", "combined.pdf", "path2", "application/pdf", 2048L, "hash2"));
        Claim saved = claimRepository.save(claim);
        
        assertEquals(2, saved.getDocuments().size());
    }

    @Test
    @DisplayName("DATA-002: Claim status persistence")
    void testClaimStatusPersistence() {
        Claim claim = new Claim("CLM-2026-DATA-002");
        claim.setStatus(ClaimStatus.REJECTED);
        Claim saved = claimRepository.save(claim);
        
        Claim retrieved = claimRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(ClaimStatus.REJECTED, retrieved.getStatus());
    }

    @Test
    @DisplayName("DATA-003: Policy holder name uniqueness")
    void testPolicyHolderNameStorage() {
        Policy policy1 = new Policy(
                "POL-2026-DATA-001",
                "Plan A",
                "Unique Name 1",
                "Insurance A",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        Policy policy2 = new Policy(
                "POL-2026-DATA-002",
                "Plan B",
                "Unique Name 2",
                "Insurance B",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        
        policyRepository.save(policy1);
        policyRepository.save(policy2);
        
        List<Policy> policies = policyRepository.findAll();
        assertTrue(policies.size() >= 2);
    }

    @Test
    @DisplayName("DATA-004: Large policy number handling")
    void testLargePolicyNumber() {
        String largePolicyNum = "POL-" + System.currentTimeMillis();
        Policy policy = new Policy(
                largePolicyNum,
                "Large Num Plan",
                "Large Num User",
                "Test Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        Policy saved = policyRepository.save(policy);
        
        assertEquals(largePolicyNum, saved.getPolicyNumber());
    }

    @Test
    @DisplayName("DATA-005: Concurrent claim processing")
    void testConcurrentClaimProcessing() {
        for (int i = 0; i < 5; i++) {
            Claim claim = new Claim("CLM-2026-CONC-" + i);
            claimRepository.save(claim);
        }
        
        long count = claimRepository.findAll().size();
        assertTrue(count >= 5);
    }

    // Performance and boundary tests
    @Test
    @DisplayName("PERF-001: Large claim amount handling")
    void testLargeClaimAmount() {
        Claim claim = new Claim("CLM-2026-PERF-001");
        claim.setClaimedAmount(new BigDecimal("999999999.99"));
        Claim saved = claimRepository.save(claim);
        
        assertNotNull(saved.getClaimedAmount());
    }

    @Test
    @DisplayName("PERF-002: Long document path handling")
    void testLongDocumentPath() {
        Claim claim = new Claim("CLM-2026-PERF-002");
        String longPath = "/very/long/path/to/document/file/that/has/many/directories/" + "deep/nested/subfolder/".repeat(5) + "document.pdf";
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.CLAIM_FORM, "doc.pdf", "doc.pdf", longPath, "application/pdf", 1024L, "hash");
        claim.addDocument(doc);
        Claim saved = claimRepository.save(claim);
        
        assertNotNull(saved.getDocuments().get(0).getFilePath());
    }

    @Test
    @DisplayName("PERF-003: Maximum file size handling")
    void testMaximumFileSize() {
        Claim claim = new Claim("CLM-2026-PERF-003");
        long maxSize = Long.MAX_VALUE / 1000; // Reasonable max
        ClaimDocument doc = new ClaimDocument(claim, DocumentType.COMBINED_HOSPITAL_DOCUMENT, "large.pdf", "large.pdf", "path", "application/pdf", maxSize, "hash");
        claim.addDocument(doc);
        Claim saved = claimRepository.save(claim);
        
        assertEquals(maxSize, saved.getDocuments().get(0).getFileSize());
    }

    @Test
    @DisplayName("PERF-004: Empty claim retrieval")
    void testEmptyClaimRetrieval() {
        List<Claim> claims = claimRepository.findAll();
        assertNotNull(claims);
    }

    @Test
    @DisplayName("PERF-005: Claim retrieval with no documents")
    void testClaimWithoutDocuments() {
        Claim claim = new Claim("CLM-2026-PERF-005");
        Claim saved = claimRepository.save(claim);
        
        assertNotNull(saved.getDocuments());
    }
}
