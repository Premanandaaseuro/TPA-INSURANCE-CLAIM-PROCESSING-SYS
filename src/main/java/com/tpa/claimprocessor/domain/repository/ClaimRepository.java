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

    Optional<Claim> findByClaimId(String claimId);

    boolean existsByClaimId(String claimId);

    @Query("SELECT c.claimId FROM Claim c WHERE c.claimId LIKE CONCAT('CLM-', :year, '-%')")
    List<String> findClaimIdsByYear(@Param("year") int year);
}

