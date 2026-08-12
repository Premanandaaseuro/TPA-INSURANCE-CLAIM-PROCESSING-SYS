package com.tpa.claimprocessor.domain.repository;

import com.tpa.claimprocessor.domain.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    @Query("SELECT DISTINCT c FROM Claim c WHERE c.claimId = :claimId OR c.claimNumber = :claimId OR CAST(c.id AS string) = :claimId")
    Optional<Claim> findByClaimId(@Param("claimId") String claimId);

    boolean existsByClaimId(String claimId);

    boolean existsByPolicyNumber(String policyNumber);

    @Query("SELECT c.claimId FROM Claim c WHERE c.claimId LIKE CONCAT('CLM-', :year, '-%') OR c.claimNumber LIKE CONCAT('CLM-', :year, '-%')")
    List<String> findClaimIdsByYear(@Param("year") int year);

    /**
     * Fetch claim + documents eagerly (single bag JOIN FETCH is safe).
     * ruleResults are loaded lazily within the active @Transactional session.
     */
    @Query("SELECT DISTINCT c FROM Claim c LEFT JOIN FETCH c.documents WHERE c.claimId = :claimId OR c.claimNumber = :claimId OR CAST(c.id AS string) = :claimId")
    Optional<Claim> findByClaimIdWithDetails(@Param("claimId") String claimId);

    /**
     * Fetch all claims + documents eagerly. ruleResults lazy-loaded within transaction.
     */
    @Query("SELECT DISTINCT c FROM Claim c LEFT JOIN FETCH c.documents ORDER BY c.createdAt DESC")
    List<Claim> findAllWithDetails();
}
