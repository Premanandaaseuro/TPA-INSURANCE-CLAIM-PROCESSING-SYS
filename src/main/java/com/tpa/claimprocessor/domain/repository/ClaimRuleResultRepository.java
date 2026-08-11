package com.tpa.claimprocessor.domain.repository;

import com.tpa.claimprocessor.domain.entity.ClaimRuleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRuleResultRepository extends JpaRepository<ClaimRuleResult, Long> {
    List<ClaimRuleResult> findByClaimClaimId(String claimId);
}
