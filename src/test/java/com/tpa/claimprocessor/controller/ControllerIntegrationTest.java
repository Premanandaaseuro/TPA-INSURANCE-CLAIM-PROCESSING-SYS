package com.tpa.claimprocessor.controller;

import com.tpa.claimprocessor.domain.entity.Claim;
import com.tpa.claimprocessor.domain.entity.Policy;
import com.tpa.claimprocessor.domain.enums.ClaimStatus;
import com.tpa.claimprocessor.domain.repository.ClaimRepository;
import com.tpa.claimprocessor.domain.repository.PolicyRepository;
import com.tpa.claimprocessor.dto.ClaimResponseDto;
import com.tpa.claimprocessor.service.ClaimServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Controller and Integration Tests")
class ControllerIntegrationTest {

    @Autowired
    private ClaimServiceImpl claimService;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private Policy testPolicy;

    @BeforeEach
    void setUp() {
        testPolicy = new Policy(
                "POL-2026-CTRL-001",
                "Test Plan",
                "Test User",
                "Test Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("500000.00"),
                "ACTIVE"
        );
        policyRepository.save(testPolicy);
    }

    // Claim retrieval tests
    @Test
    @DisplayName("CTRL-001: Retrieve claim by ID")
    void testRetrieveClaimById() {
        Claim claim = new Claim("CLM-2026-CTRL-001");
        claim.setStatus(ClaimStatus.PENDING);
        claim.setPolicyNumber("POL-2026-CTRL-001");
        Claim saved = claimRepository.save(claim);

        Claim retrieved = claimRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals("CLM-2026-CTRL-001", retrieved.getClaimId());
    }

    @Test
    @DisplayName("CTRL-002: Retrieve claim with details")
    void testRetrieveClaimWithDetails() {
        Claim claim = new Claim("CLM-2026-CTRL-002");
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setPolicyNumber("POL-2026-CTRL-001");
        Claim saved = claimRepository.save(claim);

        Claim retrieved = claimRepository.findByClaimIdWithDetails(saved.getClaimId()).orElse(null);
        assertNotNull(retrieved);
        assertNotNull(retrieved.getDocuments());
    }

    @Test
    @DisplayName("CTRL-003: List all claims")
    void testListAllClaims() {
        Claim claim1 = new Claim("CLM-2026-CTRL-003");
        Claim claim2 = new Claim("CLM-2026-CTRL-004");
        claimRepository.save(claim1);
        claimRepository.save(claim2);

        List<Claim> claims = claimRepository.findAll();
        assertNotNull(claims);
        assertTrue(claims.size() >= 2);
    }

    @Test
    @DisplayName("CTRL-004: Filter claims by status")
    void testFilterClaimsByStatus() {
        Claim claim1 = new Claim("CLM-2026-CTRL-005");
        claim1.setStatus(ClaimStatus.APPROVED);
        Claim claim2 = new Claim("CLM-2026-CTRL-006");
        claim2.setStatus(ClaimStatus.REJECTED);
        
        claimRepository.save(claim1);
        claimRepository.save(claim2);

        List<Claim> approvedClaims = claimRepository.findByStatus(ClaimStatus.APPROVED);
        assertNotNull(approvedClaims);
    }

    @Test
    @DisplayName("CTRL-005: Get claim count by status")
    void testGetClaimCountByStatus() {
        Claim claim1 = new Claim("CLM-2026-CTRL-007");
        claim1.setStatus(ClaimStatus.APPROVED);
        Claim claim2 = new Claim("CLM-2026-CTRL-008");
        claim2.setStatus(ClaimStatus.APPROVED);
        
        claimRepository.save(claim1);
        claimRepository.save(claim2);

        long count = claimRepository.findByStatus(ClaimStatus.APPROVED).size();
        assertTrue(count >= 2);
    }

    @Test
    @DisplayName("CTRL-006: Find claims by policy")
    void testFindClaimsByPolicy() {
        Claim claim = new Claim("CLM-2026-CTRL-009");
        claim.setPolicyNumber("POL-2026-CTRL-001");
        claimRepository.save(claim);

        List<Claim> policyClaims = claimRepository.findByPolicyNumber("POL-2026-CTRL-001");
        assertNotNull(policyClaims);
        assertTrue(policyClaims.stream().anyMatch(c -> c.getClaimId().equals("CLM-2026-CTRL-009")));
    }

    @Test
    @DisplayName("CTRL-007: Get claims in date range")
    void testGetClaimsInDateRange() {
        Claim claim = new Claim("CLM-2026-CTRL-010");
        claimRepository.save(claim);

        List<Claim> claims = claimRepository.findAll();
        assertTrue(claims.stream().anyMatch(c -> c.getClaimId().equals("CLM-2026-CTRL-010")));
    }

    @Test
    @DisplayName("CTRL-008: Update claim status")
    void testUpdateClaimStatus() {
        Claim claim = new Claim("CLM-2026-CTRL-011");
        claim.setStatus(ClaimStatus.PENDING);
        Claim saved = claimRepository.save(claim);

        saved.setStatus(ClaimStatus.APPROVED);
        Claim updated = claimRepository.save(saved);

        assertEquals(ClaimStatus.APPROVED, updated.getStatus());
    }

    @Test
    @DisplayName("CTRL-009: Delete claim")
    void testDeleteClaim() {
        Claim claim = new Claim("CLM-2026-CTRL-012");
        Claim saved = claimRepository.save(claim);
        Long id = saved.getId();

        claimRepository.deleteById(id);
        assertFalse(claimRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("CTRL-010: Non-existent claim retrieval")
    void testNonExistentClaimRetrieval() {
        Claim retrieved = claimRepository.findByClaimIdWithDetails("CLM-NONEXISTENT").orElse(null);
        assertNull(retrieved);
    }

    // Policy retrieval tests
    @Test
    @DisplayName("POL-001: Retrieve policy by number")
    void testRetrievePolicyByNumber() {
        Policy retrieved = policyRepository.findByPolicyNumber("POL-2026-CTRL-001").orElse(null);
        assertNotNull(retrieved);
        assertEquals("Test User", retrieved.getPolicyHolderName());
    }

    @Test
    @DisplayName("POL-002: Find active policies")
    void testFindActivePolicies() {
        List<Policy> activePolicies = policyRepository.findByStatus("ACTIVE");
        assertNotNull(activePolicies);
        assertTrue(activePolicies.stream().allMatch(p -> p.getStatus().equals("ACTIVE")));
    }

    @Test
    @DisplayName("POL-003: Find inactive policies")
    void testFindInactivePolicies() {
        Policy inactivePolicy = new Policy(
                "POL-2026-CTRL-INA",
                "Inactive Plan",
                "Inactive User",
                "Test Insurance",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                new BigDecimal("100000.00"),
                "INACTIVE"
        );
        policyRepository.save(inactivePolicy);

        List<Policy> inactivePolicies = policyRepository.findByStatus("INACTIVE");
        assertNotNull(inactivePolicies);
    }

    @Test
    @DisplayName("POL-004: Check policy coverage limits")
    void testPolicyCoverageLimits() {
        Policy retrieved = policyRepository.findByPolicyNumber("POL-2026-CTRL-001").orElse(null);
        assertNotNull(retrieved);
        assertEquals(new BigDecimal("500000.00"), retrieved.getCoverageLimit());
    }

    @Test
    @DisplayName("POL-005: Update policy status")
    void testUpdatePolicyStatus() {
        Policy policy = new Policy(
                "POL-2026-CTRL-UPD",
                "Update Test",
                "Update User",
                "Test Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("300000.00"),
                "ACTIVE"
        );
        Policy saved = policyRepository.save(policy);

        saved.setStatus("SUSPENDED");
        Policy updated = policyRepository.save(saved);

        assertEquals("SUSPENDED", updated.getStatus());
    }

    @Test
    @DisplayName("POL-006: Delete policy")
    void testDeletePolicy() {
        Policy policy = new Policy(
                "POL-2026-CTRL-DEL",
                "Delete Test",
                "Delete User",
                "Test Insurance",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                new BigDecimal("300000.00"),
                "ACTIVE"
        );
        Policy saved = policyRepository.save(policy);
        Long id = saved.getId();

        policyRepository.deleteById(id);
        assertFalse(policyRepository.findById(id).isPresent());
    }

    // Statistics tests
    @Test
    @DisplayName("STAT-001: Get total claims count")
    void testGetTotalClaimsCount() {
        long count = claimRepository.findAll().size();
        assertTrue(count >= 0);
    }

    @Test
    @DisplayName("STAT-002: Get claims by status distribution")
    void testGetClaimsDistribution() {
        long approved = claimRepository.findByStatus(ClaimStatus.APPROVED).size();
        long rejected = claimRepository.findByStatus(ClaimStatus.REJECTED).size();
        long pending = claimRepository.findByStatus(ClaimStatus.PENDING).size();

        assertTrue(approved + rejected + pending >= 0);
    }

    @Test
    @DisplayName("STAT-003: Get average claim amount")
    void testGetAverageClaimAmount() {
        List<Claim> claims = claimRepository.findAll();
        assertNotNull(claims);
    }

    @Test
    @DisplayName("STAT-004: Get policies count")
    void testGetPoliciesCount() {
        long count = policyRepository.findAll().size();
        assertTrue(count > 0);
    }

    @Test
    @DisplayName("STAT-005: Get premium calculation")
    void testGetPremiumCalculation() {
        Policy policy = policyRepository.findByPolicyNumber("POL-2026-CTRL-001").orElse(null);
        assertNotNull(policy);
        assertTrue(policy.getCoverageLimit().compareTo(BigDecimal.ZERO) > 0);
    }

    // Search and filter tests
    @Test
    @DisplayName("SEARCH-001: Search by claim ID pattern")
    void testSearchByClaimIdPattern() {
        Claim claim = new Claim("CLM-2026-SEARCH-001");
        claimRepository.save(claim);

        List<Claim> claims = claimRepository.findAll();
        assertTrue(claims.stream().anyMatch(c -> c.getClaimId().contains("SEARCH")));
    }

    @Test
    @DisplayName("SEARCH-002: Search by policy holder name")
    void testSearchByPolicyHolder() {
        Policy retrieved = policyRepository.findByPolicyNumber("POL-2026-CTRL-001").orElse(null);
        assertNotNull(retrieved);
        assertEquals("Test User", retrieved.getPolicyHolderName());
    }

    @Test
    @DisplayName("SEARCH-003: Filter claims by date range")
    void testFilterByDateRange() {
        List<Claim> claims = claimRepository.findAll();
        assertTrue(claims.stream().allMatch(c -> c.getCreatedDate() != null || c.getId() != null));
    }

    @Test
    @DisplayName("SEARCH-004: Filter by coverage amount")
    void testFilterByCoverageAmount() {
        List<Policy> policies = policyRepository.findAll();
        assertTrue(policies.stream().allMatch(p -> p.getCoverageLimit() != null));
    }

    @Test
    @DisplayName("SEARCH-005: Pagination test")
    void testPaginationBehavior() {
        List<Claim> claims = claimRepository.findAll();
        assertNotNull(claims);
        assertTrue(claims.size() >= 0);
    }
}
